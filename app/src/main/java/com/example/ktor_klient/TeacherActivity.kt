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
import com.example.ktor_klient.adapters.TeacherItemViewAdapter
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Teachers
import com.example.ktor_klient.components.SwipeActionsCallback
import com.example.ktor_klient.databinding.TeacherListBinding
import com.example.ktor_klient.models.Teacher
import com.example.ktor_klient.models.TeacherRequest
import com.google.android.material.snackbar.Snackbar
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class TeacherActivity : AppCompatActivity() {
    private val mainScope = MainScope()

    private lateinit var mAdapter: TeacherItemViewAdapter
    private lateinit var b: TeacherListBinding
    private val teachersItemsList: MutableList<Teacher> = mutableListOf()
    var recyclerView: RecyclerView? = null
    var constraintLayout: RelativeLayout? = null
    var facultyId: Int = -1
    var chairId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = TeacherListBinding.inflate(layoutInflater)
        setContentView(b.root)
        val props = intent.extras
        if (props != null) {
            facultyId = props.getInt("faculty")
            chairId = props.getInt("chair")
        }

        mainScope.launch {
            fetchTeachers()
        }
        setUpAdapter()
        enableSwipeToDeleteAndUndo()

        b.floatingActionButton.setOnClickListener {
            addTeacher()
        }
    }

    private fun setUpAdapter() {
        recyclerView = findViewById(R.id.TeachersList)
        constraintLayout = findViewById(R.id.constraintLayout)
        mAdapter = TeacherItemViewAdapter(this, teachersItemsList)
        b.TeachersList.adapter = mAdapter
        b.TeachersList.layoutManager = LinearLayoutManager(this)
    }

    private suspend fun fetchTeachers() {
        if (chairId > -1) {
            val api = ApiFactory.getClient()
            kotlin.runCatching {
                api.get(Teachers.Chair.Id(id = chairId)).body<List<Teacher>>()
            }.onSuccess {
                Log.i("RESPONSE", it.toString())
                teachersItemsList.addAll(it)
                mAdapter.notifyDataSetChanged()
            }.onFailure {
                Log.e("REQUEST", it.toString())
                Toast.makeText(this, "Ошибка сервера TUT!", Toast.LENGTH_LONG).show()
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
                    val dialogBuilder = AlertDialog.Builder(this@TeacherActivity)
                    val dialogLayout = LinearLayout(
                        this@TeacherActivity
                    )
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    dialogLayout.layoutParams = lp
                    dialogLayout.orientation = LinearLayout.VERTICAL

                    val secondNameInput = EditText(dialogLayout.context)
                    secondNameInput.layoutParams = lp
                    secondNameInput.hint = "Фамилия"
                    secondNameInput.setPadding(25)

                    val firstNameInput = EditText(dialogLayout.context)
                    firstNameInput.layoutParams = lp
                    firstNameInput.hint = "Имя"
                    firstNameInput.setPadding(25)

                    val lastNameInput = EditText(dialogLayout.context)
                    lastNameInput.layoutParams = lp
                    lastNameInput.hint = "Отчество"
                    lastNameInput.setPadding(25)

                    val phoneInput = EditText(dialogLayout.context)
                    phoneInput.layoutParams = lp
                    phoneInput.hint = "Телефон"
                    phoneInput.setPadding(10)

                    val emailInput = EditText(dialogLayout.context)
                    emailInput.layoutParams = lp
                    emailInput.hint = "E-mail"
                    emailInput.setPadding(25)

                    dialogLayout.addView(secondNameInput)
                    dialogLayout.addView(firstNameInput)
                    dialogLayout.addView(lastNameInput)
                    dialogLayout.addView(phoneInput)
                    dialogLayout.addView(emailInput)

                    dialogBuilder.setView(dialogLayout)

                    firstNameInput.text = Editable.Factory.getInstance().newEditable(item.FirstName)
                    secondNameInput.text =
                        Editable.Factory.getInstance().newEditable(item.SecondName)
                    lastNameInput.text =
                        Editable.Factory.getInstance().newEditable(item.LastName)
                    phoneInput.text =
                        Editable.Factory.getInstance().newEditable(item.Email)
                    emailInput.text =
                        Editable.Factory.getInstance().newEditable(item.Phone)

                    dialogBuilder.setTitle("Редактировать преподавателя: ${item.SecondName} ${item.FirstName}")

                    dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
                        mainScope.launch {
                            mAdapter.editItem(
                                Teacher(
                                    Id = item.Id,
                                    Chair = item.Chair,
                                    Post = item.Post,
                                    FirstName = firstNameInput.text.toString(),
                                    SecondName = secondNameInput.text.toString(),
                                    LastName = lastNameInput.text.toString(),
                                    Phone = phoneInput.text.toString(),
                                    Email = emailInput.text.toString()
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

    private fun addTeacher() {
        val dialogBuilder = AlertDialog.Builder(this@TeacherActivity)
        val dialogLayout = LinearLayout(
            this@TeacherActivity
        )
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        dialogLayout.layoutParams = lp
        dialogLayout.orientation = LinearLayout.VERTICAL

        val secondNameInput = EditText(dialogLayout.context)
        secondNameInput.layoutParams = lp
        secondNameInput.hint = "Фамилия"
        secondNameInput.setPadding(25)

        val firstNameInput = EditText(dialogLayout.context)
        firstNameInput.layoutParams = lp
        firstNameInput.hint = "Имя"
        firstNameInput.setPadding(25)

        val lastNameInput = EditText(dialogLayout.context)
        lastNameInput.layoutParams = lp
        lastNameInput.hint = "Отчество"
        lastNameInput.setPadding(25)

        val phoneInput = EditText(dialogLayout.context)
        phoneInput.layoutParams = lp
        phoneInput.hint = "Телефон"
        phoneInput.setPadding(25)

        val emailInput = EditText(dialogLayout.context)
        emailInput.layoutParams = lp
        emailInput.hint = "E-mail"
        emailInput.setPadding(25)

        dialogLayout.addView(secondNameInput)
        dialogLayout.addView(firstNameInput)
        dialogLayout.addView(lastNameInput)
        dialogLayout.addView(phoneInput)
        dialogLayout.addView(emailInput)

        dialogBuilder.setView(dialogLayout)

        dialogBuilder.setTitle("Добавить преподавателя")

        dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
            mainScope.launch {
                mAdapter.addItem(
                    TeacherRequest(
                        Chair = chairId,
                        Post = 1,
                        FirstName = firstNameInput.text.toString(),
                        SecondName = secondNameInput.text.toString(),
                        LastName = lastNameInput.text.toString(),
                        Phone = phoneInput.text.toString(),
                        Email = emailInput.text.toString()
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