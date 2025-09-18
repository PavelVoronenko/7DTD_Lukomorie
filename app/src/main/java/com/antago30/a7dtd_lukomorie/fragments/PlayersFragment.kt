package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.adapter.PlayersAdapter
import com.antago30.a7dtd_lukomorie.utils.Constants
import java.io.IOException

class PlayersFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlayersAdapter
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_players, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        emptyText = view.findViewById(R.id.empty_text)
        adapter = PlayersAdapter(emptyList())
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
            val players = webParser.parsePlayers(Constants.PLAYERS_URL)
            adapter.updateData(players)
            emptyText.visibility = if (players.isEmpty()) View.VISIBLE else View.GONE
        } catch (e: IOException) {
            emptyText.text = "Ошибка загрузки"
            emptyText.visibility = View.VISIBLE
        }
    }*/
}