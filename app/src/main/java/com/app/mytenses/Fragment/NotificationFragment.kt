package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    inner class NotificationAdapter(private val items: List<NotificationItem>) :
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
    }

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

        val rv = view.findViewById<RecyclerView>(R.id.rvNotifList)
        rv.layoutManager = LinearLayoutManager(requireContext())

        // Ambil nama depan dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", "User") ?: "User"
        val firstName = fullName.split(" ").firstOrNull() ?: "User"

        // Dummy data pakai nama depan
        val dummyData = listOf(
            NotificationItem("Hai, $firstName!", "Ayo lanjutkan belajarmu!", "14-06-2025", "18:40"),
            NotificationItem("Selamat Datang, $firstName!", "Rasakan Pengalaman Belajar Menyenangkan dengan MyTenses", "14-06-2025", "10:30")
        )

        rv.adapter = NotificationAdapter(dummyData)

        val settingsButton = view.findViewById<ImageView>(R.id.settings_button)
        settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
