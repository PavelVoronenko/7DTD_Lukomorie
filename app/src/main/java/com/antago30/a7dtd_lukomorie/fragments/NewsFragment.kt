package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.adapter.NavigationAdapter
import com.antago30.a7dtd_lukomorie.model.NewsItem
import com.antago30.a7dtd_lukomorie.utils.Constants
import java.io.IOException

class NewsFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NavigationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        adapter = NavigationAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return view
    }

    override fun loadData(): Any {
        return try {
            webParser.parseNews(Constants.NEWS_URL)
        } catch (e: IOException) {
            Log.e("NewsFragment", "Ошибка загрузки новостей", e)
            emptyList()
        }
    }

    override fun updateUI(data: Any) {
        if (data is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val newsList = data as List<NewsItem>
            adapter.updateData(newsList)
        } else {
            adapter.updateData(emptyList())
        }
    }
}