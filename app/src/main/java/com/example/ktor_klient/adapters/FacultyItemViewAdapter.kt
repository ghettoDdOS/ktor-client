package com.example.ktor_klient.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.databinding.RecyclerItemLayoutBinding
import com.example.ktor_klient.models.Faculty


class FacultyItemViewAdapter(private val context: Context, private val itemList:MutableList<Faculty>)
    : RecyclerView.Adapter<FacultyItemViewAdapter.FacultyItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyItemViewHolder {
        val binding = RecyclerItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return FacultyItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FacultyItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    fun removeItem(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(item: Faculty, position: Int) {
        itemList.add(position, item)
        notifyItemInserted(position)
    }

    fun getData() = itemList

    class FacultyItemViewHolder(facultiesItemLayoutBinding: RecyclerItemLayoutBinding)
        : RecyclerView.ViewHolder(facultiesItemLayoutBinding.root) {

        private val binding = facultiesItemLayoutBinding

        fun bind(facultyItem: Faculty) {
            binding.label.text = facultyItem.NameFaculty
        }
    }
}