package com.dongy.w4l.Retrofit2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubService {
//    @GET("/users/{user}/repos")
//    fun retrieveRepositories(@Path("user") user: String): Call<List<Repository>>

    @GET("maps/api/place/nearbysearch/json")
    fun retrievePlaces(@Query("location")  location: String,
                       @Query("radius")  radius: String,
                       @Query("type")  type: String,
                       @Query("language")  language: String,
                       @Query("key")  key: String): Call<Spots>

//    https://maps.googleapis.com/maps/api/place/nearbysearch/json
//    ? location= 37.602592,126.654335
//      &radius=1000  
//    &type=restauran  t
//    &fields=contact
//      &key=AIzaSyD3H-668if2DLRN4CGKUFihpPH5Pgrv6_0

    @GET("maps/api/place/details/json")
    fun retrievePlaceDetails(@Query("place_id")  location: String,
                       @Query("fields")  radius: String,
                       @Query("key")  type: String): Call<Spots>

//    https://maps.googleapis.com/maps/api/place/details/json
//    ? place_id=ChIJDSNTFgCBfDURHNmgS49tDnY  
//    &fields=name,rating,url,types,formatted_phone_number,formatted_address  
//    &key=AIzaSyD3H-668if2DLRN4CGKUFihpPH5Pgrv6_0
}