package android.musicfeel;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

 
/**
 * Activity encargada de la generacion de una Splash Screen al inicio de la aplicacion.
 * @author Miriam Martin Gonzalez y Ricardo Jose Garcia Pinel
 *
 */
public class SplashScreenActivity extends Activity {

 
    // Definimos la duracion de la splash screen
    private static final long SPLASH_SCREEN_DELAY = 3000;

    /**
	 * Metodo invocado al inicio de la actividad.
	 * 
	 * Inicializacion de variables, definicion de TimerTask. 
	 * @param savedInstanceState Bundle que contiene el estado anterior de la 
	 * actividad en caso de que haya sido suspendida.
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Definimos orientacion de pantalla PORTRAIT
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Escondemos el titulo de la aplicacion
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        TimerTask task = new TimerTask() {

        	@Override
            public void run() {
                // Empezamos la actividad MainActivity.
                Intent mainIntent = new Intent().setClass(
                        SplashScreenActivity.this, MainActivity.class);
                startActivity(mainIntent);
                // Cierra la actividad para no volver a ella cuando pulsamos 
                // el boton retroceder.
                finish();
            }
        };
 

        // Simulamos el proceso de carga de la aplicacion.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}