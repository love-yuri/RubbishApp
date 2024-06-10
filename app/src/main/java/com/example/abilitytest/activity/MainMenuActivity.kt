package com.example.abilitytest.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.abilitytest.R
import com.example.abilitytest.databinding.ActivityMainBinding
import com.example.abilitytest.dataroom.CurrentUser
import com.example.abilitytest.dataroom.User
import com.example.abilitytest.dataroom.UserService
import com.example.abilitytest.fragment.EncyclopediaFragment
import com.example.abilitytest.fragment.NoteFragment
import com.example.abilitytest.fragment.QuestionsFragment
import com.example.abilitytest.fragment.SettingsFragment
import com.example.abilitytest.utils.MessageUtil
import com.example.abilitytest.utils.SharedPreferencesUtil
import com.example.abilitytest.utils.USER_SP

class MainMenuActivity : AppCompatActivity() {
    private val msgUtil = MessageUtil(this)
    private lateinit var binding: ActivityMainBinding
    private lateinit var spUtil: SharedPreferencesUtil
    private lateinit var service: UserService

    private val fragmentMap = mapOf(
        R.id.questions to QuestionsFragment(),
        R.id.note to NoteFragment(),
        R.id.encyclopedia to EncyclopediaFragment(),
        R.id.settings to SettingsFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        spUtil = SharedPreferencesUtil(this)
        service = UserService(this)

        setContentView(binding.root)
        // 设置初始fragment
        changeFragment(fragmentMap[R.id.questions]!!, true)

        // 切换fragment
        binding.bottomNavigation.setOnItemSelectedListener {
            changeFragment(fragmentMap[it.itemId]!!)
            true
        }

        val settingsFragment = fragmentMap[R.id.settings] as SettingsFragment
        settingsFragment.logOutListener = {
            spUtil.remove(SharedPreferencesUtil.Type.USER, USER_SP.CURRENT_USERNAME)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 初始化用户
        initUser()
    }

    private fun initUser() {
        val username = spUtil.get(SharedPreferencesUtil.Type.USER, USER_SP.CURRENT_USERNAME) ?: run {
            TODO("当前没有登录用户")
        }

        service.dao.findByUserName(username)?.also {
            CurrentUser.username = it.username
            CurrentUser.avatar = it.avatar
            CurrentUser.password = it.password
        } ?: run {
            spUtil.remove(SharedPreferencesUtil.Type.USER, USER_SP.CURRENT_USERNAME)
            TODO("不存在的用户")
        }

    }

    /**
     * 切换fragment
     */
    private fun changeFragment(next: Fragment, isInit: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_left,  // 进入动画
                R.anim.slide_out_left,  // 退出动画
            )
            if (isInit) {
                add(R.id.frameLayout, next)
            } else {
                replace(R.id.frameLayout, next)
            }
            commit()
        }
    }
}