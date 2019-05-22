package xyz.mylib.creator.task;

import android.graphics.Bitmap;

import java.io.File;

import xyz.mylib.creator.IProvider;
import xyz.mylib.creator.Processable;
import xyz.mylib.creator.encoder.AvcEncoder;
import xyz.mylib.creator.handler.CreatorExecuteResponseHander;

public class AvcExecuteAsyncTask extends BaseExecuteAsyncTask implements Processable {

    private final int bitRate;

    private AvcExecuteAsyncTask(IProvider<Bitmap> provider, int fps, CreatorExecuteResponseHander<String> hander, String path, int bitRate) {
        super(provider, fps, hander, path);
        this.bitRate = bitRate;
    }

    @Override
    protected String doInBackground(Void... voids) {
        new AvcEncoder(mProvider, fps, new File(mPath), bitRate, this).start();
        return "";
    }

    public static void execute(IProvider<Bitmap> provider, int delay, CreatorExecuteResponseHander handler, String path, int bitRate) {
        try {
            AvcExecuteAsyncTask asyncTask = new AvcExecuteAsyncTask(provider, delay, handler, path, bitRate);
            asyncTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
            handler.onFailure(e.getMessage());
        }
    }

    public static void execute(IProvider<Bitmap> provider, int delay, CreatorExecuteResponseHander handler, String path) {
        execute(provider, delay, handler, path, 0);
    }

    @Override
    public void onProcess(int process) {
        _publishProgress(process);
    }
}
