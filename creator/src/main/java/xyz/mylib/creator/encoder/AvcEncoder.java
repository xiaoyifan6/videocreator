package xyz.mylib.creator.encoder;


import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import xyz.mylib.creator.IProvider;
import xyz.mylib.creator.IProviderExpand;
import xyz.mylib.creator.Processable;


public class AvcEncoder {
    private final static String TAG = "MeidaCodec";

    private final int mFrameRate;
    private final File out;
    private final int bitRate;
    private final IProvider<Bitmap> mProvider;
    private final Processable mProcessable;

    private MediaCodec mediaCodec;
    //    public byte[] configByte;
    public boolean isRunning;
    private MediaMuxer mediaMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;
    private int colorFormat;


    public AvcEncoder(IProvider<Bitmap> provider, int framerate, File out, int bitRate, Processable processable) {
        this.mFrameRate = framerate;
        this.out = out;
        this.bitRate = bitRate;
        this.mProvider = provider;
        this.mProcessable = processable;

        this.isRunning = false;
        this.mTrackIndex = 0;
        this.mMuxerStarted = false;


    }

    private void init(int width, int height) {

        int bitRate0 = bitRate;
        if (bitRate == 0) {
            bitRate0 = width * height;
        }

        int[] formats = this.getMediaCodecList();

        lab:
        for (int format : formats) {
            switch (format) {
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar: // yuv420sp
                    colorFormat = format;
                    break lab;
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar: // yuv420p
                    colorFormat = format;
                    break lab;
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar: // yuv420psp
                    colorFormat = format;
                    break lab;
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar: // yuv420pp
                    colorFormat = format;
                    break lab;
            }
        }

        if (colorFormat <= 0) {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }


        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);//COLOR_FormatYUV420SemiPlanar
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate0);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);

//
//        对于planar的YUV格式，先连续存储所有像素点的Y，紧接着存储所有像素点的U，随后是所有像素点的V。
//        对于packed的YUV格式，每个像素点的Y,U,V是连续交*存储的。
//
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //创建生成MP4初始化对象
            mediaMuxer = new MediaMuxer(out.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();

        isRunning = true;
    }

    public int[] getMediaCodecList() {
        //获取解码器列表
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            //轮训所要的解码器
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    found = true;
                }
            }
            if (!found) {
                continue;
            }
            codecInfo = info;
        }
        Log.d(TAG, "found" + codecInfo.getName() + "supporting" + " video/avc");
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        return capabilities.colorFormats;
    }


    public void finish() {
        isRunning = false;
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
        }
        if (mediaMuxer != null) {
            try {
                if (mMuxerStarted) {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(mProvider instanceof IProviderExpand){
            ((IProviderExpand<Bitmap>) mProvider).finish();
        }

    }

    public void start() {
        try {
            if(mProvider instanceof IProviderExpand){
                ((IProviderExpand<Bitmap>) mProvider).prepare();
            }

            if (mProvider.size() > 0) {
                mProcessable.onProcess(1);
                Bitmap bitmap = mProvider.next();
                if (bitmap != null) {
                    this.init(getSize(bitmap.getWidth()), getSize(bitmap.getHeight()));
                    mProcessable.onProcess(2);
                    this.run(bitmap);
                }
            }
        } finally {

            finish();
            mProcessable.onProcess(100);
        }
    }

    private int getSize(int size) {
        return size / 4 * 4;
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / mFrameRate;
    }

    private byte[] getNV12(int inputWidth, int inputHeight, Bitmap scaled) {
        // Reference (Variation) : https://gist.github.com/wobbals/5725412

        int[] argb = new int[inputWidth * inputHeight];

        //Log.i(TAG, "scaled : " + scaled);
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];

        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar: // yuv420sp
                encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar: // yuv420p
                encodeYUV420P(yuv, argb, inputWidth, inputHeight);
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar: // yuv420psp
                encodeYUV420PSP(yuv, argb, inputWidth, inputHeight);
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar: // yuv420pp
                encodeYUV420PP(yuv, argb, inputWidth, inputHeight);
                break;
        }


//        scaled.recycle();

        return yuv;
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
//                R = (argb[index] & 0xff000000) >>> 24;
//                G = (argb[index] & 0xff0000) >> 16;
//                B = (argb[index] & 0xff00) >> 8;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                V = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128; // Previously U
                U = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128; // Previously V

                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    private void encodeYUV420P(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + width * height / 4;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
//                R = (argb[index] & 0xff000000) >>> 24;
//                G = (argb[index] & 0xff0000) >> 16;
//                B = (argb[index] & 0xff00) >> 8;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                V = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128; // Previously U
                U = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128; // Previously V


                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[vIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv420sp[uIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }



                index++;
            }
        }
    }

    private void encodeYUV420PSP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
//        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
//                R = (argb[index] & 0xff000000) >>> 24;
//                G = (argb[index] & 0xff0000) >> 16;
//                B = (argb[index] & 0xff00) >> 8;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                V = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128; // Previously U
                U = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128; // Previously V

                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[yIndex + 1] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[yIndex + 3] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                if (index % 2 == 0) {
                    yIndex++;
                }
                index++;
            }
        }
    }

    private void encodeYUV420PP(byte[] yuv420sp, int[] argb, int width, int height) {

        int yIndex = 0;
        int vIndex = yuv420sp.length / 2;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
//                R = (argb[index] & 0xff000000) >>> 24;
//                G = (argb[index] & 0xff0000) >> 16;
//                B = (argb[index] & 0xff00) >> 8;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                V = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128; // Previously U
                U = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128; // Previously V

                if (j % 2 == 0 && index % 2 == 0) {// 0
                    yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                    yuv420sp[yIndex + 1] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[vIndex + 1] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yIndex++;

                } else if (j % 2 == 0 && index % 2 == 1) { //1
                    yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                } else if (j % 2 == 1 && index % 2 == 0) { //2
                    yuv420sp[vIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                    vIndex++;
                } else if (j % 2 == 1 && index % 2 == 1) { //3
                    yuv420sp[vIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                }
                index++;
            }
        }
    }


    private void drainEncoder(boolean endOfStream, MediaCodec.BufferInfo bufferInfo) {
        final int TIMEOUT_USEC = 10000;

        ByteBuffer[] buffers = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buffers = mediaCodec.getOutputBuffers();
        }

        if (endOfStream) {
            try {
                mediaCodec.signalEndOfInputStream();
            } catch (Exception e) {
            }
        }

        while (true) {
            int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break; // out of while
                } else {
                    Log.i(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }

                MediaFormat mediaFormat = mediaCodec.getOutputFormat();
                mTrackIndex = mediaMuxer.addTrack(mediaFormat);
                mediaMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.i(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else {
                ByteBuffer outputBuffer = null;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffer = buffers[encoderStatus];
                } else {
                    outputBuffer = mediaCodec.getOutputBuffer(encoderStatus);
                }

                if (outputBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer "
                            + encoderStatus + " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    Log.d(TAG, "BufferInfo: " + bufferInfo.offset + ","
                            + bufferInfo.size + ","
                            + bufferInfo.presentationTimeUs);

                    try {
                        mediaMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
                    } catch (Exception e) {
                        Log.i(TAG, "Too many frames");
                    }

                }

                mediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.i(TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.i(TAG, "end of stream reached");
                    }
                    break; // out of while
                }
            }
        }
    }

    public void run(Bitmap bitmap) {
        final int TIMEOUT_USEC = 10000;
        isRunning = true;
        long generateIndex = 0;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        ByteBuffer[] buffers = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buffers = mediaCodec.getInputBuffers();
        }

        while (isRunning) {

            int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                long ptsUsec = computePresentationTime(generateIndex);
                if (generateIndex >= mProvider.size()) {
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, ptsUsec,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isRunning = false;
                    drainEncoder(true, info);

                } else {
                    if (bitmap == null) {
                        bitmap = mProvider.next();
                    }
                    byte[] input = getNV12(getSize(bitmap.getWidth()), getSize(bitmap.getHeight()), bitmap);//AvcEncoder.this.getNV21(bitmap);
                    if(mProvider instanceof IProviderExpand){
                        ((IProviderExpand<Bitmap>) mProvider).finishItem(bitmap);
                    }
                    bitmap = null;
                    //有效的空的缓存区
                    ByteBuffer inputBuffer = null;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        inputBuffer = buffers[inputBufferIndex];
                    } else {
                        inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);//inputBuffers[inputBufferIndex];
                    }
                    inputBuffer.clear();
                    inputBuffer.put(input);
                    //将数据放到编码队列
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, ptsUsec, 0);
                    drainEncoder(false, info);
                }

                mProcessable.onProcess((int) (generateIndex * 96 / mProvider.size()) + 2);

                generateIndex++;
            } else {
                Log.i(TAG, "input buffer not available");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//            byte[] input = null;
//
//            if (input != null) {
//                try {
//                    // 拿到有空闲的输入缓存区下标
//                    long ptsUsec = 0l;
//                    if (inputBufferIndex >= 0) {
//                        ptsUsec = computePresentationTime(generateIndex);
//                        //有效的空的缓存区
//                        ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);//inputBuffers[inputBufferIndex];
//                        inputBuffer.clear();
//                        inputBuffer.put(input);
//                        //将数据放到编码队列
//                        mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, ptsUsec, 0);
//                        generateIndex += 1;
//                    }
//
//                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//
//                    //得到成功编码后输出的out buffer Id
//                    int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
//                    while (outputBufferIndex >= 0) {
//                        //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
//                        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);//outputBuffers[outputBufferIndex];
//                        byte[] outData = new byte[bufferInfo.size];
//                        outputBuffer.get(outData);
//
//                        if (bufferInfo.flags == 2) {
//                            configByte = new byte[bufferInfo.size];
//                            configByte = outData;
//                        } else if (bufferInfo.flags == 1) {
//                            byte[] keyframe = new byte[bufferInfo.size + configByte.length];
//                            System.arraycopy(configByte, 0, keyframe, 0, configByte.length);
//                            System.arraycopy(outData, 0, keyframe, configByte.length, outData.length);
//
////                            outputStream.write(keyframe, 0, keyframe.length);
//                        } else {
////                            outputStream.write(outData, 0, outData.length);
//                        }
//
//                        outputBuffer.position(bufferInfo.offset);
//                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
//                        // 将编码后的数据写入到MP4复用器
//                        mediaMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
//
//                        //释放output buffer
//                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
//                        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
//                    }
//
//                    if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                        MediaFormat mediaFormat = mediaCodec.getOutputFormat();
//                        mTrackIndex = mediaMuxer.addTrack(mediaFormat);
//                        mediaMuxer.start();
//                    }
//
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            } else {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}
