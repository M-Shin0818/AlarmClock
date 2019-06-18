package com.example.alarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class AlarmBroadcastReceiver :BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra("onReceive", true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
//        Toast.makeText(context, "アラームを受信しました", Toast.LENGTH_SHORT).show()
//        context?.toast("アラームを受信しました")
    }
}
