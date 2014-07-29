package android.musicfeel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListaMusica extends ListActivity {
	
	/**
	 * Lista que contendra las String con el nombre de los ficheros y directorios contenidos en 
	 * la ruta actual.
	 */
    private List<String> item;
    
    /**
     * String que contiene el directorio actual del cual se muesta su informacion en la lista.
     */
    private String directorioActual;
    
    /**
     * Objeto TextView que muestra el directorio actual.
     */
    private TextView dir_actual;
    
    /**
	 * Metodo invocado al inicio de la actividad.
	 * Inicializacion de variables, procesamiento de informacion de los ficheros,
	 * definicion de ArrayAdapter.
	 * @param savedInstanceState Bundle que contiene el estado anterior de la 
	 * actividad en caso de que haya sido suspendida.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		 setContentView(R.layout.list_main);
		 
		 dir_actual=(TextView)findViewById(R.id.dir_actual);
         directorioActual=Environment.getExternalStorageDirectory() + "/Music/";
		 item = new ArrayList<String>();
		 Bundle datos=getIntent().getExtras();
		 
		 if(datos == null){
			 dir_actual.setText(directorioActual);
			 item.add("../");
			 //Defino la ruta donde busco los ficheros
		     File f = new File(directorioActual);
		     //Creo el array de tipo File con el contenido de la carpeta
		     File[] files = f.listFiles();
		     if(files !=null){
		     //Hacemos un Loop por cada fichero para extraer el nombre de cada uno
		     for (int i = 0; i < files.length; i++){
		         //Sacamos del array files un fichero
		         File file = files[i];
		 
		            //Si es directorio...
		            if (file.isDirectory())
		            	
		                item.add(file.getName() + "/");
		 
		            //Si es fichero...
		            else{
		            	if(comprobarExtension(getExtension(file.getName()))){
		                item.add(file.getName());
		            	}
		            }
		            }
		        }else{
		        	item.clear();
		        	f.mkdir();
		        	item.add("../");
		        	
		        }
		 }else{
			 
			 directorioActual=datos.getString("directorio");
			 dir_actual.setText(directorioActual);
			 
			//Defino la ruta donde busco los ficheros
		     File f = new File(directorioActual);
		     
		     //Creo el array de tipo File con el contenido de la carpeta
		     File[] files = f.listFiles();
		     
		     if(files !=null){
		    	 if(!directorioActual.equals("/storage")){
		    	 item.add("../");
		    	 }
		     //Hacemos un Loop por cada fichero para extraer el nombre de cada uno
		     for (int i = 0; i < files.length; i++){
		         //Sacamos del array files un fichero
		         File file = files[i];
		 
		            //Si es directorio...
		            if (file.isDirectory())
		            	
		            	item.add(file.getName() + "/");
		 
		            //Si es fichero...
		            else{
		            	if(comprobarExtension(getExtension(file.getName()))){
			                item.add(file.getName());
			            	}
		            }
		            	
		                
		        }
		        }else{
		        	item.clear();
		        	item.add("../");
		        }
			 
		 }
		 
		
	        item=ordenaLista(item);
	        //Localizamos y llenamos la lista con el array
	        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, item);
	        setListAdapter(fileList);
	        
      
	}
	
	/**
	 * Listener que permite definir que hacer cuando pulsamos un elemento de la lista.
	 */
	@Override 
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		Object o = getListAdapter().getItem(position);
		String nombre=o.toString();
		File file=new File(directorioActual+"/"+nombre);
		if(o.toString().charAt(nombre.length()-1)=='/'){
			//En el caso de que queramos acceder a otro directorio volvemos de nuevo
			//a la actividad ListaMusica con el directorio actual igual al directorio
			//que queremos accerder
			Intent i = new Intent(this,ListaMusica.class);
			try {
				i.putExtra("directorio",file.getCanonicalPath() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			startActivity(i);
			//Definimos la transicion entre actividades.
			overridePendingTransition(R.anim.entrada, R.anim.salida);
			finish();
		}else{
			//En el caso de que se pase el fichero, se ira a la Activity ActivityReproductor
			Intent i = new Intent(this,ActivityReproductor.class);
			i.putExtra("resultado",file.toString());
			i.putExtra("nombre", nombre);
			startActivity(i);
			
		}
	}
	


	/**
	 * Metodo encargado de devolver la extension del nombre del fichero pasado como parametro.
	 * @param filename Nombre del fichero que se quiere obtener su extension.
	 * @return String con la extension del fichero.
	 */
	 public static String getExtension(String filename) {
         int index = filename.lastIndexOf('.');
         if (index == -1) {
               return "";
         } else {
               return filename.substring(index + 1);
         }
	 }
	 
	 /**
	  * Metodo que permite comprobar si la extension del fichero es uno de los tipos
	  * soportados por la aplicacion.
	  * @param extension parametro de tipo String que representa la extension del fichero
	  * que queremos comprobar.
	  * @return boolean True en el caso de que la extension sea de los tipos aceptado, False si
	  * no lo es
	  */
	 public static boolean comprobarExtension(String extension) {
         if(extension.equals("raw")){
        	 return true;
         }else{
        	 return false;
         }
	 }

	 /**
	  * Metodo static que permite ordenar la lista de manera alfabética, colocando primero los 
	  * directorios
	  * y despues los ficheros.
	  * @param lista de String que contendra la lista de directorios y ficheros.
	  * @return List<String> que contiene los directorios y fichero colocados.
	  * 
	  */
	 public static List<String> ordenaLista(List<String> lista){
		 List<String> listaDir=new ArrayList<String>();
		 List<String> listaFil=new ArrayList<String>();
		 List<String> listaFinal = new ArrayList<String>();
		 for(String cadena:lista){
			 if(cadena.charAt(cadena.length()-1)==('/')){
				 listaDir.add(cadena);
			 }else{
				 listaFil.add(cadena);
			 }
		 }
		 Collections.sort(listaDir);
		 Collections.sort(listaFil);
		 listaFinal.addAll(listaDir);
		 listaFinal.addAll(listaFil);
		 return listaFinal;
	 }
	 
}
