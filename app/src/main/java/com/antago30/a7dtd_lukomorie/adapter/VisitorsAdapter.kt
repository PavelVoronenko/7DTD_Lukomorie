package com.antago30.a7dtd_lukomorie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.VisitorItem

class VisitorsAdapter(private var visitors: List<VisitorItem>) :
    RecyclerView.Adapter<VisitorsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.visitor_name)
        val time: TextView = view.findViewById(R.id.visitor_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visitor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visitor = visitors[position]
        holder.name.text = visitor.name
        holder.time.text = visitor.time
    }

    override fun getItemCount() = visitors.size

    fun updateData(newVisitors: List<VisitorItem>) {
        visitors = newVisitors
        notifyDataSetChanged()
    }
}