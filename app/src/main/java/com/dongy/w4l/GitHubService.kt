package com.dongy.w4l

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("/users/{user}/repos")
    fun retrieveRepositories(@Path("user") user: String): Call<List<Repository>>
}