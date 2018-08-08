package com.example.android.reminderapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int NO_IMAGE = -1;
    long entry;
    boolean stop = false;
    boolean isStop = false;
    CountDownTimer countDownTimer;
    MediaPlayer mediaPlayer;
    AudioManager audioManager;

    AudioManager.OnAudioFocusChangeListener changeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(1);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            }
        }
    };
    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };

    public long getEntry(long mHr, long mMin, long mSec) {

        long mEntry = (mHr + mMin + mSec);

        return mEntry;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        final TextView timeLeft = findViewById(R.id.timeLeft);
        timeLeft.setVisibility(View.GONE);
        final TextView resultText = findViewById(R.id.result);
        resultText.setVisibility(View.GONE);
        final EditText entrySec = findViewById(R.id.entrySec);
        final EditText entryHour = findViewById(R.id.entryHour);
        final EditText entryMinute = findViewById(R.id.entryMin);
        final LinearLayout timer = findViewById(R.id.timer);
        final TextView okText = findViewById(R.id.pause);
        final TextView entryTime = findViewById(R.id.enterTime);
        okText.setVisibility(View.GONE);

        final Button start = findViewById(R.id.button);
        final Button stopButton = findViewById(R.id.button2);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timer.setVisibility(View.VISIBLE);
                resultText.setVisibility(View.GONE);
                stop = false;
                isStop = false;
                long hr = Long.parseLong(entryHour.getText().toString()) * 3600;
                final long min = Long.parseLong(entryMinute.getText().toString()) * 60;
                long sec = Long.parseLong(entrySec.getText().toString());

                entry = getEntry(hr, min, sec);

                countDownTimer = new CountDownTimer(entry * 1000, 100) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (isStop) {
                            millisUntilFinished = 0;
                            timeLeft.setText(String.valueOf(millisUntilFinished));
                            timer.setVisibility(View.VISIBLE);
                            timeLeft.setVisibility(View.GONE);
                            cancel();

                        } else {
                            timeLeft.setText(String.valueOf(millisUntilFinished / 1000));
                            String text = String.format(Locale.getDefault(), "%02d hr : %02d min : %02d sec",
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 60,
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                            timeLeft.setText(text);
                            timeLeft.setVisibility(View.VISIBLE);
                            timer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFinish() {
                        resultText.setVisibility(View.VISIBLE);
                        resultText.animate().rotationX(360).setDuration(1000);
                        countDownTimer.cancel();

                        int result = audioManager.requestAudioFocus(changeListener, AudioManager.STREAM_MUSIC
                                , AudioManager.AUDIOFOCUS_GAIN);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ring);
                            mediaPlayer.start();

                            mediaPlayer.setOnCompletionListener(completionListener);
                            okText.setVisibility(View.VISIBLE);
                            timeLeft.setVisibility(View.GONE);
                            resultText.setVisibility(View.GONE);
                            entryHour.setVisibility(View.GONE);
                            entryMinute.setVisibility(View.GONE);
                            entrySec.setVisibility(View.GONE);
                            timer.setVisibility(View.INVISIBLE);
                            stopButton.setVisibility(View.GONE);
                            start.setVisibility(View.GONE);
                            entryHour.setVisibility(View.GONE);
                            entryTime.setVisibility(View.INVISIBLE);
                        }


                    }
                }.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeLeft.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                isStop = true;
            }
        });

        okText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaPlayer();
                timeLeft.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                resultText.setVisibility(View.GONE);
                entryHour.setVisibility(View.VISIBLE);
                entryMinute.setVisibility(View.VISIBLE);
                entrySec.setVisibility(View.VISIBLE);
                okText.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                start.setVisibility(View.VISIBLE);
                entryHour.setVisibility(View.VISIBLE);
                entryTime.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {

            mediaPlayer.release();

            mediaPlayer = null;
            audioManager.abandonAudioFocus(changeListener);
        }
    }
}
