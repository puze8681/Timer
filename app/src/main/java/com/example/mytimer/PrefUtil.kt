package com.example.mytimer

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONException

class PrefUtil(context: Context) {
    private var preferences: SharedPreferences = context.getSharedPreferences("Data", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = preferences.edit()

    var ALARM_NAME = "ALARM_NAME"
    var ALARM_TIME = "ALARM_TIME"
    var STOP_NAME = "STOP_NAME"
    var STOP_TIME = "STOP_TIME"
    var COUNT_NAME = "COUNT_NAME"
    var COUNT_TIME = "COUNT_TIME"

    var isTimer : Boolean
        get() = preferences.getBoolean("isTimer", false)
        set(value) {
            editor.putBoolean("isTimer", value)
            editor.apply()
        }

    fun setStringArrayPref(key: String, values: ArrayList<String>) {
        val a = JSONArray()
        for (i in 0 until values.size) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    fun setLongArrayPref(key: String, values: ArrayList<Long>) {
        val a = JSONArray()
        for (i in 0 until values.size) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    fun getStringArrayPref(key: String): ArrayList<String> {
        val json = preferences.getString(key, null)
        val urls = ArrayList<String>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optString(i)
                    urls.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return urls
    }

    fun getLongArrayPref(key: String): ArrayList<Long> {
        val json = preferences.getString(key, null)
        val datas = ArrayList<Long>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optLong(i)
                    datas.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return datas
    }

    fun addStringArrayPref(key: String, values: String){
        val list = getStringArrayPref(key)
        list.add(values)
        setStringArrayPref(key, list)
    }

    fun addLongArrayPref(key: String, values: Long){
        val list = getLongArrayPref(key)
        list.add(values)
        setLongArrayPref(key, list)
    }

    fun deleteIntArrayPref(key: String, position: Int){
        val list = getStringArrayPref(key)
        list.removeAt(position)
        setStringArrayPref(key, list)
    }

    fun deleteLongArrayPref(key: String, values: Long){
        val list = getLongArrayPref(key)
        list.remove(values)
        setLongArrayPref(key, list)
    }

    fun deleteStringArrayPref(key: String, values: String){
        val list = getStringArrayPref(key)
        list.remove(values)
        setStringArrayPref(key, list)
    }

    fun removeArrayPref(key: String){
        Log.d("LOGTAG", "remove $key")
        editor.remove(key)
    }

    fun resetPref(){
        preferences.edit().clear().commit()
        editor.clear().commit()
    }
}