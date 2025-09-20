package com.antago30.a7dtd_lukomorie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.PlayerItem

class LeaderboardAdapter(private var players: List<PlayerItem>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.player_name)
        val level: TextView = view.findViewById(R.id.player_level)
        val deaths: TextView = view.findViewById(R.id.player_deaths)
        val zombies: TextView = view.findViewById(R.id.zombies_killed)
        val players: TextView = view.findViewById(R.id.players_killed)
        val score: TextView = view.findViewById(R.id.total_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.level.text = "Уровень: ${player.level}"
        holder.deaths.text = "Смертей: ${player.deaths}"
        holder.zombies.text = "Убито зомби: ${player.zombiesKilled}"
        holder.players.text = "Убито игроков: ${player.playersKilled}"
        holder.score.text = "Всего очков: ${player.totalScore}"
    }

    override fun getItemCount() = players.size

    fun updateData(newPlayers: List<PlayerItem>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}