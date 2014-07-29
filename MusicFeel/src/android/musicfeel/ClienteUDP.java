package android.musicfeel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Clase que representa la comunicación de un cliente UDP a traves de DatagramSocket
 * @author Miriam Martin Gonzalez y Ricardo Jose Garcia Pinel
 *
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ClienteUDP{
	
	String ip;
	int puerto=8080;
	/*
	 * Lista de mensajes generados correspondientes a toda la cansion
	 */
	String[]listaMensajes;
	/*
	 * Indice que permite controlar que mensaje se debe enviar en cada momento 
	 */
	int nMensaje;

	/*
	 * Constructor de la clase, debe recibir como parámetros,
	 * la lista que contiene los arrays con las energias y frecuencias
	 */
	public ClienteUDP(List<float[]>lista){
		listaMensajes=this.formMsg(lista);
		nMensaje=0;
		ip="10.100.0.51";
	}
	 
	/*
	 * Accion de envio del datagrama correspondiente a la posicion apuntada por nMensaje
	 */
	public void ejecutaCliente() {

	 Log.i("sd","asdas");
	 log(" socket " + ip + " " + puerto);
	 try {
	 DatagramSocket sk = new DatagramSocket(8080);	 
	 InetAddress addr=InetAddress.getByName(ip);
	 String msg=listaMensajes[nMensaje];
	 int sizeMsg;
	 int i=0;
	 msg+=i;
	 sizeMsg=msg.length();
	 DatagramPacket dp =new DatagramPacket(msg.getBytes(), sizeMsg, addr , puerto);	 
	 
	 sk.send(dp);
	 sk.close();
	 
	 }catch (Exception e) {
		 log("error: " + e.toString());
	  }
	  }
	 
	  private void log(String string) {
	     Log.i("MiConexion", string + "\n");
	  }
	  
	  /*
	   * Procesado de la lista de energias para generar todos los mensajes correspondientes
	   * y devolverlo en forma de array
	   */
	  private String[] formMsg(List<float[]>lista){
		  String[]listaMensajes=new String[lista.size()];
		  String informacion="";
		  int pos=0;
		  for(float[]elemento : lista){
			  for(int i=0;i<21;i++){
				  informacion+=elemento[i]+"/";
			  }
			  listaMensajes[pos]=informacion;
			  pos++;
			  informacion="";
		  }
		  return listaMensajes;
	  }
	}
