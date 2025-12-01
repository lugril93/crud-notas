package com.ebc.crud_notes.repository

import androidx.lifecycle.LiveData
import com.ebc.crud_notes.db.NotesDao
import com.ebc.crud_notes.db.models.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesRepository (private val notesDao: NotesDao){

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insert(note: Note){
        coroutineScope.launch(Dispatchers.IO) {
            notesDao.insert(note)
        }
    }

    fun update (note: Note){
        coroutineScope.launch(Dispatchers.IO) {
            notesDao.update(note)
        }
    }

    fun all(): LiveData<List<Note>>{
        return notesDao.all()
    }

    suspend fun findById(id: Int) : Note {
        return notesDao.findById(id)
    }

    fun delete(id:Int) {
        coroutineScope.launch(Dispatchers.IO){
            notesDao.delete(id)
        }
    }

}