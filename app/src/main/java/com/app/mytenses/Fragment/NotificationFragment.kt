package com.app.mytenses.Fragment

import com.app.mytenses.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationFragment : Fragment() {

    // Data class untuk item notifikasi
    data class NotificationItem(
        val title: String,
        val subtitle: String,
        val date: String,
        val time: String,
        val iconResId: Int = R.drawable.notif
    )

    // Adapter RecyclerView
    inner class NotificationAdapter(private val items: List<NotificationItem>) :
        RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

        inner class NotifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvNotifTitle)
            val subtitle: TextView = view.findViewById(R.id.tvNotifSubtitle)
            val date: TextView = view.findViewById(R.id.tvNotifDate)
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
            holder.date.text = item.date
            holder.time.text = item.time
            holder.icon.setImageResource(item.iconResId)
        }

        override fun getItemCount() = items.size
    }

    // Menyiapkan view fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    // Men-setup RecyclerView saat view selesai dibuat
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvNotifList)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val dummyData = listOf(
            NotificationItem("Hai, Keiko!", "Ayo lanjutkan belajarmu!", "14-06-2025", "18:40"),
            NotificationItem(
                "Selamat Datang",
                "Hari ini kamu belum belajar",
                "14-06-2025",
                "10:30"
            ),
            NotificationItem("Reminder", "Sesi belajar belum selesai", "13-06-2025", "19:20")
        )

        rv.adapter = NotificationAdapter(dummyData)

        val settingsButton = view.findViewById<ImageView>(R.id.settings_button)
        settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    SettingFragment()
                ) // <- Pastikan ID container sesuai
                .addToBackStack(null)
                .commit()
        }
    }
}
