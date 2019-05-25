package xyz.mylib.video_creator.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommonAdapter<T> extends BaseAdapter<T> implements Filterable {
    private Context mContext;
    private int mLayoutId;
    private MultiAdapterSupport<T> mSupport;
    private int mPosition;
    private ItemDataListener<T> listener;
    private Filter mFilter;

    public CommonAdapter(Context context, List<T> data, int layoutId, ItemDataListener<T> listener) {
        super(data == null ? new ArrayList<>() : new ArrayList<>(data));
        this.mContext = context;
        this.mLayoutId = layoutId;
        this.listener = listener;
    }

    public CommonAdapter(Context context, List<T> data, MultiAdapterSupport<T> support, ItemDataListener<T> listener) {
        this(context, data, 0, listener);
        this.mSupport = support;
    }

    @NonNull
    @Override
    public CommonHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        // 如果是多条目，viewType就是布局ID
        View view;
        if (mSupport != null) {
            int layoutId = mSupport.getLayoutId(data.get(mPosition));
            view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        }

        CommonHolder holder = new CommonHolder(view);
        return holder;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        if (mSupport == null || recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            // 如果设置合并单元格就占用SpanCount那个多个位置
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mSupport.isSpan(data.get(position))) {
                        return gridLayoutManager.getSpanCount();
                    } else if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull CommonHolder holder) {
        if (mSupport == null) {
            return;
        }
        int position = holder.getLayoutPosition();
        // 如果设置合并单元格
        if (mSupport.isSpan(data.get(position))) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CommonHolder commonHolder, int i) {
        if (listener != null) {
            listener.setItemData(commonHolder, data.get(i), i);
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonHolder holder;
        if (convertView == null) {
            int layoutId = mLayoutId;
            // 多条目的
            if (mSupport != null) {
                layoutId = mSupport.getLayoutId(data.get(position));
            }
            // 创建ViewHolder
            holder = createListHolder(parent, layoutId);
        } else {
            holder = (CommonHolder) convertView.getTag();
            // 防止失误，还要判断
            if (mSupport != null) {
                int layoutId = mSupport.getLayoutId(data.get(position));
                // 如果布局ID不一样，又重新创建
                if (layoutId != holder.getLayoutId()) {
                    // 创建ViewHolder
                    holder = createListHolder(parent, layoutId);
                }
            } else {
                holder = createListHolder(parent, mLayoutId);
            }
        }
        // 绑定View的数据
        if (listener != null) {
            listener.setItemData(holder, data.get(position), position);
        }
        return holder.itemView;
    }

    /**
     * 创建ListView的Holer
     */
    @NonNull
    private CommonHolder createListHolder(ViewGroup parent, int layoutId) {
        CommonHolder holder;
        View itemView = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        holder = new CommonHolder(itemView, layoutId);
        itemView.setTag(holder);
        return holder;
    }

    @Override
    public int getViewTypeCount() {
        // 多条目的
        return mSupport != null ? mSupport.getViewTypeCount() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        mPosition = position;
        // 多条目的
        return mSupport != null ? mSupport.getItemViewType(data.get(position)) : super.getItemViewType(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter != null) return mFilter;
        mFilter = new Filter() {
            List<T> list = new ArrayList<>();
            List<T> source = new ArrayList<>(data);

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterStr = constraint.toString();
                FilterResults results = new FilterResults();
                if (filterStr.isEmpty()) {
                    results.values = source;
                } else {
                    list.clear();
                    for (T bean : source) {
                        //如果字符串是否以指定的前缀开始，那么就是想要的结果
                        if ((bean instanceof String) && ((String) bean).startsWith(filterStr)) {
                            list.add(bean);
                        }
                    }
                    results.values = list;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data.clear();
                data.addAll((Collection<? extends T>) results.values);
                notifyData();
            }
        };

        return mFilter;
    }

//    public void setItemDataListener(ItemDataListener listener) {
//        this.listener = listener;
//    }

    public interface ItemDataListener<T> {//接口

        void setItemData(CommonHolder holder, T data, int position);
    }


    //==========================================数据相关================================================
    public void add(T elem) {
        data.add(elem);
        notifyData();

    }

    public void addAll(List<T> data0) {
        data.addAll(data0);
        notifyData();
    }

    public void addFirst(T elem) {
        data.add(0, elem);
        notifyData();
    }

    public void set(T oldElem, T newElem) {
        set(data.indexOf(oldElem), newElem);
        notifyData();
    }

    public void set(int index, T elem) {
        data.set(index, elem);
        notify();
    }

    public void remove(T elem) {
        data.remove(elem);
        notifyData();
    }

    public void remove(int index) {
        data.remove(index);
        notifyData();
    }

    public void replaceAll(List<T> elem) {
        data.clear();
        data.addAll(elem);
        notifyData();
    }

    /**
     * 清除
     */
    public void clear() {
        data.clear();
        notifyData();
    }

    private void notifyData() {
        notifyDataChanged();
    }

    public List<T> getData() {
        return data;
    }


}
