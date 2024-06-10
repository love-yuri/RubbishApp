package com.example.abilitytest.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.abilitytest.R
import com.example.abilitytest.activity.CreateNoteActivity
import com.example.abilitytest.databinding.FragmentNoteBinding
import com.example.abilitytest.databinding.NoteCardBinding
import com.example.abilitytest.dataroom.CurrentUser
import com.example.abilitytest.dataroom.Note
import com.example.abilitytest.dataroom.NoteService
import com.example.abilitytest.utils.MessageUtil
import org.w3c.dom.Text

class NoteFragment: Fragment() {
    private lateinit var binding: FragmentNoteBinding
    private lateinit var msgUtil: MessageUtil
    private lateinit var service: NoteService
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteBinding.inflate(inflater, container, false)
        service = NoteService(requireContext())
        msgUtil = MessageUtil(requireContext())

        // 绑定写入事件
        binding.write.apply {
            setOnClickListener {
                this@NoteFragment.startActivity(Intent(activity, CreateNoteActivity::class.java))
            }
        }

        return binding.root
    }

    override fun onResume() {
        binding.recycler.apply {
            layoutManager = GridLayoutManager(activity, 1, GridLayoutManager.VERTICAL, false)
            adapter = NoteAdapter(service.dao.getAll(CurrentUser.username), { note ->
                service.dao.delete(note)
                msgUtil.createToast(getString(R.string.deleteSuccess))
                this@NoteFragment.onResume()
            },{ note ->
                this@NoteFragment.startActivity(Intent(activity, CreateNoteActivity::class.java).apply {
                    putExtra("noteId", note.id)
                })
            })
        }
        super.onResume()
    }
}

class NoteAdapter(
    private val noteList: List<Note>,
    val deleteFun: (Note) -> Unit,
    val editFun: (Note) -> Unit
): RecyclerView.Adapter<NoteAdapter.Holder>() {
    class Holder(val binding: NoteCardBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = NoteCardBinding.inflate(LayoutInflater.from(parent.context))
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = noteList[position]
        holder.binding.title.text = item.update
        holder.binding.content.text = item.content
        holder.binding.edit.setOnClickListener {
            editFun(item)
        }

        holder.binding.delete.setOnClickListener {
            deleteFun(item)
        }
    }
}