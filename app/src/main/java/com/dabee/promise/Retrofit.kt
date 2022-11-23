package com.dabee.promise

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface Retrofit {

    @GET("")
    fun weather()

//    @Headers("X-Naver-Client-Id: YjAQSFFHRk4yaZjAV7Es","X-Naver-Client-Secret: yZk4S3njKA")
//    @GET("/v1/search/shop.json?display=50")
//    fun searchData(@Query("query") query: String): Call<NaverSearchResponse>
}