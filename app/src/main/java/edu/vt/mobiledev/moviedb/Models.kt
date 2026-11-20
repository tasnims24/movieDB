package edu.vt.mobiledev.moviedb

data class MovieResponse(
    val page: Int,
    val results: List<Movie>
)

data class Movie(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val vote_count: Int,
    val original_language: String,
    var isLiked: Boolean = false
) {
    val posterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$poster_path"
}
