package com.example.bowls

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

fun sendMatchFileToPhone(context: Context, fileName: String, fileContent: String) {
    Log.d("WearApp", "Attempting to send file: $fileName")
    val dataClient: DataClient = Wearable.getDataClient(context)
    val dataMapReq = PutDataMapRequest.create("/match_files")
    val dataMap = dataMapReq.dataMap
    dataMap.putString("file_name", fileName)
    dataMap.putString("file_content", fileContent)
    dataMapReq.setUrgent() // Optional: Mark the data as urgent to send immediately
    val putDataReq = dataMapReq.asPutDataRequest()
    dataClient.putDataItem(putDataReq)
        .addOnSuccessListener {
            Log.d("WearApp", "Successfully sent file: $fileName")
        }
        .addOnFailureListener { e ->
            Log.e("WearApp", "Failed to send file: $fileName", e)
        }
}