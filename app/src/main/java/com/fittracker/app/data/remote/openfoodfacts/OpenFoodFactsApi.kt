package com.fittracker.app.data.remote.openfoodfacts

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") terms: String,
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 25,
        @Query("fields") fields: String = "code,product_name,product_name_es,brands,nutriments,image_url"
    ): Response<OpenFoodSearchResponse>

    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "code,product_name,product_name_es,brands,nutriments,image_url"
    ): Response<OpenFoodProductResponse>
}
