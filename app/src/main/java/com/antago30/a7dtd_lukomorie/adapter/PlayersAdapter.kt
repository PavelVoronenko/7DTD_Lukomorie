package com.antago30.a7dtd_lukomorie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.Player

class PlayersAdapter(private var players: List<Player>) : RecyclerView.Adapter<PlayersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.player_name)
        val level: TextView = view.findViewById(R.id.player_level)
        val deaths: TextView = view.findViewById(R.id.player_deaths)
        val zombies: TextView = view.findViewById(R.id.player_zombies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.level.text = "Ур.: ${player.level}"
        holder.deaths.text = "Смертей: ${player.deaths}"
        holder.zombies.text = "Зомби: ${player.zombiesKilled}"
    }

    override fun getItemCount() = players.size

    fun updateData(newPlayers: List<Player>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}