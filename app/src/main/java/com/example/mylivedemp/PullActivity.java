package com.example.mylivedemp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.VideoView;

public class PullActivity extends AppCompatActivity implements View.OnClickListener {
    private String path = "";
    private VideoView mVideoView;
    private EditText mEtURL;
    private Button mBtnStart;
    private Button mBtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull);
//        if (!LibsChecker.checkVitamioLibs(this)) {
//            return;
//        }
        if(!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;
        mEtURL = (EditText) findViewById(R.id.et_url);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mBtnStart = (Button) findViewById(R.id.start);
        mBtnStop = (Button) findViewById(R.id.stop);

        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                path = mEtURL.getText().toString();
                if (!TextUtils.isEmpty(path)) {
                    mVideoView.setVideoPath(path);
                }
                break;
            case R.id.stop:
                mVideoView.stopPlayback();
                break;
            default:
                break;
        }
    }
}