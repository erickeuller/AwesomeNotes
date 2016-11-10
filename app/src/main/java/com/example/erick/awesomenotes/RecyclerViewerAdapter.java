package com.example.erick.awesomenotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/**
 * Created by erick on 03/11/16.
 */
public class RecyclerViewerAdapter extends RecyclerView.Adapter<RecyclerViewHolders> {
    private List<Note> notes;
    protected Context context;
    public RecyclerViewerAdapter(Context context, List<Note> notes) {
        this.notes = notes;
        this.context = context;
    }
    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewHolders viewHolder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note, parent, false);
        viewHolder = new RecyclerViewHolders(layoutView, notes);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        holder.noteContent.setText(notes.get(position).getNoteContent());
    }
    @Override
    public int getItemCount() {
        return this.notes.size();
    }
}
