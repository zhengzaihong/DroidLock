package com.zzh.droidlock.dpm

import android.app.usage.NetworkStats
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zzh.droidlock.HorizontalPadding
import com.zzh.droidlock.R
import com.zzh.droidlock.formatDate
import com.zzh.droidlock.formatFileSize
import com.zzh.droidlock.ui.FunctionItem
import com.zzh.droidlock.ui.MyScaffold
import com.zzh.droidlock.ui.MySmallTitleScaffold
import com.zzh.droidlock.ui.SwitchItem
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable object Network

@Composable
fun NetworkScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    MyScaffold(R.string.network, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 30) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(NetworkOptions) }
        }
    }
}

@Serializable object NetworkOptions

@Composable
fun NetworkOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT>=30 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            SwitchItem(R.string.lockdown_admin_configured_network, icon = R.drawable.wifi_password_fill0,
                getState = { dpm.hasLockdownAdminConfiguredNetworks(receiver) }, onCheckedChange = { dpm.setConfiguredNetworksLockdownState(receiver,it) },
                onClickBlank = { dialog = 1 }
            )
        }
    }
    if(dialog != 0) AlertDialog(
        text = { Text(stringResource(R.string.info_lockdown_admin_configured_network)) },
        confirmButton = {
            TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = { dialog = 0 }
    )
}


@Serializable
data class NetworkStatsViewer(
    val stats: List<Data>
) {
    @Serializable
    data class Data(
        val rxBytes: Long,
        val rxPackets: Long,
        val txBytes: Long,
        val txPackets: Long,
        val uid: Int,
        val state: Int,
        val startTime: Long,
        val endTime: Long,
        val tag: Int?,
        val roaming: Int?,
        val metered: Int?
    )
}

@Composable
fun NetworkStatsViewerScreen(nsv: NetworkStatsViewer, onNavigateUp: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val size = nsv.stats.size
    val ps = rememberPagerState { size }
    index = ps.currentPage
    val coroutine = rememberCoroutineScope()
    MySmallTitleScaffold(R.string.network_stats, onNavigateUp, 0.dp) {
        if(size > 1) Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        ) {
            IconButton(
                onClick = {
                    coroutine.launch {
                        ps.animateScrollToPage(index - 1)
                    }
                },
                enabled = index > 0
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = null)
            }
            Text("${index + 1} / $size", modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(
                onClick = {
                    coroutine.launch {
                        ps.animateScrollToPage(index + 1)
                    }
                },
                enabled = index < size - 1
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
        HorizontalPager(ps, Modifier.padding(top = 8.dp)) { page ->
            val data = nsv.stats[page]
            Column(Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)) {
                Row(Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SimpleDateFormat("", Locale.getDefault()).format(Date(data.startTime))
                    Text(
                        formatDate("yyyy/MM/dd", data.startTime) + "\n" + formatDate("HH:mm:ss", data.startTime),
                        textAlign = TextAlign.Center
                    )
                    Text("~", Modifier.padding(horizontal = 8.dp))
                    Text(
                        formatDate("yyyy/MM/dd", data.endTime) + "\n" + formatDate("HH:mm:ss", data.endTime),
                        textAlign = TextAlign.Center
                    )
                }
                val txBytes = data.txBytes
                Text(stringResource(R.string.transmitted), style = typography.titleLarge)
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                    Text("$txBytes bytes (${formatFileSize(txBytes)})")
                    Text(data.txPackets.toString() + " packets")
                }
                val rxBytes = data.rxBytes
                Text(stringResource(R.string.received), style = typography.titleLarge)
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                    Text("$rxBytes bytes (${formatFileSize(rxBytes)})")
                    Text(data.rxPackets.toString() + " packets")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = when(data.state) {
                        NetworkStats.Bucket.STATE_ALL -> R.string.all
                        NetworkStats.Bucket.STATE_DEFAULT -> R.string.default_str
                        NetworkStats.Bucket.STATE_FOREGROUND -> R.string.foreground
                        else -> R.string.unknown
                    }
                    Text(stringResource(R.string.state), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(text))
                }
                if(VERSION.SDK_INT >= 24) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val tag = data.tag
                        Text(stringResource(R.string.tag), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                        Text(if(tag == NetworkStats.Bucket.TAG_NONE) stringResource(R.string.all) else tag.toString())
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val text = when(data.roaming) {
                            NetworkStats.Bucket.ROAMING_ALL -> R.string.all
                            NetworkStats.Bucket.ROAMING_YES -> R.string.yes
                            NetworkStats.Bucket.ROAMING_NO -> R.string.no
                            else -> R.string.unknown
                        }
                        Text(stringResource(R.string.roaming), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(text))
                    }
                }
                if(VERSION.SDK_INT >= 26) Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = when(data.metered) {
                        NetworkStats.Bucket.METERED_ALL -> R.string.all
                        NetworkStats.Bucket.METERED_YES -> R.string.yes
                        NetworkStats.Bucket.METERED_NO -> R.string.no
                        else -> R.string.unknown
                    }
                    Text(stringResource(R.string.metered), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(text))
                }
            }
        }
    }
}
