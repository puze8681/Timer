package com.example.mytimer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class CountDown extends AppCompatActivity {
    private EditText mEditTextInput, mEditTextInputSecond, editCount, editRepeat;
    private TextView mTextViewCountDown,txt, textRepeat;
    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private Button btn_5min, btn_10min, btn_30min;
    private CountDownTimer mCountDownTimer;
    private long temp;
    private String input;
    private String input2;
    public boolean mTimerRunning;
    private boolean mTimerWrite=false;
    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private PrefUtil prefUtil;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        prefUtil = new PrefUtil(this);
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        editor = prefs.edit();
        mEditTextInput = findViewById(R.id.edit_text_input);
        mEditTextInputSecond = findViewById(R.id.edit_text_input_second);
        editCount = findViewById(R.id.edit_count);
        editRepeat = findViewById(R.id.edit_repeat);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        textRepeat = findViewById(R.id.text_repeat);
        btn_5min=findViewById(R.id.btn_5min);
        btn_10min=findViewById(R.id.btn_10min);
        btn_30min=findViewById(R.id.btn_30min);
        mButtonSet = findViewById(R.id.button_set);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);
        mTimerWrite=false;
        txt=findViewById(R.id.txt);
        btn_5min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextInput.setText("5");
                mEditTextInputSecond.setText("0");
            }
        });
        btn_10min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextInput.setText("10");
                mEditTextInputSecond.setText("0");
            }
        });
        btn_30min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextInput.setText("30");
                mEditTextInputSecond.setText("0");
            }
        });
        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String input="";
                    input = mEditTextInput.getText().toString();
                    input2 = mEditTextInputSecond.getText().toString();
                    //txt.setText(input);
                    //System.out.println(txt);
                if (input.length() == 0) {
                    Toast.makeText(CountDown.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (input2.length() == 0) {
                    Toast.makeText(CountDown.this, "Field2 can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = (Long.parseLong(input) * 60000) + (Long.parseLong(input2) * 1000);
                if (millisInput == 0) {
                    Toast.makeText(CountDown.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(editRepeat.getText().toString().length() == 0){
                    Toast.makeText(CountDown.this, "Please enter repeat number", Toast.LENGTH_SHORT).show();
                    return;
                }
                int repeat = Integer.parseInt(editRepeat.getText().toString());
                if(repeat < 1){
                    Toast.makeText(CountDown.this, "Repeat number must be bigger than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                editor.putInt("repeatCount", repeat);
                editor.putInt("currentCount", 1);
                setTime(millisInput);
                mEditTextInput.setText("");
                mEditTextInputSecond.setText("");
            }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
    }

    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer() {
        editor.putString("countName", editCount.getText().toString());
        editor.apply();
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
                editor.putLong("startTimeInMillis", mStartTimeInMillis);
                editor.putLong("millisLeft", mTimeLeftInMillis);
                editor.putLong("endTime", mEndTime);
                editor.putBoolean("timerRunning", mTimerRunning);
                editor.apply();
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                Log.d("LOGTAG, CountDown", "onFinish");
                startSound(getApplicationContext());
                repeatTimer();
            }
        }.start();
        mTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.apply();
        updateWatchInterface();
    }

    private void resetTimer() {
        editor.putInt("currentCount", 1);
        editRepeat.setVisibility(View.VISIBLE);
        mEditTextInput.setVisibility(View.VISIBLE);
        mEditTextInputSecond.setVisibility(View.VISIBLE);
        mButtonSet.setVisibility(View.VISIBLE);
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void repeatTimer(){
        int repeatCount = prefs.getInt("repeatCount", 0);
        int currentCount = prefs.getInt("currentCount", 0);
        if(currentCount < repeatCount){
            editor.putInt("currentCount", currentCount+1);
            mTimeLeftInMillis = mStartTimeInMillis;
            updateCountDownText();
            startTimer();
        }else{
            mTimerRunning = false;
            editor.putBoolean("timerRunning", mTimerRunning);
            editor.apply();
            updateWatchInterface();
        }
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        int repeatCount = prefs.getInt("repeatCount", 0);
        int currentCount = prefs.getInt("currentCount", 0);
        textRepeat.setText("전체 세트 : "+String.valueOf(repeatCount)+"/ 현재 세트 : "+String.valueOf(currentCount));
        mTextViewCountDown.setText(timeLeftFormatted);

    }

    @SuppressLint("LongLogTag")
    private void updateWatchInterface() {
        Log.d("LOGTAG, COUNTDOWN, updataWatchInterface", String.valueOf(mEndTime));
        Log.d("LOGTAG, COUNTDOWN, updataWatchInterface", String.valueOf(mTimerRunning));
        if (mTimerRunning) {
            mEditTextInput.setVisibility(View.INVISIBLE);
            mEditTextInputSecond.setVisibility(View.INVISIBLE);
            editRepeat.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mButtonStartPause.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onStop() {
        super.onStop();
        if (mCountDownTimer != null) {
            Log.d("LOGTAG, COUNTDOWN, onSTop", "cancle");
            mCountDownTimer.cancel();
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onStart() {
        super.onStart();

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            Log.d("LOGTAG, COUNTDOWN, onStart2", String.valueOf(mEndTime));
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            Log.d("LOGTAG, COUNTDOWN, onStart2", String.valueOf(mTimeLeftInMillis));
            String countName = prefs.getString("countName", "");
            if(countName != ""){
                editCount.setText(countName);
            }else{
                editCount.setHint("이름을 입력하세요.");
            }
            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                editor.putBoolean("timerRunning", mTimerRunning);
                editor.apply();
                Log.d("LOGTAG, COUNTDOWN, onStart2", String.valueOf(mTimerRunning));
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }else{
            Log.d("LOGTAG, COUNTDOWN, onStart2", "else");
            if(mCountDownTimer != null){
                Log.d("LOGTAG, COUNTDOWN, onStart2", "cancle");
                mCountDownTimer.cancel();
            }
        }
    }

    public void startSound(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.start();
        Log.d("LOGTAG", "startSound");
    }
}
