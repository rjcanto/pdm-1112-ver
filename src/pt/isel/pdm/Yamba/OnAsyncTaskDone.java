package pt.isel.pdm.Yamba;

public interface OnAsyncTaskDone<T> {
	void onTaskDone(AsyncTaskResult<T> asyncTaskResult);
}