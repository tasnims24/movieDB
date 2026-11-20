package edu.vt.mobiledev.moviedb

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = MoviePreferences(application)

    var movies by mutableStateOf<List<Movie>>(emptyList())
        private set
    var isGridView by mutableStateOf(false)
        private set

    @set:JvmName("internalSetSortOption")
    var sortOption by mutableStateOf("Title")
        private set

    @set:JvmName("internalSetFilterOption")
    var filterOption by mutableStateOf("All")
        private set

    var likedMovieIds by mutableStateOf(setOf<String>())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var currentScreen by mutableStateOf(Screen.LIST)
        private set
    var selectedMovieId by mutableStateOf<Int?>(null)
        private set

    enum class Screen { LIST, DETAIL }

    init {
        viewModelScope.launch {
            launch { prefs.viewModeFlow.collect { isGridView = it } }
            launch { prefs.sortOptionFlow.collect { sortOption = it } }
            launch { prefs.filterOptionFlow.collect { filterOption = it } }
            launch {
                prefs.likedIdsFlow.collect {
                    likedMovieIds = it
                    updateMovieLikedStatus()
                }
            }
        }
    }

    fun navigateTo(screen: Screen, movieId: Int? = null) {
        selectedMovieId = movieId
        currentScreen = screen
    }

    fun toggleViewMode() {
        viewModelScope.launch { prefs.saveViewMode(!isGridView) }
    }

    fun setSortOption(sort: String) {
        viewModelScope.launch { prefs.saveSortOption(sort) }
    }

    fun setFilterOption(filter: String) {
        viewModelScope.launch { prefs.saveFilterOption(filter) }
    }

    fun toggleLike(movie: Movie) {
        viewModelScope.launch {
            val newLikedState = !movie.isLiked
            prefs.toggleLike(movie.id.toString(), newLikedState)
        }
    }

    private fun updateMovieLikedStatus() {
        movies = movies.map { movie ->
            movie.copy(isLiked = likedMovieIds.contains(movie.id.toString()))
        }
    }

    val displayedMovies: List<Movie>
        get() {
            val filtered = when (filterOption) {
                "Liked" -> movies.filter { it.isLiked }
                else -> movies
            }
            return when (sortOption) {
                "Release Date" -> filtered.sortedByDescending { it.release_date }
                "User Rating" -> filtered.sortedByDescending { it.vote_average }
                else -> filtered.sortedBy { it.title }
            }
        }

    //Gemini built-in AI used to help with the filtering by title, release date or user rating
    fun loadMovies(apiKey: String) {
        if (apiKey.isBlank() || apiKey == "YOUR_TMDB_BEARER_TOKEN_HERE") return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isLoading = true }
            val allMovies = mutableListOf<Movie>()

            // Fetch 3 pages of movies
            listOf(1, 2, 3).forEach { pageNum ->
                try {
                    val isBearerToken = apiKey.startsWith("eyJ")
                    val urlString = if (isBearerToken) {
                        "https://api.themoviedb.org/3/movie/now_playing?page=$pageNum"
                    } else {
                        "https://api.themoviedb.org/3/movie/now_playing?api_key=$apiKey&page=$pageNum"
                    }

                    val connection = URL(urlString).openConnection()
                    if (isBearerToken) {
                        connection.setRequestProperty("Authorization", "Bearer $apiKey")
                    }
                    connection.setRequestProperty("accept", "application/json")

                    val responseString = connection.getInputStream().bufferedReader().use { it.readText() }

                    val jsonObject = JSONObject(responseString)
                    val resultsArray = jsonObject.getJSONArray("results")

                    for (i in 0 until resultsArray.length()) {
                        val item = resultsArray.getJSONObject(i)
                        val lang = item.getString("original_language")

                        // Filter for English-language movies as specified in requirements
                        if (lang == "en") {
                            allMovies.add(
                                Movie(
                                    id = item.getInt("id"),
                                    title = item.getString("title"),
                                    overview = item.optString("overview"),
                                    poster_path = item.optString("poster_path"),
                                    backdrop_path = item.optString("backdrop_path"),
                                    release_date = item.optString("release_date"),
                                    vote_average = item.getDouble("vote_average"),
                                    vote_count = item.getInt("vote_count"),
                                    original_language = lang
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                movies = allMovies.map { it.copy(isLiked = likedMovieIds.contains(it.id.toString())) }
                isLoading = false
            }
        }
    }
}
