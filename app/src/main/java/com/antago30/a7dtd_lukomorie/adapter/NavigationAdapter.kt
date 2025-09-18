package com.antago30.a7dtd_lukomorie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.NewsItem

class NavigationAdapter(private var items: List<NewsItem>) : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.player_name)
        val content: TextView = view.findViewById(R.id.player_level)
        val timestamp: TextView = view.findViewById(R.id.player_deaths)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.content.text = item.content
        holder.timestamp.text = item.timestamp
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}