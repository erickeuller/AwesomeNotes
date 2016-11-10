package com.example.erick.awesomenotes;

/**
 * Created by erick on 03/11/16.
 */
public class Note {
    private String noteContent;

    public Note(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
