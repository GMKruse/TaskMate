package com.example.taskmate.services

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class QuoteResponse(
    @SerializedName("q") val quote: String,
    @SerializedName("a") val author: String,
    @SerializedName("h") val html: String
)

interface ZenQuotesApi {
    @GET("api/today")
    fun getTodayQuote(): Call<List<QuoteResponse>>
}

interface IQuoteService {
    fun fetchTodayQuote(onSuccess: (String) -> Unit, onError: (String) -> Unit)
}

class QuoteService : IQuoteService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ZenQuotesApi::class.java)

    override fun fetchTodayQuote(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        api.getTodayQuote().enqueue(object : Callback<List<QuoteResponse>> {
            override fun onResponse(
                call: Call<List<QuoteResponse>>,
                response: Response<List<QuoteResponse>>
            ) {
                if (response.isSuccessful) {
                    val quotes = response.body()
                    if (!quotes.isNullOrEmpty()) {
                        val quoteText = "${quotes[0].quote} - ${quotes[0].author}"
                        onSuccess(quoteText)
                    } else {
                        onError("No quote available")
                    }
                } else {
                    onError("Failed to fetch quote: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<QuoteResponse>>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }
}