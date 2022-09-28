package com.example.ktor_klient.adapters

import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.ChairActivity
import com.example.ktor_klient.TeacherActivity
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Chairs
import com.example.ktor_klient.databinding.RecyclerItemLayoutBinding
import com.example.ktor_klient.models.Chair
import com.example.ktor_klient.models.ChairRequest
import com.example.ktor_klient.models.Faculty
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*


class ChairItemViewAdapter(private val context: Context, private val itemList:MutableList<Chair>)
    : RecyclerView.Adapter<ChairItemViewAdapter.ChairItemViewHolder>() {

    private val api = ApiFactory.getClient()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChairItemViewHolder {
        val binding = RecyclerItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return ChairItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChairItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, context)
    }

    override fun getItemCount(): Int = itemList.size

    suspend fun removeItem(id: Int, position: Int) {
        api.delete(Chairs.Id(id=id))
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    suspend fun restoreItem(item: Chair, position: Int) {
        api.post(Chairs()){
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        itemList.add(position, item)
        notifyItemInserted(position)
    }

    suspend fun editItem(item: Chair, position: Int){
        api.patch(Chairs.Id(id=item.Id)){
            contentType(ContentType.Application.Json)
            setBody(ChairRequest(Faculty = item.Faculty, NameChair = item.NameChair, ShortNameChair = item.ShortNameChair))
        }
        itemList[position].Faculty = item.Faculty
        itemList[position].NameChair = item.NameChair
        itemList[position].ShortNameChair = item.ShortNameChair
        notifyItemChanged(position)
    }

    suspend fun addItem(item: ChairRequest){
        api.post(Chairs()){
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        itemList.add(Chair(Id=itemCount,Faculty = item.Faculty, NameChair = item.NameChair, ShortNameChair = item.ShortNameChair))
        notifyItemInserted(itemList.size)
    }

    fun getData() = itemList

    class ChairItemViewHolder(itemLayoutBinding: RecyclerItemLayoutBinding)
        : RecyclerView.ViewHolder(itemLayoutBinding.root) {

        private val binding = itemLayoutBinding

        fun bind(item: Chair, context: Context) {
            binding.label.text = item.NameChair
            itemView.setOnClickListener {
                if (item.Id != null) {
                    val intent = Intent(context, TeacherActivity::class.java)
                    intent.putExtra("chair", item.Id)
                    context.startActivity(intent)
                }
            }
        }
    }
}