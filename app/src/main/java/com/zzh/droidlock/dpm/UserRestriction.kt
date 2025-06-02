package com.zzh.droidlock.dpm

import android.os.Build
import android.os.Bundle
import android.os.UserManager
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzh.droidlock.R
import com.zzh.droidlock.ui.FunctionItem
import com.zzh.droidlock.ui.MyScaffold
import com.zzh.droidlock.ui.SwitchItem
import kotlinx.serialization.Serializable

@Serializable
data class Restriction(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val requiresApi: Int = 0
)

@Serializable object UserRestriction

@RequiresApi(24)
@Composable
fun UserRestrictionScreen(onNavigateUp: () -> Unit, onNavigate: (Int, List<Restriction>) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    MyScaffold(R.string.user_restriction, onNavigateUp, 0.dp) {
        Spacer(Modifier.padding(vertical = 2.dp))
        Text(text = stringResource(R.string.switch_to_disable_feature), modifier = Modifier.padding(start = 16.dp))
        if(context.isProfileOwner) { Text(text = stringResource(R.string.profile_owner_is_restricted), modifier = Modifier.padding(start = 16.dp)) }
        if(context.isProfileOwner && dpm.isManagedProfile(receiver)) {
            Text(text = stringResource(R.string.some_features_invalid_in_work_profile), modifier = Modifier.padding(start = 16.dp))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        FunctionItem(R.string.network, icon = R.drawable.language_fill0) {
            onNavigate(R.string.network, RestrictionData.internet)
        }
        FunctionItem(R.string.connectivity, icon = R.drawable.devices_other_fill0) {
            onNavigate(R.string.connectivity, RestrictionData.connectivity)
        }
        FunctionItem(R.string.other, icon = R.drawable.more_horiz_fill0) {
            onNavigate(R.string.other, RestrictionData.other)
        }
    }
}

@Serializable
data class UserRestrictionOptions(
    val title: Int, val items: List<Restriction>
)

@RequiresApi(24)
@Composable
fun UserRestrictionOptionsScreen(
    data: UserRestrictionOptions, restrictions: Bundle,
    onNavigateUp: () -> Unit, onRestrictionChange: (String, Boolean) -> Unit
) {
    MyScaffold(data.title, onNavigateUp, 0.dp) {
        data.items.filter { Build.VERSION.SDK_INT >= it.requiresApi }.forEach { restriction ->
            SwitchItem(
                restriction.name, restriction.id, restriction.icon,
                restrictions.getBoolean(restriction.id), { onRestrictionChange(restriction.id, it) }, padding = true
            )
        }
    }
}

@Suppress("InlinedApi")
object RestrictionData {
    val internet = listOf(
        Restriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, R.string.config_mobile_network, R.drawable.signal_cellular_alt_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_WIFI, R.string.config_wifi, R.drawable.wifi_fill0),
        Restriction(UserManager.DISALLOW_DATA_ROAMING, R.string.data_roaming, R.drawable.network_cell_fill0, 24),
        Restriction(UserManager.DISALLOW_CELLULAR_2G, R.string.cellular_2g, R.drawable.network_cell_fill0, 34),
        Restriction(UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO, R.string.ultra_wideband_radio, R.drawable.wifi_tethering_fill0, 34),
        Restriction(UserManager.DISALLOW_ADD_WIFI_CONFIG, R.string.add_wifi_conf, R.drawable.wifi_fill0, 33),
        Restriction(UserManager.DISALLOW_CHANGE_WIFI_STATE, R.string.change_wifi_state, R.drawable.wifi_fill0, 33),
    )
    val connectivity = listOf(
        Restriction(UserManager.DISALLOW_BLUETOOTH, R.string.bluetooth, R.drawable.bluetooth_fill0, 26),
        Restriction(UserManager.DISALLOW_BLUETOOTH_SHARING, R.string.bt_share, R.drawable.bluetooth_searching_fill0, 26),
        Restriction(UserManager.DISALLOW_SHARE_LOCATION, R.string.share_location, R.drawable.location_on_fill0),
    )
    val users = listOf(
        Restriction(UserManager.DISALLOW_ADD_USER, R.string.add_user, R.drawable.account_circle_fill0),
        Restriction(UserManager.DISALLOW_REMOVE_USER, R.string.remove_user, R.drawable.account_circle_fill0),
        Restriction(UserManager.DISALLOW_USER_SWITCH, R.string.switch_user, R.drawable.account_circle_fill0, 28),
        Restriction(UserManager.DISALLOW_ADD_PRIVATE_PROFILE, R.string.create_private_space, R.drawable.lock_fill0, 35),
        Restriction(UserManager.DISALLOW_SET_USER_ICON, R.string.set_user_icon, R.drawable.account_circle_fill0, 24),
        Restriction(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE, R.string.cross_profile_copy, R.drawable.content_paste_fill0),
        Restriction(UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE, R.string.share_into_managed_profile, R.drawable.share_fill0, 28),
        Restriction(UserManager.DISALLOW_UNIFIED_PASSWORD, R.string.unified_pwd, R.drawable.work_fill0, 28)
    )
    val other = listOf(
        Restriction(UserManager.DISALLOW_FACTORY_RESET, R.string.factory_reset, R.drawable.android_fill0),
        Restriction(UserManager.DISALLOW_SAFE_BOOT, R.string.safe_boot, R.drawable.security_fill0, 23),
        Restriction(UserManager.DISALLOW_DEBUGGING_FEATURES, R.string.debug_features, R.drawable.adb_fill0)
    )
    fun getAllRestrictions() = internet + connectivity + users + other
}
