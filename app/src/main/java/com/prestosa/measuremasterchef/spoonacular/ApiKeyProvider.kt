package com.prestosa.measuremasterchef.spoonacular

import android.content.Context
import com.prestosa.measuremasterchef.R
import java.io.IOException
import java.util.Properties

object ApiKeyProvider {

    fun getApiKey(context: Context): String {
        val properties = Properties()
        try {
            // Load the properties file from the raw resources
            val rawResource = context.resources.openRawResource(R.raw.spoonacular)
            properties.load(rawResource)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Get the API key value
        return properties.getProperty("spoonacular_api_key", "")
    }
}
