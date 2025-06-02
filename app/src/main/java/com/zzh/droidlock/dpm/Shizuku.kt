package com.zzh.droidlock.dpm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzh.droidlock.R
import com.zzh.droidlock.ui.MySmallTitleScaffold
import kotlinx.serialization.Serializable


@Serializable
data class Accounts(
    val list: List<Account>
) {
    @Serializable data class Account(val type: String, val name: String)
}

@Composable
fun AccountsScreen(accounts: Accounts, onNavigateUp: () -> Unit) {
    MySmallTitleScaffold(R.string.accounts, onNavigateUp) {
        accounts.list.forEach {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(15)).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SelectionContainer {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.type) + ": " + it.type)
                        Text(stringResource(R.string.name) + ": " + it.name)
                    }
                }
            }
        }
    }
}
