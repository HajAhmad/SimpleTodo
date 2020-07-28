package ir.simpletodo.data;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

abstract class BaseAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {
    private WeakReference<Context> mContextReference;
    private DbTask mDbInstance;
    private OnFinishListener<Result> mListener;

    public BaseAsyncTask(Context context) {
        this.mContextReference = new WeakReference<>(context);
    }

    public BaseAsyncTask(Context context, OnFinishListener<Result> listener) {
        this(context);
        mListener = listener;
    }

    protected DbTask getReadableDbTask() {
        mDbInstance = DbTask.getInstance(mContextReference.get());
        mDbInstance.openReadableDatabase();
        return mDbInstance;
    }

    protected DbTask getWritableDbTask() {
        mDbInstance = DbTask.getInstance(mContextReference.get());
        mDbInstance.openWritableDatabase();
        return mDbInstance;
    }

    protected void close() {
        mDbInstance.close();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        mContextReference.clear();
        if (mListener != null) mListener.onFinished(result);
    }

}