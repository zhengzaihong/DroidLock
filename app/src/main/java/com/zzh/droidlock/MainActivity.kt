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
import com.zzh.droidlock.dpm.AddApnSetting
import com.zzh.droidlock.dpm.AddApnSettingScreen
import com.zzh.droidlock.dpm.AddDelegatedAdmin
import com.zzh.droidlock.dpm.AddDelegatedAdminScreen
import com.zzh.droidlock.dpm.AddNetwork
import com.zzh.droidlock.dpm.AddNetworkScreen
import com.zzh.droidlock.dpm.AddPreferentialNetworkServiceConfig
import com.zzh.droidlock.dpm.AddPreferentialNetworkServiceConfigScreen
import com.zzh.droidlock.dpm.AffiliationId
import com.zzh.droidlock.dpm.AffiliationIdScreen
import com.zzh.droidlock.dpm.AlwaysOnVpnPackage
import com.zzh.droidlock.dpm.AlwaysOnVpnPackageScreen
import com.zzh.droidlock.dpm.ApplicationDetails
import com.zzh.droidlock.dpm.ApplicationDetailsScreen
import com.zzh.droidlock.dpm.BlockUninstall
import com.zzh.droidlock.dpm.BlockUninstallScreen
import com.zzh.droidlock.dpm.CaCert
import com.zzh.droidlock.dpm.CaCertScreen
import com.zzh.droidlock.dpm.ChangeTime
import com.zzh.droidlock.dpm.ChangeTimeScreen
import com.zzh.droidlock.dpm.ChangeTimeZone
import com.zzh.droidlock.dpm.ChangeTimeZoneScreen
import com.zzh.droidlock.dpm.ChangeUsername
import com.zzh.droidlock.dpm.ChangeUsernameScreen
import com.zzh.droidlock.dpm.ClearAppStorage
import com.zzh.droidlock.dpm.ClearAppStorageScreen
import com.zzh.droidlock.dpm.ContentProtectionPolicy
import com.zzh.droidlock.dpm.ContentProtectionPolicyScreen
import com.zzh.droidlock.dpm.CreateUser
import com.zzh.droidlock.dpm.CreateUserScreen
import com.zzh.droidlock.dpm.CredentialManagerPolicy
import com.zzh.droidlock.dpm.CredentialManagerPolicyScreen
import com.zzh.droidlock.dpm.CrossProfilePackages
import com.zzh.droidlock.dpm.CrossProfilePackagesScreen
import com.zzh.droidlock.dpm.CrossProfileWidgetProviders
import com.zzh.droidlock.dpm.CrossProfileWidgetProvidersScreen
import com.zzh.droidlock.dpm.DelegatedAdmins
import com.zzh.droidlock.dpm.DelegatedAdminsScreen
import com.zzh.droidlock.dpm.DeleteWorkProfile
import com.zzh.droidlock.dpm.DeleteWorkProfileScreen
import com.zzh.droidlock.dpm.DeviceAdmin
import com.zzh.droidlock.dpm.DeviceAdminScreen
import com.zzh.droidlock.dpm.DeviceInfo
import com.zzh.droidlock.dpm.DeviceInfoScreen
import com.zzh.droidlock.dpm.DeviceOwner
import com.zzh.droidlock.dpm.DeviceOwnerScreen
import com.zzh.droidlock.dpm.DisableAccountManagement
import com.zzh.droidlock.dpm.DisableAccountManagementScreen
import com.zzh.droidlock.dpm.DisableMeteredData
import com.zzh.droidlock.dpm.DisableMeteredDataScreen
import com.zzh.droidlock.dpm.DisableUserControl
import com.zzh.droidlock.dpm.DisableUserControlScreen
import com.zzh.droidlock.dpm.EnableSystemApp
import com.zzh.droidlock.dpm.EnableSystemAppScreen
import com.zzh.droidlock.dpm.FrpPolicy
import com.zzh.droidlock.dpm.FrpPolicyScreen
import com.zzh.droidlock.dpm.HardwareMonitor
import com.zzh.droidlock.dpm.HardwareMonitorScreen
import com.zzh.droidlock.dpm.Hide
import com.zzh.droidlock.dpm.HideScreen
import com.zzh.droidlock.dpm.InstallExistingApp
import com.zzh.droidlock.dpm.InstallExistingAppScreen
import com.zzh.droidlock.dpm.InstallSystemUpdate
import com.zzh.droidlock.dpm.InstallSystemUpdateScreen
import com.zzh.droidlock.dpm.KeepUninstalledPackages
import com.zzh.droidlock.dpm.KeepUninstalledPackagesScreen
import com.zzh.droidlock.dpm.Keyguard
import com.zzh.droidlock.dpm.KeyguardDisabledFeatures
import com.zzh.droidlock.dpm.KeyguardDisabledFeaturesScreen
import com.zzh.droidlock.dpm.KeyguardScreen
import com.zzh.droidlock.dpm.LockTaskMode
import com.zzh.droidlock.dpm.LockTaskModeScreen
import com.zzh.droidlock.dpm.MtePolicy
import com.zzh.droidlock.dpm.MtePolicyScreen
import com.zzh.droidlock.dpm.NearbyStreamingPolicy
import com.zzh.droidlock.dpm.NearbyStreamingPolicyScreen
import com.zzh.droidlock.dpm.Network
import com.zzh.droidlock.dpm.NetworkLogging
import com.zzh.droidlock.dpm.NetworkLoggingScreen
import com.zzh.droidlock.dpm.NetworkOptions
import com.zzh.droidlock.dpm.NetworkOptionsScreen
import com.zzh.droidlock.dpm.NetworkScreen
import com.zzh.droidlock.dpm.NetworkStatsScreen
import com.zzh.droidlock.dpm.NetworkStatsViewer
import com.zzh.droidlock.dpm.NetworkStatsViewerScreen
import com.zzh.droidlock.dpm.OverrideApn
import com.zzh.droidlock.dpm.OverrideApnScreen
import com.zzh.droidlock.dpm.Password
import com.zzh.droidlock.dpm.PasswordInfo
import com.zzh.droidlock.dpm.PasswordInfoScreen
import com.zzh.droidlock.dpm.PasswordScreen
import com.zzh.droidlock.dpm.PermissionPolicy
import com.zzh.droidlock.dpm.PermissionPolicyScreen
import com.zzh.droidlock.dpm.Permissions
import com.zzh.droidlock.dpm.PermissionsManager
import com.zzh.droidlock.dpm.PermissionsManagerScreen
import com.zzh.droidlock.dpm.PermissionsScreen
import com.zzh.droidlock.dpm.PermittedAccessibilityServices
import com.zzh.droidlock.dpm.PermittedAccessibilityServicesScreen
import com.zzh.droidlock.dpm.PermittedInputMethods
import com.zzh.droidlock.dpm.PermittedInputMethodsScreen
import com.zzh.droidlock.dpm.PreferentialNetworkService
import com.zzh.droidlock.dpm.PreferentialNetworkServiceScreen
import com.zzh.droidlock.dpm.PrivateDns
import com.zzh.droidlock.dpm.PrivateDnsScreen
import com.zzh.droidlock.dpm.ProfileOwner
import com.zzh.droidlock.dpm.ProfileOwnerScreen
import com.zzh.droidlock.dpm.QueryNetworkStats
import com.zzh.droidlock.dpm.RecommendedGlobalProxy
import com.zzh.droidlock.dpm.RecommendedGlobalProxyScreen
import com.zzh.droidlock.dpm.RequiredPasswordComplexity
import com.zzh.droidlock.dpm.RequiredPasswordComplexityScreen
import com.zzh.droidlock.dpm.RequiredPasswordQuality
import com.zzh.droidlock.dpm.RequiredPasswordQualityScreen
import com.zzh.droidlock.dpm.ResetPassword
import com.zzh.droidlock.dpm.ResetPasswordScreen
import com.zzh.droidlock.dpm.ResetPasswordToken
import com.zzh.droidlock.dpm.ResetPasswordTokenScreen
import com.zzh.droidlock.dpm.Restriction
import com.zzh.droidlock.dpm.SecurityLogging
import com.zzh.droidlock.dpm.SecurityLoggingScreen
import com.zzh.droidlock.dpm.SetDefaultDialer
import com.zzh.droidlock.dpm.SetDefaultDialerScreen
import com.zzh.droidlock.dpm.SetSystemUpdatePolicy
import com.zzh.droidlock.dpm.ShizukuScreen
import com.zzh.droidlock.dpm.SupportMessage
import com.zzh.droidlock.dpm.SupportMessageScreen
import com.zzh.droidlock.dpm.Suspend
import com.zzh.droidlock.dpm.SuspendPersonalApp
import com.zzh.droidlock.dpm.SuspendPersonalAppScreen
import com.zzh.droidlock.dpm.SuspendScreen
import com.zzh.droidlock.dpm.SystemManager
import com.zzh.droidlock.dpm.SystemManagerScreen
import com.zzh.droidlock.dpm.SystemOptions
import com.zzh.droidlock.dpm.SystemOptionsScreen
import com.zzh.droidlock.dpm.SystemUpdatePolicyScreen
import com.zzh.droidlock.dpm.TransferOwnership
import com.zzh.droidlock.dpm.TransferOwnershipScreen
import com.zzh.droidlock.dpm.UninstallApp
import com.zzh.droidlock.dpm.UninstallAppScreen
import com.zzh.droidlock.dpm.UserInfo
import com.zzh.droidlock.dpm.UserInfoScreen
import com.zzh.droidlock.dpm.UserOperation
import com.zzh.droidlock.dpm.UserOperationScreen
import com.zzh.droidlock.dpm.UserRestriction
import com.zzh.droidlock.dpm.UserRestrictionOptions
import com.zzh.droidlock.dpm.UserRestrictionOptionsScreen
import com.zzh.droidlock.dpm.UserRestrictionScreen
import com.zzh.droidlock.dpm.UserSessionMessage
import com.zzh.droidlock.dpm.UserSessionMessageScreen
import com.zzh.droidlock.dpm.Users
import com.zzh.droidlock.dpm.UsersOptions
import com.zzh.droidlock.dpm.UsersOptionsScreen
import com.zzh.droidlock.dpm.UsersScreen
import com.zzh.droidlock.dpm.WiFi
import com.zzh.droidlock.dpm.WifiAuthKeypair
import com.zzh.droidlock.dpm.WifiAuthKeypairScreen
import com.zzh.droidlock.dpm.WifiScreen
import com.zzh.droidlock.dpm.WifiSecurityLevel
import com.zzh.droidlock.dpm.WifiSecurityLevelScreen
import com.zzh.droidlock.dpm.WifiSsidPolicyScreen
import com.zzh.droidlock.dpm.WipeData
import com.zzh.droidlock.dpm.WipeDataScreen
import com.zzh.droidlock.dpm.dhizukuErrorStatus
import com.zzh.droidlock.dpm.dhizukuPermissionGranted
import com.zzh.droidlock.dpm.getDPM
import com.zzh.droidlock.dpm.getReceiver
import com.zzh.droidlock.dpm.isDeviceAdmin
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

//Whether it is a silent configuration version
const val DROID_LOCK_BECOME_SILENT= true



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
                if (DROID_LOCK_BECOME_SILENT){
                    SetupScreen()
                }else{
                    Home(vm)
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
//                statusMessage = getContext().getString(R.string.app_factory_reset_disabling)
//                dpm.addUserRestriction(receiver, UserManager.DISALLOW_FACTORY_RESET)
//                delay(1000)
//                statusMessage = getContext().getString(R.string.app_security_disabling)
//                dpm.addUserRestriction(receiver, UserManager.DISALLOW_SAFE_BOOT)
//                delay(1000)
//                statusMessage = getContext().getString(R.string.app_debugging_disabling)
////                dpm.addUserRestriction(receiver, UserManager.DISALLOW_DEBUGGING_FEATURES)
//                delay(1000)
//                statusMessage = getContext().getString(R.string.app_auto_update_disabling)
//                val policy = SystemUpdatePolicy.createWindowedInstallPolicy(0, 0)
//                dpm.setSystemUpdatePolicy(receiver, policy)
//                statusMessage =getContext().getString(R.string.app_configuring_success)
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
                    style = typography.bodyLarge)
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
            PermissionsScreen(::navigateUp, { navController.navigate(it) }) { navController.navigate(ShizukuScreen, it) }
        }
        composable<ShizukuScreen> { ShizukuScreen(it.arguments!!, ::navigateUp) { navController.navigate(it) } }
        composable<Accounts>(mapOf(serializableNavTypePair<List<Accounts.Account>>())) { AccountsScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceAdmin> { DeviceAdminScreen(::navigateUp) }
        composable<ProfileOwner> { ProfileOwnerScreen(::navigateUp) }
        composable<DeviceOwner> { DeviceOwnerScreen(::navigateUp) }
        composable<DelegatedAdmins> { DelegatedAdminsScreen(::navigateUp, ::navigate) }
        composable<AddDelegatedAdmin>{ AddDelegatedAdminScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceInfo> { DeviceInfoScreen(::navigateUp) }
//        composable<LockScreenInfo> { LockScreenInfoScreen(::navigateUp) }
        composable<SupportMessage> { SupportMessageScreen(::navigateUp) }
        composable<TransferOwnership> { TransferOwnershipScreen(::navigateUp) }

        composable<SystemManager> { SystemManagerScreen(::navigateUp, ::navigate) }
        composable<SystemOptions> { SystemOptionsScreen(::navigateUp) }
        composable<Keyguard> { KeyguardScreen(::navigateUp) }
        composable<HardwareMonitor> { HardwareMonitorScreen(::navigateUp) }
        composable<ChangeTime> { ChangeTimeScreen(::navigateUp) }
        composable<ChangeTimeZone> { ChangeTimeZoneScreen(::navigateUp) }
        //composable<> { KeyPairs(::navigateUp) }
        composable<ContentProtectionPolicy> { ContentProtectionPolicyScreen(::navigateUp) }
        composable<PermissionPolicy> { PermissionPolicyScreen(::navigateUp) }
        composable<MtePolicy> { MtePolicyScreen(::navigateUp) }
        composable<NearbyStreamingPolicy> { NearbyStreamingPolicyScreen(::navigateUp) }
        composable<LockTaskMode> { LockTaskModeScreen(::navigateUp) }
        composable<CaCert> { CaCertScreen(::navigateUp) }
        composable<SecurityLogging> { SecurityLoggingScreen(::navigateUp) }
        composable<DisableAccountManagement> { DisableAccountManagementScreen(::navigateUp) }
        composable<SetSystemUpdatePolicy> { SystemUpdatePolicyScreen(::navigateUp) }
        composable<InstallSystemUpdate> { InstallSystemUpdateScreen(::navigateUp) }
        composable<FrpPolicy> { FrpPolicyScreen(::navigateUp) }
        composable<WipeData> { WipeDataScreen(::navigateUp) }

        composable<Network> { NetworkScreen(::navigateUp, ::navigate) }
        composable<WiFi> { WifiScreen(::navigateUp, { navController.navigate(it) }) { navController.navigate(AddNetwork, it)} }
        composable<NetworkOptions> { NetworkOptionsScreen(::navigateUp) }
        composable<AddNetwork> { AddNetworkScreen(it.arguments!!, ::navigateUp) }
        composable<WifiSecurityLevel> { WifiSecurityLevelScreen(::navigateUp) }
        composable<WifiSsidPolicyScreen> { WifiSsidPolicyScreen(::navigateUp) }
        composable<QueryNetworkStats> { NetworkStatsScreen(::navigateUp, ::navigate) }
        composable<NetworkStatsViewer>(mapOf(serializableNavTypePair<List<NetworkStatsViewer.Data>>())) {
            NetworkStatsViewerScreen(it.toRoute()) { navController.navigateUp() }
        }
        composable<PrivateDns> { PrivateDnsScreen(::navigateUp) }
        composable<AlwaysOnVpnPackage> { AlwaysOnVpnPackageScreen(::navigateUp) }
        composable<RecommendedGlobalProxy> { RecommendedGlobalProxyScreen(::navigateUp) }
        composable<NetworkLogging> { NetworkLoggingScreen(::navigateUp) }
        composable<WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
        composable<PreferentialNetworkService> { PreferentialNetworkServiceScreen(::navigateUp, ::navigate) }
        composable<AddPreferentialNetworkServiceConfig> { AddPreferentialNetworkServiceConfigScreen(it.toRoute(), ::navigateUp) }
        composable<OverrideApn> { OverrideApnScreen(::navigateUp) { navController.navigate(AddApnSetting, it) } }
        composable<AddApnSetting> { AddApnSettingScreen(it.arguments?.getParcelable("setting"), ::navigateUp) }

//        composable<WorkProfile> { WorkProfileScreen(::navigateUp, ::navigate) }
//        composable<OrganizationOwnedProfile> { OrganizationOwnedProfileScreen(::navigateUp) }
//        composable<CreateWorkProfile> { CreateWorkProfileScreen(::navigateUp) }
        composable<SuspendPersonalApp> { SuspendPersonalAppScreen(::navigateUp) }
//        composable<CrossProfileIntentFilter> { CrossProfileIntentFilterScreen(::navigateUp) }
        composable<DeleteWorkProfile> { DeleteWorkProfileScreen(::navigateUp) }

//        composable<ApplicationsList> {
//            AppChooserScreen(it.toRoute(), {
//                if(it == null) navigateUp() else navigate(ApplicationDetails(it))
//            }, {
//                SharedPrefs(context).applicationsListView = false
//                navController.navigate(ApplicationsFeatures) {
//                    popUpTo(Home)
//                }
//            })
//        }
//        composable<ApplicationsFeatures> {
//            ApplicationsFeaturesScreen(::navigateUp, ::navigate) {
//                SharedPrefs(context).applicationsListView = true
//                navController.navigate(ApplicationsList(true)) {
//                    popUpTo(Home)
//                }
//            }
//        }
        composable<ApplicationDetails> { ApplicationDetailsScreen(it.toRoute(), ::navigateUp, ::navigate) }
        composable<Suspend> { SuspendScreen(::navigateUp) }
        composable<Hide> { HideScreen(::navigateUp) }
        composable<BlockUninstall> { BlockUninstallScreen(::navigateUp) }
        composable<DisableUserControl> { DisableUserControlScreen(::navigateUp) }
        composable<PermissionsManager> { PermissionsManagerScreen(::navigateUp, it.toRoute()) }
        composable<DisableMeteredData> { DisableMeteredDataScreen(::navigateUp) }
        composable<ClearAppStorage> { ClearAppStorageScreen(::navigateUp) }
        composable<UninstallApp> { UninstallAppScreen(::navigateUp) }
        composable<KeepUninstalledPackages> { KeepUninstalledPackagesScreen(::navigateUp) }
        composable<InstallExistingApp> { InstallExistingAppScreen(::navigateUp) }
        composable<CrossProfilePackages> { CrossProfilePackagesScreen(::navigateUp) }
        composable<CrossProfileWidgetProviders> { CrossProfileWidgetProvidersScreen(::navigateUp) }
        composable<CredentialManagerPolicy> { CredentialManagerPolicyScreen(::navigateUp) }
        composable<PermittedAccessibilityServices> { PermittedAccessibilityServicesScreen(::navigateUp) }
        composable<PermittedInputMethods> { PermittedInputMethodsScreen(::navigateUp) }
        composable<EnableSystemApp> { EnableSystemAppScreen(::navigateUp) }
        composable<SetDefaultDialer> { SetDefaultDialerScreen(::navigateUp) }

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
        composable<UserInfo> { UserInfoScreen(::navigateUp) }
        composable<UsersOptions> { UsersOptionsScreen(::navigateUp) }
        composable<UserOperation> { UserOperationScreen(::navigateUp) }
        composable<CreateUser> { CreateUserScreen(::navigateUp) }
        composable<ChangeUsername> { ChangeUsernameScreen(::navigateUp) }
        composable<UserSessionMessage> { UserSessionMessageScreen(::navigateUp) }
        composable<AffiliationId> { AffiliationIdScreen(::navigateUp) }

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
//        composable<ApiSettings> { ApiSettings(::navigateUp) }
//        composable<Notifications> { NotificationsScreen(::navigateUp) }
//        composable<About> { AboutScreen(::navigateUp) }

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
    var activateType by remember { mutableStateOf("") }
    val deviceAdmin = context.isDeviceAdmin
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val refreshStatus by dhizukuErrorStatus.collectAsState()
    LaunchedEffect(refreshStatus) {
        activated = context.isProfileOwner || context.isDeviceOwner
        activateType = if(SharedPrefs(context).dhizuku) context.getString(R.string.dhizuku) + " - " else ""
        activateType += context.getString(
            if(deviceOwner) { R.string.device_owner }
            else if(profileOwner) {
                if(VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)) R.string.work_profile_owner else R.string.profile_owner
            }
            else if(deviceAdmin) R.string.device_admin else R.string.click_to_activate
        )
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
                    if(activateType != "") { Text(text = activateType, color = colorScheme.onPrimary) }
                }
            }
//            HomePageItem(R.string.system, R.drawable.android_fill0) { onNavigate(SystemManager) }

//            if(deviceOwner || profileOwner) { HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Network) } }
//            if(
//                (VERSION.SDK_INT < 24 && !deviceOwner) || (dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) ||
//                                (profileOwner && dpm.isManagedProfile(receiver))
//                        )
//            ) {
//                HomePageItem(R.string.work_profile, R.drawable.work_fill0) { onNavigate(WorkProfile) }
//            }
//            if(deviceOwner || profileOwner) HomePageItem(R.string.applications, R.drawable.apps_fill0) {
//                onNavigate(if(SharedPrefs(context).applicationsListView) ApplicationsList(true) else ApplicationsFeatures)
//            }
            if(VERSION.SDK_INT >= 24 && (profileOwner || deviceOwner)) {
                HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
            }
//            HomePageItem(R.string.users,R.drawable.manage_accounts_fill0) { onNavigate(Users) }
//            if(deviceOwner || profileOwner) HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0) { onNavigate(Password) }
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
