package com.bonak.steady

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Home2 : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val newsList = mutableListOf<NewsResult>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home2, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        newsAdapter = NewsAdapter(newsList)
        recyclerView.adapter = newsAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadNewsData()
        }

        loadNewsData()

        return view
    }

    private fun loadNewsData() {
        swipeRefreshLayout.isRefreshing = true
        val apiKey = "a5970f06539b27add35e8eceedc87b22f0441e4caf670528ec16000e02b93eb7"
        val query = "landslide OR earthquake Baguio City OR Benguet"
        val call = NetworkClient.serpApiService.getGoogleNews(query, apiKey = apiKey)
        call.enqueue(object : Callback<SerpApiResponse> {
            override fun onResponse(call: Call<SerpApiResponse>, response: Response<SerpApiResponse>) {
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    response.body()?.news_results?.let {
                        newsList.clear()
                        newsList.addAll(it)
                        newsAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(context, "Failed to load news", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SerpApiResponse>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}