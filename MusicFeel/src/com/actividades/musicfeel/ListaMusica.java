package com.actividades.musicfeel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.widget.ArrayAdapter;

public class ListaMusica extends ListActivity {
	

    private List<String> item;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Localizamos y llenamos la lista con el array
        super.onCreate(savedInstanceState);
		 setContentView(R.layout.list_main);
	
        
		 item = new ArrayList<String>();
		 
	        //Defino la ruta donde busco los ficheros
	        File f = new File(Environment.getExternalStorageDirectory() + "/Music/");
	        //Creo el array de tipo File con el contenido de la carpeta
	        File[] files = f.listFiles();
	        if(files !=null){
	        //Hacemos un Loop por cada fichero para extraer el nombre de cada uno
	        for (int i = 0; i < files.length; i++)
	 
	        {
	            //Sacamos del array files un fichero
	            File file = files[i];
	 
	            //Si es directorio...
	            if (file.isDirectory())
	            	
	                item.add(file.getName() + "/");
	 
	            //Si es fichero...
	            else
	 
	                item.add(file.getName());
	        }
	        }else{
	        	item.clear();
	        }
	        
	        //Localizamos y llenamos la lista con el array
	        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, item);
	        setListAdapter(fileList);
	        
      
	}


}
