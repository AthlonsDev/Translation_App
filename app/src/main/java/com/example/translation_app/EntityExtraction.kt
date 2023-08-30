package com.example.translation_app

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.translation_app.Constants.TAG
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.nl.entityextraction.DateTimeEntity
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.nl.entityextraction.FlightNumberEntity
import com.google.mlkit.nl.entityextraction.MoneyEntity


class EntityExtraction: AppCompatActivity() {

    fun initEntityExtraction() {
        val entityExtractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                .build()
        )

//        Download Model to the Device
        entityExtractor.downloadModelIfNeeded()
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start accessing the model now.
                // ...
                extractEntities("My flight is LX373, please pick me up at 8am tomorrow.", entityExtractor)
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                // ...
            }



    }

    fun extractEntities(input: String, entityExtractor: EntityExtractor) {
        entityExtractor
            .downloadModelIfNeeded()
            .onSuccessTask {
                entityExtractor.annotate(input)
            }
            .addOnFailureListener { e: Exception? ->
                Log.e(TAG, "Annotation failed", e)
            }
            .addOnSuccessListener { result: List<EntityAnnotation> ->
                if (result.isEmpty()) {
                    return@addOnSuccessListener
                }
                for (entityAnnotation in result) {
                    val entities = entityAnnotation.entities
                    val annotatedText = entityAnnotation.annotatedText
                    for (entity in entities) {
//                        displayEntityInfo(annotatedText, entity)
//                        output.append("\n")
                        Log.d(TAG, "Entity: $entity")
                        Log.d(TAG, "Annotated text: $annotatedText")
                        when (entity) {
                            is DateTimeEntity -> {
                                Log.d(TAG, "DateTimeEntity: ${entity.asDateTimeEntity()}")
                                Log.d(TAG, "DateTimeEntity: ${entity.asDateTimeEntity()}")

                            }
                            is FlightNumberEntity -> {
                                Log.d(TAG, "FlightNumberEntity: ${entity.airlineCode}")
                                Log.d(TAG, "FlightNumberEntity: ${entity.flightNumber}")
                            }
                            is MoneyEntity -> {
                                Log.d(TAG, "MoneyEntity: ${entity.asMoneyEntity()}")
                                Log.d(TAG, "MoneyEntity: ${entity.asMoneyEntity()}")
                            }
                            else -> {
                                Log.d(TAG, "Entity: $entity")
                            }
                        }
                    }
                }
            }
    }
}

