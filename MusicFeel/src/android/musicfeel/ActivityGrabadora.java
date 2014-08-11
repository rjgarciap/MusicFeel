package android.musicfeel;

import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


/**
 * Actividad ActivityGrabadora. 
 * Traduce a vibraciones y una visualizacion gr�fica los sonidos grabados a 
 * traves del microfono para que de esta manera una persona sorda pueda disfrutar 
 * de la musica. 
 * 
 * @author Miriam Martin Gonzalez y Ricardo J. Garcia Pinel
 * 
 */
@SuppressLint({ "WrongCall", "NewApi" })
public class ActivityGrabadora extends Activity {


	/**
	 * Variable que define en que buffer se graba el sonido captado por
	 * el micr�fono, ademas de en que formato, codificacion... se hace.
	 */
	private AudioRecord audio;

	/**
	 * Array Bidimensional
	 * Guardaremos las muestras que nos da el buffer de grabacion
	 * para luego tratarlas.
	 */
	private short[][] info;

	/**
	 * Variable que define si estamos grabando a traves del micr�fono (=1)
	 * o no (=0).	
	 */
	private int grabando=0;
	/**
	 * Contador para cambiar el array donde guardamos las muestras
	 */
	private int n;

	/**
	 * Tama�o minimo del buffer de grabacion
	 */
	private int N;

	/**
	 * Inicializacion de variables que se usaran como contadores.
	 */
	private int i;
	private float a;
	private float c;
	private int r;
	private int q;
	private int w;

	/**
	 * Umbral minimo en unidades logaritmicas que decidimos para
	 * que el dispositivo vibre, si no supera este umbral no vibrara.
	 */
	private int umbralMinimo=280;
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
	 * el patron de vibraci�n.
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
	 * Array que almacena las energias medias de un grupo de muestras
	 * pero en unidades logaritmicas.
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
	 * Array que guardara el tiempo en ms que conformara el patron de vibracion.
	 */
	private long [] patrones;
	/**
	 * Objeto de la clase Vibrator que se encargara de las vibraciones.
	 */
	private Vibrator vibra;

	/**
	 * Frecuencia en Hz en la que esta la maxima energia de la se�al.
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
	 * Objeto de la clase ImageButton, su pulsacion inicia la grabacion.
	 */   
	private ImageButton recordButton;
	/**
	 * Objeto de la clase Imagen Button, su pulsacion para la grabacion.
	 */
	private ImageButton recordStop;
	/**
	 * Objeto de la clase VisualizerView, clase que extiende a View, sobre el que ejecutaremos 
	 * onDraw(), el cual pintara la visualizacion sobre la pantalla.
	 */
	private VisualizerView mVisualizerView;
	/**
	 * Objeto de la clase LinearLayout, el cual es representaci�n del Layout que contiene al objeto
	 * VisualizerView, siendo as� �ste el lugar donde se dibujara la visualizacion.
	 */
	private LinearLayout visualizacion;
	/**
	 * Variable de tipo entero que permitira seleccionar entre las diferentes visualizaciones.
	 * 0 => Visualizacion Circulos.
	 */
	private int nVis=0;
	
	/**
	 * RadioGroup y RadioButton correspondientes a la orientacion portrait 
	 */
	private RadioGroup radioGroup;
	private RadioButton radioCirculosGroup;
	private RadioButton radioTrompetasGroup;
	private RadioButton radioEcualizadorGroup;
	
	/**
	 * RadioGroup y RadioButton correspondientes a la orientacion landscape 
	 */
    private ImageButton recordButton_land;
    private ImageButton recordStop_land;
    private RadioGroup radioGroup_land;
    private RadioButton radioCirculosGroup_land;
    private RadioButton radioTrompetasGroup_land;
    private RadioButton radioEcualizadorGroup_land;
    
    /**
     * Objeto de la clase ProcesadorMuestras, que permite realizar el metodo FFT
     */
	private ProcesadorMuestras procesadorMuestras=new ProcesadorMuestras();

	/**
	 * Variable de apoyo que identifica la orientacion de la pantalla
	 */
	protected boolean landscape=false;
	
	/**
	 * Metodo invocado al inicio de la actividad.
	 * 
	 * Inicializacion de variables, definicion de los listener de eventos 
	 * en los botones, definicion de TimerTask. 
	 *  @param savedInstanceState Bundle que contiene el estado anterior de la 
	 *  actividad en caso de que haya sido suspendida.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mVisualizerView = new VisualizerView(this);
		setContentView(R.layout.activity_grabadora);        
		visualizacion=(LinearLayout)findViewById(R.id.surfaceViewG);
		visualizacion.addView(mVisualizerView);
		mVisualizerView.inicializacionColores();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		radioGroup=(RadioGroup)findViewById(R.id.RadioGroup);
		radioGroup_land=(RadioGroup)findViewById(R.id.RadioGroup_vert);
		/**
		 * Tama�o minimo del buffer de grabaci�n.
		 */
		N = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		/**
		 * Constructor
		 * Buffer de grabacion de un tama�o 10 veces mayor al minimo.
		 * Frecuencia de muestreo de 8000 Hz.
		 * Fuente de tipo MIC.
		 * Grabacion en mono.
		 * Codificacion de 16 bits PCM.
		 */ 
		audio = new AudioRecord (AudioSource.MIC,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,N*10);

		/**
		 * Constructor
		 * Array para tratar las muestras grabadas.
		 */
		info = new short [256][257];

		/**
		 * Inicializacion de arrays.
		 */
		energias = new float [128];

		energiasMedias = new float [19];

		energiasMediasDb = new float [19];

		energiasMediasDbDibuja = new float [19];
		patrones = new long [3];

		/*
		 * Constructor
		 * objeto que permite configurar la vibracion del dispositivo.
		 */
		vibra = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		/*
		 * Inicializacion de variables a los valores por defecto si no decidimos nada,
		 */
		grabando=0;
		w=1;

		/**
		 * Inicializa el ImageButton a traves del id del recurso ic_media_record definida
		 * en el layout de la actividad.
		 */
		recordButton=(ImageButton)findViewById(R.id.ic_media_record);
		recordButton_land=(ImageButton)findViewById(R.id.ic_media_record2);

		/**
		 * Inicializa el ImageButton a traves del id del recurso ic_media_stopG definida
		 * en el layout de la actividad. 
		 */
		recordStop=(ImageButton)findViewById(R.id.ic_media_stopG);
		recordStop_land=(ImageButton)findViewById(R.id.ic_media_stopG2);


        radioCirculosGroup = (RadioButton) findViewById(R.id.radioButton1);
        radioTrompetasGroup = (RadioButton) findViewById(R.id.radioButton2);
        radioEcualizadorGroup = (RadioButton) findViewById(R.id.radioButton3);
        radioCirculosGroup_land = (RadioButton) findViewById(R.id.radioButton1_vert);
        radioTrompetasGroup_land = (RadioButton) findViewById(R.id.radioButton2_vert);
        radioEcualizadorGroup_land = (RadioButton) findViewById(R.id.radioButton3_vert);
        radioCirculosGroup.setChecked(true);
        
        
        
        
        radioCirculosGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nVis=0;
				landscape=false;
				alternar();
                mVisualizerView.invalidate();						
			}
		});
        
        radioCirculosGroup_land.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nVis=0;
				landscape=false;
				alternar();
				radioCirculosGroup.setChecked(true);
                mVisualizerView.invalidate();						
			}
		});
        
		
		radioTrompetasGroup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				nVis=1;
				landscape=true;
				alternar();
				radioTrompetasGroup_land.setChecked(true);
                mVisualizerView.invalidate();	
			}
		});

		radioTrompetasGroup_land.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				nVis=1;
				landscape=true;
				alternar();
                mVisualizerView.invalidate();	
			}
		});
		
		radioEcualizadorGroup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				nVis=2;
				landscape=false;
				alternar();
                mVisualizerView.invalidate();
			}
		});

		radioEcualizadorGroup_land.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				nVis=2;
				landscape=false;
				alternar();
				radioEcualizadorGroup.setChecked(true);
                mVisualizerView.invalidate();
			}
		});
				
			 



		/**
		 * Listener del boton recordButton
		 * Define que hacer cuando pulsamos sobre el ImageButton recordButton
		 * Vibrara una vez para senalar que ha comenzado
		 * Comenzara a grabar
		 * Iniciara el temporizador
		 * Iniciara el segundo hilo para el calculo de las energias
		 */
		recordButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				recordButton.setVisibility(View.INVISIBLE);
				recordStop.setVisibility(View.VISIBLE);
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
								if(w>=1){
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

								mVisualizerView.invalidate();
							}
						});
					}};
					//Definimos el periodo del Timer, 100ms    
					t.schedule(scanTask, 0, 100); 
					//Creacion del segundo hilo para procesar la informacion
					new MiTarea2().execute();

			}
		});
		recordButton_land.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				recordButton_land.setVisibility(View.INVISIBLE);
				recordStop_land.setVisibility(View.VISIBLE);

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
								if(w>=1){
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

								mVisualizerView.invalidate();
							}
						});
					}};
					//Definimos el periodo del Timer, 100ms    
					t.schedule(scanTask, 0, 100); 
					//Creacion del segundo hilo para procesar la informacion
					new MiTarea2().execute();

			}
		});

		/**
		 * Listener del boton recordButton
		 * Define que hacer cuando pulsamos sobre el ImageButton recordButton
		 * Cancelamos las vibraciones
		 * Cancelamos el temporizador
		 * Vibra dos veces para se�alar que ha parado de grabar
		 */
		recordStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				recordButton.setVisibility(View.VISIBLE);
				recordStop.setVisibility(View.INVISIBLE);
				grabando=0;
				//Actualizamos el ecualizador para que se borre de la pantalla

				//Paramos el temporizador
				t.cancel();
				//Cancelamos las vibraciones
				vibra.cancel();
				//Vibra dos veces para indicar que ha terminado el proceso
				long [] patron = {0,500,200,500};
				vibra.vibrate(patron,-1);


			}
		});
		
		recordStop_land.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				recordButton_land.setVisibility(View.VISIBLE);
				recordStop_land.setVisibility(View.INVISIBLE);
				grabando=0;
				//Actualizamos el ecualizador para que se borre de la pantalla

				//Paramos el temporizador
				t.cancel();
				//Cancelamos las vibraciones
				vibra.cancel();
				//Vibra dos veces para indicar que ha terminado el proceso
				long [] patron = {0,500,200,500};
				vibra.vibrate(patron,-1);


			}
		});
	}

	/**
	 * Metodo encargado de alternar las vistas en modo landscape o portrait
	 */
	public void alternar(){
		if(landscape){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			radioGroup.setVisibility(View.INVISIBLE);
			radioGroup_land.setVisibility(View.VISIBLE);
			if(grabando==1){
				recordStop_land.setVisibility(View.VISIBLE);
				recordStop.setVisibility(View.INVISIBLE);
			}else{
				recordButton_land.setVisibility(View.VISIBLE);
				recordButton.setVisibility(View.INVISIBLE);
			}
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			radioGroup.setVisibility(View.VISIBLE);
			radioGroup_land.setVisibility(View.INVISIBLE);
			if(grabando==1){
				recordStop_land.setVisibility(View.INVISIBLE);
				recordStop.setVisibility(View.VISIBLE);
			}else{
				recordButton_land.setVisibility(View.INVISIBLE);
				recordButton.setVisibility(View.VISIBLE);
			}
		}
		
	}
	/**
	 * Metodo que se ejecuta cuando la actividad se pone en modo Pausa. 
	 * En nuestro caso cierra la aplicacion.
	 */
	public void onPause(){
		super.onPause();
	}

	/**
	 * Metodo que se ejecuta cuando la aplicacion se pone en modo Parada. 
	 * En nuestro caso cierra la aplicacion.
	 */
	public void onStop(){
		if (grabando == 1) {
			grabando=0;
		}
		super.onStop();
	}

	/**
	 * Metodo que se ejecuta cuando la actividad termina. 
	 * Termina con la actividad y cancela todas las vibraciones.
	 */
	public void onDestroy() {
		vibra.cancel();
		finish();
		super.onDestroy();
	}

	/**
	 * Metodo
	 * Calcula los patrones de vibracion a partir de la frecuencia a la que esta 
	 * la maxima energia entre 0-8000Hz y de la energia total.
	 */
	public void vibracion () {
		//Calculamos el tiempo que debe durar el patron de vibracion
		referencia=8000;

		indice = (frecuenciaMaxEnergia/(referencia/90))+10;
		tiempo = 1000/indice;
		//Calculamos el ciclo de trabajo de la vibracion

		if (energiaTotalDb<umbralMinimo){
			cicloDeTrabajo=0;
		}
		else{
			cicloDeTrabajo=(energiaTotalDb-umbralMinimo)/(umbralMaximo-umbralMinimo);
		}

		//Creamos un array con el patron de vibracion
		patrones[0]=0;
		patrones[1]=(long)(cicloDeTrabajo*tiempo);
		patrones[2]=(long)(tiempo-(cicloDeTrabajo*tiempo));
	}



	/**Dibujar segun unos patrones, que vayan en conjunto con la vibracion. 
	 * Hacer timertask para luego cogerlo desde el pincel para pintarlo segun ese patron*/


	/**
	 * Clase 
	 * Define un hilo paralelo que se ejecutara a la vez que la actividad
	 * principal para llevar a cabo el tratamiento de la se�al.
	 */
	private class MiTarea2 extends AsyncTask<Void, Void, Void> {
		/**
		 * Metodo
		 * Se lleva a cabo todo el procesamiento de la se�al y el 
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
				procesadorMuestras.realfft(muestras);
				//Inicializamos las variables
				i = 0;
				a=0;
				c = 0;
				calculo = 0;

				mediaPonderada=0;
				energiaTotal=0;
				for (i=0; i<128;i++){
					a= muestras [i];
					c= muestras [256-i];
					//Calculo de la energia de cada muestra
					energias[i]= (float) Math.sqrt(Math.pow(a, 2)+Math.pow(c, 2));
					calculo += (i+1)*energias[i];
					//Calculo de la energia total
					//Si es voz, solo de las 64 primeras muestras
					energiaTotal += energias[i];
				}
				//Inicializacion de variables 
				r=0;
				for (r = 0;r<energiasMedias.length;r++){
					energiasMedias[r]=0;
				}



				//Calculo de las energ�as por bandas bas�ndonos en la escala de Bark
				float continua = energias[0];
				energiasMedias[0]= continua;
				int nMuestras=0;
				float suma=0;
				int indiceArray=1;

				for (int i=2;i<energias.length;i++){
					suma+=energias[i];
					nMuestras++;
					if(i==3||i==5||i==6||i==8||i==10||i==12||i==15||i==17||i==20||i==32||i==37||i==43||i==50||i==59||i==70||i==85||i==102||i==123){
						energiasMedias[indiceArray]=suma/(float)nMuestras;

						indiceArray++;
						suma=energias[i];
						nMuestras=1;
					}

				};


				//Pasamos a unidades logaritmicas las energias y eliminamos ruido
				for (int u=0;u<energiasMediasDb.length;u++){
					energiasMediasDb[u] = 20*(float)Math.log(energiasMedias[u]);
					if (energiasMediasDb[u] <= 120) {
						energiasMediasDb[u]=0;
					}
					else 
						energiasMediasDb[u] = (5/2)*(energiasMediasDb[u]-120);
				}

				//Llamamos al metodo que se encarga de calcular y asignar el offset que se le va a
				//aplicar al radio de los circulos para recrear el movimiento de un altavoz.
				mVisualizerView.rellenaCirculos();

				//Llamamos al metodo que se encarga de calcular la energ�a y de aqui los 
				//parametros necesario para la seleccion de las imagenes.
				mVisualizerView.calculaInstrumentos();

				energiaTotalDb=20*(float)Math.log(energiaTotal);

				mediaPonderada = calculo/energiaTotal;
				frecuenciaMaxEnergia = (mediaPonderada*8000)/128;


				//Liberamos memoria
				muestras = null;
				System.gc();
			}
			return null;
		}

		//Metodo que se ejecutara antes del proceso de tratamiento de la se�al.
		protected void onPreExecute () {
			Toast toast1 =
					Toast.makeText(getApplicationContext(),
							"grabando",Toast.LENGTH_SHORT);
			toast1.show();
		}

		//Metodo que se ejecutara despues del proceso de tratamiento de la se�al.
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
	 * 
	 * Clase que extiende View y que representa las visualizaciones
	 * @author 
	 *
	 */
	class VisualizerView extends View {

		private float offsetBlue1=0;
		private float offsetRed1=0;
		private float offsetOrange1=0;
		private float offsetYellow=0;
		private float offsetGreen1=0;
		private float offsetGreen2=0;
		private float offsetPurple1=0;

		//De mas agudo a mas grave

		Bitmap image;
		private int[]valorInstrumentos=new int[4];

		/**
		 * Array donde definimos los colores del ecualizador grafico
		 */
		public int colores [][] = new int [22][3];
		/**
		 * Objeto de la clase Paint que nos permitira dibujar los elementos
		 * que van a componer la visualizacion, asignandole el color correspondiente a 
		 * cada circulo.
		 */
		Paint pincel1= new Paint();
		/**
		 * Objeto que define el color negro
		 */
		private Paint negro = new Paint();

		public VisualizerView(Context context) {
			super(context);

		}
		/**
		 * Metodo que permite inicializar los valores rgb correspondientes a los diferentes
		 * niveles del ecualizador
		 */
		public void inicializacionColores(){
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
		 * Rellena el array con constantes de los colores que conformaran el ecualizador grafico
		 */
		private void rellenaCirculos(){

			offsetGreen1=(float)((5/2)*20*Math.log((((energiasMedias[0] + energiasMedias[1])/2)))-120)/30;
			offsetGreen2=(float)((5/2)*20*Math.log((((energiasMedias[2] + energiasMedias[3])/2)))-120)/30;
			offsetYellow=(float)((5/2)*20*Math.log((((energiasMedias[4] + energiasMedias[5])/2)))-120)/30;
			offsetOrange1=(float)((5/2)*20*Math.log((((energiasMedias[6] + energiasMedias[7])/2)))-120)/30;
			offsetRed1=(float)((5/2)*20*Math.log((((energiasMedias[8] + energiasMedias[9])/2)))-120)/30;
			offsetPurple1= (float)((5/2)*20*Math.log((((energiasMedias[10] + energiasMedias[11])/2)))-120)/30;
			offsetBlue1=(float)((5/2)*20*Math.log((((energiasMedias[14] + energiasMedias[15])/2)))-120)/30;



		}

		/**
		 * Metodo que se encarga de calcular diverso parametros para la visualizacion
		 * cuarteto.
		 */
		private void calculaInstrumentos(){
			valorInstrumentos[3]=(int) (((5/2)*20*Math.log((energiasMedias[2]))-120)/60);
			valorInstrumentos[2]=(int) (((5/2)*20*Math.log((( energiasMedias[7])))-120)/60);
			valorInstrumentos[1]=(int) (((5/2)*20*Math.log(( energiasMedias[12] ))-120)/60);
			valorInstrumentos[0]=(int) (((5/2)*20*Math.log((energiasMedias[17]))-120)/60);
			if(valorInstrumentos[3]>10){valorInstrumentos[3]=10;};
			if(valorInstrumentos[2]>10){valorInstrumentos[2]=10;};
			if(valorInstrumentos[1]>10){valorInstrumentos[1]=10;};
			if(valorInstrumentos[0]>10){valorInstrumentos[0]=10;};

		}
		/**
		 * Metodo
		 * Dibuja donde dibuja las diferentes visualizaciones, dependiendo del valor 
		 * de la varialbe nVis:
		 * 0=>Visualizacion Circulos
		 * 2=>Visualizaci�n cuarteto
		 * 3=>Visualizaci�n ecualizador
		 */
		public void onDraw(Canvas canvas){

			super.onDraw(canvas);
			float alto=canvas.getHeight();
			float ancho=canvas.getWidth();

			switch (nVis) {
			//En el caso de seleccionar visualizacion circulos
			case 0:

				canvas.drawColor(Color.DKGRAY);

				float minimo=alto;
				if(ancho<alto){
					minimo=ancho;
				}
				int GREEN2= Color.rgb(32, 138, 0);
				int GREEN1= Color.rgb(95, 146, 0);
				int ORANGE1= Color.rgb(249, 131, 0);
				int RED1= Color.rgb(245, 20, 0);
				int PURPLE1= Color.rgb(129, 0, 84);  
				int BLUE1= Color.rgb(34, 0, 137);

				canvas.translate(0,0);


				pincel1.setColor(BLUE1);
				canvas.drawCircle(ancho/2, alto*0.4f, (7*minimo/27f)+offsetBlue1+offsetPurple1+offsetRed1+offsetOrange1+offsetYellow+offsetGreen2+offsetGreen1, pincel1);


				pincel1.setColor(PURPLE1);
				canvas.drawCircle(ancho/2, alto*0.4f, (6*minimo/27f)+offsetPurple1+offsetRed1+offsetOrange1+offsetYellow+offsetGreen2+offsetGreen1, pincel1);

				pincel1.setColor(RED1);
				canvas.drawCircle(ancho/2, alto*0.4f, (5*minimo/27f)+offsetRed1+offsetOrange1+offsetYellow+offsetGreen2+offsetGreen1, pincel1);

				pincel1.setColor(ORANGE1);
				canvas.drawCircle(ancho/2, alto*0.4f, (4*minimo/27f)+offsetOrange1+offsetYellow+offsetGreen2+offsetGreen1, pincel1);

				pincel1.setColor(Color.YELLOW);
				canvas.drawCircle(ancho/2, alto*0.4f, (3*minimo/27f)+offsetYellow+offsetGreen2+offsetGreen1, pincel1);

				pincel1.setColor(GREEN2);
				canvas.drawCircle(ancho/2, alto*0.4f, (2*minimo/27f)+offsetGreen2+offsetGreen1, pincel1);

				pincel1.setColor(GREEN1);
				canvas.drawCircle(ancho/2, alto*0.4f, (minimo/27f)+offsetGreen1, pincel1);

				break;
				//En el caso de seleccionar visualizacion cuarteto
			case 1:

				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				Drawable myDrawable = getResources().getDrawable(R.drawable.cuarteto_final);
				Bitmap cuarteto = ((BitmapDrawable) myDrawable).getBitmap();
				canvas.drawBitmap(cuarteto, 0, 0, pincel1);

				for(int i=0;i<valorInstrumentos.length;i++){
					if(valorInstrumentos[i]!=0){
						image=((BitmapDrawable) getResources().getDrawable( getResources().getIdentifier ("t"+i+""+valorInstrumentos[i], "drawable",getPackageName()))).getBitmap();
						canvas.drawBitmap(image,0,0, pincel1);}
				}

				break;
				//En el caso de seleccionar visualizacion ecualizador
			case 2:
				int aumentoAncho=canvas.getWidth()/19;
				int o=0;
				for (int x =0;x<19;x++){
					energiasMediasDbDibuja[x]=energiasMediasDb[x];
				}

				ancho=-17;
				float ancho1=ancho;
				float alto1=alto;
				float ancho2=ancho;
				float alto2=alto;

				if (grabando==1){
					for (int h =0;h<19;h++){
						ancho1+=aumentoAncho;
						ancho2+=aumentoAncho;
						alto2=(float)(canvas.getHeight()*0.4)+15;
						alto1=(float)(canvas.getHeight()*0.4)-15;
						while (energiasMediasDbDibuja[h]!=0){
							if(energiasMediasDbDibuja[h]<20){
								alto1-=20;
								alto2+=20;
								alto1 =alto1-energiasMediasDbDibuja[h];
								alto2 =alto2+energiasMediasDbDibuja[h];
								pincel1.setColor(Color.rgb(colores[o][0], colores[o][1], colores[o][2]));
								canvas.drawRect(ancho1,alto1,ancho1+12, alto1, pincel1);
								canvas.drawRect(ancho2,alto2,ancho2+12, alto2, pincel1);
								o=0;
								energiasMediasDbDibuja[h] =0;

							}
							else {

								pincel1.setColor(Color.rgb(colores[o][0], colores[o][1], colores[o][2]));
								canvas.drawRect(ancho1,alto1-15,ancho1+12, alto1, pincel1);
								canvas.drawRect(ancho2,alto2+15,ancho2+12, alto2, pincel1);
								alto1 -=1;
								alto2+=1;
								canvas.drawRect(ancho1,alto1-1,ancho1+12, alto1, negro);
								canvas.drawRect(ancho2,alto2+1,ancho2+12, alto2, negro);
								energiasMediasDbDibuja[h]-=20;
								o++;
								alto1 -=12;
								alto2 +=12;
							}

						}
					}
				}


				break;
				
			
			default:
				break;
			}




		}
	}



}
