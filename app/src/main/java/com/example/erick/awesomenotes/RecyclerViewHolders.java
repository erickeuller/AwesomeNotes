package com.example.erick.awesomenotes;

/**
 * Created by erick on 03/11/16.
 */
import android.content.Context;
import android.graphics.drawable.Icon;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class RecyclerViewHolders extends RecyclerView.ViewHolder{
    private static final String TAG = RecyclerViewHolders.class.getSimpleName();
    public ImageView editIcon;
    public EditText noteContent;
    public ImageView deleteIcon;
    private List<Note> noteObj;
    public RecyclerViewHolders(final View itemView, final List<Note> nodeObj) {
        super(itemView);
        this.noteObj = nodeObj;
        noteContent = (EditText) itemView.findViewById(R.id.node_title);
        editIcon = (ImageView)itemView.findViewById(R.id.node_edit);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String note = noteContent.getText().toString();

                if (!noteContent.isEnabled()) {
                    noteContent.setEnabled(true);
                    return;
                }

                String taskTitle = nodeObj.get(getAdapterPosition()).getNoteContent();
                noteContent.setEnabled(false);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query applesQuery = ref.orderByChild("noteContent").equalTo(taskTitle);
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().setValue(new Note(note));
                            nodeObj.get(getAdapterPosition()).setNoteContent(note);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });
            }
        });
        deleteIcon = (ImageView)itemView.findViewById(R.id.node_delete);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*                Toast.makeText(v.getContext(), "Delete icon has been clicked", Toast.LENGTH_LONG).show();*/
                String taskTitle = nodeObj.get(getAdapterPosition()).getNoteContent();
                Log.d(TAG, "Task Title " + taskTitle);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query applesQuery = ref.orderByChild("noteContent").equalTo(taskTitle);
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });
            }
        });
    }
}
