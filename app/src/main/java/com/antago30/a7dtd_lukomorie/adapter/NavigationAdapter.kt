package com.antago30.a7dtd_lukomorie.adapter

import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.NewsItem

class NavigationAdapter(private var items: List<NewsItem>) : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.news_title)
        val content: TextView = view.findViewById(R.id.news_content)
        val timestamp: TextView = view.findViewById(R.id.news_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.timestamp.text = item.timestamp
        holder.content.text = HtmlCompat.fromHtml(item.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.content.movementMethod = LinkMovementMethod.getInstance()
        holder.content.linksClickable = true
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}