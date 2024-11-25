package com.bonak.steady

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsItem>
)

data class NewsItem(
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val source: Source,
    val link: String
)

data class Source(
    val id: String?,
    val name: String
)