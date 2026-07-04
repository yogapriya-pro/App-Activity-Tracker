package com.example.appactivitytracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appactivitytracker.adapter.AppUsageAdapter
import com.example.appactivitytracker.data.AppUsageData
import com.example.appactivitytracker.data.UsageRepository
import com.example.appactivitytracker.databinding.ActivityAppActivityDetailsBinding
import java.text.SimpleDateFormat
import java.util.*

class AppActivityDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppActivityDetailsBinding
    private lateinit var repository: UsageRepository
    private lateinit var adapter: AppUsageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = UsageRepository(this)

        setupToolbar()
        setupDropdown()
        setupRecyclerView()
        setupTimerBanner()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvTitle.text = "App activity details"
    }

    private fun setupDropdown() {
        val options = listOf("Screen time", "Notifications", "Times opened")
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item, options)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerMetric.adapter = spinnerAdapter

        binding.spinnerMetric.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Currently only screen time is implemented
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = AppUsageAdapter { appData ->
            val intent = Intent(this, AppDetailActivity::class.java).apply {
                putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, appData.packageName)
                putExtra(AppDetailActivity.EXTRA_APP_NAME, appData.appName)
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupTimerBanner() {
        binding.btnDismissBanner.setOnClickListener {
            binding.bannerSetTimers.visibility = View.GONE
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE

        Thread {
            val (totalMillis, appList) = repository.getTodayUsage()
            val weeklyData = repository.getWeeklyTotalUsage()

            val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.tvTotalTime.text = formatMillis(totalMillis)
                binding.tvDate.text = "Today"

                // Set bar chart weekly data
                binding.barChart.setData(weeklyData, todayIndex)

                // Set date label
                val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                binding.tvCurrentDay.text = sdf.format(Date())

                adapter.submitList(appList)

                // Show/hide empty state
                if (appList.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }.start()
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
