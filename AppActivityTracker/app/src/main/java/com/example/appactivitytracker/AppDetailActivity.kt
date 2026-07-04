package com.example.appactivitytracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.appactivitytracker.data.UsageRepository
import com.example.appactivitytracker.databinding.ActivityAppDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class AppDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
    }

    private lateinit var binding: ActivityAppDetailBinding
    private lateinit var repository: UsageRepository
    private var packageName2: String = ""
    private var appName: String = ""
    private var isHourlyView = false
    private var currentDailyData: List<Long> = emptyList()
    private var currentHourlyData: List<Long> = emptyList()
    private var currentUsageMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        packageName2 = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        appName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
        repository = UsageRepository(this)

        setupToolbar()
        setupDropdowns()
        setupAppDashboard()
        setupAppTimer()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvAppTitle.text = appName

        try {
            val pm = packageManager
            val icon = pm.getApplicationIcon(packageName2)
            binding.ivAppIcon.setImageDrawable(icon)
        } catch (e: Exception) {
            binding.ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    private fun setupDropdowns() {
        // Metric spinner
        val metricOptions = listOf("Screen time", "Notifications", "Times opened")
        val metricAdapter = ArrayAdapter(this, R.layout.spinner_item, metricOptions)
        metricAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerMetric.adapter = metricAdapter

        // Period spinner (Daily / Hourly)
        val periodOptions = listOf("Daily", "Hourly")
        val periodAdapter = ArrayAdapter(this, R.layout.spinner_item, periodOptions)
        periodAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerPeriod.adapter = periodAdapter

        binding.spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                isHourlyView = (position == 1)
                switchChartView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupAppDashboard() {
        binding.llShowDashboard.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName2")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    private fun setupAppTimer() {
        binding.llAppTimer.setOnClickListener {
            // In a real app, this would open a time picker dialog
            showTimerDialog()
        }
    }

    private fun showTimerDialog() {
        val options = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours", "Remove timer")
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogDark)
            .setTitle("Set app timer")
            .setItems(options) { _, which ->
                val timerText = if (which == options.size - 1) "No timer" else options[which]
                binding.tvTimerValue.text = timerText
            }
            .show()
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE

        Thread {
            val (_, appList) = repository.getTodayUsage()
            val appData = appList.find { it.packageName == packageName2 }

            currentDailyData = appData?.dailyUsage ?: List(7) { 0L }
            currentHourlyData = appData?.hourlyUsage ?: List(24) { 0L }
            currentUsageMillis = appData?.usageTimeMillis ?: 0L

            val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.tvTotalTime.text = formatMillis(currentUsageMillis)
                binding.tvDate.text = "Today"
                binding.tvCurrentDay.text = sdf.format(Date())

                switchChartView()
            }
        }.start()
    }

    private fun switchChartView() {
        val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        if (isHourlyView) {
            binding.barChartDaily.visibility = View.GONE
            binding.barChartHourly.visibility = View.VISIBLE
            binding.barChartHourly.setData(currentHourlyData)
        } else {
            binding.barChartDaily.visibility = View.VISIBLE
            binding.barChartHourly.visibility = View.GONE
            binding.barChartDaily.setData(currentDailyData, todayIndex)
        }
    }

    private fun formatMillis(millis: Long): String {
        val totalMinutes = millis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours hr, $minutes min"
            hours > 0 -> "$hours hr"
            minutes > 0 -> "$minutes min"
            else -> "0 min"
        }
    }
}
