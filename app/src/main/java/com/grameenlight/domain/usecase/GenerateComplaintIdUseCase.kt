package com.grameenlight.domain.usecase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateComplaintIdUseCase @Inject constructor() {
    private var sequence = 0

    operator fun invoke(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        sequence++
        val seqStr = String.format(Locale.getDefault(), "%04d", sequence)
        return "GL-$dateStr-$seqStr"
    }
}
