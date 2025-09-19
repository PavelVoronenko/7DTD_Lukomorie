package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var reloadNewsButton: Button

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


        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        reloadNewsButton = view.findViewById(R.id.reload_news_button)


        reloadNewsButton.setOnClickListener {
            loadData()
        }

        return view
    }

    override fun loadData(): Any {
        return try {
            webParser.parseNews(Constants.NEWS_URL)
        } catch (e: IOException) {
            emptyList<NewsItem>()
        }
    }

    override fun updateUI(data: Any) {
        if (data is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val newsList = data as List<NewsItem>

            if (newsList.isEmpty()) {
                // Если список пустой, показываем пустое состояние
                recyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            } else {
                // Если есть новости, показываем список и скрываем пустое состояние
                recyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
                adapter.updateData(newsList)
            }
        } else {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
            adapter.updateData(emptyList())
        }
    }
}