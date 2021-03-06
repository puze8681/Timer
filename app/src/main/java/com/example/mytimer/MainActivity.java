package com.example.mytimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btn_stopwatch, btn_alarm, btn_countdown, btn_delete;
    RecyclerView recycler;
    PrefUtil prefUtil;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public TextView textStopwatch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        editor = prefs.edit();
        btn_stopwatch=findViewById(R.id.btn_stopwatch);
        btn_alarm=findViewById(R.id.btn_alarm);
        btn_countdown=findViewById(R.id.btn_countdown);
        btn_delete=findViewById(R.id.btn_delete);
        recycler=findViewById(R.id.recycler_main);
        prefUtil = new PrefUtil(this);
    }

    public void mOnClick(View v){
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.btn_stopwatch:
                intent=new Intent(MainActivity.this, StopWatch2.class );
                startActivity(intent);
                break;

            case R.id.btn_alarm:
                intent=new Intent(MainActivity.this, Alarm.class);
                startActivity(intent);
                break;

            case R.id.btn_countdown:
                intent=new Intent(MainActivity.this, CountDown.class);
                startActivity(intent);
                break;
            case R.id.btn_delete:
                Log.d("LOGTAG", "delete");
                prefUtil.resetPref();
                editor.putBoolean("timerRunning", false);
                editor.apply();
                setRecyclerView(prefUtil);
                Intent alarmIntent = new Intent(this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
        }
    }

    public void setRecyclerView(final PrefUtil prefUtil) {
        final ArrayList<MainData> mainData = new ArrayList<>();
        ArrayList<String> alarmNameList = prefUtil.getStringArrayPref(prefUtil.getALARM_NAME());
        ArrayList<Long> alarmTimeList = prefUtil.getLongArrayPref(prefUtil.getALARM_TIME());
        ArrayList<String> stopNameList = prefUtil.getStringArrayPref(prefUtil.getSTOP_NAME());
        ArrayList<Long> stopTimeList = prefUtil.getLongArrayPref(prefUtil.getSTOP_TIME());
        for(int i=0; i<alarmNameList.size(); i++) {
            mainData.add(new MainData(0, alarmNameList.get(i), alarmTimeList.get(i)));
        }
        for(int i=0; i<stopTimeList.size(); i++) {
            mainData.add(new MainData(1, stopNameList.get(i), stopTimeList.get(i)));
        }
        Boolean mRunning = prefs.getBoolean("timerRunning", false);
        String countName = prefs.getString("countName", "Count Down");
        if(mRunning){
            mainData.add(new MainData(2, countName, 0));
        }
        Log.d("LOGTAG, recycler", mainData.toString());
        MainRecyclerAdapter mainAdapter = new MainRecyclerAdapter(mainData, this, prefUtil);
        Log.d("mainAdapter", String.valueOf(mainAdapter.getSize()));
        recycler.setAdapter(mainAdapter);
        recycler.getAdapter().notifyDataSetChanged();
        mainAdapter.setLongClick(new MainRecyclerAdapter.LongClick() {
            @Override
            public void onLongClick(@Nullable View view, int position) {
                switch (mainData.get(position).getType()){
                    case 0:
                        prefUtil.deleteStringArrayPref(prefUtil.getALARM_NAME(),mainData.get(position).getName());
                        prefUtil.deleteLongArrayPref(prefUtil.getALARM_TIME(),mainData.get(position).getTime());
                        Toast.makeText(getApplicationContext(),"알람 삭제."+mainData.get(position).getName()+mainData.get(position).getTime(), Toast.LENGTH_LONG).show();
                        setRecyclerView(prefUtil);
                        break;
                    case 1:
                        prefUtil.deleteStringArrayPref(prefUtil.getSTOP_NAME(),mainData.get(position).getName());
                        prefUtil.deleteLongArrayPref(prefUtil.getSTOP_TIME(),mainData.get(position).getTime());
                        Toast.makeText(getApplicationContext(),"스톱워치 삭제."+mainData.get(position).getName()+mainData.get(position).getTime(), Toast.LENGTH_LONG).show();
                        setRecyclerView(prefUtil);
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(),"카운트다운은 삭제 불가."+mainData.get(position).getName()+mainData.get(position).getTime(), Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),"예외입니다."+mainData.get(position).getName()+mainData.get(position).getTime(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecyclerView(prefUtil);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setRecyclerView(prefUtil);
    }
}
