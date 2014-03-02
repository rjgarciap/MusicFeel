package com.actividades.musicfeel;

import java.util.Timer;


import java.util.TimerTask;



import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
/**
 * Actividad principal de la aplicación. 
 * Traduce a vibraciones los sonidos grabados a traves del microfono
 * para que una persona sorda o sordo-ciega sea capaz de percibir sonidos
 * y de comunicarse. 
 * 
 * @author Ayla Chabouk Jokhadar y Carlos Gomez Gonzalez
 * @name AudioSense
 * @version 1.0
 * @date 24/04/2012
 */
public class ActivityGrabadora extends Activity {
	private static final float VISUALIZER_HEIGHT_DIP = 50f;
	/**
	 * Boton de Salir
	 * Con su pulsacion salimos de la aplicacion.
	 */
	private Button b;
	/**
	 * Grupo que une 4 radioButtons, para decidir
	 * el tiempo máximo que dura el patron de vibracion.
	 * Solo se puede seleccionar uno de los 4.
	 */
	private RadioGroup rBG;
	/**
	 * RadioButton perteneciente al grupo rBG.
	 * Duracion maxima del patron de vibración 100ms.
	 */
	private RadioButton rB1;
	/**
	 * RadioButton perteneciente al grupo rBG.
	 * Duracion maxima del patron de vibración 200ms.
	 */ 
	private RadioButton rB2;
	/**
	 * RadioButton perteneciente al grupo rBG.
	 * Duracion maxima del patron de vibración 500ms.
	 */
	private RadioButton rB3;
	/**
	 * RadioButton perteneciente al grupo rBG.
	 * Duracion maxima del patron de vibración 1000ms.
	 */
	private RadioButton rB4;
	/**
	 * Variable que decide que boton esta activado, 
	 * Si el boton activado es rB1 vale 1
	 * Si el boton activado es rB1 vale 2
	 * Si el boton activado es rB1 vale 5
	 * Si el boton activado es rB1 vale 10
	 */
	private int boton;
	/**
	 * Check Box 
	 * Para decidir si el sonido a grabar se trata de voz o musica
	 */
	private CheckBox spiner;
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
	 * Toggle buton
	 * Boton de doble pulsacion que determina si ponemos en funcionamiento la aplicacion
	 * o terminamos de hacerlo, segun la posicion que tenga (según la pulsación anterior).
	 * Primera pulsación activación, segunda desactivación y así sucesivamente.
	 */
	private ToggleButton tb;
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
	 * Array auxiliar que nos ayuda a dibujar el ecualizador.
	 */
	private float[] energiasMediasDbDibuja;
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
     * Objeto de la clase Canvas para dibujar en la pantalla el ecualizador.
     */
	private Canvas canvas;
	/**
	 * Interfaz grafica de tipo lineal.
	 */
    private LinearLayout minearLayout;
    /**
     * Objeto de la clase VisualizerView que definira que veremos en la pantalla.
     */
    private VisualizerView mVisualizerView;
    /**
     * Objeto de la clase TextView que define el texto que aparece por pantalla.
     */
    private TextView mStatusTextView;
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
		System.loadLibrary("FFT");
		}
	/**
	 * Metodo que se inicia al arranque de la aplicacion.
	 * Inicializamos los constructores, la interfaz grafica,
	 * ademas de definir los listeners de los botones.
	 * 
	 *  @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStatusTextView = new TextView(this);
		//Creacion de la intefaz de tipo lineal
        minearLayout = new LinearLayout(this);
        minearLayout.setOrientation(LinearLayout.VERTICAL);
        minearLayout.addView(mStatusTextView);
        setContentView(minearLayout);        
        metodo();
	}
	/**
	 * Metodo llamado en el metodo onCreate para la inicializacion de las variables.
	 */
	public void metodo (){
		/**
		 * Constructor 
		 * radio botones y del radio grupo que los unira.
		 */
		rB1 = new RadioButton (this);
		rBG = new RadioGroup (this);
		rB2 = new RadioButton (this);
		rB3 = new RadioButton (this);
		rB4 = new RadioButton (this);
		rBG = new RadioGroup (this);
		/**
		 * Constructor
		 * Boton de Salir.
		 */
		b = new Button (this);
		/**
		 * Constructor
		 * Check Box para la seleccion si es voz o música.
		 */
		spiner = new CheckBox (this);
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
		 * Constructor
		 * Boton de doble pulsacion.
		 */
		tb = new ToggleButton (this);
		/**
		 * Inicializacion de arrays.
		 */
		energias = new float [128];
		
		energiasMedias = new float [28];
		
		energiasMediasDb = new float [28];
		
		energiasMediasDbDibuja = new float [28];
		
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
		boton=1;
		w=1;
		/**
		 * Constructor
		 * Interfaz grafica de definicion de sus parametros principales
		 * para los componentes que la van a componer.
		 */
		mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int)(VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mVisualizerView.setLayoutParams(layoutParams);
        /**
         * Configuracion de los textos, anchos y largos de los botones.
         */
        rB1.setText("10Hz");
        rB2.setText("5Hz");
        rB3.setText("2Hz");
        rB4.setText("1Hz");
        b.setText("Salir");
        b.setWidth(200);
        b.setHeight(10);
        tb.setTextOff("Comenzar");
        tb.setLayoutParams(layoutParams);
        tb.setTextOn("Terminar");
        spiner.setHeight(20);
        spiner.setText("Voz");
        /**
         * Definimos la orientacion de los componentes de la interfaz.
         */
        rBG.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row = new LinearLayout
        		(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row1 = new LinearLayout
        		(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row2 = new LinearLayout
        		(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row3 = new LinearLayout
        		(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        /**
         * Simulamos dos pulsaciones en el boton de doble pulsacion
         * para que aparezca al encender la aplicacion la palabra comenzar
         * en el boton.
         */
        tb.performClick();
        tb.performClick();
        /**
         * Listener del boton Empezar y Terminar
         * Define que hacer cuando pulsamos el boton de doble pulsacion 
         * En la primera pulacion:
         * Vibrara para señalar que ha comenzado
         * Comprobara el check box, para ver si es voz o musica
         * Comprobara los radio botones
         * Comenzara a grabar
         * Iniciara el temporizador
         * Iniciara el segundo hilo para el calculo de las energias
         * En la segunda pulsacion:
         * Cancelamos las vibraciones
         * Cancelamos el temporizador
         * Vibra para señalar que ha terminado
         */
		tb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (tb.isChecked()) {
					//Vibra una vez para indicar que comenzamos el proceso
					Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					vi.vibrate(500);
					vibra = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					//Comprobacion si es para voz o no el procesamiento de la informacion
			        if (spiner.isChecked()) {
			            voz = 1;
			        }
			        else {
			        	voz = 0;
			        }
			        //Comprobacion de los radio botones
			        if (rB1.isChecked()){
			        	boton=1;
			        }
			        else if (rB2.isChecked()){
			        	boton=2;
			        }
			        else if (rB3.isChecked()){
			        	boton=5;
			        }
			        else if (rB4.isChecked()){
			        	boton=10;
			        }
			        else {
			        	boton=1;
			        }
			        
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
				                         mVisualizerView.updateVisualizer();
				                        }
				               });
				        }};
				    //Definimos el periodo del Timer, 100ms    
				    t.schedule(scanTask, 0, 100); 
				    //Creacion del segundo hilo para procesar la informacion
					new MiTarea2().execute();
			    } else {
					grabando=0;
					//Actualizamos el ecualizador para que se borre de la pantalla
					mVisualizerView.updateVisualizer();
					//Paramos el temporizador
					t.cancel();
					//Cancelamos las vibraciones
					vibra.cancel();
					//Vibra dos veces para indicar que ha terminado el proceso
					long [] patron = {0,500,200,500};
					vibra.vibrate(patron,-1);
			    }
			}
		});
		/**
		 * Listener del boton Salir
		 * Termina con la aplicacion y con la vibracion
		 */
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				grabando=0;
				onDestroy(); 
			}
		});
		//Añade a la pantalla los elementos de la interfaz a la pantalla
		//en la posicion adecuada
		row.addView(b);
		row.addView(spiner);
		row1.addView(tb);
		rBG.addView(rB1);
		rBG.addView(rB2);
		rBG.addView(rB3);
		rBG.addView(rB4);       
		minearLayout.addView(row);
        minearLayout.addView(row2);
        minearLayout.addView(row1);
        minearLayout.addView(rBG);
        minearLayout.addView(row3);
        minearLayout.addView(mVisualizerView);
	}
	/**
	 * Metodo que define que hacer cuando la aplicacion
	 * se pone en modo Pausa. En nuestro caso cierra la
	 * aplicacion.
	 */
	public void onPause(){
		super.onPause();
	}
	/**
	 * Metodo que define que hacer cuando la aplicacion
	 * se pone en modo Parada. En nuestro caso tambien cierra la
	 * aplicacion
	 */
	public void onStop(){
		if (grabando == 1) {
			tb.performClick();
			grabando=0;
		}
		super.onStop();
	}
	/**
	 * Metodo que define que hacer cuando la aplicacion
	 * se termina (pulsación boton Salir). Termina con la actividad y 
	 * cancela todas las vibraciones.
	 */
	public void onDestroy() {
		vibra.cancel();
		finish();
		super.onDestroy();
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
	/**
	 * Clase
	 * Determina lo que vemos en la interfaz grafica ademas del ecualizador grafico
	 */
	class VisualizerView extends View {
		/**
		 * Objeto de la clase Paint que define el color del ecualizador
		 */
	    private Paint mForePaint = new Paint();
	    /**
	     * Objeto que define el color negro
	     */
	    private Paint negro = new Paint();
	    /**
	     * Array donde definimos los colores del ecualizador grafico
	     */
		public int colores [][] = new int [22][3];
		/**
		 * Definicion de constantes
		 */
	    private float altura =0;
	    private float alturaTotal=490;
	    private float ancho = -20;
	    private int o =0;
	    public VisualizerView(Context context) {
	        super(context);
	        init();
	    }
	    /**
	     * Metodo
	     * Rellena el array con constantes de los colores que conformaran el ecualizador grafico
	     */
	    private void rellena (){
	    	colores[0][1]=255;
	    	colores[1][1]=245;
	    	colores[2][1]=235;
	    	colores[3][1]=225;
	    	colores[4][1]=215;
	    	colores[5][1]=205;
	    	colores[6][1]=195;
	    	colores[7][1]=185;
	    	colores[8][1]=175;
	    	colores[9][1]=165;
	    	colores[10][0]=185;
	    	colores[11][0]=195;
	    	colores[12][0]=205;
	    	colores[13][0]=215;
	    	colores[14][0]=225;
	    	colores[15][0]=235;
	    	colores[16][0]=255;
	    	colores[17][0]=170;
	    	colores[18][0]=190;
	    	colores[19][0]=210;
	    	colores[20][0]=235;
	    	colores[21][0]=255;
	    	for (int g = 0;g<10;g++){
	    		colores[g][0]=0;
	    		colores[g][2]=0;
	    	}
	    	for (int g = 10;g<17;g++){
	    		colores[g][1]=colores[g][0];
	    		colores[g][2]=0;
	    	}
	    	for (int g = 17;g<22;g++){
	    		colores[g][1]=0;
	    		colores[g][2]=0;
	    	}
	    }
	    /**
	     * Metodo
	     * Incializacion de los colores del ecualizador y del negro
	     */
	    private void init() {
	        mForePaint.setStrokeWidth(1f);
	        mForePaint.setAntiAlias(true);
	        mForePaint.setColor(Color.rgb(0, 128, 255));
	        negro.setStrokeWidth(1f);
	        negro.setAntiAlias(true);
	        negro.setColor(Color.rgb(0, 0, 0));
	    }
	    /**
	     * Metodo
	     * Actualiza el ecualizador grafico
	     */
	    public void updateVisualizer() {
	    	invalidate();
	    }
	    /**
	     * Metodo
	     * Dibuja el ecualizador grafico
	     */
	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        rellena();
	        for (int x =0;x<28;x++){
	        	energiasMediasDbDibuja[x]=energiasMediasDb[x];
	        }
	        ancho=-17;
	        if (grabando==1){
	        	for (int h =0;h<28;h++){
	        		ancho+=17;
	        		while (energiasMediasDbDibuja[h]!=0){
	        			if(energiasMediasDbDibuja[h]<20){
	        				alturaTotal-=20;
	        				altura =alturaTotal-energiasMediasDbDibuja[h];
	        				mForePaint.setColor(Color.rgb(colores[o][0], colores[o][1], colores[o][2]));
	        				canvas.drawRect(ancho,altura,ancho+12, alturaTotal, mForePaint);
	        				o=0;
	        				energiasMediasDbDibuja[h] =0;
	        				alturaTotal=490;
	        			}
	        			else {
	        				alturaTotal -=20;
	        				mForePaint.setColor(Color.rgb(colores[o][0], colores[o][1], colores[o][2]));
	        				canvas.drawRect(ancho,alturaTotal-20,ancho+12, alturaTotal, mForePaint);
	        				alturaTotal -=1;
	        				canvas.drawRect(ancho,alturaTotal-1,ancho+12, alturaTotal, negro);
	        				energiasMediasDbDibuja[h]-=21;
	        				o++;
	        			}
	        			
	        		}
	        	}
	        }
	        if (grabando ==0) {
	        }
	                
	    }
	}
}
	