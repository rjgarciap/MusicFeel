package android.musicfeel;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint("NewApi")
/**
 *  Actividad principal de la aplicacion MusicFeel, en la que se definiran
 *  dos botones que nos van a permitir acceder a los dos modos de la aplicacion:
 *  1.-Reproduccion desde archivo.
 *  2.-Reproduccion en vivo.
 *  Y los diferentes elementos que conforman la interfaz inicial.
 * @author Miriam Martin Gonzalez y Ricardo J. Garcia Pinel
 * @version 1.0
 */
public class MainActivity extends Activity {
	
	/**
	 * Objeto de la clase Button, cuya pulsacion nos permite acceder al modo
	 * Reproducir desde archivo.
	 */
	Button boton_archivo;
	
	/**
	 * Objeto de la clase Button, cuya pulsacion nos permite acceder al modo
	 * Reproducir en vivo.
	 */
	Button boton_grabar;
	
	/**
	 * Objeto de la clase TextView, que presenta el titulo de la aplicacion,
	 * y al que le vamos a asignar una fuente personalizada.
	 */
	TextView tituloApp;
	
	/**
	 * Metodo invocado invocado al inicio de la actividad.
	 * 
	 * Inicializacion de variables, definicion de los listener de eventos 
	 * en los botones.
	 *  @param savedInstanceState Bundle que contiene el estado anterior de la 
	 *  actividad en caso de que haya sido suspendida.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		boton_archivo= (Button) findViewById(R.id.boton_archivo);
		boton_grabar= (Button) findViewById(R.id.boton_grabacion);
		tituloApp=(TextView) findViewById(R.id.fuente);
		Typeface fuente= Typeface.createFromAsset(getAssets(), "CashCurrency.ttf");
		tituloApp.setTypeface(fuente);
		animate();
		
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


	/**
	 * Metodo que se ejecuta cuando se pulsa la opcion de Reproducir desde archivo y
	 * que lanza el intento para iniciar la actividad ListaMusica que nos permitira seleccionar
	 * la cancion a reproducir
	 */
	public void pulsaArchivo(){
		Intent i = new Intent(this,ListaMusica.class);
		startActivity(i);
	}
	
	/**
	 * Metodo que se ejecuta cuando se pulsa la opcion de Reproducir en vivo y
	 * que lanza el intento para iniciar la actividad ActivityGrabadora
	 */
	public void pulsaGrabar(){
		Intent i= new Intent(this,ActivityGrabadora.class);
		startActivity(i);
	}
	
	/**
	 * Metodo que se encarga de generar el efecto de Frame Image,
	 * que se ve en la actividad inicial.
	 */
	private void animate(){
		ImageView imgView=(ImageView)findViewById(R.id.icono_pantallaprincipal);
		imgView.setVisibility(ImageView.VISIBLE);
		imgView.setBackgroundResource(R.drawable.frame_animation);
		
		AnimationDrawable frame =(AnimationDrawable) imgView.getBackground();
		
		if(frame.isRunning()){
			frame.stop();
		}else{
			frame.stop();
			frame.start();
		}
	}
	
	
}
