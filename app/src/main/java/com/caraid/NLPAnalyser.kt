/*
Callum Smith - S2145086
 */

package com.caraid

import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient

//This class is used to analyse the content of a message using Google Cloud Natural Language API.
class NlpAnalyser {

    companion object {
        //This function takes a string as input and returns a map of sentiment analysis results.
        fun analyseMessage(text: String): Map<String, Any> {
            val languageService = LanguageServiceClient.create()
            val doc = Document.newBuilder()
                .setContent(text)
                .setType(Document.Type.PLAIN_TEXT)
                .build()

            val sentiment = languageService.analyzeSentiment(doc).documentSentiment
            val entities = languageService.analyzeEntities(doc).entitiesList
            val entitySentiments = languageService.analyzeEntitySentiment(doc).entitiesList
            val classifications = languageService.classifyText(doc).categoriesList
            val tokens = languageService.analyzeSyntax(doc).tokensList

            languageService.close()

            return mapOf(
                "sentiment" to sentiment.score,
                "entities" to entities.map { it.name },
                "entitySentiments" to entitySentiments.associate { it.name to it.sentiment.score },
                "classifications" to classifications.map { it.name },
                "syntax" to tokens.map { "${it.text.content} (${it.partOfSpeech.tag})" }
            )
        }
    }
}