package com.example.ktor_klient.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.ChairActivity
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Faculties
import com.example.ktor_klient.databinding.RecyclerItemLayoutBinding
import com.example.ktor_klient.models.Faculty
import com.example.ktor_klient.models.FacultyRequest
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*


class FacultyItemViewAdapter(
    private val context: Context,
    private val itemList: MutableList<Faculty>
) : RecyclerView.Adapter<FacultyItemViewAdapter.FacultyItemViewHolder>() {

    private val api = ApiFactory.getClient()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyItemViewHolder {
        val binding = RecyclerItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return FacultyItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FacultyItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, context)
    }

    override fun getItemCount(): Int = itemList.size
    suspend fun removeItem(id: Int, position: Int) {
        api.delete(Faculties.Id(id = id))
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    suspend fun restoreItem(item: Faculty, position: Int) {
        api.post(Faculties()) {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        itemList.add(position, item)
        notifyItemInserted(position)
    }

    suspend fun editItem(item: Faculty, position: Int) {
        kotlin.runCatching {
            api.patch(Faculties.Id(id = item.Id)) {
                contentType(ContentType.Application.Json)
                setBody(FacultyRequest(item.NameFaculty, item.ShortNameFaculty))
            }
        }.onSuccess {
            itemList[position].NameFaculty = item.NameFaculty
            itemList[position].ShortNameFaculty = item.ShortNameFaculty
            notifyItemChanged(position)
        }.onFailure {
            Toast.makeText(context, "Ошибка сервера!", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun addItem(item: FacultyRequest) {
        api.post(Faculties()) {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        itemList.add(
            Faculty(
                Id = itemList.size,
                NameFaculty = item.NameFaculty,
                ShortNameFaculty = item.ShortNameFaculty
            )
        )
        notifyItemInserted(itemList.size)
    }

    fun getData() = itemList

    class FacultyItemViewHolder(facultiesItemLayoutBinding: RecyclerItemLayoutBinding) :
        RecyclerView.ViewHolder(facultiesItemLayoutBinding.root) {

        private val binding = facultiesItemLayoutBinding

        fun bind(facultyItem: Faculty, context: Context) {
            binding.label.text = facultyItem.NameFaculty
            itemView.setOnClickListener {
                if (facultyItem.Id != null) {
                    val intent = Intent(context, ChairActivity::class.java)
                    intent.putExtra("faculty", facultyItem.Id)
                    context.startActivity(intent)
                }
            }
        }
    }
}