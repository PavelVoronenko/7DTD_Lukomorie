package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.parser.WebParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.antago30.a7dtd_lukomorie.utils.Constants

abstract class BaseFragment : Fragment() {

    protected lateinit var webParser: WebParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webParser = WebParser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_base, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startRefresh()
    }

    override fun onPause() {
        super.onPause()
        stopRefresh()
    }

    abstract fun loadData(): Any // 👈 ВСЕ ФРАГМЕНТЫ ВОЗВРАЩАЮТ СВОИ ДАННЫЕ

    private var refreshJob: Job? = null

    private fun startRefresh() {
        refreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                val data = withContext(Dispatchers.IO) {
                    // 👇 Вызываем loadData() — он выполняется в фоне
                    loadData()
                }

                // 👇 Переключаемся обратно в Main-поток для обновления UI
                withContext(Dispatchers.Main) {
                    updateUI(data) // 👈 Теперь мы передаём ANY — фрагмент сам разберётся
                }

                delay(10000)
            }
        }
    }

    private fun stopRefresh() {
        refreshJob?.cancel()
    }

    // 👇 АБСТРАКТНЫЙ МЕТОД — КАЖДЫЙ ФРАГМЕНТ САМ ОПРЕДЕЛЯЕТ, КАК ОБНОВЛЯТЬ UI
    protected abstract fun updateUI(data: Any)
}