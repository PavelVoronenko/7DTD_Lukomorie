package com.antago30.a7dtd_lukomorie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.antago30.a7dtd_lukomorie.fragments.InfoFragment
import com.antago30.a7dtd_lukomorie.fragments.LeaderboardFragment
import com.antago30.a7dtd_lukomorie.fragments.NewsFragment
import com.antago30.a7dtd_lukomorie.fragments.PlayersFragment
import com.antago30.a7dtd_lukomorie.fragments.VisitorsFragment
import com.antago30.a7dtd_lukomorie.model.MenuItem
import android.widget.ImageButton
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: com.antago30.a7dtd_lukomorie.ui.SwipeAwareDrawerLayout
    private lateinit var navigationView: NavigationView

    private val menuItems = listOf(
        MenuItem("Информация", InfoFragment::class.java),
        MenuItem("Новости", NewsFragment::class.java),
        MenuItem("Игроки онлайн", PlayersFragment::class.java),
        MenuItem("Таблица лидеров", LeaderboardFragment::class.java),
        MenuItem("Посетители за сутки", VisitorsFragment::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                getColor(R.color.status_bar_dark),
                getColor(R.color.status_bar_white)
            )
        )

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val fabToggle = findViewById<ImageButton>(R.id.fab_drawer_toggle)
        fabToggle.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        supportFragmentManager.fragmentFactory = AppFragmentFactory()

        setupNavigation()

        val firstFragment = InfoFragment()
        loadFragment(firstFragment)
    }

    private fun setupNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val selectedFragment = menuItems.find { it.title == menuItem.title }?.fragmentClass
            selectedFragment?.let { fragmentClass ->
                val fragment = fragmentClass.newInstance() as Fragment
                loadFragment(fragment)
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }
}