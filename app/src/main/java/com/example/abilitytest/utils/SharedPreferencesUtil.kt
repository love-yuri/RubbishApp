package com.example.abilitytest.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil(
    private val context: Context
) {
    private val mutableMap = mutableMapOf<Type, SharedPreferences>()
    private val msgUtil = MessageUtil(context)
    enum class Type {
        USER, // 和用户相关的信息
    }

    /**
     * 获取 SharedPreferences 实例
     * 需要传入type
     */
    fun get(type: Type): SharedPreferences {
        if (!mutableMap.containsKey(type)) {
            mutableMap[type] = context.getSharedPreferences(type.name, Context.MODE_PRIVATE)
        }
        return mutableMap.getValue(type)
    }

    /**
     * 获取实例内容
     * 默认返回null
     */
    fun get(type: Type, key: Any, default: String? = null): String? {
        if (!mutableMap.containsKey(type)) {
            mutableMap[type] = context.getSharedPreferences(type.name, Context.MODE_PRIVATE)
        }
        return mutableMap.getValue(type).getString(key.toString(), default)
    }

    /**
     * 设置实例内容
     */
    fun set(type: Type, key: Any, value: String) {
        if (!mutableMap.containsKey(type)) {
            mutableMap[type] = context.getSharedPreferences(type.name, Context.MODE_PRIVATE)
        }
        mutableMap.getValue(type).edit().apply {
            putString(key.toString(), value)
            commit()
        }
    }

    /**
     * 删除实列内容
     * 传入key
     */
    fun remove(type: Type, key: Any) {
        if (!mutableMap.containsKey(type)) {
            mutableMap[type] = context.getSharedPreferences(type.name, Context.MODE_PRIVATE)
        }
        mutableMap.getValue(type).edit().apply {
            remove(key.toString())
            commit()
        }
    }
}

// user相关的内容
enum class USER_SP {
    AUTO_LOGIN_USERNAME, // 当前自动登录用户名
    CURRENT_USERNAME, // 当前登录的用户名
}