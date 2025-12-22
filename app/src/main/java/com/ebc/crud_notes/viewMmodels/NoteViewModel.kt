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
import com.ebc.crud_notes.network.ApiClient
import com.ebc.crud_notes.repository.NotesRepository
import com.ebc.crud_notes.states.ImagePathState
import com.ebc.crud_notes.states.TextFieldState
import com.google.gson.internal.GsonBuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import com.ebc.crud_notes.BuildConfig


class NoteViewModel(application: Application): ViewModel() {
    private val repository: NotesRepository

    private val banxicoApi = ApiClient.banxicoApi

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

    private val apiRest = ApiClient.geetQuoteApi

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
                    text = ""
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
                coroutineScope.launch(Dispatchers.IO) {
                event.id?.let {repository.delete(it)}
                    _eventFlow.emit(Event.ShowSnackBar("Nota eliminada"))
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
                    repository.insert(Note(text = text.value.text, update = Date(), imagePath = imagePath.value.path))
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

            is Event.FireQuote -> {
                coroutineScope.launch(Dispatchers.IO) {
                    val quote = fetchRandomQuote()
                    _eventFlow.emit(Event.FireQuote(quote))
                }
            }

            is Event.FetchDollar -> {
                coroutineScope.launch(Dispatchers.IO) {
                    val result = fetchDollarFix()
                    _eventFlow.emit(Event.FetchDollar(result))
                }
            } else -> Unit
        }
    }

    private suspend fun fetchRandomQuote(): String{
        val response = withContext(Dispatchers.IO){
            try {
                apiRest.getRandomQuote()
            } catch (error: Exception) {
                error.printStackTrace()
                "Error al obtener la frase"
            }
        }

        return response
    }

    private suspend fun fetchDollarFix(): String {
        return try {
            val token = BuildConfig.BANXICO_TOKEN

            val response = banxicoApi.getDatoOportuno(
                idSerie = "SF43718",
                token = token
            )

            val dato = response.bmx.series.firstOrNull()?.datos?.firstOrNull()

            if (dato == null) {
                "No se encontró el dato del tipo de cambio."
            } else {
                "Así está el dolar el ${dato.fecha}: ${dato.dato} MXN"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al consultar a Banxico."
        }
    }



}