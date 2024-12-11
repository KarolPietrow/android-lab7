package pl.karolpietrow.kp7

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyViewModel : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    fun addBook(book: Book) {
        _books.value += book
    }

    fun clearList() {
        _books.value = emptyList()
    }
}