
package com.example.mstudbook

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, // Resource ID for "Open"
            R.string.navigation_drawer_close // Resource ID for "Close"
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HorseSearchFragment())
                .commit()
            navView.setCheckedItem(R.id.fragment_horse_search)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_horse_search -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HorseSearchFragment())
                    .commit()
            }
            R.id.fragment_favorites -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FavoritesFragment())
                    .commit()
            }
            R.id.fragment_trainer_horse -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TrainerHorseFragment())
                    .commit()
            }
            R.id.fragment_homepage_link -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomepageLinkFragment())
                    .commit()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}