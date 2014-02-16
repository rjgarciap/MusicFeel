package com.actividades.musicfeel;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	Button boton_archivo;
	Button boton_grabar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		boton_archivo= (Button) findViewById(R.id.boton_archivo);
		boton_grabar= (Button) findViewById(R.id.boton_grabacion);
		
		boton_archivo.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				pulsaArchivo();
				
			}
		});
		
		boton_grabar.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				pulsaGrabar();
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void pulsaArchivo(){
		
	}
	public void pulsaGrabar(){
		
	}

}
