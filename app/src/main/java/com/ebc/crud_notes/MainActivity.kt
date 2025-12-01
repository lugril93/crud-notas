package com.ebc.crud_notes

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrudnotesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
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
                    modifier = Modifier.padding(20.dp)) {
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



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun CrudScreen (
    allNotes: List<Note>,
    openDialog: Boolean,
    text: String,
    onEvent: (Event) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ){
        LazyColumn {
            items(allNotes){
                note ->
                ListItem(
                    headlineContent = {Text(note.text) },
                    supportingContent = { Text(note.update.toString())},
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CrudnotesTheme {
        Greeting("Android")
    }
}