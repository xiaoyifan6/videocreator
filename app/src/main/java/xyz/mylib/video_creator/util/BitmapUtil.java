package xyz.mylib.video_creator.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * 对Bitmap进行缩放的工具类
 *
 * @author ousir
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * 根据资源id获取图片,并进行压缩
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampleBitmapFromResource(Resources res, int resId,
                                                        int reqWidth, int reqHeight) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opts);
        int inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, opts);
        return bitmap;
    }

    /**
     * 根据文件名获取图片,并进行压缩
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampleBitmapFromResource(String pathName,
                                                        int reqWidth, int reqHeight) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        int inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);
        return bitmap;
    }

    /**
     * 从byte数组中获取图片并压缩
     *
     * @param data
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampleBitmapFromByteArray(byte[] data,
                                                         int reqWidth, int reqHeight) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        int inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        return bitmap;
    }

    /**
     * 计算缩放比例
     *
     * @param opts
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(Options opts, int reqWidth,
                                             int reqHeight) {
        if (opts == null) return 1;

        int inSampleSize = 1;
        int realWidth = opts.outWidth;
        int realHeight = opts.outHeight;

        if (realHeight > reqHeight || realWidth > reqWidth) {
            int widthRatio = realWidth / reqWidth;
            int heightRatio = realHeight / reqHeight;

            inSampleSize = (heightRatio > widthRatio) ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    /**
     * 将drawable转换为bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_4444 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 缩放图片
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap newBitmap = null;

        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) w / width);
            float scaleHeight = ((float) h / height);
            matrix.postScale(scaleWidth, scaleHeight);
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }

        return newBitmap;
    }

    /**
     * 按比例缩放图片
     *
     * @param bitmap
     * @param scale
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, float scale) {
        if (scale <= 0) scale = 1;
        Bitmap newBitmap = null;
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        return newBitmap;
    }

    /**
     * 从图片中获取字节数组
     */
    public static byte[] getBytes(Bitmap bitmap) {
        byte[] buffer = new byte[bitmap.getRowBytes() * bitmap.getHeight()];
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        bitmap.copyPixelsToBuffer(byteBuffer);
        return buffer;
    }

    /**
     * 将字节数组加载到图片中
     *
     * @param bitmap
     * @param buffer
     */
    public static Bitmap loadFromBytes(Bitmap bitmap, byte[] buffer) {
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return bitmap;
    }

    public static Bitmap loadFromBitmap(Bitmap bitmap, Bitmap bitmap_src) {
        return loadFromBitmap(bitmap, bitmap_src, true);
    }

    /**
     * 从图片加载到另一张图片
     *
     * @param bitmap
     * @param bitmap_src
     * @return
     */
    public static Bitmap loadFromBitmap(Bitmap bitmap, Bitmap bitmap_src, boolean clearBmp) {
        if (clearBmp)
            bitmap.eraseColor(Color.TRANSPARENT);
//		bitmap_src = zoomBitmap(bitmap_src, bitmap.getWidth(), bitmap.getHeight());
//		loadFromBytes(bitmap, getBytes(bitmap_src));
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        float rw = (float) bitmap.getWidth() / bitmap_src.getWidth();
        float rh = (float) bitmap.getHeight() / bitmap_src.getHeight();
        canvas.scale(rw, rh);
        canvas.drawBitmap(bitmap_src, 0, 0, null);
        return bitmap;
    }

    /**
     * 将图片转化为字节数组
     *
     * @param bitmap
     * @return
     */
    public static byte[] toBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 将字节数组转化为图片
     *
     * @param bytes
     * @return
     */
    public static Bitmap toBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 压缩图片为png
     *
     * @param bitmap
     * @return
     */
    public static byte[] getBytesByPNG(Bitmap bitmap) {
        ByteArrayOutputStream out = null;
        byte[] buffer = null;
        try {
            out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, out);
            out.flush();
            buffer = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return buffer;
    }

    /**
     * 解析图片从png字节里面
     *
     * @param buffer
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap loadFromBytesByPNG(byte[] buffer, int reqWidth, int reqHeight) {
        if (buffer == null) return null;
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(buffer, 0, buffer.length, opts);
        int inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, opts);
        return bitmap;
    }

    /**
     * 解析图片从png字节里面
     *
     * @param buffer
     * @return
     */
    public static Bitmap loadFromBytesByPNG(byte[] buffer) {
        return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
    }

    /**
     * 保存图片
     *
     * @param path
     * @param bmp
     */
    public static void saveBitmapAsJPEG(String path, Bitmap bmp) {
        File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存图片
     *
     * @param path
     * @param bmp
     */
    public static void saveBitmapAsPNG(String path, Bitmap bmp) {
        File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(CompressFormat.PNG, 100, out);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建图片
     *
     * @param width
     * @param height
     * @return
     */
    public static Bitmap create(int width, int height) {
        return Bitmap.createBitmap(width, height, Config.ARGB_4444);
    }

    public static int[] getImageSize(String path) {
        if (path.endsWith(".mp4")) {
            return getVideoSize(path);
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        return new int[]{options.outWidth, options.outHeight};
    }

    private static int[] getVideoSize(String path) {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        int[] size = {-1, -1};

        try {
            mmr.setDataSource(path);
//			String duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

            size[0] = Integer.parseInt(width);
            size[1] = Integer.parseInt(height);
        } catch (Exception ex) {
            Log.e(TAG, "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release();
        }
        return size;
    }
}
