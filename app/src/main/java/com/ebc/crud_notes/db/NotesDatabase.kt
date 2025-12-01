package com.ebc.crud_notes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ebc.crud_notes.db.models.Note

@Database(entities = [(Note::class)], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase(){

    abstract fun notesDao() : NotesDao

    companion object {//nos ayuda a que el bloque siempre sea el mismo y no se pierda
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: Context) : NotesDatabase {
            synchronized(this){
                var instance = this.INSTANCE

                if(instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NotesDatabase::class.java,
                        "notes_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    this.INSTANCE = instance

                    return instance
                } else {
                    return instance
                }
            }
        }
    }
}


