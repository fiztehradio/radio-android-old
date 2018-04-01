package com.vk.sergezakharov.phystechradio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class RadioService extends AbstractService implements OnPreparedListener,
		OnBufferingUpdateListener, OnErrorListener {

	public static final int MSG_PLAYING = 1;
	public static final int MSG_NO_PLAYING = 2;
	public static final int MSG_READY_TO_PLAY = 3;
	public static final int MSG_STOPPED_PLAYING = 4;
	
	
	
	private MediaPlayer mp;
	private final String addressString = "http://campus-hosting.mipt.ru:8410/stream"; //http://campus-hosting.mipt.ru:8410/stream http://fr3.ah.fm:9000/ 
	private final String TAG = getClass().getSimpleName();
	public static boolean isPlaying = false;
	public static boolean isReadyToPlay = false;

	public void onCompletion(MediaPlayer mp) {
		mp.stop();
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		StringBuilder sb = new StringBuilder();
		sb.append("Media Player Error: ");
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			sb.append("Not Valid for Progressive Playback");
			break;
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			sb.append("Server Died");
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			sb.append("Unknown");
			//Toast.makeText(this, "Probably, no stream", Toast.LENGTH_LONG).show();
			send(Message.obtain(null, MSG_NO_PLAYING));
			break;
		default:
			sb.append(" Non standard (");
			sb.append(what);
			sb.append(")");
		}
		sb.append(" (" + what + ") ");
		sb.append(extra);
		//Log.e(TAG, sb.toString());
		return true;
	}

	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		//Log.d(TAG, "PlayerService onBufferingUpdate : " + percent + "%");
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		//Log.d(TAG, "Stream is prepared");
		isReadyToPlay = true;
		send(Message.obtain(null, MSG_READY_TO_PLAY));
		//проигрывать не сразу
		

	}

	@Override
	public void onStartService() {

		//Log.d("my_log", "address string: \'" + addressString + "\'");

		Uri myUri = Uri.parse(addressString);
		try {
			if (mp == null) {
				this.mp = new MediaPlayer();
			} else {
				mp.stop();
				mp.reset();
			}
			mp.setDataSource(this, myUri); // Go to Initialized state
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.setOnPreparedListener(this);
			mp.setOnBufferingUpdateListener(this);

			mp.setOnErrorListener(this);
			mp.prepareAsync();
			

			//Log.d(TAG, "LoadClip Done");
		} catch (Throwable t) {
			//Log.d(TAG, t.toString());
			Toast.makeText(this, "No stream", 10).show();
			stopSelf();
			// play.setClickable(true); TODO как-то донести до клиента, что не получилось нынче
		}
	}

	@Override
	public void onStopService() {
		
		mp.stop();
		isPlaying = false;
		// TODO какая-либо хрень
	}

	@Override
	public void onReceiveMessage(Message msg) {
		
		//при нажатии на изображение начать проигрывание
		switch(msg.what){
		
		case FiztehRadio.MSG_START_PLAYING:
			mp.start();
			isPlaying = true;
			send(Message.obtain(null, MSG_PLAYING));
			break;
		case FiztehRadio.MSG_STOP_PLAYING:
			mp.pause();
			isPlaying = false;
			send(Message.obtain(null, MSG_STOPPED_PLAYING));
			break;
		
		case FiztehRadio.MSG_IS_RADIO_PLAYING:
			//Log.d("my_log", "asked if playing");
			if(RadioService.isPlaying){
				//Log.d("my_log", "is playing: " + isPlaying);
				//mClients.add(msg.replyTo);
				send(Message.obtain(null, MSG_PLAYING));
			}
			break;
		default:
			break;
		}
	}
}