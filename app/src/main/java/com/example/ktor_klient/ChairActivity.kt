package com.example.ktor_klient

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.adapters.ChairItemViewAdapter
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Chairs
import com.example.ktor_klient.components.SwipeActionsCallback
import com.example.ktor_klient.databinding.ChairListBinding
import com.example.ktor_klient.models.Chair
import com.google.android.material.snackbar.Snackbar
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ChairActivity : AppCompatActivity() {
    private val mainScope = MainScope()

    private lateinit var mAdapter: ChairItemViewAdapter
    private lateinit var b: ChairListBinding
    private val chairItemsList: MutableList<Chair> = mutableListOf()
    var recyclerView: RecyclerView? = null
    var constraintLayout: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ChairListBinding.inflate(layoutInflater)
        setContentView(b.root)

        mainScope.launch {
            fetchChairs()
        }
        setUpAdapter()
        enableSwipeToDeleteAndUndo()
    }

    private fun setUpAdapter() {
        recyclerView = findViewById(R.id.ChairsList)
        constraintLayout = findViewById(R.id.constraintLayout)
        mAdapter = ChairItemViewAdapter(this,chairItemsList)
        b.ChairsList.adapter = mAdapter
        b.ChairsList.layoutManager = LinearLayoutManager(this)
    }

    private suspend fun  fetchChairs() {
        val props = intent.extras

        var facultyId:Int = -1
        if (props != null) facultyId = props.getInt("faculty")
        Log.i("PROPS",facultyId.toString())

        if (facultyId > -1){
            val api = ApiFactory.getClient()
            kotlin.runCatching {
                api.get(Chairs.Faculty.Id(id=facultyId)).body<List<Chair>>()
            }.onSuccess {
                chairItemsList.addAll(it)
                mAdapter.notifyDataSetChanged()
            }.onFailure {
                Log.e("REQUEST", it.toString())
                Toast.makeText(this, "Ошибка сервера!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeActionsCallback: SwipeActionsCallback = object : SwipeActionsCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = mAdapter.getData()[position]
                if (direction == ItemTouchHelper.LEFT){
                    mainScope.launch {
                        kotlin.runCatching {
                            mAdapter.removeItem(position)
                        }.onSuccess {
                            chairItemsList.addAll(it)
                            mAdapter.notifyDataSetChanged()
                        }.onFailure {
                            Log.e("REQUEST", it.toString())
                            Toast.makeText(this, "Ошибка сервера!", Toast.LENGTH_LONG).show()
                        }
                    }

                    val restoreAction = Snackbar
                        .make(
                            constraintLayout!!,
                            "Элемент удален.",
                            Snackbar.LENGTH_LONG
                        )
                    restoreAction.setAction("ОТМЕНИТЬ") {
                        mAdapter.restoreItem(item, position)
                        recyclerView!!.scrollToPosition(position)
                    }
                    restoreAction.setActionTextColor(Color.YELLOW)
                    restoreAction.show()
                } else if(direction == ItemTouchHelper.RIGHT) {


                }
                recyclerView?.adapter?.notifyItemChanged(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeActionsCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}