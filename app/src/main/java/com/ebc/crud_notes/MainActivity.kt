package com.ebc.crud_notes

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ebc.crud_notes.utils.saveImageToInternalStorage
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
fun NoteDialog(openDialog: Boolean = true, text: String = "Ejemplo", imagePath: String? = null, onEvent: (Event) -> Unit ={}) {

    var imagePathDialog by remember { mutableStateOf<String?>(null) }
    imagePathDialog = imagePath

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri: Uri? ->
        uri?.let {
            val path = saveImageToInternalStorage(context, it)
            if (path != null) {
                imagePathDialog = path

                onEvent(Event.SetImagePath(path))
            }
        }
    }

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
                    Spacer(Modifier.height(20.dp))

                    if(imagePath != null) {
                        val bitmap by remember(imagePathDialog){
                            mutableStateOf(BitmapFactory.decodeFile(imagePathDialog))
                        }

                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Imagen de la nota",
                                modifier = Modifier.size(150.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop

                            )
                        }
                    } else {
                        Text(text = "Nota sin imagen")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {imagePickerLauncher.launch("image/*")}
                    ) {
                        Text(text="Seleccionar imagen")
                    }


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
    imagePath: String? = null,
    onEvent: (Event) -> Unit = {}
) {
    val localeEsp = Locale("es","MX")
    val formatter = SimpleDateFormat("dd/MMM/yy hh:mm a", localeEsp)
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ){
        Button(
            onClick = {
                onEvent(Event.FireQuote(null))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "Frase Geek motivacional"
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text= "Frase Geek motivacional")
        }

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

    NoteDialog(openDialog = openDialog, text = text, imagePath = imagePath, onEvent = onEvent)
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
                is Event.SetImagePath -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Imagen guardada"
                        )
                    }
                }

                is Event.FireQuote -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            event.quote?: ""
                        )
                    }
                }
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
            imagePath = viewModel.imagePath.value.path,
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