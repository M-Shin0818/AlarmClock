package com.example.alarmclock

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.TimedText
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.WindowManager.LayoutParams.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.lang.IllegalArgumentException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*
import java.util.regex.Pattern



class MainActivity : AppCompatActivity()
   ,SimpleAlertDialog.OnClickListener
   ,DatePickerFragment.OnDateSelectedListener
   ,TimePickerFragment.OnTimeSelectedListener{

   private lateinit var player : MediaPlayer

   @RequiresApi(Build.VERSION_CODES.CUPCAKE)
   override fun onSelected(year: Int, month: Int, date: Int) {
      val c = Calendar.getInstance()
      c.set(year, month, date)
      dateText.text = android.text.format.DateFormat.format("yyyy/MM/dd", c)
   }

   override fun onSelected(hourOfDay: Int, minute: Int) {
      timeText.text = "%1$02d:%2$02d".format(hourOfDay,minute)
   }

   override fun onPositiveClick() {
      player.stop()
      player.release()
      finish()
   }

   override fun onNegativeClick() {
      player.stop()
      player.seekTo(0)
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = System.currentTimeMillis()
      calendar.add(Calendar.MINUTE, 5)
      setAlarmManager(calendar)
      finish()
   }



   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      player = MediaPlayer.create(this, R.raw.bellsound)

      if (intent?.getBooleanExtra("onReceive", false)==true){
         @Suppress("DEPRECATION")
         when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
               window.addFlags(FLAG_TURN_SCREEN_ON or FLAG_SHOW_WHEN_LOCKED)
            else ->
               window.addFlags(FLAG_TURN_SCREEN_ON or FLAG_SHOW_WHEN_LOCKED or FLAG_DISMISS_KEYGUARD)
         }
         val dialog = SimpleAlertDialog()
         dialog.show(supportFragmentManager, "alert_dialog")
         player.start()
      }

      setContentView(R.layout.activity_main)

      setAlarm.setOnClickListener{
         val date = "${dateText.text} ${timeText.text}".toDate()
         when{
            date != null ->{
               val calendar = Calendar.getInstance()
               calendar.time = date
               setAlarmManager(calendar)
               toast("アラームをセットしました")
            }
            else ->{
               toast("日付の形式が正しくありません")
            }

         }

//            val calendar = Calendar.getInstance()
//            calendar.timeInMillis = System.currentTimeMillis()
//            calendar.add(Calendar.SECOND, 5)
//            setAlarmManager(calendar)
      }

      cancelAlarm.setOnClickListener {
         cancelAlarmManager()
      }

      dateText.setOnClickListener{
         val dialog = DatePickerFragment()
         dialog.show(supportFragmentManager, "date_dialog")
      }

      timeText.setOnClickListener{
         val dialog = TimePickerFragment()
         dialog.show(supportFragmentManager, "time_dialog")
      }


   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   private fun setAlarmManager(calendar: Calendar){
      val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val intent = Intent (this, AlarmBroadcastReceiver::class.java)
      val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
      when{
         Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
            val info = AlarmManager.AlarmClockInfo(
               calendar.timeInMillis, null)
            am.setAlarmClock(info, pending)
         }
         Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
         }
         else -> {
            am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
         }
      }
   }

   private  fun cancelAlarmManager(){
      val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val intent = Intent(this, AlarmBroadcastReceiver::class.java)
      val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
      am.cancel(pending)
   }

   fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm"):Date?{
      val sdFormat = try {
         SimpleDateFormat(pattern)
      } catch (e: IllegalArgumentException){
         null
      }
      val date = sdFormat?.let {
         try {
            it.parse(this)
         } catch (e: ParseException){
            null
         }
      }
      return date
   }


}