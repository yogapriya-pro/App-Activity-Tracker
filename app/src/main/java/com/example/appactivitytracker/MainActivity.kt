package com.example.appactivitytracker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appactivitytracker.data.UsageRepository
import com.example.appactivitytracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: UsageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = UsageRepository(this)
        checkPermissionAndNavigate()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionAndNavigate()
    }

    private fun checkPermissionAndNavigate() {
        if (repository.hasUsagePermission()) {
            startActivity(Intent(this, AppActivityDetailsActivity::class.java))
        } else {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogDark)
            .setTitle("Usage Access Required")
            .setMessage("This app needs access to usage statistics to show your screen time data. Please grant access in Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}