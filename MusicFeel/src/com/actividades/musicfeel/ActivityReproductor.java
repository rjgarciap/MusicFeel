package com.actividades.musicfeel;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


public class ActivityReproductor extends Activity implements 
OnBufferingUpdateListener, OnCompletionListener, 
MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
private MediaPlayer mediaPlayer;
private SurfaceView surfaceView;
private SurfaceHolder surfaceHolder;
private EditText editText;
private ImageButton bPlay, bPause, bStop, bLog;
private TextView logTextView;
private boolean pause;
private String path;
private int savePos = 0;

public void onCreate(Bundle bundle) {
super.onCreate(bundle);
setContentView(R.layout.activity_reproductor);
surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
surfaceHolder = surfaceView.getHolder();
surfaceHolder.addCallback(this);
surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
editText = (EditText) findViewById(R.id.path);
editText.setText(
       "http://personales.gan.upv.es/~jtomas/video.3gp");


bPlay = (ImageButton) findViewById(R.id.ic_media_play);
bPlay.setOnClickListener(new OnClickListener() {
public void onClick(View view) {
       if (mediaPlayer != null) {
              if (pause) {
                    mediaPlayer.start();
              } else {
                    playVideo();
              }
       }
}
});
bPause = (ImageButton) findViewById(R.id.ic_media_pause);
bPause.setOnClickListener(new OnClickListener() {
public void onClick(View view) {
       if (mediaPlayer != null) {
              pause = true;
              mediaPlayer.pause();
       }
}
});
bStop = (ImageButton) findViewById(R.id.stop);
bStop.setOnClickListener(new OnClickListener() {
public void onClick(View view) {
       if (mediaPlayer != null) {
              pause = false;
              mediaPlayer.stop();
       }
}
});
}
private void playVideo() {
    try {
           pause = false;
           path = editText.getText().toString();
           mediaPlayer = new MediaPlayer();
           mediaPlayer.setDataSource(path);
           mediaPlayer.setDisplay(surfaceHolder);
           mediaPlayer.prepare();
           // mMediaPlayer.prepareAsync(); Para streaming
           mediaPlayer.setOnBufferingUpdateListener(this);
           mediaPlayer.setOnCompletionListener(this);
           mediaPlayer.setOnPreparedListener(this);
           mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
           mediaPlayer.seekTo(savePos);
    } catch (Exception e) {
           log("ERROR: " + e.getMessage());
    }
}
public void onBufferingUpdate(MediaPlayer arg0, int percent) {
    log("onBufferingUpdate percent:" + percent);
}

public void onCompletion(MediaPlayer arg0) {
    log("onCompletion called");
}
public void onPrepared(MediaPlayer mediaplayer) {
    log("onPrepared called");
    int mVideoWidth = mediaPlayer.getVideoWidth();
    int mVideoHeight = mediaPlayer.getVideoHeight();
    if (mVideoWidth != 0 && mVideoHeight != 0) {
           surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
           mediaPlayer.start();
    }
}
public void surfaceCreated(SurfaceHolder holder) {
    log("surfaceCreated called");
    playVideo();
}

public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
    log("surfaceChanged called");
}

public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    log("surfaceDestroyed called");
}

//Este método se invoca cuando la actividad va a ser destruida. Dado que un objeto de la clase MediaPlayer consume muchos recursos, resulta interesante liberarlos lo antes posible.
@Override protected void onDestroy() {
    super.onDestroy();
    if (mediaPlayer != null) {
           mediaPlayer.release();
           mediaPlayer = null;
    }
}

    @Override 
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null & !pause) {
               mediaPlayer.pause();
        }
    }

  @Override 
  public void onResume() {
        super.onResume();
        if (mediaPlayer != null & !pause) {
               mediaPlayer.start();
        }
  }

  @Override
  protected void onSaveInstanceState(Bundle guardarEstado) {
        super.onSaveInstanceState(guardarEstado);
        if (mediaPlayer != null) {
               int pos = mediaPlayer.getCurrentPosition();
               guardarEstado.putString("ruta", path);
               guardarEstado.putInt("posicion", pos);
        }
  }

  @Override
  protected void onRestoreInstanceState(Bundle recEstado) {
        super.onRestoreInstanceState(recEstado);
        if (recEstado != null) {
               path = recEstado.getString("ruta");
               savePos = recEstado.getInt("posicion");
        }
  }
  
  private void log(String s) {
      logTextView.append(s + "\n");
}
}
