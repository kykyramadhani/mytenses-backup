package com.app.mytenses

import com.app.mytenses.Course
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(private val courseList: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    // ViewHolder untuk setiap item
    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIconCourse)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitleCourse)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarCourse)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatusCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_course, parent, false)
        return CourseViewHolder(view)
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