package com.grameenlight.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.grameenlight.domain.repository.StorageRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : StorageRepository {
    
    override suspend fun uploadComplaintPhoto(complaintId: String, photoUri: Uri): Result<String> {
        return try {
            val ref = firebaseStorage.reference.child("complaint_photos/$complaintId.jpg")
            ref.putFile(photoUri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
