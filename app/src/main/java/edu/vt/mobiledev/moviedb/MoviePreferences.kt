package edu.vt.mobiledev.moviedb

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "movie_prefs")

class MoviePreferences(private val context: Context) {
    companion object {
        val VIEW_MODE_KEY = booleanPreferencesKey("is_grid_view")
        val SORT_OPTION_KEY = stringPreferencesKey("sort_option")
        val FILTER_OPTION_KEY = stringPreferencesKey("filter_option")
        val LIKED_IDS_KEY = stringSetPreferencesKey("liked_movie_ids")
    }

    val viewModeFlow: Flow<Boolean> = context.dataStore.data.map { it[VIEW_MODE_KEY] ?: false }
    val sortOptionFlow: Flow<String> = context.dataStore.data.map { it[SORT_OPTION_KEY] ?: "Title" }
    val filterOptionFlow: Flow<String> = context.dataStore.data.map { it[FILTER_OPTION_KEY] ?: "All" }
    val likedIdsFlow: Flow<Set<String>> = context.dataStore.data.map { it[LIKED_IDS_KEY] ?: emptySet() }

    suspend fun saveViewMode(isGrid: Boolean) {
        context.dataStore.edit { it[VIEW_MODE_KEY] = isGrid }
    }

    suspend fun saveSortOption(sort: String) {
        context.dataStore.edit { it[SORT_OPTION_KEY] = sort }
    }

    suspend fun saveFilterOption(filter: String) {
        context.dataStore.edit { it[FILTER_OPTION_KEY] = filter }
    }

    suspend fun toggleLike(movieId: String, isLiked: Boolean) {
        context.dataStore.edit { prefs ->
            val currentLikes = prefs[LIKED_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (isLiked) currentLikes.add(movieId) else currentLikes.remove(movieId)
            prefs[LIKED_IDS_KEY] = currentLikes
        }
    }
}
