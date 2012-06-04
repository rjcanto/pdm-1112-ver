package pt.isel.pdm.Yamba.util;

import pt.isel.pdm.Yamba.App;
import android.os.AsyncTask;
import android.util.Log;

public abstract class CustomAsyncTask<Params, Progress, Result> 
	extends AsyncTask<Params, Progress, Void> 
	implements AsyncTaskResult<Result> {
	
	private Exception _error;
	private OnAsyncTaskDone<Result> _onAsyncTaskDone;
	private Result _result;
	private boolean _isDone;

	public CustomAsyncTask(OnAsyncTaskDone<Result> onAsyncTaskDone) {
		Log.d(App.TAG, "new CustomAsyncTask");
		_onAsyncTaskDone = onAsyncTaskDone;
	}
	
	public CustomAsyncTask() {
		this(null);
		Log.d(App.TAG, "new CustomAsyncTask");
	}
	
	protected abstract Result doWork(Params...params);
	
	@Override
	final protected Void doInBackground(Params... params) {
		Log.d(App.TAG, "CustomAsyncTask.doInBackground");
		try {
			_error = null;
			_result = doWork(params);
		}
		catch (Exception te) {
			_error = te;
			_result = null;
		}
		return null;					
	}
	
	@Override
	final protected void onPostExecute(Void ignore) {
		Log.d(App.TAG, "CustomAsyncTask.onPostExecute");
		
		_isDone = true;
		
		if (_onAsyncTaskDone != null)
			_onAsyncTaskDone.onTaskDone(this);
	}
	
	public void setOnAsyncTaskDone(OnAsyncTaskDone<Result> onAsyncTaskDone) {
		_onAsyncTaskDone = onAsyncTaskDone;
	}
	
	public Throwable error() {
		return _error;
	}

	public Result result() {			
		return _result;
	}
	
	public boolean isDone() {
		return _isDone;
	}
}