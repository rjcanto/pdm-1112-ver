package pt.isel.pdm.Yamba.util;

public interface OnAsyncTaskDone<T> {
	void onTaskDone(AsyncTaskResult<T> asyncTaskResult);
}