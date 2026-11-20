package edu.vt.mobiledev.moviedb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun MovieAppRoot(viewModel: MovieViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (viewModel.currentScreen) {
            MovieViewModel.Screen.LIST -> MovieListScreen(viewModel)
            MovieViewModel.Screen.DETAIL -> MovieDetailScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(viewModel: MovieViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing Movies") },
                actions = {
                    // Toggle Grid vs Column view
                    Button(onClick = { viewModel.toggleViewMode() }) {
                        Text(if (viewModel.isGridView) "List View" else "Grid View")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // Filter and Sort Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter: All vs Liked
                FilterChip(
                    selected = viewModel.filterOption == "Liked",
                    onClick = {
                        viewModel.setFilterOption(if (viewModel.filterOption == "All") "Liked" else "All")
                    },
                    label = { Text("Filter: ${viewModel.filterOption}") }
                )

                // Sort Options
                var sortExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { sortExpanded = true }) {
                        Text("Sort: ${viewModel.sortOption}")
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        listOf("Title", "Release Date", "User Rating").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val movies = viewModel.displayedMovies
                if (viewModel.isGridView) {
                    // LazyGrid (Poster thumbnails only)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies) { movie ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clickable { viewModel.navigateTo(MovieViewModel.Screen.DETAIL, movie.id) }
                            ) {
                                AsyncImage(
                                    model = movie.posterUrl,
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    // LazyColumn (Title, thumbnail, vote average)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies) { movie ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo(MovieViewModel.Screen.DETAIL, movie.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = movie.posterUrl,
                                        contentDescription = movie.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(70.dp, 100.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = movie.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Rating: ${movie.vote_average} / 10", fontSize = 14.sp)
                                        Text(text = "Release: ${movie.release_date}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(viewModel: MovieViewModel) {
    val movie = viewModel.movies.find { it.id == viewModel.selectedMovieId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movie?.title ?: "Movie Details") },
                navigationIcon = {
                    Button(onClick = { viewModel.navigateTo(MovieViewModel.Screen.LIST) }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        if (movie != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.height(300.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = movie.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Release Date: ${movie.release_date}")
                Text(text = "Rating: ${movie.vote_average} (${movie.vote_count} votes)")
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = movie.overview ?: "No overview available.")
                Spacer(modifier = Modifier.height(24.dp))

                // Like / Unlike Button
                IconButton(onClick = { viewModel.toggleLike(movie) }) {
                    Icon(
                        imageVector = if (movie.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Button",
                        tint = if (movie.isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
