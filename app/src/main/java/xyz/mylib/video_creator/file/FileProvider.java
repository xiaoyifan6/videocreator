package xyz.mylib.video_creator.file;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 *     author : yangzhi.ou
 *     e-mail : xxx@xx
 *     time   : 2019/05/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public final class FileProvider implements Iterable<FileProvider.FileData> {

    public static final int TYPE_DIR = 1;
    public static final int TYPE_FILE = 2;

    private final String mRootPath;
    private final int mType;
    private final String mOldPath;

    private String curPath;
    private boolean mFilter;
    private List<FileData> mFileDataList;


    public static FileProvider newInstance(String oldPath, int type) {
        File rootFile = new File("/");
        if (rootFile.exists() && rootFile.list() != null) {
            return new FileProvider(type, oldPath, rootFile.getPath());
        } else {
            rootFile = new File("/mnt/");
            if (rootFile.exists() && rootFile.list() != null) {
                return new FileProvider(type, oldPath, rootFile.getPath());
            } else {
                throw new UnsupportedOperationException("");
            }
        }
    }

    private FileProvider(int type, String oldPath, String rootPath) {
        this.mType = type;
        this.mOldPath = oldPath;
        this.mRootPath = rootPath;
        this.curPath = mRootPath;
        this.mFileDataList = new ArrayList<>();
        this.mFilter = true;
        this.setData();
    }


    public List<FileData> setFilter(boolean filter) {
        this.mFilter = filter;
        setData();
        return mFileDataList;
    }

    public FileData getFileData(File file, FilenameFilter filter, String info) {
        boolean isDir = file.isDirectory();
        return new FileData(
                file.getName(),
                isDir,
                file.getPath(),
                mType == (isDir ? 1 : 2),
                info);
    }

    private String getSizeStr(long size) {
        if (size >= 1024 * 1024 * 1024) {
            return String.format("%.2f G", (float) size / 1073741824L);
        } else if (size >= 1024 * 1024) {
            return String.format("%.2f M", (float) size / 1048576L);
        } else if (size >= 1024) {
            return String.format("%.2f K", (float) size / 1024);
        }
        return size + "B";
    }

    @SuppressLint("SimpleDateFormat")
    private void setData() {
        this.mFileDataList.clear();
        FilenameFilter filenameFilter = (dir, name) -> !name.startsWith(".");
        File[] files = mFilter ? new File(curPath).listFiles(filenameFilter) : new File(curPath).listFiles();

        if (files != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            for (File file : files) {
                boolean isDir = file.isDirectory();
                String info;

                if (isDir) {
                    int size = 0;
                    String[] names = mFilter ? file.list(filenameFilter) : file.list();
                    if (names != null) {
                        size = names.length;
                    }
                    if (mType == TYPE_FILE && size == 0) continue;
                    info = size + "项 | " + dateFormat.format(new Date(file.lastModified()));
                    mFileDataList.add(getFileData(file, filenameFilter, info));
                } else {
                    info = getSizeStr(file.length()) +
                            " | " +
                            dateFormat.format(new Date(file.lastModified()));
                    mFileDataList.add(getFileData(file, filenameFilter, info));
                }

            }
        }

        Collections.sort(mFileDataList, (o1, o2) -> {
            if (o1.isDir == o2.isDir) return o1.name.compareTo(o2.name);
            return o2.isDir ? 1 : -1;
        });

        if (isRoot()) {
            if (mOldPath != null && !mOldPath.equals(mRootPath)) {
                File oldFile = new File(mOldPath);
                if (oldFile.exists()) {
                    mFileDataList.add(0, new FileData(oldFile.getName(), true, oldFile.getPath(), false, "[上次打开目录] " + oldFile.getPath()));
                }
            }
        } else {
            String realPath = new File(curPath).getParent();
            mFileDataList.add(0, new FileData("../", true, realPath, false, "[返回上一级] " + realPath));
        }

    }

    public List<FileData> list() {
        return mFileDataList;
    }

    public boolean isRoot() {
        return curPath.equalsIgnoreCase(mRootPath);
    }

    public List<FileData> gotoParent() {
        if (!isRoot()) {
            curPath = new File(curPath).getParent();
            setData();
        }
        return mFileDataList;
    }

    public List<FileData> gotoChild(int position) {
        if (position >= 0 && position < mFileDataList.size() && mFileDataList.get(position).isDir) {
            curPath = mFileDataList.get(position).realPath;
        }
        setData();
        return mFileDataList;
    }

    public FileData getItem(int position) {
        return mFileDataList.get(position);
    }

    public int size() {
        return mFileDataList.size();
    }

    public String getCurPath() {
        return curPath;
    }

    public int getType() {
        return mType;
    }

    @NonNull
    @Override
    public Iterator<FileData> iterator() {
        return mFileDataList.iterator();
    }

    public static class FileData {
        /**
         * 文件名称
         */
        public final String name;
        /**
         * 是否为文件夹
         */
        public final boolean isDir;
        /**
         * 真实路径
         */
        public final String realPath;
        /**
         * 是否可选择
         */
        public final boolean selectable;
        /**
         * 文件信息
         */
        public final String info;

        public FileData(String name, boolean isDir, String realPath, boolean selectable, String info) {
            this.name = name;
            this.isDir = isDir;
            this.realPath = realPath;
            this.selectable = selectable;
            this.info = info;
        }

        @Override
        public int hashCode() {
            return realPath.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof FileData) return ((FileData) obj).realPath.equals(realPath);
            if (obj instanceof String) return obj.equals(realPath);
            return false;
        }
    }
}
