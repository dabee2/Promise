package com.dabee.promise

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {

//    @GET("getMidLandFcst?serviceKey=Zb2rfa2mmu%2BbKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5%2FFuVfwFpls8uns2Q%3D%3D&numOfRows=10&pageNo=1&dataType=JSON&regId=11B00000&tmFc=202212020600")
//    fun searchData(): Call<String>

    @GET("getMidLandFcst?")
    fun searchWeather(@Query("serviceKey") serviceKey:String, @Query("dataType") dataType:String, @Query("regId") regId:String, @Query("tmFc") tmFc:String): Call<String>

    @GET("getMidTa?")
    fun searchTemperature(@Query("serviceKey") serviceKey:String, @Query("dataType") dataType:String, @Query("regId") regId:String, @Query("tmFc") tmFc:String): Call<String>

}