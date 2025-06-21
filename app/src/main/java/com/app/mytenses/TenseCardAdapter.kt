package com.app.mytenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class TenseCardAdapter(
    private var tenseCards: MutableList<TenseCard>,
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
        val diffCallback = TenseCardDiffCallback(tenseCards, newTenseCards)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tenseCards.clear()
        tenseCards.addAll(newTenseCards)
        diffResult.dispatchUpdatesTo(this)
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

    private class TenseCardDiffCallback(
        private val oldList: List<TenseCard>,
        private val newList: List<TenseCard>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].lessonId == newList[newItemPosition].lessonId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}