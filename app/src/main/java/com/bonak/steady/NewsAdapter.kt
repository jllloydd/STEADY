package com.bonak.steady

import android.view.LayoutInflater
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsAdapter(private val newsList: List<NewsResult>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val titleView: TextView = itemView.findViewById(R.id.news_title)
        val dateView: TextView = itemView.findViewById(R.id.news_date)
        val sourceView: TextView = itemView.findViewById(R.id.news_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_item_layout, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = newsList[position]
        holder.titleView.text = currentItem.title
        holder.dateView.text = currentItem.date
        holder.sourceView.text = currentItem.source

        Glide.with(holder.itemView.context)
            .load(currentItem.thumbnail)
            .placeholder(R.drawable.logo)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.link))
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount() = newsList.size
}