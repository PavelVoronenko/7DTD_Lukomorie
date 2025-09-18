package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.adapter.NavigationAdapter
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
        TODO("Not yet implemented")
    }

    override fun updateUI(data: Any) {
        TODO("Not yet implemented")
    }

    /*override fun loadData() {
        try {
            val news = webParser.parseNews(Constants.NEWS_URL)
            adapter.updateData(news)
        } catch (e: IOException) {
            // Handle error
        }
    }*/
}