package com.example.ktor_klient

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.adapters.ChairItemViewAdapter
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Chairs
import com.example.ktor_klient.components.SwipeActionsCallback
import com.example.ktor_klient.databinding.ChairListBinding
import com.example.ktor_klient.models.Chair
import com.example.ktor_klient.models.ChairRequest
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
    var constraintLayout: RelativeLayout? = null
    var facultyId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ChairListBinding.inflate(layoutInflater)
        setContentView(b.root)
        val props = intent.extras
        if (props != null) facultyId = props.getInt("faculty")

        mainScope.launch {
            fetchChairs()
        }
        setUpAdapter()
        enableSwipeToDeleteAndUndo()

        b.floatingActionButton.setOnClickListener {
            addChair()
        }
    }

    private fun setUpAdapter() {
        recyclerView = findViewById(R.id.ChairsList)
        constraintLayout = findViewById(R.id.constraintLayout)
        mAdapter = ChairItemViewAdapter(this, chairItemsList)
        b.ChairsList.adapter = mAdapter
        b.ChairsList.layoutManager = LinearLayoutManager(this)
    }

    private suspend fun fetchChairs() {
        if (facultyId > -1) {
            val api = ApiFactory.getClient()
            kotlin.runCatching {
                api.get(Chairs.Faculty.Id(id = facultyId)).body<List<Chair>>()
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

                if (direction == ItemTouchHelper.LEFT) {
                    mainScope.launch {
                        kotlin.runCatching {
                            mAdapter.removeItem(item.Id, position)
                        }.onSuccess {
                            val restoreAction = Snackbar
                                .make(
                                    constraintLayout!!,
                                    "Элемент удален.",
                                    Snackbar.LENGTH_LONG
                                )
                            restoreAction.setAction("ОТМЕНИТЬ") {
                                mainScope.launch {
                                    mAdapter.restoreItem(item, position)
                                    recyclerView!!.scrollToPosition(position)
                                }
                            }
                            restoreAction.setActionTextColor(Color.YELLOW)
                            restoreAction.show()
                        }
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    val dialogBuilder = AlertDialog.Builder(this@ChairActivity)
                    val dialogLayout = LinearLayout(
                        this@ChairActivity
                    )
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    dialogLayout.layoutParams = lp
                    dialogLayout.orientation = LinearLayout.VERTICAL

                    val nameInput = EditText(dialogLayout.context)
                    nameInput.layoutParams = lp
                    nameInput.hint = "Наименование"
                    nameInput.setPadding(25)

                    val shortNameInput = EditText(dialogLayout.context)
                    shortNameInput.layoutParams = lp
                    shortNameInput.hint = "Аббревиатура"
                    shortNameInput.setPadding(25)

                    dialogLayout.addView(nameInput)
                    dialogLayout.addView(shortNameInput)

                    dialogBuilder.setView(dialogLayout)

                    nameInput.text = Editable.Factory.getInstance().newEditable(item.NameChair)
                    shortNameInput.text =
                        Editable.Factory.getInstance().newEditable(item.ShortNameChair)

                    dialogBuilder.setTitle("Редактировать кафедру: ${item.ShortNameChair}")

                    dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
                        mainScope.launch {
                            mAdapter.editItem(
                                Chair(
                                    Id = item.Id,
                                    Faculty = facultyId,
                                    NameChair = nameInput.text.toString(),
                                    ShortNameChair = shortNameInput.text.toString()
                                ),
                                position
                            )
                        }
                        dialog.cancel()
                    }
                    dialogBuilder.setNegativeButton("Отменить") { dialog, _ -> dialog.cancel() }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                }
                recyclerView?.adapter?.notifyItemChanged(position)
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeActionsCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun addChair() {
        val dialogBuilder = AlertDialog.Builder(this@ChairActivity)
        val dialogLayout = LinearLayout(
            this@ChairActivity
        )
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        dialogLayout.layoutParams = lp
        dialogLayout.orientation = LinearLayout.VERTICAL

        val nameInput = EditText(dialogLayout.context)
        nameInput.layoutParams = lp
        nameInput.hint = "Наименование"
        nameInput.setPadding(25)

        val shortNameInput = EditText(dialogLayout.context)
        shortNameInput.layoutParams = lp
        shortNameInput.hint = "Аббревиатура"
        shortNameInput.setPadding(25)

        dialogLayout.addView(nameInput)
        dialogLayout.addView(shortNameInput)

        dialogBuilder.setView(dialogLayout)

        dialogBuilder.setTitle("Добавить кафедру")

        dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
            mainScope.launch {
                mAdapter.addItem(
                    ChairRequest(
                        Faculty = facultyId,
                        NameChair = nameInput.text.toString(),
                        ShortNameChair = shortNameInput.text.toString()
                    )
                )
            }
            dialog.cancel()
        }
        dialogBuilder.setNegativeButton("Отменить") { dialog, _ -> dialog.cancel() }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}