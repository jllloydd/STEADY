package com.bonak.steady

data class SerpApiResponse(
    val news_results: List<NewsResult>
)

data class NewsResult(
    val title: String,
    val link: String,
    val source: String,
    val date: String,
    val snippet: String,
    val thumbnail: String?
)