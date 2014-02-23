package com.actividades.musicfeel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListaMusica extends ListActivity {
	

    private List<String> item;
    private String directorioActual;
    private TextView dir_actual;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Localizamos y llenamos la lista con el array
        super.onCreate(savedInstanceState);
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
	
	@Override 
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		Object o = getListAdapter().getItem(position);
		String nombre=o.toString();
		File file=new File(directorioActual+"/"+nombre);
		if(o.toString().charAt(nombre.length()-1)=='/'){
			Intent i = new Intent(this,ListaMusica.class);
			try {
				i.putExtra("directorio",file.getCanonicalPath() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			startActivity(i);
			overridePendingTransition(R.anim.entrada, R.anim.salida);
		}else{
			Intent i = new Intent(this,ActivityReproductor.class);
			i.putExtra("resultado",file.toString());
			i.putExtra("nombre", nombre);
			startActivity(i);
			
		}
	}
	
	protected void onPause(){
		super.onPause();
		}

	 public static String getExtension(String filename) {
         int index = filename.lastIndexOf('.');
         if (index == -1) {
               return "";
         } else {
               return filename.substring(index + 1);
         }
	 }
	 
	 public static boolean comprobarExtension(String extension) {
         if(extension.equals("mp3")){
        	 return true;
         }else{
        	 return false;
         }
	 }

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
