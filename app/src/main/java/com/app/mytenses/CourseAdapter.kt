package com.app.mytenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(private var courseList: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun updateData(newCourseList: List<Course>) {
        this.courseList = newCourseList
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Course {
        return courseList[position]
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_course, parent, false)
        return CourseViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.ivIcon.setImageResource(course.iconResId)
        holder.tvTitle.text = course.title
        holder.progressBar.progress = course.progress
        holder.tvStatus.text = course.status
    }

    override fun getItemCount(): Int = courseList.size
}