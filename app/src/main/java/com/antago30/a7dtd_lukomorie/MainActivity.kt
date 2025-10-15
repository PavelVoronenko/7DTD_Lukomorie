package com.antago30.a7dtd_lukomorie

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.antago30.a7dtd_lukomorie.fragments.*
import com.antago30.a7dtd_lukomorie.model.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.antago30.a7dtd_lukomorie.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        val menuItemsContainer = findViewById<LinearLayout>(R.id.menu_items_container)
        val menuItems = listOf(
            MenuItem("Информация", InfoFragment::class.java, R.drawable.ic_info_white_24dp),
            MenuItem("Новости", NewsFragment::class.java, R.drawable.ic_news_white_24dp),
            MenuItem("Бан-лист", BanListFragment::class.java, R.drawable.ic_ban_white_24dp),
            MenuItem("Игроки онлайн", PlayersFragment::class.java, R.drawable.ic_people_white_24dp),
            MenuItem("Лидеры сервера", LeaderboardFragment::class.java, R.drawable.ic_leaderboard_white_24dp),
            MenuItem("Посетители за сутки", VisitorsFragment::class.java, R.drawable.ic_visitors_white_24dp)
        )

        for (item in menuItems) {
            val menuItemView = layoutInflater.inflate(R.layout.drawer_menu_item, menuItemsContainer, false)
            val icon: ImageView = menuItemView.findViewById(R.id.menu_item_icon)
            val title: TextView = menuItemView.findViewById(R.id.menu_item_title)

            icon.setImageResource(item.iconResId)
            title.text = item.title

            menuItemView.setOnClickListener {
                val classLoader = item.fragmentClass.classLoader ?: this.javaClass.classLoader
                val fragment = supportFragmentManager.fragmentFactory.instantiate(
                    classLoader,
                    item.fragmentClass.name
                )
                loadFragment(fragment)
                drawerLayout.closeDrawers()
            }

            menuItemsContainer.addView(menuItemView)
        }

        val btnWebsite = findViewById<LinearLayout>(R.id.btn_website)
        val btnSteam = findViewById<LinearLayout>(R.id.btn_steam)
        val btnDiscord = findViewById<LinearLayout>(R.id.btn_discord)
        val btnTelegram = findViewById<LinearLayout>(R.id.btn_telegram)

        btnWebsite.setOnClickListener {
            openUrl(Constants.BASE_URL)
        }

        btnSteam.setOnClickListener {
            openUrl(Constants.STEAM_GROUP)
        }

        btnDiscord.setOnClickListener {
            openUrl(Constants.DISCORD_GROUP)
        }

        btnTelegram.setOnClickListener {
            openUrl(Constants.TELEGRAM_GROUP)
        }

        setupBottomMenuButton(btnWebsite, R.drawable.internet, "Страничка сервера")
        setupBottomMenuButton(btnSteam, R.drawable.steam, "Группа Steam")
        setupBottomMenuButton(btnDiscord, R.drawable.discord, "Discord")
        setupBottomMenuButton(btnTelegram, R.drawable.telegram, "Telegram")

        val fabHome = findViewById<ImageButton>(R.id.fab_home)
        fabHome.setOnClickListener {
            val infoFragment = InfoFragment()
            loadFragment(infoFragment)
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

    private fun setupBottomMenuButton(button: LinearLayout, iconResId: Int, title: String) {
        val icon: ImageView = button.findViewById(R.id.menu_item_icon)
        val text: TextView = button.findViewById(R.id.menu_item_title)
        icon.setImageResource(iconResId)
        text.text = title
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
        val currentFragment = supportFragmentManager.findFragmentById(R.id.content_frame)

        if (currentFragment != null && fragment.javaClass == currentFragment.javaClass) {
            return
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.content_frame, fragment)
            .commit()
    }
}