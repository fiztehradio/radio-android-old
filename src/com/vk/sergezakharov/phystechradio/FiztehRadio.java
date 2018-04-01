package com.vk.sergezakharov.phystechradio;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest.ErrorCode;

public class FiztehRadio extends Activity implements AdListener {

	
	private final int ABOUT = 1;
	private final int EXIT = 2;
	
	private ProgressBar pgBar;
	private ServiceManager manager;
	private ImageView image;
	private TextView onAir;
	
	public final static int MSG_START_PLAYING = 1;
	public final static int MSG_STOP_PLAYING = 2;
	public final static int MSG_IS_RADIO_PLAYING = 3;
	
	private final int PLAY = 4; // треугольник
	private final int STOPPED = 5;		 // наушники
	private final int PAUSE = 6;		 // сделать паузу

	private int curImage = STOPPED;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.activity_fizteh_radio);

		pgBar = (ProgressBar) findViewById(R.id.pb_wait_upload);
		image = (ImageView) findViewById(R.id.fizteh_image);
		onAir = (TextView) findViewById(R.id.onAir);
		
		image.setClickable(true);
		
	//запрос о том, проигрывает ли
		if(RadioService.isPlaying){
			image.setImageResource(R.drawable.fizteh_pause);
			
            curImage = PAUSE;
            onAir.setVisibility(TextView.VISIBLE);
            pgBar.setVisibility(ProgressBar.INVISIBLE);
            image.setClickable(true);
		}
		else if(RadioService.isReadyToPlay){

        	image.setImageResource(R.drawable.x_44448cfe);
        	curImage = PLAY;
        	onAir.setVisibility(TextView.VISIBLE);
        	image.setClickable(true);
        	
        	pgBar.setVisibility(ProgressBar.INVISIBLE);
    	}
		
		
		this.manager = new ServiceManager(FiztehRadio.this, RadioService.class, new Handler() {
		      @Override
		      public void handleMessage(Message msg) {
		        // Receive message from service
		        switch (msg.what) {
		          case RadioService.MSG_READY_TO_PLAY:
		        	//Log.d("my_log", "READY_TO_PLAY message came");
		        	//play.setText("Stop");
		        	
		        	image.setImageResource(R.drawable.x_44448cfe);
		        	onAir.setVisibility(TextView.VISIBLE);
		        	curImage = PLAY;
		        	image.setClickable(true);
		        	
		        	pgBar.setVisibility(ProgressBar.INVISIBLE);
		        	
		            break;
		          case RadioService.MSG_PLAYING:
		            image.setImageResource(R.drawable.fizteh_pause);
		            curImage = PAUSE;
		            pgBar.setVisibility(ProgressBar.INVISIBLE);
		            image.setClickable(true);
		            break;
		          case RadioService.MSG_NO_PLAYING:
		        	  manager.stop();
		        	  onAir.setVisibility(TextView.INVISIBLE);
		        	  Toast.makeText(FiztehRadio.this, "No stream", Toast.LENGTH_LONG).show();
		        	  image.setImageResource(R.drawable.fizteh_stop);
		        	  curImage = STOPPED;
		        	break;
		          case RadioService.MSG_STOPPED_PLAYING:
		        	  //manager.stop();
		        	  image.setImageResource(R.drawable.x_44448cfe);
		        	  curImage = PLAY;
		        	  image.setClickable(true);
		        	break;
		          default:
		            super.handleMessage(msg);
		        } 
		      }
		    });
		
		//Log.d("myLog", "Starting manager");
		manager.start();
/*		
		try {
			Log.d("my_log", "asking if playing");
			manager.send(Message.obtain(null, MSG_IS_RADIO_PLAYING));
			Log.d("my_log", "asking if playing");
			manager.send(Message.obtain(null, MSG_IS_RADIO_PLAYING));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	*/
		image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.d("my_log", "clickes are heard");
				try {
					
					if (v.getId() == R.id.fizteh_image){//play
						if (curImage == PLAY){
					
						manager.send(Message.obtain(null, MSG_START_PLAYING));
						image.setClickable(false);
						pgBar.setVisibility(ProgressBar.VISIBLE);
						//Log.d("my_log", "x_44448cfe was clicked, MSG_START_PLAYING is send");
					}
					else if (curImage == PAUSE){
						manager.send(Message.obtain(null, MSG_STOP_PLAYING));
						
						}
					else if (curImage == STOPPED){
						Toast.makeText(FiztehRadio.this, "Сейчас не вещаем", Toast.LENGTH_LONG).show();
						
						}
					}
				} catch (RemoteException e) {
					
					e.printStackTrace();
				}
				
			}
		});
		
		
	}
	
	
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.vk.sergezakharov.phystechradio.RadioService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ABOUT, 0, "О программе");
		menu.add(1, EXIT, 0, "Выход");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case ABOUT:
			showAbout();
			break;
		case EXIT:
			exit();
			break;
		
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void showAbout() {
		//создаём диалоговое окно
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("О программе");
    	builder.setMessage("Creator: SergeZakh \n sergei.zaharov@phystech.edu");
    	builder.setNegativeButton("ОК", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
	}

	@Override
	protected void onStop() {
		if (!RadioService.isPlaying){
			manager.stop();
		}
		manager.unbind();
		// TODO Auto-generated method stub
		super.onStop();
	}

	private void exit() {
		//создаём диалоговое окно
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Вы уверены, что хотите выйти из программы?");
    	builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				FiztehRadio.this.finish();
			}
    		
    	});
    	
    	builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
	}



	@Override
	public void onDismissScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveAd(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

}