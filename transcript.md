tasnims24
built in Gemini AI

how do i use the apikey to work with the application

To use your TMDB API Key / Access Token in the application, follow these steps:
How It Works
TMDB supports two authentication formats:
1.
API Read Access Token (v4 Bearer Token): A long JWT token starting with eyJhbGci.... The app passes this in the HTTP request header as Authorization: Bearer <TOKEN>.
2.
API Key (v3 Key): A 32-character key (e.g. 849af290...). The app appends this as a query parameter (?api_key=<KEY>).
The updated MovieViewModel automatically detects which format you provide and formats the network request accordingly.

Step-by-Step Usage
1.
Open   MainActivity.kt.
2.
Replace "placeholder" with your actual TMDB key or token string:
Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
enableEdgeToEdge()

    // Pass your TMDB API Read Access Token (eyJ...) or v3 API Key
    val tmdbApiKey = "eyJhbGciOiJIUzI1NiJ9..." 
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
3.
Run the application. viewModel.loadMovies(tmdbApiKey) will:
◦
Fetch 3 pages of now_playing movies from TMDB.
◦
Filter for English-language movies (original_language == "en").
◦
Sync the liked status from Jetpack DataStore preferences and display the movies in the list/grid.

In the LazyColumn, you should show the movie title, a thumbnail of the movie poster, and the vote_average (this is on a scale of one to 10).
The list should also provide a way to change the sort order. The sort options should be:
By Title <--Default By Release Date By User Rating

The LazyColumn item layout and sort order options have been updated to meet your requirements:
Updates Made
1.
LazyColumn Content (  MovieScreens.kt): Each list item displays:
◦
Poster Thumbnail: Rendered via AsyncImage with posterUrl.
◦
Movie Title: Styled in bold text.
◦
User Rating: Formatted on a scale of 1 to 10 ("Rating: ${movie.vote_average} / 10").
2.
Sort Options & Defaults (  MovieScreens.kt,   MovieViewModel.kt,   MoviePreferences.kt):
◦
By Title (Default)
◦
By Release Date
◦
By User Rating
Selected sort options are automatically persisted across app restarts via MoviePreferences and Jetpack DataStore.

