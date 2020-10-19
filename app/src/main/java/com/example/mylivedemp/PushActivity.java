package com.example.mylivedemp;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.seu.magicfilter.utils.MagicFilterType;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.SocketException;

public class PushActivity extends AppCompatActivity implements SrsEncodeHandler.SrsEncodeListener, RtmpHandler.RtmpListener, SrsRecordHandler.SrsRecordListener, View.OnClickListener {
    private Button mBtnPublish;
    private Button mBtnCameraSW;
    private Button mBtnEncoder;
    private EditText mEtRtmpURL;
    private SrsPublisher mPublisher;
    private String mRtmpURL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_push);

        mBtnPublish = (Button) findViewById(R.id.btn_push_stream);
        mBtnCameraSW = (Button) findViewById(R.id.btn_swCam);
        mBtnEncoder = (Button) findViewById(R.id.btn_swEnc);
        mEtRtmpURL = (EditText) findViewById(R.id.et_url);

        mBtnPublish.setOnClickListener(this);
        mBtnCameraSW.setOnClickListener(this);
        mBtnEncoder.setOnClickListener(this);

        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));
        //编码状态回调
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));

        //RTMP推流状态回调
        mPublisher.setRtmpHandler(new RtmpHandler(this));

        //预览分辨率
        mPublisher.setPreviewResolution(1280, 720);

        //推流分辨率
        mPublisher.setOutputResolution(720, 1280);

        //传输速率
        mPublisher.setVideoHDMode();

        //开启美颜
        mPublisher.switchCameraFilter(MagicFilterType.BEAUTY);

        //开启摄像头，开始预览（未推流）
        mPublisher.startCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //开始、停止推流
            case R.id.btn_push_stream:
                if (mBtnPublish.getText().toString().contentEquals("开始")) {
                    mRtmpURL = mEtRtmpURL.getText().toString();
                    if (TextUtils.isEmpty(mRtmpURL)) {
                        Toast.makeText(getApplicationContext(), "地址不能为空！", Toast.LENGTH_SHORT).show();
                    }
                    mPublisher.startPublish(mRtmpURL);
                    mPublisher.startCamera();

                    if (mBtnEncoder.getText().toString().contentEquals("软编码")) {
                        Toast.makeText(getApplicationContext(), "当前使用硬编码！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "当前使用软编码！", Toast.LENGTH_SHORT).show();
                    }
                    mBtnPublish.setText("停止");
                    mBtnEncoder.setEnabled(false);
                } else if (mBtnPublish.getText().toString().contentEquals("停止")) {
                    mPublisher.stopPublish();
                    mPublisher.stopRecord();
                    mBtnPublish.setText("开始");
                    mBtnEncoder.setEnabled(true);
                }
                break;
            //切换摄像头
            case R.id.btn_swCam:
                mPublisher.switchCameraFace((mPublisher.getCameraId() + 1) % Camera.getNumberOfCameras());
                break;
            //切换编码方式
            case R.id.btn_swEnc:
                if (mBtnEncoder.getText().toString().contentEquals("软编码")) {
                    mPublisher.switchToHardEncoder();
                    mBtnEncoder.setText("硬编码");
                } else if (mBtnEncoder.getText().toString().contentEquals("硬编码")) {
                    mPublisher.switchToSoftEncoder();
                    mBtnEncoder.setText("软编码");
                }
                break;
            default:
                break;
        }
    }

    public void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
            mPublisher.stopRecord();
            mBtnPublish.setText("开始");
        } catch (Exception e1) {
            //
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPublisher.resumeRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
       // mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mPublisher.stopPublish();
       // mPublisher.stopEncode();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.stopRecord();
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (mBtnPublish.getText().toString().contentEquals("停止")) {
            mPublisher.startEncode();
        }
        mPublisher.startCamera();
    }


    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {

    }

    @Override
    public void onRtmpAudioStreaming() {

    }

    @Override
    public void onRtmpStopped() {
        Toast.makeText(getApplicationContext(), "已停止", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "未连接服务器", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {

    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }


    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "网络信号弱", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkResume() {

    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }
}