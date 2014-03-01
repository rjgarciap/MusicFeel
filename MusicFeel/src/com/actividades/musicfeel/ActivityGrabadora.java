package com.actividades.musicfeel;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import java.util.Timer;
import java.util.TimerTask;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ActivityGrabadora extends Activity{
	private int boton;
	private Button bGrabar;
	
	/**
	 * Variable que define en que buffer se graba el sonido captado por
	 * el micrófono, ademas de en que formato, codificacion... se hace.
	 */
	private AudioRecord audio;
	/**
	 * Array Bidimensional
	  Guardaremos las muestras que nos da el buffer de grabacion
	 * para luego tratarlas.
	 */
	private short[][] info;
	/**
	 * Variable que define si estamos grabando a traves del micrófono (=1)
	 * o no (=0).	
	 */
	private int grabando=0;
	/**
        * Contador para cambiar el array donde guardamos las muestras
        */
	private int n;
	/**
        * Tamaño minimo del buffer de grabacion
        */
	private int N;

	/**
	 * Declaracion del metodo que importamos de un codigo C
	 * @param r, muestras grabadas por el microfono.
	 */
	public native void realfft (float r[]);
	/**
	 * Inicializacion de variables que se usaran como contadores.
	 */
	private int i;
	private float a;
	private float c;
	private int h;
	private int r;
	private int k;
	private int l;
	private int j;
	private int q;
	private int w;
	/**
	 * Voz, indica si lo que se graba es voz (=1) o si
	 * es musica (=0)
	 */
	private int voz;
	/**
	 * Umbral minimo en unidades logaritmicas que decidimos para
	 * que el dispositivo vibre, si no supera este umbral no vibrara.
	 */
	private int umbralMinimo=300;
	/**
	 * Umbral maximo en unidades logaritmicas que decidimos para 
	 * que el dispositivo vibre, si llega hasta este valor vibrara
	 * durante el maximo tiempo.
	 */
	private int umbralMaximo=360;
	/**
	 * Frecuencia en Hz que determina la duracion del patron de vibracion
	 * calculado.
	 */
	private double indice;
	/**
	 * Tiempo en ms del indice calculado en Hz para el patron de vibracion.
	 */
	private double tiempo;
	/**
	 * Ciclo de Trabajo con el que vibrara en un determinado momento
	 * el patron de vibración.
	 */
	private double cicloDeTrabajo;
	/**
	 * Array donde almacenamos la energia de cada muestra.
	 */
	private float[] energias;
	/**
	 * Array donde almacenamos la energia media de un grupo de muestras.
	 */
	private float[] energiasMedias;
	/**
	 * Array que almacena las energias medias de un grupo de muestras
	 * pero en unidades logaritmicas.
	 */
	private float[] energiasMediasDb;

	/**
	 * Variable que guarda la suma de la energia de las 128 muestras
	 * multiplicadas cada una por la posicion que ocupa en el array (media ponderada).
	 */
	private float calculo;
	/**
	 * Si estamos grabando voz tendra un valor de 2000, y si grabamos
	 * musica tendra un valor de 4000 (depende del spiner).
	 */
	private int referencia;
	/**
	 * Variable que guarda la suma de la energia de las 64 primeras muestras
	 * multiplicadas cada una por la posicion que ocupa en el array (media ponderada).
	 */
	private float calculoVoz;
	/**
	 * Media ponderada nos mostrara cual es la muestra a la que se encuentra la
	 * la maxima energia.
	 */
	private float mediaPonderada;
	/**
	 * Suma de la energias de todas las muestras en unidades lineales.
	 */
	private float energiaTotal;
	/**
	 * Suma de las energias de todas las muestras en unidades logaritmicas.
	 */
	private float energiaTotalDb;
	/**
	 * Suma de las energias de las primeras 64 muestras en unidades lineales.
	 */
	private float energiaTotalVoz;
	/**
	 * Suma de las energias de las primeras 64 muestras en unidades logaritmicas.
	 */
	private float energiaTotalVozDb;
	/**
	 * Array que guardara el tiempo en ms que conformara el patron de vibracion.
	 */
	private long [] patrones;
	/**
	 * Objeto de la clase Vibrator que se encargara de las vibraciones.
	 */
    private Vibrator vibra;

    /**
     * Frecuencia en Hz en la que esta la maxima energia de la señal.
     */
    private float frecuenciaMaxEnergia;
    /**
     * Accion que define que hacer cuando interrumpe el temporizador.
     */
    private TimerTask scanTask;
    /**
     * Objeto de tipo Handler que permite definir metodos run()
     * que se ejecuten en el mismo hilo de la aplicacion
     */
    final Handler handler = new Handler();
    /**
     * Temporizador: para el refresco de la pantalla y el 
     * calculo de los nuevos patrones de vibracion.
     */
    private Timer t;
    /**
     *  Importamos la libreria formada por el codigo C
     */
    static {
    	 Log.i("load so > ","load so");
		System.loadLibrary("FFT");
		
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabadora);      
        
        bGrabar=(Button) findViewById(R.id.icono_grabar);
        
        /**
		 * Tamaño minimo del buffer de grabación.
		 */
		N = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		/**
		 * Constructor
		 * Buffer de grabacion de un tamaño 10 veces mayor al minimo.
		 * Frecuencia de muestreo de 8000 Hz.
		 * Fuente de tipo MIC.
		 * Grabacion en mono.
		 * Codificacion de 16 bits PCM.
		 */ 
		audio = new AudioRecord (AudioSource.MIC,8000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,N*10);
		/**
		 * Constructor
		 * Array para tratar las muestras grabadas.
		 */
		info = new short [256][257];
		
		/**
		 * Inicializacion de arrays.
		 */
		energias = new float [128];
		
		energiasMedias = new float [28];
		
		energiasMediasDb = new float [28];
		
		
		patrones = new long [3];
		/**
		 * Constructor
		 * objeto que permite configurar la vibracion del dispositivo.
		 */
		vibra = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		/**
		 * Inicializacion de variables a los valores por defecto si no decidimos nada,
		 */
		grabando=0;
		voz = 0;
		w=1;
		boton=1;
        
        bGrabar.setOnClickListener(new View.OnClickListener() {
		
        	
			@Override
			public void onClick(View v) {
				//Vibra una vez para indicar que comenzamos el proceso
				Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				vi.vibrate(500);
				vibra = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				
				n=1;
				grabando = 1;  
				//Comienza a grabar la aplicacion
				audio.startRecording();
				vibra = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				//Creacion del temporizador
				t = new Timer();
				scanTask = new TimerTask() {
			        public void run() {
			                handler.post(new Runnable() {
			                        public void run() {
			                        if(w>=boton){
			                        	vibra.cancel();
			                        	vibracion();
			                        	w=1;
			                        	if (grabando==1){
			                   			 vibra.vibrate(patrones,0); 
			                               }
			                               else{
			                              	 vibra.cancel();
			                               }
			                        }
			                        else{
			                        	w++;
			                        }

			                        }
			               });
			        }};
			    //Definimos el periodo del Timer, 100ms    
			    t.schedule(scanTask, 0, 100); 
			    //Creacion del segundo hilo para procesar la informacion
				new MiTarea2().execute();
				
			}
		});
        

	}
	
	/**
	 * Metodo
	 * Calcula los patrones de vibracion a partir 
	 * de la frecuencia a la que esta la maxima energia y a partir
	 * de la energia total diferenciando entre musica (0-4000Hz) y 
	 * voz (0-2000Hz)
	 */
	public void vibracion () {
		//Calculamos el tiempo que debe durar el patron de vibracion
		if (voz==1){
			referencia = 2000;
		}
		else {
			referencia=4000;
		}
		if (boton==1){
			indice = (frecuenciaMaxEnergia/(referencia/90))+10;
		}
		if (boton==2){
			indice = (frecuenciaMaxEnergia/(referencia/95))+5;
		}
		if (boton==5){
			indice = (frecuenciaMaxEnergia/(referencia/98))+2;
		}
		if (boton==10){
			indice = (frecuenciaMaxEnergia/(referencia/99))+1;
		}
		tiempo = 1000/indice;
		//Calculamos el ciclo de trabajo de la vibracion
		if (voz == 1){
			if (energiaTotalVozDb<umbralMinimo){
				cicloDeTrabajo=0;
			}
			else{
				cicloDeTrabajo=(energiaTotalVozDb-umbralMinimo)/(umbralMaximo-umbralMinimo);
			}
		}
		else {
			if (energiaTotalDb<umbralMinimo){
				cicloDeTrabajo=0;
			}
			else{
				cicloDeTrabajo=(energiaTotalDb-umbralMinimo)/(umbralMaximo-umbralMinimo);
			}
		}
		//Creamos un array con el patron de vibracion
		patrones[0]=0;
		patrones[1]=(long)(cicloDeTrabajo*tiempo);
		patrones[2]=(long)(tiempo-(cicloDeTrabajo*tiempo));
	}
	/**
	 * Clase 
	 * Define un hilo paralelo que se ejecutara a la vez que la actividad
	 * principal para llevar a cabo el tratamiento de la señal.
	 */
	private class MiTarea2 extends AsyncTask<Void, Void, Void> {
		/**
		 * Metodo
		 * Se lleva a cabo todo el procesamiento de la señal y el 
		 * calculo de las energias
		 */
		protected Void doInBackground(Void...voids ) {
			while (grabando == 1) {
				//Leemos del buffer de grabacion las muestras 
				float[] muestras = new float [257];
				short [] buffer= info[n++ % info.length];
				audio.read(buffer,0 ,buffer.length);
				for (q = 0; q<257;q++){
					muestras[q]= 0;
					muestras[q] = (float) buffer[q];
				}
				

				i=0;
				//Realizamos la FFT
				realfft(muestras);
				//Inicializamos las variables
				i = 0;
				a=0;
				c = 0;
				calculo = 0;
				calculoVoz=0;
				mediaPonderada=0;
				energiaTotal=0;
				energiaTotalVoz=0;
				for (i=0; i<128;i++){
					a= muestras [i];
					c= muestras [256-i];
					//Calculo de la energia de cada muestra
					energias[i]= (float) Math.sqrt(Math.pow(a, 2)+Math.pow(c, 2));
					calculo += (i+1)*energias[i];
					//Calculo de la energia total
					//Si es voz, solo de las 64 primeras muestras
					if (voz ==1 && i<64){
						energiaTotalVoz+=energias[i];
						calculoVoz +=(i+1)*energias[i];
					}
					energiaTotal += energias[i];
				}
				//Inicializacion de variables 
				r=0;
				for (r = 0;r<28;r++){
					energiasMedias[r]=0;
				}
				k= 0;
				j = 0;
				l=0;
				float continua = energias[0];
				//Calculo de las energias medias de las muestras a representar en el ecualizador gráfico 
				//en caso de que no sea voz la señal tratada a partir de las frecuencias
                          //de las notas musicales de la 4 a la 7 octava
				if (voz == 0){
					energiasMedias[0]= (energias[8]+energias[9])/2;
					energiasMedias[1]=(energias[9]+energias[10])/2;
					energiasMedias[2]=(energias[10]+energias[11])/2;
					energiasMedias[3]=(energias[11]+energias[12])/2;
					energiasMedias[4]=(energias[12]+energias[13])/2;
					energiasMedias[5]=energias[14];
					energiasMedias[6]=(energias[15]+energias[16])/2;
					energiasMedias[7]=(energias[16]+energias[17])/2;
					energiasMedias[8]=(energias[18]+energias[20])/2;
					energiasMedias[9]=(energias[21]+energias[22])/2;
					energiasMedias[10]=(energias[22]+energias[23])/2;
					energiasMedias[11]=(energias[25]+energias[26])/2;
					energiasMedias[12]=(energias[28]+energias[29])/2;
					energiasMedias[13]=(energias[31]+energias[32])/2;
					energiasMedias[14]=(energias[33]+energias[35])/2;
					energiasMedias[15]=(energias[37]+energias[39])/2;
					energiasMedias[16]=(energias[42]+energias[43])/2;
					energiasMedias[17]=(energias[44]+energias[47])/2;
					energiasMedias[18]=(energias[50]+energias[53])/2;
					energiasMedias[19]=(energias[56]+energias[59])/2;
					energiasMedias[20]=(energias[63]+energias[64])/2;
					energiasMedias[21]=(energias[67]+energias[71])/2;
					energiasMedias[22]=(energias[75]+energias[79])/2;
					energiasMedias[23]=(energias[84]+energias[85])/2;
					energiasMedias[24]=(energias[89]+energias[94])/2;
					energiasMedias[25]=(energias[100]+energias[106])/2;
					energiasMedias[26]=(energias[112]+energias[119])/2;
					energiasMedias[27]=(energias[126]+energias[127])/2;
					//Pasamos a unidades logaritmicas las energias y eliminamos ruido
					for (int u=0;u<28;u++){
						energiasMediasDb[u] = 20*(float)Math.log(energiasMedias[u]);
						if (energiasMediasDb[u] <= 120) {
							energiasMediasDb[u]=0;
						}
						else 
							energiasMediasDb[u] = (5/2)*(energiasMediasDb[u]-120);
					}
					
				}
				//Calculo de las energias medias de las muestras a representar en el ecualizador gráfico
				//en caso de que sea voz la señal tratada a partir de las frecuencias de la escala Bark			
				else {
					energiasMedias[0]= continua;
					energiasMedias[1]=(energias[3] + energias [4])/2;
					energiasMedias[2]=(energias[4] + energias[5])/2;
					energiasMedias[3]=(energias[6] + energias[7])/2;
					energiasMedias[4]=(energias[9] + energias[10])/2;
					energiasMedias[5]=(energias[12]+ energias[13])/2;
					energiasMedias[6]=(energias[14] + energias[15])/2;
					energiasMedias[7]=(energias[16] + energias[17])/2;
					energiasMedias[8]=(energias[18] + energias[19])/2;
					energiasMedias[9]=(energias[20] + energias[21])/2;
					energiasMedias[10]=(energias[24] + energias[25])/2;
					energiasMedias[11]=(energias[26] + energias[27])/2;
					energiasMedias[12]=(energias[29] + energias[30])/2;
					energiasMedias[13]=(energias[32] + energias[33])/2;
					energiasMedias[14]=(energias[34] + energias[35])/2;
					energiasMedias[15]=(energias[37] + energias[38])/2;
					energiasMedias[16]=(energias[40] + energias[41])/2;
					energiasMedias[17]=(energias[42] + energias[43])/2;
					energiasMedias[18]=(energias[44] + energias[45])/2;
					energiasMedias[19]=(energias[47] + energias[48])/2;
					energiasMedias[20]=(energias[51] + energias[52])/2;
					energiasMedias[21]=(energias[55] + energias[56])/2;
					energiasMedias[22]=(energias[64]);
					energiasMedias[23]=(energias[74] + energias[75])/2;
					energiasMedias[24]=(energias[80] + energias[81])/2;
					energiasMedias[25]=(energias[86] + energias[87])/2;
					energiasMedias[26]=(energias[100] + energias[101])/2;
					energiasMedias[27]=(energias[118] + energias[119])/2;
					//Pasamos a unidades logaritmicas las energias y eliminamos ruido
					for (int u=0;u<28;u++){
						energiasMediasDb[u] = 20*(float)Math.log(energiasMedias[u]);
						if (energiasMediasDb[u] <= 120) {
							energiasMediasDb[u]=0;
						}
						else 
							energiasMediasDb[u] = (5/2)*(energiasMediasDb[u]-120);
					}
					
				}
				//Calculo de la frecuencia a la que se produce la maxima energia
				energiaTotalDb=20*(float)Math.log(energiaTotal);
				energiaTotalVozDb=20*(float)Math.log(energiaTotalVoz);
				if (voz==1){
					mediaPonderada = calculoVoz/energiaTotalVoz;
					frecuenciaMaxEnergia = (mediaPonderada*2000)/64;
				}
				else {
					mediaPonderada = calculo/energiaTotal;
					frecuenciaMaxEnergia = (mediaPonderada*4000)/128;
				}

				//Liberamos memoria
				muestras = null;
				System.gc();
			}
			return null;
		}
		//Metodo que se ejecutara antes del proceso de tratamiento de la señal.
		protected void onPreExecute () {
			Toast toast1 =
					Toast.makeText(getApplicationContext(),
					"grabando",Toast.LENGTH_SHORT);
			toast1.show();
		}
		protected void onProgressUpdate () {
		}
		//Metodo que se ejecutara despues del proceso de tratamiento de la señal.
		@Override
		protected void onPostExecute(Void result) {
			Toast toast2 =
					Toast.makeText(getApplicationContext(),
					"terminando",Toast.LENGTH_SHORT);
			toast2.show();
			audio.stop();
		}	
	}
}


	