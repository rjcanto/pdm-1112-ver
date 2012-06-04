package pt.isel.pdm.Yamba.util;

public interface AsyncTaskResult<T> {
	public Throwable error();
	public T result();
	public void setOnAsyncTaskDone(OnAsyncTaskDone<T> asyncTaskDone);
	public boolean isDone();
}
