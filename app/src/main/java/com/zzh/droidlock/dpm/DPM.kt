package com.zzh.droidlock.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.app.admin.IDevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.UserManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.Dhizuku.binderWrapper
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import com.zzh.droidlock.Receiver
import com.zzh.droidlock.SharedPrefs
import com.zzh.droidlock.backToHomeStateFlow
import kotlinx.coroutines.flow.MutableStateFlow

lateinit var addDeviceAdmin: ActivityResultLauncher<Intent>

val dhizukuErrorStatus = MutableStateFlow(0)

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

