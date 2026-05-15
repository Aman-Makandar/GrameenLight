package com.grameenlight.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadComplaintPhoto(complaintId: String, photoUri: Uri): Result<String>
}
