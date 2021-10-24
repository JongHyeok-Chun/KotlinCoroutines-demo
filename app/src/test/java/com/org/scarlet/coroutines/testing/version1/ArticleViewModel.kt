package com.org.scarlet.coroutines.testing.version1

import androidx.lifecycle.*
import com.org.scarlet.livedata.ApiService
import com.org.scarlet.model.Article
import com.org.scarlet.util.Resource
import kotlinx.coroutines.*

class ArticleViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob())

    private val _articles = MutableLiveData<Resource<List<Article>>>()
    val articles: LiveData<Resource<List<Article>>> = _articles

    fun onButtonClicked() {
        scope.launch {
            loadData()
        }
    }

    suspend fun loadData() {
        val articles = networkRequest()
        update(articles)
    }

    private suspend fun networkRequest(): Resource<List<Article>> {
        return apiService.getArticles()
    }

    private fun update(articles: Resource<List<Article>>) {
        _articles.value = articles
    }

    override fun onCleared() {
        super.onCleared()
        // make sure to call cancel()
        scope.cancel()
    }
}


