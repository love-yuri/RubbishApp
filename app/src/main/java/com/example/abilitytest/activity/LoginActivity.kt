package com.example.abilitytest.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.abilitytest.R
import com.example.abilitytest.databinding.LoginLayoutBinding
import com.example.abilitytest.FILEPATH
import com.example.abilitytest.dataroom.User
import com.example.abilitytest.dataroom.UserService
import com.example.abilitytest.utils.MessageUtil
import com.example.abilitytest.utils.SharedPreferencesUtil
import com.example.abilitytest.utils.USER_SP
import com.example.abilitytest.utils.Utils
import com.google.android.material.snackbar.Snackbar


class LoginActivity: AppCompatActivity() {
    enum class Mode {
        LOGIN,
        REGISTER
    }

    private var runMode = Mode.LOGIN
    private lateinit var binding: LoginLayoutBinding
    private val msgUtil = MessageUtil(this)
    private val spUtil = SharedPreferencesUtil(this)
    private lateinit var service: UserService
    private lateinit var avatar: String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = LoginLayoutBinding.inflate(layoutInflater)
        service = UserService(this)

        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        // 获取运行状态
        runMode = intent.getStringExtra("mode")?.let(Mode::valueOf) ?: Mode.LOGIN

        // 获取图片launcher
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    Utils.copyFile(this, uri, FILEPATH.AVATAR) { path ->
                        path?.also {
                            avatar = path
                            // 使用 URI 处理图片
                            binding.avatarTips.visibility = TextView.GONE
                            binding.avatar.visibility = ImageView.VISIBLE
                            binding.avatar.setImageURI(uri)
                        }
                    }
                }
            }
        }

        when(runMode) {
            Mode.REGISTER -> {
                val upload = {
                    msgUtil.createToast("上传图片")
                    launcher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    })
                }
                // 点击头像上传图片
                binding.rememberPasswordCheckBox.visibility = CheckBox.GONE
                binding.avatarTips.setOnClickListener { upload() }
                binding.avatar.setOnClickListener { upload() }
            }

            Mode.LOGIN -> {
                binding.rememberPasswordCheckBox.visibility = CheckBox.VISIBLE
            }
        }

        // 处理自动登录
        autoLogin()

        // 检测登录状态 - 点击登录按钮
        binding.loginRegister.apply {
            text = when(runMode){
                Mode.LOGIN -> getString(R.string.login)
                Mode.REGISTER -> getString(R.string.register)
            }

            setOnClickListener {
                when(runMode) {
                    Mode.LOGIN -> login()
                    Mode.REGISTER -> register()
                }
            }
        }


        // 去登陆
        binding.goLogin.apply {
            var nextMode = Mode.LOGIN
            if (runMode == Mode.LOGIN) {
                nextMode = Mode.REGISTER
                binding.goLogin.text = getString(R.string.goRegister)
            }
            setOnClickListener {
                startActivity(Intent(this@LoginActivity, LoginActivity::class.java).apply {
                    putExtra("mode", nextMode.name)
                })
                finish()
            }
        }
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo(): User? {
        if (!::avatar.isInitialized) {
            msgUtil.createErrorDialog(getString(R.string.please_upload_avatar))
            return null
        }

        if (
            !msgUtil.valueCheck(binding.username.text, getString(R.string.usernameEmpty)) ||
            !msgUtil.valueCheck(binding.password.text, getString(R.string.passwordEmpty))
        ) {
            return null
        }

        val user = User(
            binding.username.text.toString(),
            binding.password.text.toString(),
            avatar
        )
        return user
    }

    /**
     * 登录
     */
    private fun login() {
        getUserInfo()?.also {
            service.dao.findByUserName(it.username)?.also { user ->
                if (user.password != it.password) {
                    msgUtil.createErrorDialog(getString(R.string.passwordError))
                    return
                }
                startActivity(Intent(this, MainMenuActivity::class.java))
                finish()
            }
            if (binding.rememberPasswordCheckBox.isChecked) {
                spUtil.set(SharedPreferencesUtil.Type.USER, USER_SP.AUTO_LOGIN_USERNAME, it.username)
            } else {
                spUtil.remove(SharedPreferencesUtil.Type.USER, USER_SP.AUTO_LOGIN_USERNAME)
            }
            spUtil.set(SharedPreferencesUtil.Type.USER, USER_SP.CURRENT_USERNAME, it.username)
            msgUtil.createToast(getString(R.string.login_success))
        }
    }

    /**
     * 注册
     */
    private fun register() {
        getUserInfo()?.let {
            service.dao.findByUserName(it.username)?.also {
                binding.usernameLayout.error = getString(R.string.userIsExist)
                return
            }

            binding.usernameLayout.error = null

            service.dao.insert(it)

            Snackbar.make(binding.root, getString(R.string.register_success), Snackbar.LENGTH_LONG).setAction(getString(R.string.goLogin)) {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    putExtra("mode", Mode.LOGIN.name)
                })
                finish()
            }.show()
        }

    }

    /**
     * 自动登录
     */
    private fun autoLogin() {
        // 处理头像自动显示
        binding.username.addTextChangedListener {
            service.dao.findByUserName(it.toString())?.apply {
                this@LoginActivity.avatar = avatar
                Glide.with(this@LoginActivity).load(avatar).into(binding.avatar)
                binding.avatarTips.visibility = TextView.GONE
                binding.avatar.visibility = ImageView.VISIBLE
            }
        }
        if (runMode == Mode.LOGIN) {
            spUtil.get(SharedPreferencesUtil.Type.USER, USER_SP.AUTO_LOGIN_USERNAME)?.also {
                service.dao.findByUserName(it)?.also { user ->
                    binding.avatarTips.visibility = TextView.GONE
                    binding.avatar.visibility = ImageView.VISIBLE
                    binding.rememberPasswordCheckBox.isChecked = true
                    avatar = user.avatar

                    binding.username.setText(user.username)
                    binding.password.setText(user.password)
                    BitmapFactory.decodeFile(user.avatar)?.also { img ->
                        binding.avatar.setImageBitmap(img)
                    }
                }
            }
        }
    }
}