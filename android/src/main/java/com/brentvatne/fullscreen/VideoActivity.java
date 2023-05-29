package com.brentvatne.fullscreen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.View;
import android.widget.Button;
import android.view.MotionEvent;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import android.view.WindowManager;

import com.brentvatne.react.R;

public class VideoActivity extends AppCompatActivity {
    private String videoPath;
    private int videoPosition, clickCount = 0;
    private long startTime, duration;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static int sDefaultTimeout = 3000;
    private boolean mShowing = false;
    private Bundle extras;

    private static ProgressDialog progressDialog;
    VideoView myVideoView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long progress;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.player_fullscreen);
        Intent i = getIntent();
        if(i != null){
            myVideoView = (VideoView) findViewById(R.id.videoView);
            extras = i.getExtras();
            videoPath = extras.getString("VIDEO_URL");
            videoPosition = extras.getInt("VIDEO_POSITION");
            progressDialog = ProgressDialog.show(VideoActivity.this, "", "Buffering video...", true);
            progressDialog.setCancelable(true);
            PlayVideo();
        }
        else{
            Toast.makeText(VideoActivity.this, "VideoURL not found", Toast.LENGTH_SHORT).show();
        }
        hide();
    }
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return false;
    }

    //当触摸事件处于控制器上的时候，显示控制器，不让其消失
    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);

        return false;
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void hide() {
        Button button2 = (Button) findViewById(com.brentvatne.react.R.id.closeBtn);
        button2.setVisibility(View.INVISIBLE);
    }

    public void show(int time) {
        // getActionBar().hide();
        /*if (!mShowing && mAnchorVGroup != null) {
            if (mPlayer.isFullScreen()) {//是全屏，显示上下
                showTopAndBottom();
            } else {//显示底部
                showBottom();
            }
        }

        if (mPlayer.isPlaying()) {
            iv_play.setImageResource(R.mipmap.k_stop);
        } else {
            iv_play.setImageResource(R.mipmap.k_play);
        }*/

        Button button2 = (Button) findViewById(com.brentvatne.react.R.id.closeBtn);
        button2.setVisibility(View.VISIBLE);

        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        // mShowing = true;
        if (time != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
                    time);
        }
    }

    private void PlayVideo() {
        try {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(VideoActivity.this);
            mediaController.setAnchorView(myVideoView);

            Uri video = Uri.parse(videoPath);
            myVideoView.setMediaController(mediaController);
            myVideoView.setVideoURI(video);
            myVideoView.requestFocus();
            myVideoView.setKeepScreenOn(true);
            myVideoView.seekTo(videoPosition * 1000);

            myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    progressDialog.dismiss();
                    myVideoView.start();
                }
            });


        } catch (Exception e) {
            progressDialog.dismiss();
            System.out.println("Video Play Error :" + e.toString());
            finish();
        }

    }

    protected void finishProgress() {
        this.finishProgress(false);
    }

    // Called instead of finish() to always send back the progress.
    protected void finishProgress(Boolean isEnd) {
        Intent resultIntent = new Intent(Intent.ACTION_PICK);
        int position = myVideoView.getCurrentPosition();
        if (isEnd) {
            position = 0;
        }
        resultIntent.putExtra("VIDEO_POSITION", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // Pass the progress back on the user pressing the back button.
    public void onBackPressed(){
        finishProgress();
    }

    public void onClick(View v) {
        /*switch(v.getId()) {
            case com.brentvatne.react.R.id.close_btn:
                finishProgress();
                break;
        }*/
        if (v.getId() == com.brentvatne.react.R.id.closeBtn) {
            finishProgress();
        }
    }
}