package com.antago30.a7dtd_lukomorie

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.antago30.a7dtd_lukomorie.fragments.InfoFragment
import com.antago30.a7dtd_lukomorie.fragments.LeaderboardFragment
import com.antago30.a7dtd_lukomorie.fragments.NewsFragment
import com.antago30.a7dtd_lukomorie.fragments.PlayersFragment
import com.antago30.a7dtd_lukomorie.fragments.VisitorsFragment
import com.antago30.a7dtd_lukomorie.model.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.drawerlayout.widget.DrawerLayout
import com.antago30.a7dtd_lukomorie.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

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
        val customDrawerMenu: LinearLayout = findViewById(R.id.custom_drawer_menu)
        val menuItemsContainer: LinearLayout = findViewById(R.id.menu_items_container)
        val headerImage: ImageView = customDrawerMenu.findViewById(R.id.header_image)

        headerImage.setOnClickListener {
            openUrl(Constants.BASE_URL)
        }

        val menuItems = listOf(
            MenuItem("Информация", InfoFragment::class.java, R.drawable.ic_info_white_24dp),
            MenuItem("Новости", NewsFragment::class.java, R.drawable.ic_news_white_24dp),
            MenuItem("Игроки онлайн", PlayersFragment::class.java, R.drawable.ic_people_white_24dp),
            MenuItem("Таблица лидеров", LeaderboardFragment::class.java, R.drawable.ic_leaderboard_white_24dp),
            MenuItem("Посетители за сутки", VisitorsFragment::class.java, R.drawable.ic_visitors_white_24dp)
        )

        for (item in menuItems) {
            val menuItemView = layoutInflater.inflate(R.layout.drawer_menu_item, menuItemsContainer, false)
            val icon: ImageView = menuItemView.findViewById(R.id.menu_item_icon)
            val title: TextView = menuItemView.findViewById(R.id.menu_item_title)

            icon.setImageResource(item.iconResId)
            title.text = item.title

            menuItemView.setOnClickListener {
                val fragment = item.fragmentClass.newInstance() as Fragment
                loadFragment(fragment)
                drawerLayout.closeDrawers()
            }

            menuItemsContainer.addView(menuItemView)
        }

        val fabToggle = findViewById<ImageButton>(R.id.fab_drawer_toggle)
        fabToggle.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        supportFragmentManager.fragmentFactory = AppFragmentFactory()

        val firstFragment = InfoFragment()
        loadFragment(firstFragment)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }
}