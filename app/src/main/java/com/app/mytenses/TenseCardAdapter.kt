package com.app.mytenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TenseCardAdapter(
    private var tenseCards: List<TenseCard>,
    private val onItemClick: (TenseCard) -> Unit
) : RecyclerView.Adapter<TenseCardAdapter.TenseCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenseCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tense_card, parent, false)
        return TenseCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenseCardViewHolder, position: Int) {
        val tenseCard = tenseCards[position]
        holder.bind(tenseCard)
        holder.itemView.setOnClickListener {
            onItemClick(tenseCard)
        }
    }

    override fun getItemCount(): Int = tenseCards.size

    fun updateData(newTenseCards: List<TenseCard>) {
        this.tenseCards = newTenseCards
        notifyDataSetChanged()
    }

    class TenseCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivTenseImage: ImageView = itemView.findViewById(R.id.ivTenseImage)
        private val tvTenseTitle: TextView = itemView.findViewById(R.id.tvTenseTitle)
        private val tvTenseStatus: TextView = itemView.findViewById(R.id.tvTenseStatus)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(tenseCard: TenseCard) {
            ivTenseImage.setImageResource(tenseCard.imageResId)
            tvTenseTitle.text = tenseCard.title
            tvTenseStatus.text = tenseCard.status
            progressBar.progress = tenseCard.progress
        }
    }
}