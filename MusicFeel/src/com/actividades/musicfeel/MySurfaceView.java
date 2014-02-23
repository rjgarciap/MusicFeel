package com.actividades.musicfeel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements Callback {
	private VisualizacionThread thread;
	public MySurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		getHolder().addCallback(this);
	}
	
	  @Override
	  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){
	
	
	   }
	
	 
	
	   @Override
	   public void surfaceCreated(SurfaceHolder arg0) {
		   Log.e("surfaceCreated ", "Hilo creado ");
		   thread = new VisualizacionThread(getHolder(), this);
		   thread.setRunning(true);
		   thread.start();
	
	   }


	   @Override
	   public void surfaceDestroyed(SurfaceHolder arg0) {
		   Log.e("surfaceDestroyed ", "Hilo detenido ");
		   boolean retry = true;
		      thread.setRunning(false);
		      while (retry) {
		         try {
		            thread.join();
		            retry = false;
		         } catch (InterruptedException e) { }
		      }
		   }

	   
	   
	   @Override
	   public void onDraw(Canvas canvas){
		   canvas.drawColor(Color.WHITE);
		   int altura=canvas.getHeight();
		   int ancho=canvas.getWidth();
		   
		   Paint pincel1= new Paint();
		   int ORANGE= Color.rgb(255, 153, 0);
		   
		   pincel1.setColor(Color.RED);
		   canvas.drawCircle(ancho/2, altura/2, 180, pincel1);
		   
		   pincel1.setColor(ORANGE);
		   canvas.drawCircle(ancho/2, altura/2, 130, pincel1);
		   
		   pincel1.setColor(Color.YELLOW);
		   canvas.drawCircle(ancho/2, altura/2, 80, pincel1);
		   
		   pincel1.setColor(Color.GREEN);
		   canvas.drawCircle(ancho/2, altura/2, 40, pincel1);
	   
		   
	   }
	   


}
