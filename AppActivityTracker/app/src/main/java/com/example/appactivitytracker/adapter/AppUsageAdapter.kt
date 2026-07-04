package com.example.appactivitytracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appactivitytracker.data.AppUsageData
import com.example.appactivitytracker.databinding.ItemAppUsageBinding

class AppUsageAdapter(
    private val onAppClick: (AppUsageData) -> Unit
) : ListAdapter<AppUsageData, AppUsageAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemAppUsageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppUsageData) {
            binding.appIcon.setImageDrawable(item.icon)
            binding.appName.text = item.appName
            binding.appUsageTime.text = item.getFormattedTime()
            binding.root.setOnClickListener { onAppClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppUsageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<AppUsageData>() {
        override fun areItemsTheSame(oldItem: AppUsageData, newItem: AppUsageData) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppUsageData, newItem: AppUsageData) =
            oldItem == newItem
    }
}
