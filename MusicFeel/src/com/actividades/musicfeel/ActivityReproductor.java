package com.actividades.musicfeel;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;



public class ActivityReproductor extends Activity {

	private MediaPlayer reproductor;
	private ImageButton play;
	private ImageButton pause;
	private TextView nombre;
	
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_reproductor);
		play=(ImageButton) findViewById(R.id.ic_media_play);
		pause=(ImageButton)findViewById(R.id.ic_media_pause);
		nombre=(TextView) findViewById(R.id.nombre_cancion);
		nombre.setSelected(true);
		
		play.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				play();
			}
		});
		
		pause.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				reproductor.pause();
			}
		});
		
		reproductor= new MediaPlayer();
		
		Bundle datos=getIntent().getExtras();
		if(datos!=null){
			try {
				
				nombre.setText(datos.getString("nombre"));
				reproductor.setDataSource(datos.getString("resultado"));
				reproductor.prepare();
				reproductor.start();
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	
	protected void onPause(){
		super.onPause();
		reproductor.pause();
		}
	
	public void play(){
		try {
			reproductor.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reproductor.start();
	}
	

}