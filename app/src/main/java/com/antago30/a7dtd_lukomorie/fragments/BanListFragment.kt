package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.adapter.BannedPlayersAdapter
import com.antago30.a7dtd_lukomorie.model.BannedPlayer
import com.antago30.a7dtd_lukomorie.utils.Constants

class BanListFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var reloadButton: Button
    private lateinit var adapter: BannedPlayersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ban_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout) // Это LinearLayout
        reloadButton = view.findViewById(R.id.reload_button)

        adapter = BannedPlayersAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun loadData(): Any {
        return try {
            webParser.parseBanList(Constants.FULL_DATA_URL)
        } catch (e: Exception) {
            Log.e("BanListFragment", "Ошибка загрузки бан-листа", e)
            emptyList<BannedPlayer>()
        }
    }

    override fun updateUI(data: Any) {
        if (!isAdded || view == null) return

        if (data is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val bannedPlayerList = data as List<BannedPlayer>

            if (bannedPlayerList.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
                adapter.updateData(bannedPlayerList)
            }
        } else {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
            adapter.updateData(emptyList())
        }
    }
}