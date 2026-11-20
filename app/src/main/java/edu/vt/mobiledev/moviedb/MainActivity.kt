package edu.vt.mobiledev.moviedb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import edu.vt.mobiledev.moviedb.ui.theme.MovieDBTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MovieViewModel by viewModels()
    //built-in Gemini AI helped with using the API key to work with the application
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tmdbApiKey = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4NDlhZjI5MDYzZWIwM2M0Y2U0Y2Y5ZTk0ODI3OGNhZSIsIm5iZiI6MTc5NTA0MDI2My45MjQsInN1YiI6IjZhZmUyNDA3YzY3NGE2NGU4NTgxYmQzNyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.Tg3gJ9bDDTXc1GETs-egYiQYiVBaSFqqHIJg1nKvwDE"

        viewModel.loadMovies(tmdbApiKey)

        setContent {
            MovieDBTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MovieAppRoot(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
