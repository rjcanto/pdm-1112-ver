package pt.isel.pdm.Yamba;

public interface AsyncTaskResult<T> {
	public Throwable error();
	public T result();
	public void setOnAsyncTaskDone(OnAsyncTaskDone<T> asyncTaskDone);
	public boolean isDone();
}
