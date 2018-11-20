package com.gergo.darksight.Audio;

import android.content.Context;
import android.media.MediaPlayer;

import com.gergo.darksight.R;

public class AudioMaker  {

    private Context context;
    private static AudioMaker audioMaker = null;

    private AudioMaker() {
    }

    public void ping() {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.ping);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });

    }
    public void connect() {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.connect);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });

    }
    public void notification() {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.notification);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });

    }
    public static AudioMaker getAudioMaker(){
        if(audioMaker == null){
            audioMaker = new AudioMaker();
        }
        return audioMaker;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
