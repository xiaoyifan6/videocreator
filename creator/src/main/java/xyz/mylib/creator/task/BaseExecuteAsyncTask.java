package xyz.mylib.creator.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import xyz.mylib.creator.IProvider;
import xyz.mylib.creator.handler.CreatorExecuteResponseHander;

public abstract class BaseExecuteAsyncTask extends AsyncTask<Void, Integer, String> {

    protected final CreatorExecuteResponseHander mHandler;
    protected final IProvider<Bitmap> mProvider;
    protected final int fps;
    protected final String mPath;

    protected BaseExecuteAsyncTask(IProvider<Bitmap> provider, int fps, CreatorExecuteResponseHander<String> hander, String path) {
        this.mHandler = hander;
        this.mProvider = provider;
        this.fps = fps;
        this.mPath = path;
    }


    @Override
    protected void onPreExecute() {
        mHandler.onStart();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values.length > 0 && values[0] != null && mHandler != null) {
            mHandler.onProgress(values[0].toString());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            mHandler.onSuccess(result);
        } else {
            mHandler.onFailure("结果为空!");
        }
        mHandler.onFinish();
    }

    protected void _publishProgress(int process) {
        publishProgress(process);
    }


}
