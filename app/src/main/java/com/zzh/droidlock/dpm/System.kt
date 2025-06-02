package com.zzh.droidlock.dpm

import android.app.admin.DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY
import android.app.admin.DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY
import android.app.admin.DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT
import android.app.admin.DevicePolicyManager.PERMISSION_POLICY_PROMPT
import android.content.Context
import android.os.Build.VERSION
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzh.droidlock.HorizontalPadding
import com.zzh.droidlock.R
import com.zzh.droidlock.SharedPrefs
import com.zzh.droidlock.showOperationResultToast
import com.zzh.droidlock.ui.CheckBoxItem
import com.zzh.droidlock.ui.FullWidthRadioButtonItem
import com.zzh.droidlock.ui.FunctionItem
import com.zzh.droidlock.ui.MyScaffold
import com.zzh.droidlock.ui.Notes
import com.zzh.droidlock.ui.SwitchItem
import kotlinx.serialization.Serializable

@Serializable object SystemManager

@Composable
fun SystemManagerScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sp = SharedPrefs(context)
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.system, onNavigateUp, 0.dp) {
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(SystemOptions) }
        }
        if(VERSION.SDK_INT >= 24 && deviceOwner) {
            FunctionItem(R.string.reboot, icon = R.drawable.restart_alt_fill0) { dialog = 1 }
        }
        if(deviceOwner && VERSION.SDK_INT >= 24 && (VERSION.SDK_INT < 28 || dpm.isAffiliatedUser)) {
            FunctionItem(R.string.bug_report, icon = R.drawable.bug_report_fill0) { dialog = 2 }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.permission_policy, icon = R.drawable.key_fill0) { onNavigate(PermissionPolicy) }
        }
    }
    if(dialog != 0 &&VERSION.SDK_INT >= 24) AlertDialog(
        onDismissRequest = { dialog = 0 },
        title = { Text(stringResource(if(dialog == 1) R.string.reboot else R.string.bug_report)) },
        text = { Text(stringResource(if(dialog == 1) R.string.info_reboot else R.string.confirm_bug_report)) },
        dismissButton = {
            TextButton(onClick = { dialog = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(dialog == 1) {
                        dpm.reboot(receiver)
                    } else {
                        context.showOperationResultToast(dpm.requestBugreport(receiver))
                    }
                    dialog = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Serializable object SystemOptions

@Composable
fun SystemOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val um = context.getSystemService(Context.USER_SERVICE) as UserManager
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.disable_cam, icon = R.drawable.photo_camera_fill0,
                getState = { dpm.getCameraDisabled(null) }, onCheckedChange = { dpm.setCameraDisabled(receiver,it) }
            )
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.disable_screen_capture, icon = R.drawable.screenshot_fill0,
                getState = { dpm.getScreenCaptureDisabled(null) }, onCheckedChange = { dpm.setScreenCaptureDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 34 && (deviceOwner || (profileOwner && dpm.isAffiliatedUser))) {
            SwitchItem(R.string.disable_status_bar, icon = R.drawable.notifications_fill0,
                getState = { dpm.isStatusBarDisabled}, onCheckedChange = { dpm.setStatusBarDisabled(receiver,it) }
            )
        }
        if(deviceOwner || (VERSION.SDK_INT >= 23 && profileOwner && um.isSystemUser) || dpm.isOrgProfile(receiver)) {
            if(VERSION.SDK_INT >= 30) {
                SwitchItem(R.string.auto_time, icon = R.drawable.schedule_fill0,
                    getState = { dpm.getAutoTimeEnabled(receiver) }, onCheckedChange = { dpm.setAutoTimeEnabled(receiver,it) }
                )
                SwitchItem(R.string.auto_timezone, icon = R.drawable.globe_fill0,
                    getState = { dpm.getAutoTimeZoneEnabled(receiver) }, onCheckedChange = { dpm.setAutoTimeZoneEnabled(receiver,it) }
                )
            } else {
                SwitchItem(R.string.require_auto_time, icon = R.drawable.schedule_fill0,
                    getState = { dpm.autoTimeRequired }, onCheckedChange = { dpm.setAutoTimeRequired(receiver,it) }, padding = false)
            }
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.master_mute, icon = R.drawable.volume_off_fill0,
                getState = { dpm.isMasterVolumeMuted(receiver) }, onCheckedChange = { dpm.setMasterVolumeMuted(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
            SwitchItem(R.string.backup_service, icon = R.drawable.backup_fill0,
                getState = { dpm.isBackupServiceEnabled(receiver) }, onCheckedChange = { dpm.setBackupServiceEnabled(receiver,it) },
                onClickBlank = { dialog = 1 }
            )
        }
        if(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver)) {
            SwitchItem(R.string.disable_bt_contact_share, icon = R.drawable.account_circle_fill0,
                getState = { dpm.getBluetoothContactSharingDisabled(receiver) },
                onCheckedChange = { dpm.setBluetoothContactSharingDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 30 && deviceOwner) {
            SwitchItem(R.string.common_criteria_mode , icon =R.drawable.security_fill0,
                getState = { dpm.isCommonCriteriaModeEnabled(receiver) }, onCheckedChange = { dpm.setCommonCriteriaModeEnabled(receiver,it) },
                onClickBlank = { dialog = 2 }
            )
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || dpm.isOrgProfile(receiver)) && dpm.canUsbDataSignalingBeDisabled()) {
            SwitchItem(
                R.string.disable_usb_signal, icon = R.drawable.usb_fill0, getState = { !dpm.isUsbDataSignalingEnabled },
                onCheckedChange = { dpm.isUsbDataSignalingEnabled = !it },
            )
        }
    }
    if(dialog != 0) AlertDialog(
        text = {
            Text(stringResource(
                when(dialog) {
                    1 -> R.string.info_backup_service
                    2 -> R.string.info_common_criteria_mode
                    else -> R.string.options
                }
            ))
        },
        confirmButton = {
            TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = { dialog = 0 }
    )
}


@Serializable object PermissionPolicy

@RequiresApi(23)
@Composable
fun PermissionPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var selectedPolicy by remember { mutableIntStateOf(dpm.getPermissionPolicy(receiver)) }
    MyScaffold(R.string.permission_policy, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(R.string.default_stringres, selectedPolicy == PERMISSION_POLICY_PROMPT) {
            selectedPolicy = PERMISSION_POLICY_PROMPT
        }
        FullWidthRadioButtonItem(R.string.auto_grant, selectedPolicy == PERMISSION_POLICY_AUTO_GRANT) {
            selectedPolicy = PERMISSION_POLICY_AUTO_GRANT
        }
        FullWidthRadioButtonItem(R.string.auto_deny, selectedPolicy == PERMISSION_POLICY_AUTO_DENY) {
            selectedPolicy = PERMISSION_POLICY_AUTO_DENY
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setPermissionPolicy(receiver,selectedPolicy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_permission_policy, HorizontalPadding)
    }
}



@Serializable object Keyguard

@Composable
fun KeyguardScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    MyScaffold(R.string.keyguard, onNavigateUp) {
        if(VERSION.SDK_INT >= 23) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { context.showOperationResultToast(dpm.setKeyguardDisabled(receiver, true)) },
                    enabled = deviceOwner || (VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    onClick = { context.showOperationResultToast(dpm.setKeyguardDisabled(receiver, false)) },
                    enabled = deviceOwner || (VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.enable))
                }
            }
            Notes(R.string.info_disable_keyguard)
            Spacer(Modifier.padding(vertical = 12.dp))
        }
        if(VERSION.SDK_INT >= 23) Text(text = stringResource(R.string.lock_now), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 2.dp))
        var flag by remember { mutableIntStateOf(0) }
        if(VERSION.SDK_INT >= 26 && profileOwner && dpm.isManagedProfile(receiver)) {
            CheckBoxItem(
                R.string.evict_credential_encryption_key,
                flag and FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY != 0
            ) { flag = flag xor FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY }
            Spacer(Modifier.padding(vertical = 2.dp))
        }
        Button(
            onClick = {
                if(VERSION.SDK_INT >= 26) dpm.lockNow(flag) else dpm.lockNow()
            },
            enabled = context.isDeviceAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
        if(VERSION.SDK_INT >= 26 && profileOwner && dpm.isManagedProfile(receiver)) {
            Notes(R.string.info_evict_credential_encryption_key)
        }
    }
}
