package xyz.mylib.video_creator;

import java.io.File;

import xyz.mylib.creator.handler.CreatorExecuteResponseHander;
import xyz.mylib.video_creator.loading.LoadingDialog;
import xyz.mylib.video_creator.util.FileUtil;

/**
 * <pre>
 *     author : yangzhi.ou
 *     e-mail : xxx@xx
 *     time   : 2019/05/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MergyHandler implements CreatorExecuteResponseHander {

    LoadingDialog mLoadingDialog;
    String sPath;
    String dPath;

    public MergyHandler(LoadingDialog loadingDialog,
                        String sPath,
                        String dPath) {
        this.mLoadingDialog = loadingDialog;
        this.sPath = sPath;
        this.dPath = dPath;
    }

    @Override
    public void onSuccess(Object message) {

    }

    @Override
    public void onProgress(Object message) {
        mLoadingDialog.setLoadingInfo(message.toString());
    }

    @Override
    public void onFailure(Object message) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish() {
        mLoadingDialog.dismiss();
        File dfile = new File(dPath);
        if (dfile.exists()) {
            dfile.delete();
        }
//        new File(sPath).renameTo(dfile);
        FileUtil.copyFile(sPath, dPath);
    }
}
