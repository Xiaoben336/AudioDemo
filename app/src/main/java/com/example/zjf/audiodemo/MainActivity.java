package com.example.zjf.audiodemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;

import com.example.zjf.audiodemo.play.PlayFactory;
import com.example.zjf.audiodemo.play.PlayInModeStatic;
import com.example.zjf.audiodemo.play.PlayInModeStream;
import com.example.zjf.audiodemo.record.Recordable;
import com.example.zjf.audiodemo.util.PcmToWavUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,Recordable{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST = 1001;

    private Context context;
    private AudioRecord audioRecord;
    private Button mBtnStartRecord = null;
    private Button mBtnCorvert = null;
    private Button mBtnPlay = null;

    /*
    * 需要申请的权限
    * */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * 被用户拒绝的权限
     */
    private List<String> refusePermissionsList = new ArrayList<>();

    private boolean isRecording;
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

       /* checkPermissions();*/
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
                        startRecord();
                    }else {
                        mBtnStartRecord.setText(getString(R.string.start_record));
                        stopRecord();
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
                    PlayInModeStream playInModeStream = (PlayInModeStream) playFactory.createPlay(PlayFactory.STREAMMODE);
                    //PlayInModeStatic playInModeStatic = (PlayInModeStatic) playFactory.createPlay(PlayFactory.STATICMODE);
                    final File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "16k.pcm");
                    if (strBtnPlay.equals(getString(R.string.start_play))){
                        mBtnPlay.setText(getString(R.string.stop_play));
                        playInModeStream.startPlayPcm(file);
                        //playInModeStatic.startPlayPcm();
                    }else {
                        mBtnPlay.setText(getString(R.string.start_play));
                        playInModeStream.stopPlayPcm();
                        //playInModeStatic.stopPlayPcm();
                    }
                    break;
                    default:
                        break;
            }
    }

    @Override
    public void startRecord() {
        final int minBufferSize = AudioRecord.getMinBufferSize(Consts.SAMPLE_RATE_INHZ,Consts.CHANNEL_CONFIG,Consts.AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,Consts.SAMPLE_RATE_INHZ,Consts.CHANNEL_CONFIG,
                Consts.AUDIO_FORMAT,minBufferSize);

        final byte[] data = new byte[minBufferSize];

        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"test.pcm");

        if (!file.mkdirs()){
            Log.d(TAG,"test.pcm创建失败");
        }
        if (file.exists()){
            file.delete();
        }

        audioRecord.startRecording();
        isRecording = true;

        new Thread(new Runnable() {
            @Override public void run() {

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != os) {
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, minBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i(TAG, "run: close file output stream !");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void stopRecord() {
        isRecording = false;
        //pcm文件播放
        new Thread(new Runnable() {
            @Override
            public void run() {
                final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"test.pcm");
                PlayFactory playFactory = new PlayFactory(context);
                PlayInModeStream playInModeStream = (PlayInModeStream) playFactory.createPlay(PlayFactory.STREAMMODE);
                playInModeStream.startPlayPcm(file);
            }
        }).start();

        if (audioRecord != null){
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

   /* private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    refusePermissionsList.add(permissions[i]);
                }
            }
            if (!refusePermissionsList.isEmpty()) {
                String[] permissions = refusePermissionsList.toArray(new String[refusePermissionsList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }*/
}
