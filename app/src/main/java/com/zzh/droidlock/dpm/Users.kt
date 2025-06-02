package com.zzh.droidlock.dpm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build.VERSION
import android.os.UserHandle
import android.os.UserManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zzh.droidlock.R
import com.zzh.droidlock.showOperationResultToast
import com.zzh.droidlock.ui.FunctionItem
import com.zzh.droidlock.ui.MyScaffold
import com.zzh.droidlock.ui.SwitchItem
import com.zzh.droidlock.uriToStream
import kotlinx.serialization.Serializable

@Serializable object Users

@Composable
fun UsersScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.users, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser) {
            FunctionItem(R.string.logout, icon = R.drawable.logout_fill0) { dialog = 2 }
        }
        if(deviceOwner && VERSION.SDK_INT >= 28) {
            FunctionItem(R.string.secondary_users, icon = R.drawable.list_fill0) { dialog = 1 }
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(UsersOptions) }
        }
        if(deviceOwner) {
            FunctionItem(R.string.user_operation, icon = R.drawable.sync_alt_fill0) { onNavigate(UserOperation) }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner)) {
            var changeUserIconDialog by remember { mutableStateOf(false) }
            var bitmap: Bitmap? by remember { mutableStateOf(null) }
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if(it != null) uriToStream(context, it) { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                    if(bitmap != null) changeUserIconDialog = true
                }
            }
            FunctionItem(R.string.change_user_icon, icon = R.drawable.account_circle_fill0) {
                Toast.makeText(context, R.string.select_an_image, Toast.LENGTH_SHORT).show()
                launcher.launch("image/*")
            }
        }
    }
    if(dialog != 0 && VERSION.SDK_INT >= 28) AlertDialog(
        title = { Text(stringResource(if(dialog == 1) R.string.secondary_users else R.string.logout)) },
        text = {
            if(dialog == 1) {
                val um = context.getSystemService(Context.USER_SERVICE) as UserManager
                val list = dpm.getSecondaryUsers(receiver)
                if(list.isEmpty()) {
                    Text(stringResource(R.string.no_secondary_users))
                } else {
                    Text("(" + stringResource(R.string.serial_number) + ")\n" + list.joinToString("\n") { um.getSerialNumberForUser(it).toString() })
                }
            } else {
                Text(stringResource(R.string.info_logout))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(dialog == 2) {
                        val result = dpm.logoutUser(receiver)
                        Toast.makeText(context, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                    }
                    dialog = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            if(dialog != 1) TextButton(onClick = { dialog = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@Serializable object UsersOptions

@Composable
fun UsersOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 28) {
            SwitchItem(R.string.enable_logout, getState = { dpm.isLogoutEnabled }, onCheckedChange = { dpm.setLogoutEnabled(receiver, it) })
        }
    }
}

@Serializable object UserOperation

@Composable
fun UserOperationScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var input by remember { mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    var useUserId by remember { mutableStateOf(false) }
    fun withUserHandle(operation: (UserHandle) -> Unit) {
        val userHandle = if(useUserId && VERSION.SDK_INT >= 24) {
            UserHandle.getUserHandleForUid(input.toInt() * 100000)
        } else {
            userManager.getUserForSerialNumber(input.toLong())
        }
        if(userHandle == null) {
            Toast.makeText(context, R.string.user_not_exist, Toast.LENGTH_SHORT).show()
        } else {
            operation(userHandle)
        }
    }
    val legalInput = input.toIntOrNull() != null
    MyScaffold(R.string.user_operation, onNavigateUp) {
        if(VERSION.SDK_INT >= 24) SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(!useUserId, { useUserId = false }, SegmentedButtonDefaults.itemShape(0, 2)) {
                Text(stringResource(R.string.serial_number))
            }
            SegmentedButton(useUserId, { useUserId = true }, SegmentedButtonDefaults.itemShape(1, 2)) {
                Text(stringResource(R.string.user_id))
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(stringResource(if(useUserId) R.string.user_id else R.string.serial_number)) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        if(VERSION.SDK_INT >= 28) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    withUserHandle {
                        val result = dpm.startUserInBackground(receiver, it)
                        Toast.makeText(context, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = legalInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.start_in_background))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                withUserHandle { context.showOperationResultToast(dpm.switchUser(receiver, it)) }
            },
            enabled = legalInput,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(painterResource(R.drawable.sync_alt_fill0), null, Modifier.padding(end = 4.dp))
            Text(stringResource(R.string.user_operation_switch))
        }
        if(VERSION.SDK_INT >= 28) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    withUserHandle {
                        val result = dpm.stopUser(receiver, it)
                        Toast.makeText(context, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = legalInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.stop))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                withUserHandle {
                    if(dpm.removeUser(receiver, it)) {
                        context.showOperationResultToast(true)
                        input = ""
                    } else {
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = legalInput,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, null, Modifier.padding(end = 4.dp))
            Text(stringResource(R.string.delete))
        }
    }
}

@StringRes
private fun userOperationResultCode(result:Int): Int =
    when(result) {
        UserManager.USER_OPERATION_SUCCESS -> R.string.success
        UserManager.USER_OPERATION_ERROR_UNKNOWN -> R.string.unknown_error
        UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE-> R.string.fail_managed_profile
        UserManager.USER_OPERATION_ERROR_CURRENT_USER-> R.string.fail_current_user
        else -> R.string.unknown
    }
