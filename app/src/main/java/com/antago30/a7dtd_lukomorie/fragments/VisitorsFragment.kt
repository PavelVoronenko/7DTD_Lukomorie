package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.antago30.a7dtd_lukomorie.R

class VisitorsFragment : BaseFragment() {

    private lateinit var visitorsText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visitors, container, false)
        visitorsText = view.findViewById(R.id.visitors_text)
        return view
    }

    override fun loadData(): Any {
        return 1
    }

    override fun updateUI(data: Any) {

    }

    /*override fun loadData() {
        try {
            val count = webParser.parseVisitors(Constants.VISITORS_URL)
            visitorsText.text = "Посетителей за сутки: $count"
        } catch (e: IOException) {
            visitorsText.text = "Ошибка загрузки"
        }
    }*/
}