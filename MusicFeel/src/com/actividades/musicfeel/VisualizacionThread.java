package com.actividades.musicfeel;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

@SuppressLint("WrongCall")
public class VisualizacionThread extends Thread{

	private SurfaceHolder sh;
	private MySurfaceView view;
	private boolean run;
	
	public VisualizacionThread(SurfaceHolder sh, MySurfaceView view){
		this.sh=sh;
		this.view=view;
		run=false;
	}
	
	public void setRunning(boolean run){
		this.run = run;
	}
	
	public void run(){
		Canvas canvas;
		while(run){
			canvas=null;
			try{
				canvas=sh.lockCanvas(null);
				synchronized(sh){
					if(view.offsetRed<30){
						   view.offsetRed++;
						   }else{
							   view.offsetRed=0;
						   }
					view.onDraw(canvas);
				}
			}catch(NullPointerException a){
				
			} finally {
				if(canvas != null)
					sh.unlockCanvasAndPost(canvas);
			}
		}
		
	}
	
	
	
	
}
