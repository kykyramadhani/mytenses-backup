package com.app.mytenses.Fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.mytenses.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationFragment : Fragment() {

    data class NotificationItem(
        val title: String,
        val subtitle: String,
        val date: String,  // Format: "dd-MM-yyyy"
        val time: String,  // Format: "HH:mm"
        val iconResId: Int = R.drawable.notif
    )

    inner class NotificationAdapter(private val items: MutableList<NotificationItem>) :
        RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

        inner class NotifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvNotifTitle)
            val subtitle: TextView = view.findViewById(R.id.tvNotifSubtitle)
            val time: TextView = view.findViewById(R.id.tvNotifTime)
            val icon: ImageView = view.findViewById(R.id.ivNotifIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notif_card, parent, false)
            return NotifViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.subtitle.text = item.subtitle
            holder.time.text = getRelativeTime(item.date, item.time)
            holder.icon.setImageResource(item.iconResId)
        }

        override fun getItemCount() = items.size

        fun updateData(newItems: List<NotificationItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    private lateinit var rv: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var receiver: BroadcastReceiver
    private lateinit var adapter: NotificationAdapter

    private fun getRelativeTime(date: String, time: String): String {
        return try {
            val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val notifDate = format.parse("$date $time") ?: return ""
            val now = Date()
            val diffMillis = now.time - notifDate.time

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
            val months = days / 30
            val years = days / 365

            when {
                minutes < 1 -> "baru saja"
                minutes < 60 -> "$minutes menit lalu"
                hours < 24 -> "$hours jam lalu"
                days < 30 -> "$days hari lalu"
                months < 12 -> "$months bulan lalu"
                else -> "$years tahun lalu"
            }
        } catch (e: Exception) {
            ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvNotifList)
        rv.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with an empty list
        adapter = NotificationAdapter(mutableListOf())
        rv.adapter = adapter

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadNotifications()
            swipeRefreshLayout.isRefreshing = false
        }

        loadNotifications()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                loadNotifications()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                receiver,
                IntentFilter("com.app.mytenses.NOTIF_HISTORY_UPDATED"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireContext().registerReceiver(
                receiver,
                IntentFilter("com.app.mytenses.NOTIF_HISTORY_UPDATED")
            )
        }

        val settingsButton = view.findViewById<ImageView>(R.id.settings_button)
        settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadNotifications() {
        val prefs = requireContext().getSharedPreferences("NotifHistory", Context.MODE_PRIVATE)
        val savedSet = prefs.getStringSet("data", emptySet()) ?: emptySet()

        val notifList = savedSet.mapNotNull {
            val parts = it.split("|")
            if (parts.size == 4) {
                NotificationItem(
                    title = parts[0],
                    subtitle = parts[1],
                    date = parts[2],
                    time = parts[3]
                )
            } else null
        }.sortedByDescending {
            val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            format.parse("${it.date} ${it.time}")?.time ?: 0L
        }

        adapter.updateData(notifList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(receiver)
    }
}