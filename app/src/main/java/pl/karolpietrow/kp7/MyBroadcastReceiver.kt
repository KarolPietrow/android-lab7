package pl.karolpietrow.kp7

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBroadcastReceiver(private val onDataReceived: (List<Book>) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "pl.karolpietrow.KP7.DATA_DOWNLOADED") {
            val id = intent.getIntExtra("id", -1)
            val title = intent.getStringExtra("title") ?: "N/A"
            val content = intent.getStringExtra("content") ?: ""
            val wordCount = intent.getIntExtra("wordCount", 0)
            val charCount = intent.getIntExtra("charCount", 0)
            val mostCommonWord = intent.getStringExtra("mostCommonWord") ?: "N/A"

            Log.d("MyBroadcastReceiver", "Data downloaded")
            val bookData = Book(id, title, content, wordCount, charCount, mostCommonWord)

            onDataReceived(listOf(bookData))
        }
    }
}