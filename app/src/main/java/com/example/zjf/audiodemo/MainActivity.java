package com.example.zjf.audiodemo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.zjf.audiodemo.play.PlayFactory;
import com.example.zjf.audiodemo.play.PlayInModeStatic;
import com.example.zjf.audiodemo.play.PlayInModeStream;
import com.example.zjf.audiodemo.util.PcmToWavUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private Button mBtnStartRecord = null;
    private Button mBtnCorvert = null;
    private Button mBtnPlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        mBtnStartRecord = (Button)findViewById(R.id.btnStartRecord);
        mBtnCorvert = (Button)findViewById(R.id.btnConvert);
        mBtnPlay = (Button) findViewById(R.id.btnPlay);

        mBtnStartRecord.setOnClickListener(this);
        mBtnCorvert.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
    }

    public Context getContext(){
        return context;
    }
    @Override
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnStartRecord:
                    String strBtnRecord = mBtnStartRecord.getText().toString();
                    if (strBtnRecord.equals(getString(R.string.start_record))){
                        mBtnStartRecord.setText(getString(R.string.stop_record));
                    }else {
                        mBtnStartRecord.setText(getString(R.string.start_record));
                    }
                    break;
                case R.id.btnConvert:
                    PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(Consts.SAMPLE_RATE_INHZ,Consts.CHANNEL_CONFIG,Consts.AUDIO_FORMAT);

                    File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "16k.pcm");
                    File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "16k.wav");
                    if (!wavFile.mkdirs()){
                        Log.e(TAG,"WAVFile未创建");
                    }
                    if (wavFile.exists()){
                        wavFile.delete();
                    }
                    pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(),wavFile.getAbsolutePath());
                    break;
                case R.id.btnPlay:
                    String strBtnPlay = mBtnPlay.getText().toString();
                    PlayFactory playFactory = new PlayFactory(context);
                    //PlayInModeStream playInModeStream = (PlayInModeStream) playFactory.createPlay(PlayFactory.STREAMMODE);
                    PlayInModeStatic playInModeStatic = (PlayInModeStatic) playFactory.createPlay(PlayFactory.STATICMODE);
                    if (strBtnPlay.equals(getString(R.string.start_play))){
                        mBtnPlay.setText(getString(R.string.stop_play));
                        //playInModeStream.startPlayPcm();
                        playInModeStatic.startPlayPcm();
                    }else {
                        mBtnPlay.setText(getString(R.string.start_play));
                       // playInModeStream.stopPlayPcm();
                        playInModeStatic.stopPlayPcm();
                    }
                    break;
                    default:
                        break;
            }
    }
}
