package com.example.abilitytest.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.abilitytest.FILEPATH
import com.example.abilitytest.R
import com.example.abilitytest.activity.LearnActivity
import com.example.abilitytest.databinding.EncyclopediaCardBinding
import com.example.abilitytest.databinding.FragmentEncyclopediaBinding
import com.example.abilitytest.utils.MessageUtil
import com.example.abilitytest.utils.Utils
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class EncyclopediaCard(
    val title: String,
    val content: String,
    val image: String,
    val video: String?,
)

class EncyclopediaFragment : Fragment() {
    private lateinit var binding: FragmentEncyclopediaBinding
    private lateinit var msgUtil: MessageUtil

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEncyclopediaBinding.inflate(layoutInflater, container, false)
        msgUtil = MessageUtil(requireContext())

        binding.searchView.editText.setOnEditorActionListener { _, _, _ ->
            binding.webView.apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                setWebChromeClient(object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, newProgress: Int) {
                        binding.indicator.progress = newProgress
                    }
                })
                loadUrl("https://www.baidu.com/s?wd=${binding.searchView.text}")
            }
            false
        }

        binding.recycler.apply {
            Utils.loadFileFromAssets(requireContext(), FILEPATH.KNOWLEDGE_JSON_FILE)?.also {
                try {
                    val cards = Json.decodeFromString<List<EncyclopediaCard>>(it)
                    adapter = EncyclopediaAdapter(requireContext(), cards)
                } catch (e: Exception) {
                    msgUtil.createErrorDialog("${getString(R.string.jsonDecodeError)}: -> ${e.message}")
                }
            }
            layoutManager = GridLayoutManager(requireContext(), 1)
        }

        return binding.root
    }
}

class EncyclopediaAdapter(
    private val context: Context,
    private val items: List<EncyclopediaCard>,
): RecyclerView.Adapter<EncyclopediaAdapter.Holder>() {
    inner class Holder(
        val binding: EncyclopediaCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        lateinit var card: EncyclopediaCard
        init {
            binding.root.setOnClickListener {
                context.startActivity(Intent(context, LearnActivity::class.java).apply {
                    putExtra("knowledge", Json.encodeToString(card))
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = EncyclopediaCardBinding.inflate(LayoutInflater.from(parent.context))
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.card = item
        holder.binding.apply {
            title.text = item.title
            content.text = item.content
            Glide.with(context).load(FILEPATH.IMAGE_URI + item.image).into(image)
        }
    }
}