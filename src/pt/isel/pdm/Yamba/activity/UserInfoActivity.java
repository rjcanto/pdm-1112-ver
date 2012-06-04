package pt.isel.pdm.Yamba.activity;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.R.id;
import pt.isel.pdm.Yamba.R.layout;
import pt.isel.pdm.Yamba.services.UserInfoPullService;
import pt.isel.pdm.Yamba.util.GeneralMenu;
import pt.isel.pdm.Yamba.util.OnPreferenceChangeListener;
import pt.isel.pdm.Yamba.util.Preferences;
import winterwell.jtwitter.Twitter.Status;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class UserInfoActivity extends Activity 
                 implements OnPreferenceChangeListener {
	
	private App _app;
	private TextView _tvName, _tvStatusCount, _tvNSubscriptions, _tvNSubscribers;
	private ImageView _ivProfile;
	private GeneralMenu _generalMenu;
	private Intent _serviceIntent;
	private boolean _bound;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "UserInfoActivity.onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.userinfo);
		
		_app = (App) getApplication();
		_app.prefs().registerOnPreferenceChangeListener(this);
		
		_tvName = (TextView) findViewById(R.id.userInfo_name);
		_tvStatusCount = (TextView) findViewById(R.id.userInfo_statusCount);
		_tvNSubscriptions = (TextView) findViewById(R.id.userInfo_nSubscriptions);
		_tvNSubscribers = (TextView) findViewById(R.id.userInfo_nSubscribers);
		_ivProfile = (ImageView) findViewById(R.id.profileImage);
	}
	

	@Override
	protected void onDestroy() {
		_app.prefs().unregisterOnPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d(App.TAG, "UserInfoActivity.onResume");
		super.onResume();
		refresh();
	}

	@Override
	protected void onPause() {
		Log.d(App.TAG, "UserInfoActivity.onPause");
		super.onPause();
		if(_bound) {
			Log.d(App.TAG, "UserInfoActivity.onPause: unbind");
			unbindService(_connection) ;
		}
	}

	/** Initialize options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		_generalMenu = new GeneralMenu(this, menu);
		_generalMenu.timeline().setVisible(true);
		_generalMenu.status().setVisible(true);
		_generalMenu.refresh().setVisible(false);
		_generalMenu.userInfo().setVisible(false);
		return true;
	}

	/** Process Item Menu selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return _generalMenu.processSelection(item) ? true : super.onOptionsItemSelected(item);
	}

	/** Invalidates the twitter when changing preferences */
	public void onPreferenceChanged(Preferences prefs, String key, boolean sessionInvalidated) {
		Log.d(App.TAG,"StatusActiviy.onPrefsChanged");
		if (sessionInvalidated)
			refresh();
	}

	private final Messenger _cliMessenger = new Messenger(new IncomingHandler()) ;
	private Messenger _srvMessenger ;
	
    private ServiceConnection _connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			_srvMessenger = new Messenger(service);
            _bound = true;
            Message msg = Message.obtain(null,UserInfoPullService.GET_USER_INFO) ;
            msg.replyTo = _cliMessenger ;
            
            try {
				_srvMessenger.send(msg) ;
			} catch (RemoteException e) {
				Log.d(App.TAG, "UserInfoActivity: ServiceConnection.onServiceConnected Error");
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			_srvMessenger = null;
			_bound = false;
		}
    };
    
    private class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Bundle _srvData = msg.getData() ;
			_tvNSubscribers.setText(String.valueOf(_srvData.getInt("nFollowers"))) ;
			_tvNSubscriptions.setText(String.valueOf(_srvData.getInt("nFriends"))) ;
			_tvStatusCount.setText(String.valueOf(_srvData.getInt("nStatus"))) ; 
			_tvName.setText(_srvData.getString("name"));
			_ivProfile.setImageBitmap((Bitmap) _srvData.getParcelable("image"));
			super.handleMessage(msg);
		}
    	
    }
    
	private void refresh() {
		bindService(new Intent(this, UserInfoPullService.class), _connection, Context.BIND_AUTO_CREATE) ;
	}
	
	public void onRefreshDone(Status status) {
		Log.d(App.TAG, "StatusActivity.onStatusSent");
		
	}
	
}