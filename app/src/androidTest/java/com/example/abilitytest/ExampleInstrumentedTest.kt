package com.example.abilitytest

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.abilitytest.dataroom.User
import com.example.abilitytest.dataroom.UserService
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var service: UserService

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        service = UserService(context)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        service.dao.getAll().forEach {
            Log.i("yuri", "username: ${it.username}")
        }
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        val user = User("hhh", "sfsdf")
        service.dao.insert(user)
    }
}