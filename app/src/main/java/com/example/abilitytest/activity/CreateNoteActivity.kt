package com.example.abilitytest.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.abilitytest.R
import com.example.abilitytest.databinding.EditNoteLayoutBinding
import com.example.abilitytest.dataroom.CurrentUser
import com.example.abilitytest.dataroom.Note
import com.example.abilitytest.dataroom.NoteService
import com.example.abilitytest.utils.MessageUtil
import com.example.abilitytest.utils.Utils

class CreateNoteActivity: AppCompatActivity() {
    private lateinit var binding: EditNoteLayoutBinding
    private lateinit var service: NoteService
    private val msgUtil = MessageUtil(this)
    private var currentNote: Note? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditNoteLayoutBinding.inflate(layoutInflater)
        service = NoteService(this)
        setContentView(binding.root)

        // 监听返回按钮点击
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        // 判断当前是修改还是创建
        val noteId = intent.getIntExtra("noteId", -1)
        if (noteId == -1) {
            binding.topAppBar.setTitle(getString(R.string.create))
            // 保存笔记
            binding.save.apply {
                setText(R.string.save)
                setOnClickListener {
                    createNote()
                }
            }
        } else {
            // 更新笔记
            binding.topAppBar.setTitle(getString(R.string.update))
            currentNote = service.dao.findById(noteId)?.apply {
                binding.noteContent.setText(content)
                binding.save.apply {
                    setText(R.string.update)
                    setOnClickListener {
                        updateNote()
                    }
                }
            }
        }
    }

    private fun updateNote() {
        val text: String = binding.noteContent.text.toString()
        if (text.isEmpty()) {
            msgUtil.createErrorDialog(getString(R.string.noteEmpty))
            return
        }
        currentNote!!.content = text
        service.dao.update(currentNote!!)
        msgUtil.createToast(getString(R.string.update_success))
        finish()
    }

    private fun createNote() {
        val text: String = binding.noteContent.text.toString()
        if (text.isEmpty()) {
            msgUtil.createErrorDialog(getString(R.string.noteEmpty))
            return
        }
        service.dao.insert(Note(null, text, Utils.now(), CurrentUser.username))
        msgUtil.createToast(getString(R.string.create_success))
        finish()
    }
}