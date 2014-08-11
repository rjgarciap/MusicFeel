package android.musicfeel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Actividad ActivityReproductor. 
 * Traduce a vibraciones y una visualizacion gr�fica los sonidos a partir de un
 * fichero de musica para que de esta manera una persona sorda pueda disfrutar de 
 * la musica. 
 * 
 * @author Miriam Martin Gonzalez y Ricardo J. Garcia Pinel
 * 
 */
public class ActivityReproductor extends Activity {	
    

	/**
     * Objeto de la clase Imagen Button, su pulsacion inicia la reproduccion.
     */
	private ImageButton play;
	
	/**
     * Objeto de la clase Imagen Button, su pulsacion pausa la reproduccion.
     */
	private ImageButton pause;
	
	/**
	 * Objeto de la clase TextView, en el que se representa el nombre de la 
	 * cancion que se reproduce con un efecto rotatorio.
	 */
	private TextView nombre;
	
	/**
	 * Objeto de la clase LinearLayout en el que se representara
	 * la visualizacion.
	 */
	private LinearLayout visualizacion;
	
	/**
     * Objeto de la clase VisualizerView, clase que extiende a View, sobre el que ejecutaremos 
     * onDraw(), el cual pintara la visualizacion sobre la pantalla.
     */
    private VisualizerView mVisualizerView;
    
	/**
	 * Objeto de la clase AudioTrack que permite la reproduccion del audio
	 * en Streaming desde un Buffer que almacena las muestras.
	 */
	private AudioTrack audioTrack;
	
	/**
	 * Objeto de la clase File que contendra el fichero de musica seleccionado
	 * desde la lista 
	 */
    private File musica;
    
    /**
     * Temporizador: para el refresco de la pantalla y el 
     * calculo de los nuevos patrones de vibracion.
     */
    private Timer t;
    
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
	 * Objeto de la clase Vibrator que se encargara de las vibraciones.
	 */
    private Vibrator vibra;
    
    /**
     * Variable que contiene el estado de la reproduccion.
     */
    private int reproduciendo=0;
    
    /**
	 * Array que guardara el tiempo en ms que conformara el patron de vibracion.
	 */
    private long [] patrones;
    
    /**
     * Tama�o minimo del buffer de reproduccion
     */
    int minBufferSize;
    
    /**
     * Buffer de tipo Byte en el que se guardan las muestras para su posterior analisis
     * y reproduccion.
     */
    byte [] buffer;
    /**
	 * Suma de las energias de todas las muestras en unidades logaritmicas.
	 */
	private float energiaTotalDb;
	/**
	 * Suma de las energias de todas las muestras en unidades logaritmicas.
	 */
	private float energiaTotalDbCalculo;
	/**
     * Frecuencia en Hz en la que esta la maxima energia de la se�al.
     */
    private float frecuenciaMaxEnergia;
    /**
	 * Umbral minimo en unidades logaritmicas que decidimos para
	 * que el dispositivo vibre, si no supera este umbral no vibrara.
	 */
	private int umbralMinimo=255;
	/**
	 * Umbral maximo en unidades logaritmicas que decidimos para 
	 * que el dispositivo vibre, si llega hasta este valor vibrara
	 * durante el maximo tiempo.
	 */
	private int umbralMaximo=360;
	
	/**
	 * Buffer que contiene las muestras de la cancion en Short
	 */
    short[]bufferSalidaShort;
    
    
    /**
     * Objeto de la clase ProcesadorMuestras, que permite realizar el metodo FFT
     */
    ProcesadorMuestras procesadorMuestras=new ProcesadorMuestras();
    
    /**
     * Float correspondiente a la parte real resultante de la transformada FFT
     */
    float real;
    
    /**
     * Float correspondiente a la parte imaginaria resultante de la transformada FFT
     */
    float img;
    
    /**
	 * Array donde almacenamos la energia de cada muestra.
	 */
    float[] energias=new float[128];
    
	/**
	 * Variable que guarda la suma de la energia de las 128 muestras
	 * multiplicadas cada una por la posicion que ocupa en el array (media ponderada).
	 */
    float calculo;
    
	/**
	 * Suma de la energias de todas las muestras en unidades lineales.
	 */
    float energiaTotal;
    
    /**
	 * Si estamos grabando voz tendra un valor de 2000, y si grabamos
	 * musica tendra un valor de 4000 (depende del spiner).
	 */
	private int referencia;
	/**
	 * Tiempo en ms del indice calculado en Hz para el patron de vibracion.
	 */
	private double tiempo;
	private int w;

    /**
	 * Media ponderada nos mostrara cual es la muestra a la que se encuentra la
	 * la maxima energia.
	 */
	private float mediaPonderada;
	
	/**
	 * Frecuencia en Hz que determina la duracion del patron de vibracion
	 * calculado.
	 */
	private double indice;
	
	/**
	 * Posicion en la lista que marca el array correspondiente al instante de la cancion en cada momento
	 */
    private int posLista=0;
    
    /**
     * Lista 
     */
    List<float[]> coleccion=new ArrayList<float[]>();
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
	 * Array que almacena las energias medias de un grupo de muestras
	 * pero en unidades logaritmicas.
	 */
	private float[] energiasMediasDbCalculo;
	
    /**
	 * Array donde almacenamos la energia media de un grupo de muestras.
	 */
	private float[] energiasMedias;
	float[][]resultado=new float[3000][257];
	
	/**
	 * Ciclo de Trabajo con el que vibrara en un determinado momento
	 * el patron de vibraci�n.
	 */
	private double cicloDeTrabajo;
	
	/**
	 * Arrays de tipo float usados para llevar a cabo el filtrado paso bajo
	 */
	private float[]result;
	private float[]result_sig;
	private float[]result_ant;
	/**
	 * Array donde se guarda el resultado de filtrar las muestras mediante un filtrado paso bajo
	 */
	List<float[]> mFiltradas;

	/**
	 * Conjunto total que incluye las energias filtrado por el filtrado paso bajo 
	 */
	float[]barkFiltradas;
	
	/**
	 * Barra de progreso que indica el que se esta procesando la camncion
	 */
    ProgressDialog barraProgreso;
    /**
     * Variable de tipo entero que permitira seleccionar entre las diferentes visualizaciones.
     * 0 => Visualizacion Circulos.
     */
    private int nVis=0;
    short[]muestrasShort;
    /**
	 * RadioGroup y RadioButton correspondientes a la orientacion portrait 
	 */
    private RadioGroup radioGroup;
    private RadioButton radioCirculosGroup;
    private RadioButton radioTrompetasGroup;
    private RadioButton radioEcualizadorGroup;
    
    /**
     * ImageButton correspondiente a pause y play en la orientaci�n portrait
     */
    private ImageButton play_land;
    private ImageButton pause_land;
    
    /**
	 * RadioGroup y RadioButton correspondientes a la orientacion landscape 
	 */
    private RadioGroup radioGroup_land;
    private RadioButton radioCirculosGroup_land;
    private RadioButton radioTrompetasGroup_land;
    private RadioButton radioEcualizadorGroup_land;
    
    /**
	 * Variable de apoyo que identifica la orientacion de la pantalla
	 */
    private boolean landscape=false;
    
    /**
     * Objeto de la clase ClienteUDP, encargado de proveer la conexion entre la aplicaci�n y la Raspberry Pi
     */
    private ClienteUDP clienteUDP;
    
    
    /**
	 * Metodo invocado al inicio de la actividad.
	 * 
	 * Inicializacion de variables, definicion de los listener de eventos 
	 * en los botones, definicion de TimerTask. 
	 *  @param savedInstanceState Bundle que contiene el estado anterior de la 
	 *  actividad en caso de que haya sido suspendida.
	 */
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mVisualizerView = new VisualizerView(this);
		setContentView(R.layout.activity_reproductor);      
        visualizacion=(LinearLayout)findViewById(R.id.surfaceView);
        visualizacion.addView(mVisualizerView);
        barraProgreso = new ProgressDialog(this);
		barraProgreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		barraProgreso.setMessage("Procesando...");
		mVisualizerView.inicializacionColores();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		play=(ImageButton) findViewById(R.id.ic_media_play);
		pause=(ImageButton)findViewById(R.id.ic_media_pause);
		play_land=(ImageButton)findViewById(R.id.ic_media_play2);
		pause_land=(ImageButton)findViewById(R.id.ic_media_pause2);
		radioGroup=(RadioGroup)findViewById(R.id.RadioGroup);
		radioGroup_land=(RadioGroup)findViewById(R.id.RadioGroup_vert);
		nombre=(TextView) findViewById(R.id.nombre_cancion);
		nombre.setSelected(true);
		minBufferSize=AudioTrack.getMinBufferSize(16000,AudioFormat.CHANNEL_CONFIGURATION_MONO , AudioFormat.ENCODING_PCM_16BIT);
		audioTrack= new AudioTrack(AudioManager.STREAM_MUSIC,16000,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT,minBufferSize,AudioTrack.MODE_STREAM);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.
		           Builder().permitNetwork().build());
		vibra = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		new MiTarea3().execute();
		
		
		/*
		 * Inicializacion de variables a los valores por defecto si no decidimos nada,
		 */
		reproduciendo=0;
		w=1;
		energiasMedias = new float [19];
		energiasMediasDb = new float [19];
		energiasMediasDbDibuja = new float [19];
		patrones = new long [3];
		
		/**
         * Listener del boton play
         * Define que hacer cuando pulsamos sobre el ImageButton play
         * Comenzara a reproducir
         */
		play.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				play();
			}
		});
		
		play_land.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				play();
				
			}
		});
		/**
         * Listener del boton pause
         * Define que hacer cuando pulsamos sobre el ImageButton pause
         * Pausara la reproduccion.
         */
		pause.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				pause();	
			}
		});
		
		pause_land.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				pause();			
			}
		});


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
				
			 
		
		
	}

	@Override
	protected void onPause(){
		super.onPause();
		pause();
		}
	
	

	
	@Override
	protected void onDestroy() {
        super.onDestroy();
        pause();
        barkFiltradas=null;
        if(t!=null){
        	t.cancel();
        	t.purge();
        } 
        System.gc();
        
	}
	
	/**
	 * Metodo play() encargado de iniciar la hebra encargada de la reproduccion
	 * y procesado de las muestras.
	 */
	public void play(){
		reproduciendo=1;
		if(landscape){
			play_land.setVisibility(View.GONE);
			pause_land.setVisibility(View.VISIBLE);
		}else{
			play.setVisibility(View.GONE);
			pause.setVisibility(View.VISIBLE);
		}
		new MiTarea2().execute();
		Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vi.vibrate(500);
		
		//Creacion del temporizador
		t = new Timer();

		scanTask = new TimerTask() {
	        public void run() {
	                handler.post(new Runnable() {
	                        public void run() {
	                        	
	                        		result=mFiltradas.get(posLista);
	                        		
	                        		for (int u=0;u<energiasMediasDb.length;u++){
	                        			energiasMedias[u]=result[u];
	            						energiasMediasDb[u] = 20*(float)Math.log(energiasMedias[u]);
	            						if (energiasMediasDb[u] <= 120) {
	            							energiasMediasDb[u]=0;
	            						}
	            						else 
	            							energiasMediasDb[u] = (5/2)*(energiasMediasDb[u]-120);
	            					}

	                        		
	                        	frecuenciaMaxEnergia=result[19];
		                        energiaTotalDb=result[20];		
		                        if(posLista<mFiltradas.size()-1){
		                        	posLista++;
		                        }else{
		                        	posLista=0;
		                        }
        				//Llamamos al metodo que se encarga de calcular y asignar el offset que se le va a
        				//aplicar al radio de los circulos para recrear el movimiento de un altavoz.
        				mVisualizerView.rellenaCirculos();
        				//Llamamos al metodo que se encarga de calcular la energ�a y de aqui los 
        				//parametros necesario para la seleccion de las imagenes.
        				mVisualizerView.calculaInstrumentos();
	        				if(true){
	                        	vibra.cancel();
	                        	vibracion();	                        	
	                   			
	                        	if (reproduciendo==1){
	                   			 vibra.vibrate(patrones,0);
	                   			 clienteUDP.ejecutaCliente();
	                   			if(clienteUDP.nMensaje<clienteUDP.listaMensajes.length){
	                   			 	clienteUDP.nMensaje++;
	                   		 	}else{
	                   		 		clienteUDP.nMensaje=0;
	                   		 	}
	                   			
	                               }
	                               else{
	                              	 vibra.cancel();
	                              	
	                               }
	                        	
	                        }
	                        
	                        
	                        mVisualizerView.invalidate();
	                        
	                        }
	               });
	        }};
	    //Definimos el periodo del Timer, 50ms 
	    t.schedule(scanTask, 0, 50); 
}
	
	/**
	 * Metodo encargado de alternar las vistas en modo landscape o portrait
	 */
	public void alternar(){
		if(landscape){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			radioGroup.setVisibility(View.GONE);
			radioGroup_land.setVisibility(View.VISIBLE);
			if(reproduciendo==1){
				pause_land.setVisibility(View.VISIBLE);
				pause.setVisibility(View.GONE);
			}else{
				play_land.setVisibility(View.VISIBLE);
				play.setVisibility(View.GONE);
			}
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			radioGroup.setVisibility(View.VISIBLE);
			radioGroup_land.setVisibility(View.GONE);
			if(reproduciendo==1){
				pause_land.setVisibility(View.GONE);
				pause.setVisibility(View.VISIBLE);
			}else{
				play_land.setVisibility(View.GONE);
				play.setVisibility(View.VISIBLE);
			}
		}
		
	}
	/**
	 * Metodo pause() encargado de parar la reproduccion
	 * y procesado de las muestras.
	 */
	public void pause(){
		clienteUDP.nMensaje=0;
		audioTrack.stop();
		reproduciendo=0;
		posLista=0;
		if(landscape){
			play_land.setVisibility(View.VISIBLE);
			pause_land.setVisibility(View.GONE);
		}else{
			play.setVisibility(View.VISIBLE);
			pause.setVisibility(View.GONE);
		}
		//Paramos el temporizador
		if(t!= null&& vibra!=null){
			t.cancel();
		}
		//Cancelamos las vibraciones
		vibra.cancel();
		//Vibra dos veces para indicar que ha terminado el proceso
		long [] patron = {0,500,200,500};
		vibra.vibrate(patron,-1);
	}
	
	/**
	 * Clase MiTarea2 que define la hebra en la que se lleva a cabo la reproduccion
	 * de estas muestras.
	 */
	private class MiTarea2 extends AsyncTask<Void, Void, Void> {
		
		
		protected Void doInBackground(Void...voids ) {

				audioTrack.write(muestrasShort, 0,muestrasShort.length);

	
			System.gc();

			return null;
		}

		protected void onPreExecute () {
			Toast toast1 =
					Toast.makeText(getApplicationContext(),
					"Reproduciendo",Toast.LENGTH_SHORT);
			toast1.show();
			audioTrack.play();
		}

		
		@Override
		protected void onPostExecute(Void result) {
			Toast toast2 =
					Toast.makeText(getApplicationContext(),
					"Terminado",Toast.LENGTH_SHORT);
			toast2.show();
			audioTrack.stop();
			pause();
		}	
	}
	
	/**
	 * Clase MiTarea2 que define la hebra en la que se lleva a cabo el 
	 * procesamiento de la se�al y calculo de las energias.
	 */
	private class MiTarea3 extends AsyncTask<Void, Integer, Void> {
		
		
		protected Void doInBackground(Void...voids ) {

			int bufferSize=(int)musica.length();
			buffer = new byte[bufferSize];
			
		    try {
		        InputStream inputStream = new FileInputStream(musica);
		        while((inputStream.available()) >0)
		            inputStream.read(buffer);
		        inputStream.close();
	   
		        
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		    		
					muestrasShort=arrayByteToShort(buffer, buffer.length/2, true);
					buffer=null;
					System.gc();
					
					int j=0;
					
					
					while((muestrasShort.length-(j*800))>257){
						
						
						for(int i=0;i<257;i++){
							resultado[j%3000][i]=0;
							resultado[j%3000][i]=(float)(muestrasShort[(j*800)+i]);
						}
						
						
						procesadorMuestras.realfft(resultado[j%3000]);
						energiaTotal=0;
						calculo=0;
												
						for (int i=0; i<128;i++){
							real= resultado[j%3000][i];
							img= resultado[j%3000][256-i];
							//Calculo de la energia de cada muestra
							energias[i]= (float) Math.sqrt(Math.pow(real, 2)+Math.pow(img, 2));
							calculo += (i+1)*energias[i];
							//Calculo de la energia total
						
							energiaTotal += energias[i];
						}

						

							
							
							
							float[]bark=new float[21];
	                      //Calculo de las energ�as por bandas bas�ndonos en la escala de Bark
	        				float continua =energias[0];
	        				bark[0]= continua;
	        				int nMuestras=0;
	        				float suma=0;
	        				int indiceArray=1;
	        				
	        				
	        				for (int i=2;i<energias.length-2;i++){
	        					suma+=energias[i];
	        					nMuestras++;
	        					if(i==3||i==5||i==6||i==8||i==10||i==12||i==15||i==17||i==20||i==32||i==37||i==43||i==50||i==59||i==70||i==85||i==102||i==123){
	        						bark[indiceArray]=suma/(float)nMuestras;
	        						
	        						indiceArray++;
	        						suma=energias[i];
	        						nMuestras=1;
	        					}
	        					
	        				};
	        					
	        				
	        				
	        				//Llamamos al metodo que se encarga de calcular y asignar el offset que se le va a
	        				//aplicar al radio de los circulos para recrear el movimiento de un altavoz.
	        				mediaPonderada = calculo/energiaTotal;
		        			frecuenciaMaxEnergia = (mediaPonderada*8000)/128;
	        					
	        				bark[19]=frecuenciaMaxEnergia;	        				
	        				bark[20]=20*(float)Math.log(energiaTotal);
	        					
	        				
	        				
	        				
	        				coleccion.add(coleccion.size(),bark);

	        				for (int r = 0;r<energias.length;r++){	energias[r]=0;}

					
						j++;
					}
					mFiltradas=new ArrayList<float[]>();
					
					
					for(int i=0;i<coleccion.size();i++){
						barkFiltradas=new float[21];
						if(i==0){
							result=coleccion.get(i);
	                		result_sig=coleccion.get(i+1);
	                		 for (int u=0;u<19;u++){
	                			 barkFiltradas[u]=(result[u]+result_sig[u]/2);
	                		 }
						}else{
							if(i==(coleccion.size()-1)){
								result=coleccion.get(i);
	                			result_ant=coleccion.get(i-1);
	                		    for (int u=0;u<19;u++){
	                		    	barkFiltradas[u]=(result_ant[u]+result[u]/2);
	                		    }
	                		 }else{
								result_ant=coleccion.get(i-1);
	                			result=coleccion.get(i);
	                    		result_sig=coleccion.get(i+1);
	                    		for (int u=0;u<19;u++){
	                    			barkFiltradas[u]=(result_ant[u]+result[u]+result_sig[u]/3);
	                    		}
							}
						}
						barkFiltradas[19]=result[19];
						barkFiltradas[20]=result[20];
						mFiltradas.add(barkFiltradas);
					}
					
					
					
						
					
					publishProgress(1);
					coleccion=null;
					resultado=null;
					System.gc();
					clienteUDP=new ClienteUDP(mFiltradas);
					return null;
		}

		protected void onPreExecute () {
			musica=null;
			Bundle datos=getIntent().getExtras();
			
			if(datos!=null){
				musica=new File(datos.getString("resultado"));
				nombre.setText(datos.getString("nombre"));
			}
			barraProgreso.setCancelable(false);
			barraProgreso.setMax(1);
			barraProgreso.setTitle("Procesando audio");
			barraProgreso.setProgress(0);
			barraProgreso.show();
		}
		protected void onProgressUpdate (Integer... values) {
			int progreso = values[0].intValue();
			barraProgreso.setProgress(progreso);

		}
		
		@Override
		protected void onPostExecute(Void result) {
			barraProgreso.dismiss();
		}	
	}

	public int getIdentifier (String name, String defType, String defPackage){
		return android.content.res.Resources.getSystem().getIdentifier(name, defType, defPackage);
		
	}
	
	private short[] arrayByteToShort(byte[]entrada,int size,boolean par){
		short [] resultado=new short[size];
		short dato=0;
		if(par){
			for(int i=0;i<size;i++){
				dato=byteToShort(entrada[i*2], entrada[(i*2)+1]);
				resultado[i]=dato;
			}
		}else{
			for(int i=0;i<size;i++){
				dato=byteToShort(entrada[i*2], entrada[(i*2)+1]);
				resultado[i]=dato;
				if(i==size-1){
					dato=byteToShort((byte)0, entrada[(i*2)+1]);
					resultado[i]=dato;
				}
			}
		}
		
		return resultado;
		
	}
	
	private short byteToShort(byte a,byte b){
		short sh=(short)(b&0xFF);
		short sh2=(short)(a&0xFF);
		sh<<=8;
		
		short ret =(short)(sh|sh2);
	
		return ret;
	}
	
	private float[] arrayMedia(float[]entrada,int periodo){
		

			for(int i=0;i<entrada.length;i++){
				entrada[i]=entrada[i]/periodo;
			}

		
		return entrada;
	}

	private float[] arraySuma(float[]entrada,float[]entrada2){
		for(int i=0;i<entrada.length;i++){
			entrada[i]+=entrada2[i];
		}
		return entrada;
	}
	
	/**
	 * Metodo
	 * Calcula los patrones de vibracion a partir de la frecuencia a la que esta 
	 * la maxima energia entre 0-8000Hz y de la energia total.
	 */
	public void vibracion () {
		//Calculamos el tiempo que debe durar el patron de vibracion
			referencia=8000;
			
			indice = (frecuenciaMaxEnergia/(referencia/99))+1;
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

			case 1:
				Drawable myDrawable = getResources().getDrawable(R.drawable.cuarteto_final);
				Bitmap cuarteto = ((BitmapDrawable) myDrawable).getBitmap();
				canvas.drawBitmap(cuarteto, 0, 0, pincel1);

				for(int i=0;i<valorInstrumentos.length;i++){
					if(valorInstrumentos[i]!=0){
						image=((BitmapDrawable) getResources().getDrawable( getResources().getIdentifier ("t"+i+""+valorInstrumentos[i], "drawable",getPackageName()))).getBitmap();
						canvas.drawBitmap(image,0,0, pincel1);}
				}


				break;
				//En el caso de seleccionar visualizacion cuarteto
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

				if (reproduciendo==1){
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
				//En el caso de seleccionar visualizacion ecualizador

			default:
				break;
			}




		}
	}



}
