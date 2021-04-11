package com.hackillionis.talktalk.live.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.data.FeedbackData;
import com.hackillionis.talktalk.dialog.DialogConnection;
import com.hackillionis.talktalk.dialog.DialogProgress;
import com.hackillionis.talktalk.live.stats.LocalStatsData;
import com.hackillionis.talktalk.live.stats.RemoteStatsData;
import com.hackillionis.talktalk.live.stats.StatsData;
import com.hackillionis.talktalk.live.ui.VideoGridContainer;
import com.huawei.multimedia.audiokit.utils.Constant;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.BeautyOptions;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.rtc.video.BeautyOptions.LIGHTENING_CONTRAST_NORMAL;

public class LiveActivity extends RtcBaseActivity implements RatingDialogListener, DialogConnection.OnDismissConnection {
    private static final String TAG = LiveActivity.class.getSimpleName();

    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn;
    private ImageView mMuteVideoBtn;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    private SharedPreferences sharedPreferences;

    int allot_time;
    CountDownTimer countDownTimer;

    private long UpdateTime, TimeBuff;
    private int Seconds, Minutes, MillisecondTime;
    TextView txtTimer;
    boolean isFirst = true;
    //String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
        sharedPreferences = getSharedPreferences(com.hackillionis.talktalk.util.Constants.MY_PREF, Context.MODE_PRIVATE);
        txtTimer = findViewById(R.id.txtLiveTimer);

        allot_time = Integer.parseInt(getIntent().getStringExtra("allot_time")) * 60000;

        config().setChannelName(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.UID, "guest"));
        initUI();
        initData();
    }

    public void startTimer(long total) {
        Seconds = Minutes = MillisecondTime = 0;
        UpdateTime = TimeBuff = 0L;
        countDownTimer = new CountDownTimer(total, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                MillisecondTime = (int) millisUntilFinished;
                UpdateTime = TimeBuff + MillisecondTime;
                Seconds = (int) ((UpdateTime / 1000));
                Minutes = Seconds / 60;
                Seconds %= 60;
                if (Seconds < 10) {
                    txtTimer.setText(Minutes + ":0" + Seconds);
                } else {
                    txtTimer.setText(Minutes + ":" + Seconds);
                }
            }

            @Override
            public void onFinish() {
                showDialog();
            }
        };
        countDownTimer.start();
    }

    private void initUI() {

        int role = Constants.CLIENT_ROLE_BROADCASTER;
        Log.d("hello", "role" + role);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(true);

        /*mMuteVideoBtn = findViewById(R.id.live_btn_mute_video);
        mMuteVideoBtn.setActivated(true);*/


        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());


        rtcEngine().setClientRole(role);
        startBroadcast();
    }


    private void initData() {
        mVideoDimension = com.hackillionis.talktalk.live.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.live_room_top_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.height = mStatusBarHeight + topLayout.getMeasuredHeight();
        topLayout.setLayoutParams(params);
        topLayout.setPadding(0, mStatusBarHeight, 0, 0);
    }

    private void startBroadcast() {
        rtcEngine().setClientRole(1);
        SurfaceView surface = prepareRtcVideo(0, true);
        mVideoGridContainer.addUserVideoSurface(0, surface, true);
        mMuteAudioBtn.setActivated(true);
        if (rtcEngine().getConnectionState() == 1) {
            finish();
        }
        if(isFirst) {
            isFirst = false;
            startTimer(allot_time);
        }
    }


    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
        mMuteAudioBtn.setActivated(false);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });

    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;
        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.finish();
    }

    public void onLeaveClicked(View view) {
        showDialog();
    }

    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }

    public void onBeautyClicked(View view) {
        view.setActivated(!view.isActivated());
        rtcEngine().setBeautyEffectOptions(view.isActivated(),
                new BeautyOptions(LIGHTENING_CONTRAST_NORMAL, 0.5F, 0.5F, 0.5F));
    }

    public void onMoreClicked(View view) {
        // Do nothing at the moment
    }

    public void onPushStreamClicked(View view) {
        // Do nothing at the moment
    }

    public void onMuteAudioClicked(View view) {
        rtcEngine().muteLocalAudioStream(view.isActivated());
        view.setActivated(!view.isActivated());
    }

    public void onMuteVideoClicked(View view) {
        Log.d("hello","View status : "+view.isActivated());
        if (view.isActivated()) {
            stopBroadcast();
        } else {
            startBroadcast();
        }
        view.setActivated(!view.isActivated());
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent!!!"))
                .setDefaultRating(3)
                .setTitle("Rate Listener/Speaker")
                .setDescription("Your feedback is important to us in order to understand your Listener/Speaker and maintain the usefulness of platform.")
                .setCommentInputEnabled(true)
                .setStarColor(R.color.neonGreen)
                .setNoteDescriptionTextColor(R.color.purpleLight)
                .setTitleTextColor(R.color.purpleLight)
                .setDescriptionTextColor(R.color.purpleLight)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.gray)
                .setCommentTextColor(R.color.purpleLight)
                .setCommentBackgroundColor(R.color.gray_lightest)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(LiveActivity.this)
                .show();
    }

    @Override
    public void onNegativeButtonClicked() {
        DialogConnection dialogConnection = null;
        if(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.UID,"guest")
                .equals(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO,"guest"))){
            dialogConnection = new DialogConnection(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECTED_TO,"guest"), LiveActivity.this);
        }else{
            dialogConnection = new DialogConnection(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO,"guest"), LiveActivity.this);
        }
        dialogConnection.setCancelable(false);
        dialogConnection.show(getSupportFragmentManager(),"Dialog Connection");
    }

    @Override
    public void onNeutralButtonClicked() {
        DialogConnection dialogConnection = null;
        if(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.UID,"guest")
                .equals(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO,"guest"))){
            dialogConnection = new DialogConnection(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECTED_TO,"guest"), LiveActivity.this);
        }else{
            dialogConnection = new DialogConnection(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO,"guest"), LiveActivity.this);
        }
        dialogConnection.setCancelable(false);
        dialogConnection.show(getSupportFragmentManager(),"Dialog Connection");
    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s) {
        DialogProgress dialogProgress = new DialogProgress("Thank you for your feedback!\nPlease wait...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getSupportFragmentManager(), "Dialog Progress");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Feedback/"+sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.UID,"guest"));
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String time = simpleDateFormat.format(calendar.getTime());

        FeedbackData feedbackData = new FeedbackData(String.valueOf(i),s,time,sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO, "guest"), sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECTED_TO,"guest"));
        databaseReference.child(time).setValue(feedbackData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialogProgress.dismiss();
                DialogConnection dialogConnection = null;
                if(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.UID,"guest")
                        .equals(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO,"guest"))){
                    dialogConnection = new DialogConnection(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECTED_TO,"guest"), LiveActivity.this);
                }else{
                    dialogConnection = new DialogConnection(sharedPreferences.getString(com.hackillionis.talktalk.util.Constants.CONNECT_TO,"guest"), LiveActivity.this);
                }
                dialogConnection.setCancelable(false);
                dialogConnection.show(getSupportFragmentManager(),"Dialog Connection");
            }
        });
    }

    @Override
    public void onDismissConnection() {
        finish();
    }
}
