package xyz.mylib.video_creator.adapter;

public interface MultiAdapterSupport<T> {
    /**
     * 获取多条目View类型的数量
     */
    int getViewTypeCount();

    /**
     * 根据数据，获取多条目布局ID
     */
    int getLayoutId(T data);

    /**
     * 根据数据，获取多条目的ItemViewType
     */
    int getItemViewType(T data);

    /**
     * 是否合并条目-->>使用RecyclerView时，如果无效，请用原生的RecyclerView
     */
    boolean isSpan(T data);
}
