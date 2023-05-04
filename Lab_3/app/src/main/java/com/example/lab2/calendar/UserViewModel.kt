package com.example.lab2.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserViewModel @Inject constructor(): ViewModel() {
    var user = MutableLiveData<User>(User())

    fun setUser(value: User) {
        user.value = value
    }

    fun getUser(): User {
        return user.value!!
    }

}