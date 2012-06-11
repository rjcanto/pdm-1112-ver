package pt.isel.pdm.Yamba.util;

import java.util.ArrayList;
import java.util.List;

import pt.isel.pdm.Yamba.App;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public final class Preferences implements OnSharedPreferenceChangeListener {
	
	private static final int MAX_CHARS = 140;
	private static final int MAX_POSTS = 10;
	private static final int PREVIEW_CHARS = 10;
	
	private final SharedPreferences _prefs;
	private final List<OnPreferenceChangeListener> _prefListeners;
	
	public Preferences(Context ctx) {
		Log.d(App.TAG, "new Preferences");
		_prefListeners = new ArrayList<OnPreferenceChangeListener>();		
		_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		_prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	/**
     * Registers a callback to be invoked when a change happens to a preference.
     * 
     * @param listener The callback that will run.
     */
	public void registerOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
		_prefListeners.add(listener);
	}
	
	/**
     * Registers a callback to be invoked when a change happens to a preference.
     * 
     * @param listener The callback that will run.
     */
	public void unregisterOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
		_prefListeners.remove(listener);
	}
	
	/** Notify all listeners that a preference was changed */
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		Log.d(App.TAG, "Preferences.onSharedPreferenceChanged");
		
		boolean sessionInvalidated = (key == "user" || key == "pass" || key == "url");
		
		for (OnPreferenceChangeListener listener : _prefListeners)
			listener.onPreferenceChanged(this, key, sessionInvalidated);
	}
	
	/** Returns the maxChars preference value */
	public int maxChars() {
		String maxChars = _prefs.getString("maxChars", Integer.toString(MAX_CHARS));
		return maxChars.equals("") ? MAX_CHARS : Integer.parseInt(maxChars);	
	}
	
	/** Returns the maxPosts preference value */
	public int maxPosts() {
		String maxPosts = _prefs.getString("maxPosts", Integer.toString(MAX_POSTS));		
		return maxPosts.equals("") ? MAX_POSTS : Integer.parseInt(maxPosts);		
	}
	
	/** Returns the maxPostsStored preference value */
	public int maxPostsStored() {
		String maxPostsStored = _prefs.getString("maxPostsStored", Integer.toString(MAX_POSTS));		
		return maxPostsStored.equals("") ? MAX_POSTS : Integer.parseInt(maxPostsStored);		
	}
	
	/** Returns the previewChars preference value */
	public int previewChars() {
		String previewChars = _prefs.getString("previewChars", Integer.toString(PREVIEW_CHARS));		
		return previewChars.equals("") ? PREVIEW_CHARS : Integer.parseInt(previewChars);
	}

	/** Returns the user preference value */
	public String user() {	return _prefs.getString("user", ""); }
	
	/** Returns the pass preference value */
	public String pass() { return _prefs.getString("pass", ""); }
	
	/** Returns the url preference value */
	public String url() { return _prefs.getString("url", ""); }
	
	/** Returns the auto refresh time preference value */
	public int autoRefreshTime() { return Integer.parseInt(_prefs.getString("autoRefreshTime", "-1")); }
	
	/** Returns true if auto refresh is active, otherwise is false */
	public boolean autoRefresh() {return _prefs.getBoolean("autoRefresh",false);}
	
	/** Check if required preferences are filled */
	public boolean hasRequired() {
		return !user().equals("") && !url().equals("");
	}
	
}
