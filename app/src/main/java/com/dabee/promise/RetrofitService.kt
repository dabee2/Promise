package com.dabee.promise

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {

//    @GET("getMidLandFcst?serviceKey=Zb2rfa2mmu+bKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5/FuVfwFpls8uns2Q==&pageNo=1&numOfRows=10&dataType=JSON&regId=11B00000&tmFc=202211301800")
//    fun searchData(): Call<WeatherItem>

    @GET("getMidLandFcst")
    fun searchData(@Query("serviceKey") serviceKey:String, @Query("dataType") dataType:String, @Query("regId") regId:String, @Query("tmFc") tmFc:String): Call<WeatherItem>

}