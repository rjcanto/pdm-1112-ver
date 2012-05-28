package pt.isel.pdm.Yamba;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import winterwell.jtwitter.Twitter;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class UserInfoPullService extends Service {
	
	final Messenger _messenger = new Messenger(new ServiceHandler());
	public static final int GET_USER_INFO = 1;
	private App _app;
	
	public class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case GET_USER_INFO:
					Log.d(App.TAG, "UserInfoPullService.handleMessage: GET_USER_INFO");
					Messenger cliMessenger = msg.replyTo ;
					Message response = Message.obtain() ;
					Bundle _twitterBundle = new Bundle () ;
					Twitter.User _twUser = _app.twitter().getUser(_app.prefs().user()) ;
					_twitterBundle.putInt("nFollowers", _twUser.getFollowersCount()) ;
					_twitterBundle.putInt("nFriends",_twUser.getFriendsCount()) ;
					_twitterBundle.putInt("nStatus", _twUser.getStatusesCount()) ;
					_twitterBundle.putString("name", _twUser.getName());
										
					try {
						InputStream imageSrc;
						imageSrc = _twUser.getProfileImageUrl().toURL().openStream();
						Bitmap image = BitmapFactory.decodeStream(imageSrc);					
						_twitterBundle.putParcelable("image", image);
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}					
					
					response.setData(_twitterBundle) ; 
					try {
						cliMessenger.send(response) ;
					} catch (RemoteException e) {
						Log.d(App.TAG, "UserInfoPullService.handleMessage Error");
						e.printStackTrace();
					}
						
					break;
				default:
					super.handleMessage(msg);
			}
		} 
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		_app = (App) getApplication() ;
		Log.d(App.TAG, "UserInfoPullService.onCreate");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(App.TAG, "UserInfoPullService.onBind");
		return _messenger.getBinder();
	}

	@Override
	public void onDestroy() {
		Log.d(App.TAG, "UserInfoPullService.onDestroy");		
		super.onDestroy();
	}

}
