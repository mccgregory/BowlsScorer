package com.example.bowls

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.io.File

fun sendMatchFileToPhone(
    context: Context,
    fileName: String,
    fileContent: String,
    deleteOnSuccess: Boolean = false,
    onResult: (Boolean, String?) -> Unit = { _, _ -> }
) {
    if (fileContent.isBlank()) {
        Log.w("WearApp", "File content is empty for $fileName, skipping send")
        onResult(false, "File content is empty")
        return
    }

    Log.d("WearApp", "Attempting to send file: $fileName")
    val dataClient: DataClient = Wearable.getDataClient(context)
    val dataMapReq = PutDataMapRequest.create("/match_files/$fileName")
    val dataMap = dataMapReq.dataMap
    dataMap.putString("match_data", fileContent)
    dataMap.putLong("timestamp", System.currentTimeMillis())
    dataMapReq.setUrgent()
    val putDataReq = dataMapReq.asPutDataRequest()
    dataClient.putDataItem(putDataReq)
        .addOnSuccessListener {
            Log.d("WearApp", "Successfully sent file: $fileName")
            if (deleteOnSuccess) {
                val file = File(context.filesDir, fileName)
                if (file.exists()) {
                    file.delete()
                    Log.d("WearApp", "Deleted file: $fileName")
                }
            }
            onResult(true, null)
        }
        .addOnFailureListener { e ->
            Log.e("WearApp", "Failed to send file: $fileName", e)
            onResult(false, e.message)
        }
}