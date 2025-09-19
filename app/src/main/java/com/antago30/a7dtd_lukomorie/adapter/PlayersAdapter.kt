package com.antago30.a7dtd_lukomorie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.PlayerItem

class PlayersAdapter(private var players: List<PlayerItem>) :
    RecyclerView.Adapter<PlayersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.player_name)
        val level: TextView = view.findViewById(R.id.player_level)
        val deaths: TextView = view.findViewById(R.id.player_deaths)
        val zombies: TextView = view.findViewById(R.id.zombies_killed)
        // Можно добавить клик на имя для открытия профиля Steam
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.level.text = player.level.toString()
        holder.deaths.text = player.deaths.toString()
        holder.zombies.text = player.zombiesKilled.toString()
        // holder.players.text = player.playersKilled.toString() // Можно добавить
    }

    override fun getItemCount() = players.size

    fun updateData(newPlayers: List<PlayerItem>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}