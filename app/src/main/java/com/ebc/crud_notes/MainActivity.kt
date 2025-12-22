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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material3.Switch
import androidx.compose.runtime.saveable.rememberSaveable



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(false) }
            CrudnotesTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crud(
                        darkMode = darkMode,
                        onToggleDarkMode = { darkMode = !darkMode}
                    )
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
    onEvent: (Event) -> Unit = {},
    darkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val localeEsp = Locale("es","MX")
    val formatter = SimpleDateFormat("dd/MMM/yy hh:mm a", localeEsp)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteIdToDelete by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (darkMode) "Modo oscuro" else "Modo claro",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = darkMode,
                onCheckedChange = { onToggleDarkMode() }
            )
        }
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

        Button(
            onClick = {onEvent(Event.FetchDollar(null))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Rounded.AttachMoney,
                contentDescription = "Dólar hoy"
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Tipo de cambio dólar")
        }

        LazyColumn {
            items(allNotes){
                note ->
                ListItem(
                    headlineContent = {Text(note.text) },
                    supportingContent = { Text(formatter.format(note.update))},
                    trailingContent = {
                        IconButton(
                            onClick = {
                                noteIdToDelete = note.id
                                showDeleteDialog = true
                            }
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

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                noteIdToDelete = null
            },
            title = { Text("Eliminar nota") },
            text = { Text("¿Seguro que deseas eliminar esta nota?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteIdToDelete?.let { onEvent(Event.Delete(it)) }
                        showDeleteDialog = false
                        noteIdToDelete = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        noteIdToDelete = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }
    NoteDialog(openDialog = openDialog, text = text, imagePath = imagePath, onEvent = onEvent)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

@Composable
fun CrudScreenSetup(
    viewModel: NoteViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit
){
    val allNotes by viewModel.all.observeAsState(listOf())
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* no necesitas manejar nada */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }



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
                    val msg = "Se ha guardado una nota"
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                    showSavedNotification(context,msg)
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

                is Event.FetchDollar -> {
                    val msg = event.result ?: "No se pudo obtener el tipo de cambio"
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            event.result?:""
                        )
                    }
                    showDollarNotification(context,msg)
                }
                is Event.ShowSnackBar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
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
            onEvent = {viewModel.onEvent(it)},
            darkMode = darkMode,
            onToggleDarkMode = onToggleDarkMode
        )
    }
}

@Composable
fun Crud(
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val owner = LocalViewModelStoreOwner.current

    owner?.let {
        val viewModel: NoteViewModel = viewModel(
            it,
            "NoteViewModel",
            NoteViewModelFactory(
                LocalContext.current.applicationContext as Application
            )
        )

        CrudScreenSetup(
            viewModel = viewModel,
            darkMode = darkMode,
            onToggleDarkMode = onToggleDarkMode
        )
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

private const val NOTES_CHANNEL_ID = "notes_channel"
fun createNotificationChannel(context: Context){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val channel = NotificationChannel(
            NOTES_CHANNEL_ID,
            "Notas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de guardado de notas"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

    }
}

fun showSavedNotification(context: Context, message: String){
    val notification = NotificationCompat.Builder(context, NOTES_CHANNEL_ID)
        .setSmallIcon(R.drawable.noti)
        .setContentTitle("Nota Guardada")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .build()

    val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
    manager?.notify((System.currentTimeMillis() % 100000).toInt(), notification)
}

fun showDollarNotification(context: Context, result: String) {
    val notification = NotificationCompat.Builder(context, NOTES_CHANNEL_ID)
        .setSmallIcon(R.drawable.noti)
        .setContentTitle("Se consultó el dólar hoy")
        .setContentText(result)
        .setStyle(
            NotificationCompat.BigTextStyle().bigText(result)
        )
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .build()

    val manager = ContextCompat.getSystemService(
        context,
        NotificationManager::class.java
    )

    manager?.notify(
        (System.currentTimeMillis() % 100000).toInt(),
        notification
    )
}
