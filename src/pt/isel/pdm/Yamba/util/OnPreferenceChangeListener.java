package pt.isel.pdm.Yamba.util;

public interface OnPreferenceChangeListener {
	public void onPreferenceChanged(Preferences sp,	String key, boolean sessionInvalidated);
}