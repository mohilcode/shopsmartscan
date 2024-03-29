package com.example.test.api

import com.example.test.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class RakutenAPIClient : RakutenAPI {

    suspend fun fetchRakutenApiKey(): String? = withContext(Dispatchers.IO) {
        val encryptUtil = EncryptUtil

        val decryptedUrl = BuildConfig.WUpy2M3lmyPQtMj2LyFBdPRT.let { encryptUtil.decrypt(it) }
        val decryptedToken = BuildConfig.YMNAIjPwiClJnEkLqUzbLTUkM.let { encryptUtil.decrypt(it) }

        val apiUrl = "$decryptedUrl/RAKUTEN_API_KEY"
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection

        connection.setRequestProperty("Authorization", decryptedToken)

        try {
            connection.inputStream.bufferedReader().use {
                val json = it.readText()
                val jsonObject = JSONObject(json)
                return@withContext jsonObject.getString("api_key")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }

        return@withContext null
    }
    override suspend fun fetchProductInfo(barcodeValue: String): RakutenResponse? = withContext(Dispatchers.IO) {
        val apiKey = fetchRakutenApiKey() ?: return@withContext null
        val apiUrl = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20220601?format=json&applicationId=$apiKey&keyword=$barcodeValue"

        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.inputStream.bufferedReader().use {
                val json = it.readText()
                val jsonObject = JSONObject(json)
                val itemsArray = jsonObject.getJSONArray("Items")

                if (itemsArray.length() > 0) {
                    val firstItem = itemsArray.getJSONObject(0).getJSONObject("Item")
                    val itemName = firstItem.getString("itemName")
                    val itemCaption = firstItem.getString("itemCaption")
                    return@withContext RakutenResponse(itemName, itemCaption)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }

        return@withContext null
    }


}
