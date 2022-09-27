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
import com.example.ktor_klient.adapters.FacultyItemViewAdapter
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Faculties
import com.example.ktor_klient.components.SwipeActionsCallback
import com.example.ktor_klient.databinding.ActivityMainBinding
import com.example.ktor_klient.models.Faculty
import com.example.ktor_klient.models.FacultyRequest
import com.google.android.material.snackbar.Snackbar
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val mainScope = MainScope()

    private lateinit var mAdapter: FacultyItemViewAdapter
    private lateinit var b: ActivityMainBinding
    private val facultyItemsList: MutableList<Faculty> = mutableListOf()
    var recyclerView: RecyclerView? = null
    var constraintLayout: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        mainScope.launch {
            fetchFaculties()
        }
        setUpAdapter()
        enableSwipeToDeleteAndUndo()

        b.floatingActionButton.setOnClickListener {
            addFaculty()
        }
    }

    private fun setUpAdapter() {
        recyclerView = findViewById(R.id.FacultiesList)
        constraintLayout = findViewById(R.id.constraintLayout)
        mAdapter = FacultyItemViewAdapter(this, facultyItemsList)
        b.FacultiesList.adapter = mAdapter
        b.FacultiesList.layoutManager = LinearLayoutManager(this)
    }

    private suspend fun fetchFaculties() {
        val api = ApiFactory.getClient()
        kotlin.runCatching {
            api.get(Faculties()).body<List<Faculty>>()
        }.onSuccess {
            facultyItemsList.addAll(it)
            mAdapter.notifyDataSetChanged()
        }.onFailure {
            Log.e("REQUEST", it.toString())
            Toast.makeText(this, "Ошибка сервера!", Toast.LENGTH_LONG).show()
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
                    val dialogBuilder = AlertDialog.Builder(this@MainActivity)
                    val dialogLayout = LinearLayout(
                        this@MainActivity
                    )
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    dialogLayout.layoutParams = lp
                    dialogLayout.orientation = LinearLayout.VERTICAL

                    val nameInput = EditText(dialogLayout.context)
                    nameInput.layoutParams=lp
                    nameInput.hint="Наименование"
                    nameInput.setPadding(10)
                    nameInput.height = 15

                    val shortNameInput = EditText(dialogLayout.context)
                    shortNameInput.layoutParams=lp
                    shortNameInput.hint="Аббревиатура"
                    shortNameInput.setPadding(10)
                    shortNameInput.height = 15

                    dialogLayout.addView(nameInput)
                    dialogLayout.addView(shortNameInput)

                    dialogBuilder.setView(dialogLayout)

                    nameInput.text = Editable.Factory.getInstance().newEditable(item.NameFaculty)
                    shortNameInput.text =
                        Editable.Factory.getInstance().newEditable(item.ShortNameFaculty)

                    dialogBuilder.setTitle("Редактировать факультет: ${item.ShortNameFaculty}")

                    dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
                        mainScope.launch {
                            mAdapter.editItem(
                                Faculty(
                                    Id = item.Id,
                                    NameFaculty = nameInput.text.toString(),
                                    ShortNameFaculty = shortNameInput.text.toString()
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

    fun addFaculty(){
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        val dialogLayout = LinearLayout(
            this@MainActivity
        )
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        dialogLayout.layoutParams = lp
        dialogLayout.orientation = LinearLayout.VERTICAL

        val nameInput = EditText(dialogLayout.context)
        nameInput.layoutParams=lp
        nameInput.hint="Наименование"
        nameInput.setPadding(10)

        val shortNameInput = EditText(dialogLayout.context)
        shortNameInput.layoutParams=lp
        shortNameInput.hint="Аббревиатура"
        shortNameInput.setPadding(10)

        dialogLayout.addView(nameInput)
        dialogLayout.addView(shortNameInput)

        dialogBuilder.setView(dialogLayout)

        dialogBuilder.setTitle("Добавить факультет")

        dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
            mainScope.launch {
                mAdapter.addItem(
                    FacultyRequest(
                        NameFaculty = nameInput.text.toString(),
                        ShortNameFaculty = shortNameInput.text.toString()
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