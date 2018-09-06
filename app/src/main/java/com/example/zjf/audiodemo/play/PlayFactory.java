package com.example.zjf.audiodemo.play;

import android.content.Context;

/**
*@description
*
*@author zjf
*@date 2018/9/6 10:40
*/
public class PlayFactory {
    private Context context;
    public static final int STREAMMODE = 1;
    public static final int STATICMODE = 2;
    public PlayFactory(Context mContext){
        context = mContext;
    }
    public StartPlayable createPlay(int type){
        switch (type){
            case STREAMMODE:
                return new PlayInModeStream(context);
            case STATICMODE:
                return new PlayInModeStatic(context);
                default:
                    return new PlayInModeStream(context);
        }
    }
}
