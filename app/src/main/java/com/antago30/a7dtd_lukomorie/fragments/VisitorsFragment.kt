package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.adapter.VisitorsAdapter
import com.antago30.a7dtd_lukomorie.model.VisitorItem
import com.antago30.a7dtd_lukomorie.utils.Constants
import java.io.IOException

class VisitorsFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VisitorsAdapter
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visitors, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        emptyText = view.findViewById(R.id.empty_text)
        adapter = VisitorsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return view
    }

    override fun loadData(): Any {
        return try {
            webParser.parseVisitors(Constants.VISITORS_URL)
        } catch (e: IOException) {
            Log.e("VisitorsFragment", "Ошибка загрузки посетителей", e)
            emptyList<VisitorItem>()
        }
    }

    override fun updateUI(data: Any) {
        if (data is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val visitorList = data as List<VisitorItem>
            if (visitorList.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyText.visibility = View.GONE
                adapter.updateData(visitorList)
            }
        } else {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            adapter.updateData(emptyList())
        }
    }
}