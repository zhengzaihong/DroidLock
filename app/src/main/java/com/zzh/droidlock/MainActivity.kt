package com.zzh.droidlock

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.SystemUpdatePolicy
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.UserManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rosan.dhizuku.api.Dhizuku
import com.zzh.droidlock.dpm.Accounts
import com.zzh.droidlock.dpm.AccountsScreen
import com.zzh.droidlock.dpm.DeviceAdmin
import com.zzh.droidlock.dpm.DeviceAdminScreen
import com.zzh.droidlock.dpm.DeviceInfo
import com.zzh.droidlock.dpm.DeviceInfoScreen
import com.zzh.droidlock.dpm.DeviceOwner
import com.zzh.droidlock.dpm.DeviceOwnerScreen
import com.zzh.droidlock.dpm.Keyguard
import com.zzh.droidlock.dpm.KeyguardDisabledFeatures
import com.zzh.droidlock.dpm.KeyguardDisabledFeaturesScreen
import com.zzh.droidlock.dpm.KeyguardScreen
import com.zzh.droidlock.dpm.Network
import com.zzh.droidlock.dpm.NetworkOptions
import com.zzh.droidlock.dpm.NetworkOptionsScreen
import com.zzh.droidlock.dpm.NetworkScreen
import com.zzh.droidlock.dpm.NetworkStatsViewer
import com.zzh.droidlock.dpm.NetworkStatsViewerScreen
import com.zzh.droidlock.dpm.Password
import com.zzh.droidlock.dpm.PasswordInfo
import com.zzh.droidlock.dpm.PasswordInfoScreen
import com.zzh.droidlock.dpm.PasswordScreen
import com.zzh.droidlock.dpm.PermissionPolicy
import com.zzh.droidlock.dpm.PermissionPolicyScreen
import com.zzh.droidlock.dpm.Permissions
import com.zzh.droidlock.dpm.PermissionsScreen
import com.zzh.droidlock.dpm.RequiredPasswordComplexity
import com.zzh.droidlock.dpm.RequiredPasswordComplexityScreen
import com.zzh.droidlock.dpm.RequiredPasswordQuality
import com.zzh.droidlock.dpm.RequiredPasswordQualityScreen
import com.zzh.droidlock.dpm.ResetPassword
import com.zzh.droidlock.dpm.ResetPasswordScreen
import com.zzh.droidlock.dpm.ResetPasswordToken
import com.zzh.droidlock.dpm.ResetPasswordTokenScreen
import com.zzh.droidlock.dpm.Restriction
import com.zzh.droidlock.dpm.SystemManager
import com.zzh.droidlock.dpm.SystemManagerScreen
import com.zzh.droidlock.dpm.SystemOptions
import com.zzh.droidlock.dpm.SystemOptionsScreen
import com.zzh.droidlock.dpm.UserOperation
import com.zzh.droidlock.dpm.UserOperationScreen
import com.zzh.droidlock.dpm.UserRestriction
import com.zzh.droidlock.dpm.UserRestrictionOptions
import com.zzh.droidlock.dpm.UserRestrictionOptionsScreen
import com.zzh.droidlock.dpm.UserRestrictionScreen
import com.zzh.droidlock.dpm.Users
import com.zzh.droidlock.dpm.UsersOptions
import com.zzh.droidlock.dpm.UsersOptionsScreen
import com.zzh.droidlock.dpm.UsersScreen
import com.zzh.droidlock.dpm.dhizukuErrorStatus
import com.zzh.droidlock.dpm.dhizukuPermissionGranted
import com.zzh.droidlock.dpm.getDPM
import com.zzh.droidlock.dpm.getReceiver
import com.zzh.droidlock.dpm.isDeviceOwner
import com.zzh.droidlock.dpm.isProfileOwner
import com.zzh.droidlock.dpm.setDefaultAffiliationID
import com.zzh.droidlock.ui.Animations
import com.zzh.droidlock.ui.theme.DroidLockTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.util.Locale

val backToHomeStateFlow = MutableStateFlow(false)

// UI-Model
const val DROID_LOCK_UI_STATUS= true



@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        registerActivityResult(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val context = applicationContext
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        val locale = context.resources?.configuration?.locale
        zhCN = locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.CHINESE || locale == Locale.CHINA
        val vm by viewModels<MyViewModel>()
        lifecycleScope.launch { delay(5000); setDefaultAffiliationID(context) }
        setContent {
            val theme by vm.theme.collectAsStateWithLifecycle()
            DroidLockTheme(theme) {
                if (DROID_LOCK_UI_STATUS){
                    Home(vm)
                }else{
                    SetupScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sp = SharedPrefs(applicationContext)
        if (sp.dhizuku) {
            if (Dhizuku.init(applicationContext)) {
                if (!dhizukuPermissionGranted()) { dhizukuErrorStatus.value = 2 }
            } else {
                sp.dhizuku = false
                dhizukuErrorStatus.value = 1
            }
        }
    }
}


@Composable
fun SetupScreen() {
    var isConfigured by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val receiver = context.getReceiver()
    var statusMessage by remember { mutableStateOf(getContext().getString(R.string.app_configuring_ing)) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val dpm = context.getDPM()
                dpm.addUserRestriction(receiver, UserManager.DISALLOW_CONFIG_WIFI)
                statusMessage = getContext().getString(R.string.app_wifi_disabling)
                delay(1000)
                if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dpm.addUserRestriction(receiver, UserManager.DISALLOW_BLUETOOTH)
                    dpm.addUserRestriction(receiver, UserManager.DISALLOW_BLUETOOTH_SHARING)
                    statusMessage =getContext().getString(R.string.app_bluetooth_disabling)
                    delay(1000)
                }
                statusMessage = getContext().getString(R.string.app_factory_reset_disabling)
                dpm.addUserRestriction(receiver, UserManager.DISALLOW_FACTORY_RESET)
                delay(1000)
                statusMessage = getContext().getString(R.string.app_security_disabling)
                dpm.addUserRestriction(receiver, UserManager.DISALLOW_SAFE_BOOT)
                delay(1000)

                statusMessage = getContext().getString(R.string.app_auto_update_disabling)
                val policy = SystemUpdatePolicy.createWindowedInstallPolicy(0, 0)
                dpm.setSystemUpdatePolicy(receiver, policy)
                statusMessage =getContext().getString(R.string.app_configuring_success)
            } catch (e: Exception) {
                statusMessage = getContext().getString(R.string.app_configuring_failure)
            }finally {
                isConfigured = true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isConfigured) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = statusMessage,
                    fontSize = 70.sp)
            }
        }
    }
}


@ExperimentalMaterial3Api
@Composable
fun Home(vm: MyViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val backToHome by backToHomeStateFlow.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(backToHome) {
        if(backToHome) { navController.navigateUp(); backToHomeStateFlow.value = false }
    }
    val userRestrictions by vm.userRestrictions.collectAsStateWithLifecycle()
    fun navigateUp() { navController.navigateUp() }
    fun navigate(destination: Any) { navController.navigate(destination) }
    @Suppress("NewApi") NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .imePadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusMgr.clearFocus() }) },
        enterTransition = Animations.navHostEnterTransition,
        exitTransition = Animations.navHostExitTransition,
        popEnterTransition = Animations.navHostPopEnterTransition,
        popExitTransition = Animations.navHostPopExitTransition
    ) {
        composable<Home> { HomeScreen { navController.navigate(it) } }

        composable<Permissions> {
            PermissionsScreen(::navigateUp, { navController.navigate(it) })
        }
        composable<Accounts>(mapOf(serializableNavTypePair<List<Accounts.Account>>())) { AccountsScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceAdmin> { DeviceAdminScreen(::navigateUp) }
        composable<DeviceOwner> { DeviceOwnerScreen(::navigateUp) }
        composable<DeviceInfo> { DeviceInfoScreen(::navigateUp) }
        composable<SystemManager> { SystemManagerScreen(::navigateUp, ::navigate) }
        composable<SystemOptions> { SystemOptionsScreen(::navigateUp) }
        composable<Keyguard> { KeyguardScreen(::navigateUp) }
        composable<PermissionPolicy> { PermissionPolicyScreen(::navigateUp) }

        composable<Network> { NetworkScreen(::navigateUp, ::navigate) }
        composable<NetworkOptions> { NetworkOptionsScreen(::navigateUp) }
        composable<NetworkStatsViewer>(mapOf(serializableNavTypePair<List<NetworkStatsViewer.Data>>())) {
            NetworkStatsViewerScreen(it.toRoute()) { navController.navigateUp() }
        }
        composable<UserRestriction> {
            LaunchedEffect(Unit) {
                vm.userRestrictions.value = context.getDPM().getUserRestrictions(receiver)
            }
            UserRestrictionScreen(::navigateUp) { title, items ->
                navController.navigate(UserRestrictionOptions(title, items))
            }
        }
        composable<UserRestrictionOptions>(mapOf(serializableNavTypePair<List<Restriction>>())) {
            UserRestrictionOptionsScreen(it.toRoute(), userRestrictions, ::navigateUp) { id, status ->
                try {
                    val dpm = context.getDPM()
                    if(status) dpm.addUserRestriction(receiver, id)
                    else dpm.clearUserRestriction(receiver, id)
                    @SuppressLint("NewApi")
                    vm.userRestrictions.value = dpm.getUserRestrictions(receiver)
                } catch(_: Exception) {
                    context.showOperationResultToast(false)
                }
            }
        }

        composable<Users> { UsersScreen(::navigateUp, ::navigate) }
        composable<UsersOptions> { UsersOptionsScreen(::navigateUp) }
        composable<UserOperation> { UserOperationScreen(::navigateUp) }
        composable<Password> { PasswordScreen(::navigateUp, ::navigate) }
        composable<PasswordInfo> { PasswordInfoScreen(::navigateUp) }
        composable<ResetPasswordToken> { ResetPasswordTokenScreen(::navigateUp) }
        composable<ResetPassword> { ResetPasswordScreen(::navigateUp) }
        composable<RequiredPasswordComplexity> { RequiredPasswordComplexityScreen(::navigateUp) }
        composable<KeyguardDisabledFeatures> { KeyguardDisabledFeaturesScreen(::navigateUp) }
        composable<RequiredPasswordQuality> { RequiredPasswordQualityScreen(::navigateUp) }

        composable<Settings> { SettingsScreen(::navigateUp, ::navigate) }
        composable<SettingsOptions> { SettingsOptionsScreen(::navigateUp) }
        composable<Appearance> {
            val theme by vm.theme.collectAsStateWithLifecycle()
            AppearanceScreen(::navigateUp, theme) { vm.theme.value = it }
        }
        composable<AppLockSettings> { AppLockSettingsScreen(::navigateUp) }

        dialog<AppLock>(dialogProperties = DialogProperties(false, false)) {
            AppLockDialog(::navigateUp) { (context as? Activity)?.moveTaskToBack(true) }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if(event == Lifecycle.Event.ON_CREATE && !SharedPrefs(context).lockPassword.isNullOrEmpty()) {
                navController.navigate(AppLock)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        val dpm = context.getDPM()
        val sp = SharedPrefs(context)
        val profileNotActivated = !sp.managedProfileActivated && context.isProfileOwner && (VERSION.SDK_INT < 24 || dpm.isManagedProfile(receiver))
        if(profileNotActivated) {
            dpm.setProfileEnabled(receiver)
            sp.managedProfileActivated = true
            Toast.makeText(context, R.string.work_profile_activated, Toast.LENGTH_SHORT).show()
        }
    }
    DhizukuErrorDialog()
}

@Serializable private object Home

@Composable
private fun HomeScreen(onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var activated by remember { mutableStateOf(false) }
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val refreshStatus by dhizukuErrorStatus.collectAsState()
    LaunchedEffect(refreshStatus) {
        activated = context.isProfileOwner || context.isDeviceOwner
    }
    Scaffold {
        Column(modifier = Modifier
            .padding(it)
            .verticalScroll(rememberScrollState())) {
            Spacer(Modifier.padding(vertical = 25.dp))
            Text(
                text = stringResource(R.string.app_name), style = typography.headlineLarge,
                modifier = Modifier.padding(start = HorizontalPadding)
            )
            Spacer(Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = HorizontalPadding)
                    .clip(RoundedCornerShape(15))
                    .background(color = colorScheme.primary)
                    .clickable(onClick = { onNavigate(Permissions) })
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(if(activated) R.drawable.check_circle_fill1 else R.drawable.block_fill0), null,
                    Modifier.padding(start = 14.dp), colorScheme.onPrimary
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        text = stringResource(if(activated) R.string.activated else R.string.deactivated),
                        style = typography.headlineSmall,
                        color = colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            if(VERSION.SDK_INT >= 24 && (profileOwner || deviceOwner)) {
                HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
            }
            HomePageItem(R.string.settings, R.drawable.settings_fill0) { onNavigate(Settings) }
            Spacer(Modifier.padding(vertical = 20.dp))
        }
    }
}

@Composable
fun HomePageItem(name: Int, imgVector: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(start = 30.dp))
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null
        )
        Spacer(Modifier.padding(start = 15.dp))
        Text(
            text = stringResource(name),
            style = typography.headlineSmall,
            modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp)
        )
    }
}

@Composable
private fun DhizukuErrorDialog() {
    val status by dhizukuErrorStatus.collectAsState()
    if (status != 0) {
        val sp = SharedPrefs(LocalContext.current)
        LaunchedEffect(Unit) {
            sp.dhizuku = false
        }
        AlertDialog(
            onDismissRequest = { dhizukuErrorStatus.value = 0 },
            confirmButton = {
                TextButton(onClick = { dhizukuErrorStatus.value = 0 }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            title = { Text(stringResource(R.string.dhizuku)) },
            text = {
                var text = stringResource(
                    when(status){
                        1 -> R.string.failed_to_init_dhizuku
                        2 -> R.string.dhizuku_permission_not_granted
                        else -> R.string.failed_to_init_dhizuku
                    }
                )
                if(sp.dhizuku) text += "\n" + stringResource(R.string.dhizuku_mode_disabled)
                Text(text)
            }
        )
    }
}
