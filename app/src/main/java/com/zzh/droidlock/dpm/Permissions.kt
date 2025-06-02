package com.zzh.droidlock.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rosan.dhizuku.api.Dhizuku
import com.zzh.droidlock.R
import com.zzh.droidlock.SharedPrefs
import com.zzh.droidlock.backToHomeStateFlow
import com.zzh.droidlock.ui.CheckBoxItem
import com.zzh.droidlock.ui.CopyTextButton
import com.zzh.droidlock.ui.FunctionItem
import com.zzh.droidlock.ui.InfoItem
import com.zzh.droidlock.ui.MyScaffold
import com.zzh.droidlock.writeClipBoard
import com.zzh.droidlock.yesOrNo
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable object Permissions
@SuppressLint("NewApi")
@Composable
fun PermissionsScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    var dialog by remember { mutableIntStateOf(0) }
    var bindingShizuku by remember { mutableStateOf(false) }
    MyScaffold(R.string.permissions, onNavigateUp, 0.dp) {
        if(!profileOwner && userManager.isSystemUser) {
            FunctionItem(
                R.string.device_owner, stringResource(if(deviceOwner) R.string.activated else R.string.deactivated),
                operation = { onNavigate(DeviceOwner) }
            )
        }
    }
    if(bindingShizuku) {
        Dialog(onDismissRequest = { bindingShizuku = false }) {
            CircularProgressIndicator()
        }
    }
    if(dialog != 0) {
        var input by remember { mutableStateOf("") }
        AlertDialog(
            title = {
                Text(stringResource(
                    when(dialog){
                        1 -> R.string.enrollment_specific_id
                        2 -> R.string.org_name
                        3 -> R.string.org_id
                        4 -> R.string.dhizuku
                        else -> R.string.permissions
                    }
                ))
            },
            text = {
                val focusMgr = LocalFocusManager.current
                LaunchedEffect(Unit) {
                    if(dialog == 1) input = dpm.enrollmentSpecificId
                }
                Column {
                    if(dialog != 4) OutlinedTextField(
                        value = input,
                        onValueChange = { input = it }, readOnly = dialog == 1,
                        label = {
                            Text(stringResource(
                                when(dialog){
                                    1 -> R.string.enrollment_specific_id
                                    2 -> R.string.org_name
                                    3 -> R.string.org_id
                                    else -> R.string.permissions
                                }
                            ))
                        },
                        trailingIcon = {
                            if(dialog == 1) IconButton(onClick = { writeClipBoard(context, input) }) {
                                Icon(painter = painterResource(R.drawable.content_copy_fill0), contentDescription = stringResource(R.string.copy))
                            }
                        },
                        supportingText = {
                            if(dialog == 3) Text(stringResource(R.string.length_6_to_64))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                        textStyle = typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(bottom = if(dialog == 2) 0.dp else 10.dp)
                    )
                    if(dialog == 1) Text(stringResource(R.string.info_enrollment_specific_id))
                    if(dialog == 3) Text(stringResource(R.string.info_org_id))
                    if(dialog == 4) Text(stringResource(R.string.info_dhizuku))
                }
            },
            onDismissRequest = { dialog = 0 },
            dismissButton = {
                if(dialog != 4) TextButton(
                    onClick = { dialog = 0 }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            if(dialog == 2) dpm.setOrganizationName(receiver, input)
                            if(dialog == 3) dpm.setOrganizationId(input)
                            dialog = 0
                        } catch(_: IllegalStateException) {
                            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = (dialog == 3 && input.length in 6..64) || dialog != 3
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}


@Serializable object DeviceAdmin

@Composable
fun DeviceAdminScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var deactivateDialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.device_admin, onNavigateUp) {
        Text(text = stringResource(if(context.isDeviceAdmin) R.string.activated else R.string.deactivated), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
    }
    if(deactivateDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            onDismissRequest = { deactivateDialog = false },
            dismissButton = {
                TextButton(
                    onClick = { deactivateDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dpm.removeActiveAdmin(receiver)
                        deactivateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Serializable object DeviceOwner

@Composable
fun DeviceOwnerScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var deactivateDialog by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(1) }
    val deviceOwner = context.isDeviceOwner
    MyScaffold(R.string.device_owner, onNavigateUp) {
        Text(
            modifier = Modifier.clickable{
                clickCount++
            },
            text = stringResource(if(deviceOwner) R.string.activated else R.string.deactivated), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(deviceOwner && clickCount>30) {
            Button(
                onClick = { deactivateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError)
            ) {
                Text(text = stringResource(R.string.deactivate))
            }
        }
    }
    if(deactivateDialog) {
        val sp = SharedPrefs(context)
        var resetPolicy by remember { mutableStateOf(false) }
        val coroutine = rememberCoroutineScope()
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            text = {
                Column {
                    if(sp.dhizuku) Text(stringResource(R.string.dhizuku_will_be_deactivated))
                    Spacer(Modifier.padding(vertical = 4.dp))
                    CheckBoxItem(text = R.string.reset_device_policy, checked = resetPolicy, operation = { resetPolicy = it })
                }
            },
            onDismissRequest = { deactivateDialog = false },
            dismissButton = {
                TextButton(
                    onClick = { deactivateDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutine.launch {
                            if(resetPolicy) context.resetDevicePolicy()
                            dpm.clearDeviceOwnerApp(context.dpcPackageName)
                            if(sp.dhizuku) {
                                if (!Dhizuku.init(context)) {
                                    sp.dhizuku = false
                                    backToHomeStateFlow.value = true
                                }
                            }
                            deactivateDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}



@Serializable object DeviceInfo

@Composable
fun DeviceInfoScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.device_info, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT>=34 && (context.isDeviceOwner || dpm.isOrgProfile(receiver))) {
            InfoItem(R.string.financed_device, dpm.isDeviceFinanced.yesOrNo)
        }
        if(VERSION.SDK_INT >= 33) {
            val dpmRole = dpm.devicePolicyManagementRoleHolderPackage
            InfoItem(R.string.dpmrh, if(dpmRole == null) stringResource(R.string.none) else dpmRole)
        }
        val encryptionStatus = mutableMapOf(
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE to R.string.es_inactive,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE to R.string.es_active,
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED to R.string.es_unsupported
        )
        if(VERSION.SDK_INT >= 23) { encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY] = R.string.es_active_default_key }
        if(VERSION.SDK_INT >= 24) { encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER] = R.string.es_active_per_user }
        InfoItem(R.string.encryption_status, encryptionStatus[dpm.storageEncryptionStatus] ?: R.string.unknown)
        if(VERSION.SDK_INT >= 28) {
            InfoItem(R.string.support_device_id_attestation, dpm.isDeviceIdAttestationSupported.yesOrNo, true) { dialog = 1 }
        }
        if (VERSION.SDK_INT >= 30) {
            InfoItem(R.string.support_unique_device_attestation, dpm.isUniqueDeviceAttestationSupported.yesOrNo, true) { dialog = 2 }
        }
        val adminList = dpm.activeAdmins
        if(adminList != null) {
            InfoItem(R.string.activated_device_admin, adminList.map { it.flattenToShortString() }.joinToString("\n"))
        }
    }
    if(dialog != 0) AlertDialog(
        text = { Text(stringResource(if(dialog == 1) R.string.info_device_id_attestation else R.string.info_unique_device_attestation)) },
        confirmButton = { TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) } },
        onDismissRequest = { dialog = 0 }
    )
}

private fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName) {
    try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, inputContext.getString(R.string.activate_device_admin_here))
        addDeviceAdmin.launch(intent)
    } catch(_:ActivityNotFoundException) {
        Toast.makeText(inputContext, R.string.unsupported, Toast.LENGTH_SHORT).show()
    }
}
