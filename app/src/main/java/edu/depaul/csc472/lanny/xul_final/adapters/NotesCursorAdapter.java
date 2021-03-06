package edu.depaul.csc472.lanny.xul_final.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.depaul.csc472.lanny.xul_final.R;
import edu.depaul.csc472.lanny.xul_final.sql.DBOpenHelper;

/**
 * Created by Lanny on 11/10/17.
 */

public class NotesCursorAdapter extends CursorAdapter{
    public NotesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(
                R.layout.note_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String noteText = cursor.getString(
                cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));

        int pos = noteText.indexOf(10);
        if (pos != -1){
            noteText = noteText.substring(0, pos) + " ...";
        }

        TextView tv = view.findViewById(R.id.tvNote);
        tv.setText(noteText);

    }
}
