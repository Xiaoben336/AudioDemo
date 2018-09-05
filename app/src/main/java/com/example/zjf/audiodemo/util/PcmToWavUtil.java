package com.example.zjf.audiodemo.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
*@description pcm文件转wav文件的工具类
*
*@author zjf
*@date 2018/9/5 13:06
*/
public class PcmToWavUtil {
    /*
    * 缓存的音频大小
    * */
    private int mBufferSize;
    /*
    * 采样率
    * */
    private int mSampleRate;
    /*
    * 声道数
    * */
    private int mChannel;

    /**
     *
     * @param sampleRate 采样率
     * @param channel 声道数
     * @param encoding  音频格式
     */
    public PcmToWavUtil(int sampleRate,int channel, int encoding){
        this.mSampleRate = sampleRate;
        this.mChannel = channel;
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate,mChannel,encoding);
    }

    /**
     * pcm文件转wav文件：pcm文件加wav文件头
     *
     * @param inFileName 源文件路径
     * @param outFileName 目标文件路径
     */
    public void pcmToWav(String inFileName,String outFileName){
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        //此处有改动
        long byteRate = 16 * longSampleRate * channels / 8;
        byte[] data = new byte[mBufferSize];

        try {
            in = new FileInputStream(inFileName);
            out = new FileOutputStream(outFileName);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out,totalAudioLen,totalDataLen,longSampleRate,channels,byteRate);

            while (in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     *
     * @param out   wav音频文件流
     * @param totalAudioLen     不包括header的音频数据总长度
     * @param totalDataLen      wav总区块大小
     * @param longSampleRate    采样率,也就是录制时使用的频率
     * @param channels   audioRecord的频道数量
     * @param byteRate      位元率
     */
    private void writeWaveFileHeader(FileOutputStream out,long totalAudioLen,long totalDataLen,
                                     long longSampleRate,int channels,long byteRate) throws IOException{
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        // bits per sample
        header[34] = 16;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
        Log.d("WavHeader",header.toString());
    }
}
