package com.zzh.droidlock

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.zzh.droidlock.dpm.addDeviceAdmin
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf

var zhCN = true


fun writeClipBoard(context: Context, string: String):Boolean{
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", string))
    } catch(_:Exception) {
        return false
    }
    return true
}

fun registerActivityResult(context: ComponentActivity) {
    addDeviceAdmin = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dpm = context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if(dpm.isAdminActive(ComponentName(context.applicationContext, Receiver::class.java))) {
            backToHomeStateFlow.value = true
        }
    }
}

fun Context.showOperationResultToast(success: Boolean) {
    Toast.makeText(this, if(success) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
}

@SuppressLint("PrivateApi")
fun getContext(): Context {
    return Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as Context
}

val Boolean.yesOrNo
    @StringRes get() = if(this) R.string.yes else R.string.no

const val APK_MIME = "application/vnd.android.package-archive"

inline fun <reified T> serializableNavTypePair() =
    typeOf<T>() to object : NavType<T>(false) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let { parseValue(it) }
    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, serializeAsValue(value))
    override fun parseValue(value: String): T =
        Json.decodeFromString(value)
    override fun serializeAsValue(value: T): String =
        Json.encodeToString(value)
}

val HorizontalPadding = 16.dp
