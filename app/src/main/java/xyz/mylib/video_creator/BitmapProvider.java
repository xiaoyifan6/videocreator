package xyz.mylib.video_creator;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.Queue;

import xyz.mylib.creator.IProviderExpand;
import xyz.mylib.video_creator.adapter.CommonAdapter;
import xyz.mylib.video_creator.util.BitmapUtil;

/**
 * <pre>
 *     author : yangzhi.ou
 *     e-mail : xxx@xx
 *     time   : 2019/05/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class BitmapProvider implements IProviderExpand<Bitmap> {
    private final CommonAdapter<String> adapter;
    private int index = 0;

    private Queue<byte[]> queue;

    private int[] scaleSize;

    public BitmapProvider(CommonAdapter<String> adapter, int[] scaleSize) {
        this.adapter = adapter;
        this.scaleSize = scaleSize;
        queue = new LinkedList<>();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (i < adapter.getItemCount()) {
                Bitmap bitmap = genBitmap(i);
                queue.add(BitmapUtil.getBytesByPNG(bitmap));
                bitmap.recycle();
            }
        }
    }

    @Override
    public void prepare() {
    }

    @Override
    public void finish() {
    }

    @Override
    public void finishItem(Bitmap item) {
//        item.recycle();
//        System.gc();
    }

    @Override
    public boolean hasNext() {
        return index < adapter.getItemCount();
    }

    @Override
    public int size() {
        return adapter.getItemCount();
    }

    private Bitmap genBitmap(int index) {
        String path = adapter.getItem(index);
        return BitmapUtil.decodeSampleBitmapFromResource(path, scaleSize[0], scaleSize[1]);
    }

    @Override
    public Bitmap next() {
        byte[] bytes = queue.poll();
        index++;
        return BitmapUtil.loadFromBytesByPNG(bytes);
    }
}
