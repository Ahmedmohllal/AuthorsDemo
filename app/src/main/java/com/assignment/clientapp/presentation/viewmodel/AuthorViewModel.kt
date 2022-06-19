package com.assignment.clientapp.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.assignment.clientapp.presentation.core.BaseViewModel
import com.assignment.clientapp.presentation.core.savePostsListToDataStore
import com.assignment.clientapp.presentation.core.wrapper.StateLiveData
import com.assignment.domain.model.AuthorsDomainResponse
import com.assignment.domain.model.AuthorsDomainResponseItem
import com.assignment.domain.model.PostsDomainResponseItem
import com.assignment.domain.usecase.GetAuthorsListUseCase
import com.assignment.domain.usecase.GetPostsListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
open class AuthorViewModel @Inject constructor(
    private val authorsUseCase: GetAuthorsListUseCase,
    private val postsUseCase: GetPostsListUseCase
) : BaseViewModel() {

    val authorsLiveData by lazy { StateLiveData<List<AuthorsDomainResponseItem>?>() }
    val postsLiveData by lazy { StateLiveData<List<PostsDomainResponseItem>?>() }
    fun getAuthorsList() {
        viewModelScope.launch {
            val authorList = authorsUseCase.getAuthors()
            authorsLiveData.postSuccess(authorList.authorsDomainResponse)
        }
    }

    fun getAuthorsFromStorage() {
        viewModelScope.launch {
            val authorList = authorsUseCase.getAuthorsFromStorage()
            authorsLiveData.postSuccess(authorList)
        }
    }


    fun getPostsForAuthor(authorId: String, context: Context) {

        viewModelScope.launch {
            postsUseCase.getPostsForUser(authorId)
                .onStart {
                    postsLiveData.postLoading()
                }
                .catch { error ->
                    postsLiveData.postError(error)
                }
                .collect {
                    postsLiveData.postSuccess(it.postsDomainResponse)
                    //save posts into jetpack dataStore
                    savePostsListToDataStore(context, authorId, it)
                }
        }


    }


}