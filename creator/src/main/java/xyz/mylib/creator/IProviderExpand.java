package xyz.mylib.creator;

/**
 * 提供数据方
 * <p>
 * 拓展 方便垃圾回收以及一些后续处理
 *
 * @param <T>
 */
public interface IProviderExpand<T> extends IProvider<T> {
    void prepare();

    void finish();

    void finishItem(T item);
}
