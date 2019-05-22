package xyz.mylib.creator;

/**
 * 提供数据方
 *
 * @param <T>
 */
public interface IProvider<T> {
    boolean hasNext();

    int size();

    T next();
}
