package com.example.journalapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class AdviceRequest(val notes: List<Note>)
data class AdviceResponse(val advice: String)

interface ApiService {
    @POST("api/give-advice") // Relative path, without the leading slash
    fun getAdvice(@Body request: AdviceRequest): Call<AdviceResponse>

    @POST("api/track-mood")
    fun getMood(@Body request: AdviceRequest): Call<MoodResponse>
}
