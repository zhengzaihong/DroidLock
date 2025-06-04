package com.zzh.droidlock.dpm

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.ConnectEvent
import android.app.admin.DevicePolicyManager
import android.app.admin.DnsEvent
import android.app.admin.FactoryResetProtectionPolicy
import android.app.admin.IDevicePolicyManager
import android.app.admin.SecurityLog
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageInstaller
import android.content.pm.PackageInstaller
import android.os.Build.VERSION
import android.os.UserManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.zzh.droidlock.R
import com.zzh.droidlock.Receiver
import com.zzh.droidlock.SharedPrefs
import com.zzh.droidlock.backToHomeStateFlow
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.Dhizuku.binderWrapper
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.OutputStream

lateinit var addDeviceAdmin: ActivityResultLauncher<Intent>

val Context.isDeviceOwner: Boolean
    get() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(
            if(SharedPrefs(this).dhizuku) {
                Dhizuku.getOwnerPackageName()
            } else {
                "com.zzh.droidlock"
            }
        )
    }

val Context.isProfileOwner: Boolean
    get() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isProfileOwnerApp("com.zzh.droidlock")
    }

val Context.isDeviceAdmin: Boolean
    get() {
        return getDPM().isAdminActive(getReceiver())
    }

val Context.dpcPackageName: String
    get() {
        return if(SharedPrefs(this).dhizuku) {
            Dhizuku.getOwnerPackageName()
        } else {
            "com.zzh.droidlock"
        }
    }

fun DevicePolicyManager.isOrgProfile(receiver: ComponentName): Boolean {
    return VERSION.SDK_INT >= 30 && this.isProfileOwnerApp("com.zzh.droidlock") && isManagedProfile(receiver) && isOrganizationOwnedDeviceWithManagedProfile
}

@SuppressLint("PrivateApi")
private fun binderWrapperDevicePolicyManager(appContext: Context): DevicePolicyManager? {
    try {
        val context = appContext.createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val field = manager.javaClass.getDeclaredField("mService")
        field.isAccessible = true
        val oldInterface = field[manager] as IDevicePolicyManager
        if (oldInterface is DhizukuBinderWrapper) return manager
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IDevicePolicyManager.Stub.asInterface(newBinder)
        field[manager] = newInterface
        return manager
    } catch (_: Exception) {
        dhizukuErrorStatus.value = 1
    }
    return null
}

@SuppressLint("PrivateApi")
private fun binderWrapperPackageInstaller(appContext: Context): PackageInstaller? {
    try {
        val context = appContext.createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val installer = context.packageManager.packageInstaller
        val field = installer.javaClass.getDeclaredField("mInstaller")
        field.isAccessible = true
        val oldInterface = field[installer] as IPackageInstaller
        if (oldInterface is DhizukuBinderWrapper) return installer
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IPackageInstaller.Stub.asInterface(newBinder)
        field[installer] = newInterface
        return installer
    } catch (_: Exception) {
        dhizukuErrorStatus.value = 1
    }
    return null
}

fun Context.getPackageInstaller(): PackageInstaller {
    if(SharedPrefs(this).dhizuku) {
        if (!dhizukuPermissionGranted()) {
            dhizukuErrorStatus.value = 2
            backToHomeStateFlow.value = true
            return this.packageManager.packageInstaller
        }
        return binderWrapperPackageInstaller(this) ?: this.packageManager.packageInstaller
    } else {
        return this.packageManager.packageInstaller
    }
}

fun Context.getDPM(): DevicePolicyManager {
    if(SharedPrefs(this).dhizuku) {
        if (!dhizukuPermissionGranted()) {
            dhizukuErrorStatus.value = 2
            backToHomeStateFlow.value = true
            return this.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        }
        return binderWrapperDevicePolicyManager(this) ?: this.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    } else {
        return this.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
}

fun Context.getReceiver(): ComponentName {
    return if(SharedPrefs(this).dhizuku) {
        Dhizuku.getOwnerComponent()
    } else {
        ComponentName(this, Receiver::class.java)
    }
}

val dhizukuErrorStatus = MutableStateFlow(0)

fun Context.resetDevicePolicy() {
    val dpm = getDPM()
    val receiver = getReceiver()
    RestrictionData.getAllRestrictions().forEach {
        dpm.clearUserRestriction(receiver, it.id)
    }
    dpm.accountTypesWithManagementDisabled?.forEach {
        dpm.setAccountManagementDisabled(receiver, it, false)
    }
    if (VERSION.SDK_INT >= 30) {
        dpm.setConfiguredNetworksLockdownState(receiver, false)
        dpm.setAutoTimeZoneEnabled(receiver, true)
        dpm.setAutoTimeEnabled(receiver, true)
        dpm.setCommonCriteriaModeEnabled(receiver, false)
        try {
            val frp = FactoryResetProtectionPolicy.Builder().setFactoryResetProtectionEnabled(false).setFactoryResetProtectionAccounts(listOf())
            dpm.setFactoryResetProtectionPolicy(receiver, frp.build())
        } catch(_: Exception) {}
        dpm.setUserControlDisabledPackages(receiver, listOf())
    }
    if (VERSION.SDK_INT >= 33) {
        dpm.minimumRequiredWifiSecurityLevel = DevicePolicyManager.WIFI_SECURITY_OPEN
        dpm.wifiSsidPolicy = null
    }
    if (VERSION.SDK_INT >= 28) {
        dpm.getOverrideApns(receiver).forEach { dpm.removeOverrideApn(receiver, it.id) }
        dpm.setKeepUninstalledPackages(receiver, listOf())
    }
    dpm.setCameraDisabled(receiver, false)
    dpm.setScreenCaptureDisabled(receiver, false)
    dpm.setMasterVolumeMuted(receiver, false)
    try {
        if(VERSION.SDK_INT >= 31) dpm.isUsbDataSignalingEnabled = true
    } catch (_: Exception) { }
    if (VERSION.SDK_INT >= 23) {
        dpm.setPermissionPolicy(receiver, DevicePolicyManager.PERMISSION_POLICY_PROMPT)
        dpm.setSystemUpdatePolicy(receiver, SystemUpdatePolicy.createAutomaticInstallPolicy())
    }
    if (VERSION.SDK_INT >= 24) {
        dpm.setAlwaysOnVpnPackage(receiver, null, false)
        dpm.setPackagesSuspended(receiver, arrayOf(), false)
    }
    dpm.setPermittedInputMethods(receiver, null)
    dpm.setPermittedAccessibilityServices(receiver, null)
    packageManager.getInstalledApplications(0).forEach {
        if (dpm.isUninstallBlocked(receiver, it.packageName)) dpm.setUninstallBlocked(receiver, it.packageName, false)
    }
    if (VERSION.SDK_INT >= 26) {
        dpm.setRequiredStrongAuthTimeout(receiver, 0)
        dpm.clearResetPasswordToken(receiver)
    }
    if (VERSION.SDK_INT >= 31) {
        dpm.requiredPasswordComplexity = DevicePolicyManager.PASSWORD_COMPLEXITY_NONE
    }
    dpm.setKeyguardDisabledFeatures(receiver, 0)
    dpm.setMaximumTimeToLock(receiver, 0)
    dpm.setPasswordExpirationTimeout(receiver, 0)
    dpm.setMaximumFailedPasswordsForWipe(receiver, 0)
    dpm.setPasswordHistoryLength(receiver, 0)
    if (VERSION.SDK_INT < 31) {
        dpm.setPasswordQuality(receiver, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED)
    }
    dpm.setRecommendedGlobalProxy(receiver, null)
}

data class PermissionItem(
    val permission: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val profileOwnerRestricted: Boolean = false
)

fun permissionList(): List<PermissionItem>{
    val list = mutableListOf<PermissionItem>()
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionItem(Manifest.permission.POST_NOTIFICATIONS, R.string.permission_POST_NOTIFICATIONS, R.drawable.notifications_fill0))
    }
    list.add(PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_READ_EXTERNAL_STORAGE, R.drawable.folder_fill0))
    list.add(PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_WRITE_EXTERNAL_STORAGE, R.drawable.folder_fill0))
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionItem(Manifest.permission.READ_MEDIA_AUDIO, R.string.permission_READ_MEDIA_AUDIO, R.drawable.music_note_fill0))
        list.add(PermissionItem(Manifest.permission.READ_MEDIA_VIDEO, R.string.permission_READ_MEDIA_VIDEO, R.drawable.movie_fill0))
        list.add(PermissionItem(Manifest.permission.READ_MEDIA_IMAGES, R.string.permission_READ_MEDIA_IMAGES, R.drawable.image_fill0))
    }
    list.add(PermissionItem(Manifest.permission.CAMERA, R.string.permission_CAMERA, R.drawable.photo_camera_fill0, true))
    list.add(PermissionItem(Manifest.permission.RECORD_AUDIO, R.string.permission_RECORD_AUDIO, R.drawable.mic_fill0, true))
    list.add(PermissionItem(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_ACCESS_COARSE_LOCATION, R.drawable.location_on_fill0, true))
    list.add(PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_ACCESS_FINE_LOCATION, R.drawable.location_on_fill0, true))
    if(VERSION.SDK_INT >= 29) {
        list.add(PermissionItem(Manifest.permission.ACCESS_BACKGROUND_LOCATION, R.string.permission_ACCESS_BACKGROUND_LOCATION, R.drawable.location_on_fill0, true))
    }
    list.add(PermissionItem(Manifest.permission.READ_CONTACTS, R.string.permission_READ_CONTACTS, R.drawable.contacts_fill0))
    list.add(PermissionItem(Manifest.permission.WRITE_CONTACTS, R.string.permission_WRITE_CONTACTS, R.drawable.contacts_fill0))
    list.add(PermissionItem(Manifest.permission.READ_CALENDAR, R.string.permission_READ_CALENDAR, R.drawable.calendar_month_fill0))
    list.add(PermissionItem(Manifest.permission.WRITE_CALENDAR, R.string.permission_WRITE_CALENDAR, R.drawable.calendar_month_fill0))
    if(VERSION.SDK_INT >= 31) {
        list.add(PermissionItem(Manifest.permission.BLUETOOTH_CONNECT, R.string.permission_BLUETOOTH_CONNECT, R.drawable.bluetooth_fill0))
        list.add(PermissionItem(Manifest.permission.BLUETOOTH_SCAN, R.string.permission_BLUETOOTH_SCAN, R.drawable.bluetooth_searching_fill0))
        list.add(PermissionItem(Manifest.permission.BLUETOOTH_ADVERTISE, R.string.permission_BLUETOOTH_ADVERTISE, R.drawable.bluetooth_fill0))
    }
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionItem(Manifest.permission.NEARBY_WIFI_DEVICES, R.string.permission_NEARBY_WIFI_DEVICES, R.drawable.wifi_fill0))
    }
    list.add(PermissionItem(Manifest.permission.CALL_PHONE, R.string.permission_CALL_PHONE, R.drawable.call_fill0))
    if(VERSION.SDK_INT >= 26) {
        list.add(PermissionItem(Manifest.permission.ANSWER_PHONE_CALLS, R.string.permission_ANSWER_PHONE_CALLS, R.drawable.call_fill0))
        list.add(PermissionItem(Manifest.permission.READ_PHONE_NUMBERS, R.string.permission_READ_PHONE_STATE, R.drawable.mobile_phone_fill0))
    }
    list.add(PermissionItem(Manifest.permission.READ_PHONE_STATE, R.string.permission_READ_PHONE_STATE, R.drawable.mobile_phone_fill0))
    list.add(PermissionItem(Manifest.permission.USE_SIP, R.string.permission_USE_SIP, R.drawable.call_fill0))
    if(VERSION.SDK_INT >= 31) {
        list.add(PermissionItem(Manifest.permission.UWB_RANGING, R.string.permission_UWB_RANGING, R.drawable.cell_tower_fill0))
    }
    list.add(PermissionItem(Manifest.permission.READ_SMS, R.string.permission_READ_SMS, R.drawable.sms_fill0))
    list.add(PermissionItem(Manifest.permission.RECEIVE_SMS, R.string.permission_RECEIVE_SMS, R.drawable.sms_fill0))
    list.add(PermissionItem(Manifest.permission.SEND_SMS, R.string.permission_SEND_SMS, R.drawable.sms_fill0))
    list.add(PermissionItem(Manifest.permission.READ_CALL_LOG, R.string.permission_READ_CALL_LOG, R.drawable.call_log_fill0))
    list.add(PermissionItem(Manifest.permission.WRITE_CALL_LOG, R.string.permission_WRITE_CALL_LOG, R.drawable.call_log_fill0))
    list.add(PermissionItem(Manifest.permission.RECEIVE_WAP_PUSH, R.string.permission_RECEIVE_WAP_PUSH, R.drawable.wifi_fill0))
    list.add(PermissionItem(Manifest.permission.BODY_SENSORS, R.string.permission_BODY_SENSORS, R.drawable.sensors_fill0, true))
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionItem(Manifest.permission.BODY_SENSORS_BACKGROUND, R.string.permission_BODY_SENSORS_BACKGROUND, R.drawable.sensors_fill0))
    }
    if(VERSION.SDK_INT > 29) {
        list.add(PermissionItem(Manifest.permission.ACTIVITY_RECOGNITION, R.string.permission_ACTIVITY_RECOGNITION, R.drawable.history_fill0, true))
    }
    return list
}

fun setDefaultAffiliationID(context: Context) {
    if(VERSION.SDK_INT < 26) return
    val sp = SharedPrefs(context)
    if(!sp.isDefaultAffiliationIdSet) {
        try {
            val um = context.getSystemService(Context.USER_SERVICE) as UserManager
            if(context.isDeviceOwner || (!um.isSystemUser && context.isProfileOwner)) {
                val dpm = context.getDPM()
                val receiver = context.getReceiver()
                val affiliationIDs = dpm.getAffiliationIds(receiver)
                if(affiliationIDs.isEmpty()) {
                    dpm.setAffiliationIds(receiver, setOf("DroidLock_default_affiliation_id"))
                    sp.isDefaultAffiliationIdSet = true
                    Log.d("DPM", "Default affiliation id set")
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}

fun dhizukuPermissionGranted() =
    try {
        Dhizuku.isPermissionGranted()
    } catch(_: Exception) {
        false
    }

fun parsePackageInstallerMessage(context: Context, result: Intent): String {
    val status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
    val statusMessage = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
    val otherPackageName = result.getStringExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME)
    return when(status) {
        PackageInstaller.STATUS_FAILURE_BLOCKED ->
            context.getString(
                R.string.status_failure_blocked,
                otherPackageName ?: context.getString(R.string.unknown)
            )
        PackageInstaller.STATUS_FAILURE_ABORTED ->
            context.getString(R.string.status_failure_aborted)
        PackageInstaller.STATUS_FAILURE_INVALID ->
            context.getString(R.string.status_failure_invalid)
        PackageInstaller.STATUS_FAILURE_CONFLICT ->
            context.getString(R.string.status_failure_conflict, otherPackageName ?: "???")
        PackageInstaller.STATUS_FAILURE_STORAGE ->
            context.getString(R.string.status_failure_storage) +
                    result.getStringExtra(PackageInstaller.EXTRA_STORAGE_PATH).let { if(it == null) "" else "\n$it" }
        PackageInstaller.STATUS_FAILURE_INCOMPATIBLE ->
            context.getString(R.string.status_failure_incompatible)
        PackageInstaller.STATUS_FAILURE_TIMEOUT ->
            context.getString(R.string.timeout)
        else -> ""
    } + statusMessage.let { if(it == null) "" else "\n$it" }
}
