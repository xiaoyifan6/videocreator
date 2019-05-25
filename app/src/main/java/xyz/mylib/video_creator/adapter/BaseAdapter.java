package xyz.mylib.video_creator.adapter;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<CommonHolder> implements ListAdapter, SpinnerAdapter {

    protected final List<T> data;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private CharSequence[] charSequences = new CharSequence[0];

    public BaseAdapter(List<T> data){
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataChanged() {
        mDataSetObservable.notifyChanged();
        notifyDataSetChanged();
    }

    public void notifyDataInvalidated() {
        mDataSetObservable.notifyInvalidated();
        notifyDataInvalidated();
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

    @Override
    public Object getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEmpty() {
        return this.getItemCount() == 0;
    }

    public void setAutofillOptions( CharSequence[] chars) {
        charSequences = chars;
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return charSequences;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
