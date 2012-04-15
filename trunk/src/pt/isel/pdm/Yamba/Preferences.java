package pt.isel.pdm.Yamba;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

interface OnPreferenceChangeListener {
	public void onPreferenceChanged(Preferences sp,	String key);
}

final class Preferences {
	
	private static final int MAX_CHARS = 140;
	private static final int MAX_POSTS = 10;
	
	private final SharedPreferences _prefs;
	private final List<OnPreferenceChangeListener> _prefListeners;
	
	public Preferences(Context ctx) {
		_prefListeners = new ArrayList<OnPreferenceChangeListener>();		
		_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		final Preferences that = this;
		
		_prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {			
			public void onSharedPreferenceChanged(SharedPreferences sp, String key) {				
				for (OnPreferenceChangeListener listener : _prefListeners)
					listener.onPreferenceChanged(that, key);
			}
		});
		
	}
	
	/**
     * Registers a callback to be invoked when a change happens to a preference.
     * 
     * @param listener The callback that will run.
     */
	public void registerOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
		_prefListeners.add(listener);
	}
	
	/** Returns the maxChars preference value */
	public int maxChars() {
		String maxChars = _prefs.getString("maxChars", Integer.toString(MAX_CHARS));
		return (maxChars == "") ? MAX_CHARS : Integer.parseInt(maxChars);	
	}
	
	/** Returns the maxPosts preference value */
	public int maxPosts() {
		String maxPosts = _prefs.getString("maxPosts", Integer.toString(MAX_POSTS));		
		return (maxPosts == "") ? MAX_POSTS : Integer.parseInt(maxPosts);		
	}

	/** Returns the user preference value */
	public String user() {	return _prefs.getString("user", ""); }
	
	/** Returns the pass preference value */
	public String pass() { return _prefs.getString("pass", ""); }
	
	/** Returns the url preference value */
	public String url() { return _prefs.getString("url", ""); }
	
}
