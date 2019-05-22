package xyz.mylib.creator.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import xyz.mylib.creator.IProvider;
import xyz.mylib.creator.IProviderExpand;
import xyz.mylib.creator.encoder.AnimatedGifEncoder;
import xyz.mylib.creator.handler.CreatorExecuteResponseHander;

public class GIFExecuteAsyncTask extends AsyncTask<Void, Integer, String> {

    private final CreatorExecuteResponseHander mHandler;
    private final IProvider<Bitmap> mProvider;
    private final int fps;
    private final String mPath;

    private GIFExecuteAsyncTask(IProvider<Bitmap> provider, int fps, CreatorExecuteResponseHander<String> handler, String path) {
        this.mHandler = handler;
        this.mProvider = provider;
        this.fps = fps;
        this.mPath = path;
    }

    @Override
    protected void onPreExecute() {
        mHandler.onStart();
        if (mProvider instanceof IProviderExpand) {
            ((IProviderExpand<Bitmap>) mProvider).prepare();
        }
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
        if (mProvider instanceof IProviderExpand) {
            ((IProviderExpand<Bitmap>) mProvider).finish();
        }
        mHandler.onFinish();
    }

    private void _publishProgress(int process) {
        publishProgress(process);
    }

    @Override
    protected String doInBackground(Void... voids) {

        _publishProgress(0);
        OutputStream baos = null;
        try {
            //初始化
            baos = new FileOutputStream(mPath);
            AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
            localAnimatedGifEncoder.start(baos);
            localAnimatedGifEncoder.setRepeat(0);
            localAnimatedGifEncoder.setFrameRate(fps); //setDelay(delay);
            _publishProgress(2);
            if (mProvider.size() > 0) {

                if (isCancelled()) return null;
                int i = 0;
                while (mProvider.hasNext()) {
                    Bitmap bitmap = mProvider.next();
                    localAnimatedGifEncoder.addFrame(bitmap);
                    if (mProvider instanceof IProviderExpand) {
                        ((IProviderExpand<Bitmap>) mProvider).finishItem(bitmap);
                    }
                    _publishProgress(i * 90 / mProvider.size() + 2);
                    i++;
                }

            }

            _publishProgress(92);
            localAnimatedGifEncoder.finish();

            _publishProgress(95);
            baos.flush();
            _publishProgress(98);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mHandler.onFailure(e.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.onFailure(e.getMessage());
            return null;
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        _publishProgress(100);
        return mPath;
    }

    public static void execute(IProvider<Bitmap> provider, int delay, CreatorExecuteResponseHander handler, String path) {
        try {
            GIFExecuteAsyncTask asyncTask = new GIFExecuteAsyncTask(provider, delay, handler, path);
            asyncTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
            handler.onFailure(e.getMessage());
        }
    }
}
