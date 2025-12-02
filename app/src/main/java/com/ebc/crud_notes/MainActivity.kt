package com.ebc.crud_notes

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ebc.crud_notes.db.models.Note
import com.ebc.crud_notes.ui.theme.CrudnotesTheme
import com.ebc.crud_notes.viewMmodels.Event
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ebc.crud_notes.viewMmodels.NoteViewModel
import com.ebc.crud_notes.viewMmodels.NoteViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.SimpleFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrudnotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crud()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDialog(openDialog: Boolean = true, text: String = "Ejemplo", onEvent: (Event) -> Unit ={}) {

    if(openDialog) {
        Dialog(
            onDismissRequest = {
                onEvent(Event.CloseDialog)
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ){
            Surface(modifier = Modifier.fillMaxSize()){
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = text,
                        onValueChange = {
                            onEvent(Event.SetText(it))
                        },
                        label = { Text("Texto:")},
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        )
                    )
                }
                Box(modifier = Modifier.padding(20.dp)){
                    TextButton(
                        onClick = {onEvent(Event.CloseDialog)},
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(text = "Cancelar")
                    }
                    TextButton(
                        onClick = {onEvent(Event.Save)},
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(text = "Confirmar")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CrudScreen (
    allNotes: List<Note> =listOf(
        Note(id = null, text = "Nota 1", update = Date(), imagePath = null),
        Note(id = null, text = "Nota 2", update = Date(), imagePath = null),
        Note(id = null, text = "Nota 3", update = Date(), imagePath = null)
    ),
    openDialog: Boolean = false,
    text: String ="",
    onEvent: (Event) -> Unit = {}
) {
    val localeEsp = Locale("es","MX")
    val formatter = SimpleDateFormat("dd/MMM/yy hh:mm a", localeEsp)
    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ){
        LazyColumn {
            items(allNotes){
                note ->
                ListItem(
                    headlineContent = {Text(note.text) },
                    supportingContent = { Text(formatter.format(note.update))},
                    trailingContent = {
                        IconButton(
                            onClick = {onEvent(Event.Delete(note.id))}
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Borrar nota: ${note.id}")
                        }
                    },
                    leadingContent = {
                        IconButton(
                            onClick = {onEvent(Event.Load(note.id))}
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Borrar nota: ${note.id}")
                        }
                    }
                )

                HorizontalDivider()
            }
        }
    }

    NoteDialog(openDialog = openDialog, text = text, onEvent = onEvent)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

@Composable
fun CrudScreenSetup(
    viewModel: NoteViewModel
){
    val allNotes by viewModel.all.observeAsState(listOf())

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(snackbarHostState) {
        viewModel.eventFlow.collectLatest {
            event ->
            when(event){
                Event.CloseDialog -> TODO()
                is Event.Delete -> TODO()
                is Event.Load -> TODO()
                Event.OpenDialog -> TODO()
                Event.Save -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Nota guardada"
                        )
                    }
                }
                is Event.SetText -> TODO()
            }
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(Event.Load(id = null))
                }
            ) { Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar nota"
            )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        CrudScreen(
            allNotes = allNotes,
            openDialog = viewModel.openDialog,
            text = viewModel.text.value.text,
            onEvent = {viewModel.onEvent(it)}
        )
    }
}

@Composable
fun Crud() {
    val owner = LocalViewModelStoreOwner.current

    owner?.let {
        val viewModel: NoteViewModel = viewModel(
            it,
            "NoteViewModel",
            NoteViewModelFactory(
                LocalContext.current.applicationContext as Application
            )
        )

        CrudScreenSetup(viewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CrudnotesTheme {
        Greeting("Android")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}