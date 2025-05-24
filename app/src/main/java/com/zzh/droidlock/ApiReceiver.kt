package com.zzh.droidlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zzh.droidlock.dpm.getDPM
import com.zzh.droidlock.dpm.getReceiver

class ApiReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "OwnDroid API request received. action: ${intent.action}\nkey: $requestKey"
        val sp = SharedPrefs(context)
        if(!sp.isApiEnabled) return
        val key = sp.apiKey
        if(!key.isNullOrEmpty() && key == requestKey) {
            val dpm = context.getDPM()
            val receiver = context.getReceiver()
            val app = intent.getStringExtra("package")
            val restriction = intent.getStringExtra("restriction")
            if(!app.isNullOrEmpty()) log += "\npackage: $app"
            try {
                @SuppressWarnings("NewApi")
                val ok = when(intent.action) {
                    "com.zzh.droidlock.action.HIDE" -> dpm.setApplicationHidden(receiver, app, true)
                    "com.zzh.droidlock.action.UNHIDE" -> dpm.setApplicationHidden(receiver, app, false)
                    "com.zzh.droidlock.action.SUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), true).isEmpty()
                    "com.zzh.droidlock.action.UNSUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), false).isEmpty()
                    "com.zzh.droidlock.action.ADD_USER_RESTRICTION" -> { dpm.addUserRestriction(receiver, restriction); true }
                    "com.zzh.droidlock.action.CLEAR_USER_RESTRICTION" -> { dpm.clearUserRestriction(receiver, restriction); true }
                    "com.zzh.droidlock.action.LOCK" -> { dpm.lockNow(); true }
                    else -> {
                        log += "\nInvalid action"
                        false
                    }
                }
                log += "\nsuccess: $ok"
            } catch(e: Exception) {
                e.printStackTrace()
                val message = (e::class.qualifiedName ?: "Exception") + ": " + (e.message ?: "")
                log += "\n$message"
            }
        } else {
            log += "\nUnauthorized"
        }
        Log.d(TAG, log)
    }
    companion object {
        private const val TAG = "API"
    }
}
