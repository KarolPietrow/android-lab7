package pl.karolpietrow.kp7

import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.karolpietrow.kp7.ui.theme.KP7Theme
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KP7Theme {
                val viewModel: MyViewModel = viewModel()
                MainScreen(viewModel)
            }
        }
    }
}


@Composable
fun MainScreen(viewModel: MyViewModel) {
    val books by viewModel.books.collectAsState()
    val context = LocalContext.current
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var minID by remember { mutableStateOf("5")}
    var maxID by remember { mutableStateOf("15") }

    DisposableEffect(Unit) {
        val receiver = MyBroadcastReceiver { newData ->
            newData.forEach { book ->
                if (book.title != "N/A") {
                    viewModel.addBook(book)
                } else {
                    Toast.makeText(context, "Nieprawidłowe ID książki: ${book.id}.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val intentFilter = IntentFilter("pl.karolpietrow.KP7.DATA_DOWNLOADED")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, intentFilter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Moje książki",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "ID",
                modifier = Modifier.weight(0.15f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tytuł książki",
                modifier = Modifier.weight(0.75f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Liczba słów",
                modifier = Modifier.weight(0.25f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Liczba liter",
                modifier = Modifier.weight(0.25f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Najczęstsze słowo",
                modifier = Modifier.weight(0.25f),
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .weight(1f)
        ) {
            items(books) {
                    book -> Row(
                modifier = Modifier
                    .clickable { selectedBook = book }
            ) {
                Text(text = "${book.id}", modifier = Modifier.weight(0.15f))
                Text(text = book.title, modifier = Modifier.weight(0.75f))
                Text(text = "${book.wordCount}", modifier = Modifier.weight(0.25f))
                Text(text = "${book.charCount}", modifier = Modifier.weight(0.25f))
                Text(text = book.mostCommonWord, modifier = Modifier.weight(0.25f))
            }
            }
        }
        Column (
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "ID książek do pobrania",
                fontWeight = FontWeight.Bold
            )
            Row{
                TextField(
                    value = minID,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            minID = input
                        }
                    },
                    label = { Text("Od") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(1.dp)
                )
                TextField(
                    value = maxID,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            maxID = input
                        }
                    },
                    label = { Text("Do") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(1.dp)
                )
            }
            Row {
                Button(
                    onClick = {
                        if (minID.toInt()>maxID.toInt()) {
                            Toast.makeText(context, "Nieprawidłowy zakres!", Toast.LENGTH_SHORT).show()
                        } else {
                            val intent = Intent(context, BookService::class.java).apply {
                                putExtra("minID", minID.toInt())
                                putExtra("maxID", maxID.toInt())
                            }
                            context.startService(intent)
                        }
                    },
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text("Pobierz książki")
                }
                Button(
                    onClick = {
                        viewModel.clearList()
                    },
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text("Wyczyść listę")
                }
            }
        }
    }
    if (selectedBook != null) {
        Toast.makeText(context, "Wczytanie zawartości...", Toast.LENGTH_SHORT).show()
        val contentString = File(selectedBook!!.content).readText()
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = { selectedBook = null },
            confirmButton = {
                Button(onClick = { selectedBook = null }) {
                    Text("Zamknij")
                }
            },
            title = {
                Text(text = selectedBook!!.title)
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = contentString)
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // Umożliwia pełną szerokość
            )
        )
    }
}





