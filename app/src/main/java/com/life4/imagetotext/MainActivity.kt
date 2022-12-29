package com.life4.imagetotext

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.life4.imagetotext.data.Constants
import com.life4.imagetotext.data.MyPreference
import com.life4.imagetotext.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var myPreference: MyPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (myPreference.getInputType().isNullOrEmpty())
            myPreference.setInputType(Constants.LATIN)

        val toolbar = binding.toolbar
        val bottomNavigationView = binding.bottomNav
        //bottomNavigationView.menu.getItem(1).isVisible = true
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.resultFragment -> {
                    val window = this.window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = ContextCompat.getColor(this, R.color.navy_blue)
                }
                else -> {
                    val window = this.window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = ContextCompat.getColor(this, R.color.white)
                }
            }
        }

    }
}