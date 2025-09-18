package com.antago30.a7dtd_lukomorie

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.antago30.a7dtd_lukomorie.fragments.InfoFragment
import com.antago30.a7dtd_lukomorie.fragments.LeaderboardFragment
import com.antago30.a7dtd_lukomorie.fragments.NewsFragment
import com.antago30.a7dtd_lukomorie.fragments.PlayersFragment
import com.antago30.a7dtd_lukomorie.fragments.VisitorsFragment

class AppFragmentFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            NewsFragment::class.java.name -> NewsFragment()
            InfoFragment::class.java.name -> InfoFragment()
            PlayersFragment::class.java.name -> PlayersFragment()
            LeaderboardFragment::class.java.name -> LeaderboardFragment()
            VisitorsFragment::class.java.name -> VisitorsFragment()
            else -> super.instantiate(classLoader, className)
        }
    }
}