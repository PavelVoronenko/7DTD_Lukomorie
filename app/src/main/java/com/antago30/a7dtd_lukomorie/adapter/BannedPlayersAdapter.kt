package com.antago30.a7dtd_lukomorie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.BannedPlayer

class BannedPlayersAdapter(private var items: List<BannedPlayer>) :
    RecyclerView.Adapter<BannedPlayersAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nicknameText: TextView = view.findViewById(R.id.nickname_text)
        val unbanDateText: TextView = view.findViewById(R.id.unban_date_text)
        val reasonText: TextView = view.findViewById(R.id.reason_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banned_player, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nicknameText.text = item.nickname
        holder.unbanDateText.text = "Дата разблокировки: ${item.unbanDate}"
        holder.reasonText.text = "Причина: ${item.reason}"
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<BannedPlayer>) {
        items = newItems
        notifyDataSetChanged()
    }
}