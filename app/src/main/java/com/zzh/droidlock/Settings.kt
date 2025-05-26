package com.zzh.droidlock

import android.os.Build.VERSION
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.zzh.droidlock.ui.FunctionItem
import com.zzh.droidlock.ui.MyScaffold
import com.zzh.droidlock.ui.SwitchItem
import kotlinx.serialization.Serializable

@Serializable object Settings

@Composable
fun SettingsScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
//    val context = LocalContext.current
    MyScaffold(R.string.settings, onNavigateUp, 0.dp) {
        FunctionItem(title = R.string.appearance, icon = R.drawable.format_paint_fill0) { onNavigate(Appearance) }
        FunctionItem(R.string.app_lock, icon = R.drawable.lock_fill0) { onNavigate(AppLockSettings) }
    }
}

@Serializable object SettingsOptions

@Composable
fun SettingsOptionsScreen(onNavigateUp: () -> Unit) {
    val sp = SharedPrefs(LocalContext.current)
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.show_dangerous_features, icon = R.drawable.warning_fill0,
            getState = { sp.displayDangerousFeatures },
            onCheckedChange = { sp.displayDangerousFeatures = it }
        )
    }
}

@Serializable object Appearance

@Composable
fun AppearanceScreen(onNavigateUp: () -> Unit, currentTheme: ThemeSettings, onThemeChange: (ThemeSettings) -> Unit) {
    var darkThemeMenu by remember { mutableStateOf(false) }
    var theme by remember { mutableStateOf(currentTheme) }
    val darkThemeTextID = when(theme.darkTheme) {
        1 -> R.string.on
        0 -> R.string.off
        else -> R.string.follow_system
    }
    MyScaffold(R.string.appearance, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 31) {
            SwitchItem(
                R.string.material_you_color,
                state = theme.materialYou,
                onCheckedChange = { theme = theme.copy(materialYou = it) }
            )
        }
        Box {
            FunctionItem(R.string.dark_theme, stringResource(darkThemeTextID)) { darkThemeMenu = true }
            DropdownMenu(
                expanded = darkThemeMenu, onDismissRequest = { darkThemeMenu = false },
                offset = DpOffset(x = 25.dp, y = 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.follow_system)) },
                    onClick = {
                        theme = theme.copy(darkTheme = -1)
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.on)) },
                    onClick = {
                        theme = theme.copy(darkTheme = 1)
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.off)) },
                    onClick = {
                        theme = theme.copy(darkTheme = 0)
                        darkThemeMenu = false
                    }
                )
            }
        }
        AnimatedVisibility(theme.darkTheme == 1 || (theme.darkTheme == -1 && isSystemInDarkTheme())) {
            SwitchItem(R.string.black_theme, state = theme.blackTheme, onCheckedChange = { theme = theme.copy(blackTheme = it) })
        }
        AnimatedVisibility(theme != currentTheme, Modifier.fillMaxWidth().padding(8.dp)) {
            Button({onThemeChange(theme)}) {
                Text(stringResource(R.string.apply))
            }
        }
    }
}

@Serializable object AppLockSettings

@Composable
fun AppLockSettingsScreen(onNavigateUp: () -> Unit) = MyScaffold(R.string.app_lock, onNavigateUp, 0.dp) {
    val fm = LocalFocusManager.current
    val sp = SharedPrefs(LocalContext.current)
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var allowBiometrics by remember { mutableStateOf(sp.biometricsUnlock) }
    val fr = FocusRequester()
    val alreadySet = !sp.lockPassword.isNullOrEmpty()
    val isInputLegal = password.length !in 1..3 && (alreadySet || (password.isNotEmpty() && password.isNotBlank()))
    Column(Modifier.widthIn(max = 300.dp).align(Alignment.CenterHorizontally)) {
        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.password)) },
            supportingText = { Text(stringResource(if(alreadySet) R.string.leave_empty_to_remain_unchanged else R.string.minimum_length_4)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions { fr.requestFocus() }
        )
        OutlinedTextField(
            confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth().focusRequester(fr),
            label = { Text(stringResource(R.string.confirm_password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        Button(
            onClick = {
                fm.clearFocus()
                if(password.isNotEmpty()) sp.lockPassword = password
                sp.biometricsUnlock = allowBiometrics
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isInputLegal && confirmPassword == password
        ) {
            Text(stringResource(if(alreadySet) R.string.update else R.string.set))
        }
        if(alreadySet) FilledTonalButton(
            onClick = {
                fm.clearFocus()
                sp.lockPassword = ""
                sp.biometricsUnlock = false
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.disable))
        }
    }
}
