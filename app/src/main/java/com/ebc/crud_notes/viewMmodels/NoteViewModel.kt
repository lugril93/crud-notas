package com.ebc.crud_notes.viewMmodels

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebc.crud_notes.db.NotesDatabase
import com.ebc.crud_notes.db.models.Note
import com.ebc.crud_notes.repository.NotesRepository
import com.ebc.crud_notes.states.ImagePathState
import com.ebc.crud_notes.states.TextFieldState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Date

class NoteViewModel(application: Application): ViewModel() {
    private val repository: NotesRepository

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _text = mutableStateOf(TextFieldState())
    val text: State<TextFieldState> =_text

    private val _imagePath = mutableStateOf(ImagePathState())
    val imagePath: State <ImagePathState> = _imagePath

    //TODO: Pendiente resto de los formularios

    val all: LiveData<List<Note>>

    var openDialog by mutableStateOf(false)

    private var currentId: Int? = null

        init {
            val db = NotesDatabase.getInstance(application)
            val dao = db.notesDao()
            repository = NotesRepository(dao)
            all = repository.all()
        }

    private fun load(id: Int?) {
        viewModelScope.launch {
            if (id != null) {
                repository.findById(id).also {
                    note ->
                        currentId = note.id
                        _text.value = text.value.copy(text = note.text)
                    _imagePath.value = imagePath.value.copy(
                        path = note.imagePath
                    )
                }
            } else {
                currentId = null
                _text.value = text.value.copy(
                    text = "Escribe aqui el texto"
                )
                _imagePath.value = imagePath.value.copy(
                    path = null
                )
            }
        }
    }

    fun onEvent(event: Event){
        when (event){
            is Event.SetText -> {
                _text.value = text.value.copy(
                    text = event.text
                )
            }

            is Event.CloseDialog -> {
                openDialog = false
            }
            is Event.Delete -> {
                event.id?.let {
                    repository.delete(it)
                }
            }
            is Event.Load -> {
                load(event.id)
                openDialog = true
            }
            is Event.OpenDialog -> {
                load(currentId)
                openDialog = true
            }
            Event.Save -> {
                if(currentId !=null) {
                    repository.update(Note(
                        id = currentId,
                        text = text.value.text,
                        update = Date(),
                        imagePath = imagePath.value.path))
                } else {
                    repository.insert(Note(text = text.value.text, update = Date(), imagePath = null))
                }

                openDialog = false

                coroutineScope.launch(Dispatchers.IO) {
                    _eventFlow.emit(Event.Save)
                }
            }
            is Event.SetImagePath -> {
                _imagePath.value = imagePath.value.copy(
                    path = event.imagePath
                )
            }

        }
    }


}