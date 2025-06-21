package com.app.mytenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(
    private var courseList: MutableList<Course>,
    private var onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_course, parent, false)
        return CourseViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.bind(course)
    }

    override fun getItemCount(): Int = courseList.size

    fun updateData(newCourseList: List<Course>) {
        val diffCallback = CourseDiffCallback(courseList, newCourseList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        courseList.clear()
        courseList.addAll(newCourseList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun getItem(position: Int): Course = courseList[position]

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    class CourseViewHolder(itemView: View, private val listener: OnItemClickListener?) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIconCourse)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitleCourse)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarCourse)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatusCourse)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }

        fun bind(course: Course) {
            ivIcon.setImageResource(course.iconResId)
            tvTitle.text = course.title
            tvStatus.text = course.status
            progressBar.progress = course.progress
        }
    }

    private class CourseDiffCallback(
        private val oldList: List<Course>,
        private val newList: List<Course>
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