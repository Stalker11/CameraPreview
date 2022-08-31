package com.olehel.cameraview.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.olehel.cameraview.R
import com.olehel.cameraview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val TAG = MainActivity::class.java.simpleName
    private lateinit var navController: NavController
    val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean->
        if (success) {
            Log.d("MainActivity", ": passed ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainFragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

       // navController.navigate()
    }
}