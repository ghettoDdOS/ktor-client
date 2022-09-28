package com.example.ktor_klient.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Posts
import com.example.ktor_klient.api.resources.Teachers
import com.example.ktor_klient.databinding.RecyclerItemLayoutBinding
import com.example.ktor_klient.models.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*


class TeacherItemViewAdapter(
    private val context: Context,
    private val itemList: MutableList<Teacher>
) : RecyclerView.Adapter<TeacherItemViewAdapter.TeacherItemViewHolder>() {

    private val api = ApiFactory.getClient()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherItemViewHolder {
        val binding = RecyclerItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return TeacherItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeacherItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    suspend fun removeItem(id: Int, position: Int) {
        api.delete(Teachers.Id(id = id))
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    suspend fun restoreItem(item: Teacher, position: Int) {
        api.post(Teachers()) {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        itemList.add(position, item)
        notifyItemInserted(position)
    }

    suspend fun editItem(item: Teacher, position: Int) {
        api.patch(Teachers.Id(id = item.Id)) {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    Chair = item.Chair,
                    Post = item.Post.Id,
                    FirstName = item.FirstName,
                    SecondName = item.SecondName,
                    LastName = item.LastName,
                    Phone = item.Phone,
                    Email = item.Email
                )
            )
        }
        itemList[position].Chair = item.Chair
        itemList[position].Post = item.Post
        itemList[position].FirstName = item.FirstName
        itemList[position].SecondName = item.SecondName
        itemList[position].LastName = item.LastName
        itemList[position].Phone = item.Phone
        itemList[position].Email = item.Email

        notifyItemChanged(position)
    }

    suspend fun addItem(item: TeacherRequest) {
        api.post(Teachers()) {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        kotlin.runCatching {
            api.get(Posts.Id(id=item.Post)).body<Post>()
        }.onSuccess {
            itemList.add(
                Teacher(
                    Id = itemCount,
                    Chair = item.Chair,
                    Post = it,
                    FirstName = item.FirstName,
                    SecondName = item.SecondName,
                    LastName = item.LastName,
                    Phone = item.Phone,
                    Email = item.Email
                )
            )
            notifyItemInserted(itemList.size)
        }

    }

    fun getData() = itemList

    class TeacherItemViewHolder(itemLayoutBinding: RecyclerItemLayoutBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root) {

        private val binding = itemLayoutBinding

        fun bind(item: Teacher) {
            binding.label.text = "${item.SecondName} ${item.FirstName} ${item.LastName}"
        }
    }
}