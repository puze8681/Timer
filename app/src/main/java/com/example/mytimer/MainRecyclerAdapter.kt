package com.example.mytimer

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_main.view.*
import java.text.SimpleDateFormat
import java.util.*

class MainRecyclerAdapter(var items: ArrayList<MainData>, var context: Context, var prefUtil: PrefUtil) : RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_main, null), context, prefUtil)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], context)
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(holder.itemView, position)
        }
        holder.itemView.setOnLongClickListener {
            longClick?.onLongClick(holder.itemView, position)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View, context: Context, prefUtil: PrefUtil) : RecyclerView.ViewHolder(itemView) {
        private var mCountDownTimer: CountDownTimer? = null
        private var mTimeLeftInMillis: Long = 0
        private var mStartTimeInMillis: Long = 0
        private var mRunning: Boolean = false
        private var cxt: Context = context
        private var pref: PrefUtil = prefUtil
        var prefs: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null
        fun bind(item: MainData, context: Context) {
            itemView.item_name.text = item.name
            prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            editor = prefs!!.edit()
            when(item.type){
                0->{
                    itemView.item_type.text = "알람"
                    val nextNotifyTime = Calendar.getInstance()
                    nextNotifyTime.timeInMillis = item.time
                    val currentDateTime: Date = nextNotifyTime.time
                    itemView.item_time.text = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime)
                }
                1->{
                    itemView.item_type.text = "스톱워치"
                    val handler: Handler = object : Handler() {
                        override fun handleMessage(msg: Message) {
                            itemView.item_time.text = (getTime(item.time))
                            sendEmptyMessage(0)
                        }
                    }
                    //핸들러 실행
                    handler.sendEmptyMessage(0)
                }
                2->{
                    itemView.item_type.text = "카운트다운"
                    mRunning = prefs!!.getBoolean("timerRunning", false)
                    mStartTimeInMillis = prefs!!.getLong("startTimeInMillis", 600000)
                    mTimeLeftInMillis = prefs!!.getLong("millisLeft", mStartTimeInMillis)

                    itemView.item_time.text = updateCountDownText(mTimeLeftInMillis)

                    if (mRunning) {
                        var mEndTime = prefs!!.getLong("endTime", 0)
                        mTimeLeftInMillis = mEndTime - System.currentTimeMillis()
                        if (mTimeLeftInMillis < 0) {
                            mTimeLeftInMillis = 0
                            itemView.item_time.text = updateCountDownText(mTimeLeftInMillis)
                        } else {
                            startTimer(itemView, mStartTimeInMillis)
                        }
                    }else{
                        if(mCountDownTimer != null){
                            mCountDownTimer!!.cancel()
                        }
                    }
                }
            }
        }

        fun getTime(baseTime: Long): String? { //경과된 시간 체크
            val nowTime = SystemClock.elapsedRealtime()
            //시스템이 부팅된 이후의 시간
            val overTime: Long = nowTime - baseTime
            val m = overTime / 1000 / 60
            val s = overTime / 1000 % 60
            val ms = overTime % 1000
            return String.format("%02d:%02d:%03d", m, s, ms)
        }

        fun updateCountDownText(mTimeLeftInMillis: Long): String{
            var hours: Int = ((mTimeLeftInMillis / 1000) / 3600).toInt()
            var minutes: Int= ((mTimeLeftInMillis / 1000 % 3600) / 60).toInt()
            var seconds: Int = ((mTimeLeftInMillis / 1000) % 60).toInt()

            var timeLeftFormatted: String
            if (hours > 0)
            {
                timeLeftFormatted = String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, seconds)
            } else
            {
                timeLeftFormatted = String.format(Locale.getDefault(),
                        "%02d:%02d", minutes, seconds)
            }
            return timeLeftFormatted
        }

        private fun startTimer(view: View, mStartTimeInMillis: Long) {
            mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    mTimeLeftInMillis = millisUntilFinished
                    view.item_time.text = updateCountDownText(mTimeLeftInMillis)
                    var mEndTime = System.currentTimeMillis() + mTimeLeftInMillis
                    editor!!.putLong("startTimeInMillis", mStartTimeInMillis)
                    editor!!.putLong("millisLeft", mTimeLeftInMillis)
                    editor!!.putLong("endTime", mEndTime)
                    editor!!.apply()
                    Log.d("LOGTAG, RECYCLER TiMER", "running ${mTimeLeftInMillis}")
                    if(!(prefs!!.getBoolean("timerRunning", false))){
                        cancel()
                    }
                }

                override fun onFinish() {
                    startSound(cxt)
                    repeatTimer()
                }
            }.start()
            mRunning = true
        }

        fun startSound(context: Context?) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
            mediaPlayer.start()
            Log.d("LOGTAG", "startSound")
        }

        private fun repeatTimer() {
            val repeatCount = prefs!!.getInt("repeatCount", 0)
            val currentCount = prefs!!.getInt("currentCount", 0)
            if (currentCount < repeatCount) {
                editor!!.putInt("currentCount", currentCount + 1)
                mTimeLeftInMillis = mStartTimeInMillis
                updateCountDownText(mTimeLeftInMillis)
                startTimer(itemView, mStartTimeInMillis)
            } else {
                mRunning = false
                editor!!.putBoolean("timerRunning", mRunning)
                editor!!.apply()
            }
        }
    }

    var itemClick: ItemClick? = null
    var longClick: LongClick? = null

    interface ItemClick {
        fun onItemClick(view: View?, position: Int)
    }

    interface LongClick {
        fun onLongClick(view: View?, position: Int)
    }

    fun getSize(): Int{
        return items.size
    }
}
