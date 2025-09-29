package com.rewardsbot.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

class SearchGenerator {
    
    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .build()
    
    // Categorias de palavras para gerar pesquisas interessantes
    private val topics = listOf(
        "tecnologia", "ciência", "saúde", "esportes", "entretenimento", "viagem", "culinária",
        "educação", "história", "arte", "música", "cinema", "livros", "jogos", "natureza",
        "animais", "carros", "moda", "fotografia", "arquitetura", "política", "economia"
    )
    
    private val adjectives = listOf(
        "melhor", "novo", "interessante", "incrível", "popular", "famoso", "importante",
        "útil", "moderno", "clássico", "avançado", "simples", "complexo", "criativo",
        "inovador", "tradicional", "contemporâneo", "digital", "virtual", "inteligente"
    )
    
    private val questions = listOf(
        "como funciona", "o que é", "por que", "quando", "onde encontrar",
        "como fazer", "qual melhor", "como aprender", "como usar", "benefícios de",
        "história de", "futuro da", "tendências em", "novidades em", "dicas de"
    )
    
    private val currentEvents = listOf(
        "Copa do Mundo 2024", "Olimpíadas Paris", "eleições", "IA artificial",
        "sustentabilidade", "mudanças climáticas", "energia renovável", "criptomoedas",
        "realidade virtual", "carros elétricos", "exploração espacial", "medicina moderna"
    )
    
    // Palavras populares e trending
    private val trendingWords = listOf(
        "ChatGPT", "inteligência artificial", "metaverso", "NFT", "blockchain",
        "streaming", "trabalho remoto", "e-commerce", "redes sociais", "podcast",
        "influencer", "startup", "fintech", "foodtech", "healthtech", "edtech"
    )
    
    suspend fun generateRandomSearches(count: Int): List<String> = withContext(Dispatchers.IO) {
        val searches = mutableSetOf<String>()
        
        // Gera pesquisas até atingir a quantidade desejada
        while (searches.size < count) {
            val searchType = Random.nextInt(6)
            
            val search = when (searchType) {
                0 -> generateTopicSearch()
                1 -> generateQuestionSearch()
                2 -> generateTrendingSearch()
                3 -> generateCurrentEventSearch()
                4 -> generateRandomFactSearch()
                else -> generateCombinedSearch()
            }
            
            // Adiciona variações para tornar mais natural
            val finalSearch = addNaturalVariation(search)
            searches.add(finalSearch)
        }
        
        return@withContext searches.toList()
    }
    
    private fun generateTopicSearch(): String {
        val topic = topics.random()
        val adjective = adjectives.random()
        return "$adjective $topic"
    }
    
    private fun generateQuestionSearch(): String {
        val question = questions.random()
        val topic = topics.random()
        return "$question $topic"
    }
    
    private fun generateTrendingSearch(): String {
        return trendingWords.random()
    }
    
    private fun generateCurrentEventSearch(): String {
        val event = currentEvents.random()
        val variation = listOf("notícias sobre", "últimas sobre", "informações sobre", "").random()
        return if (variation.isEmpty()) event else "$variation $event"
    }
    
    private fun generateRandomFactSearch(): String {
        val topic = topics.random()
        val factWords = listOf("curiosidades sobre", "fatos interessantes sobre", "você sabia sobre", "descobertas sobre")
        return "${factWords.random()} $topic"
    }
    
    private fun generateCombinedSearch(): String {
        val topic1 = topics.random()
        val topic2 = topics.random()
        val connectors = listOf("e", "vs", "para", "em", "com")
        return "$topic1 ${connectors.random()} $topic2"
    }
    
    private fun addNaturalVariation(search: String): String {
        val variations = listOf(
            search,
            "$search 2024",
            "$search Brasil",
            "melhores $search",
            "$search hoje",
            "$search online",
            "como $search",
            "$search grátis"
        )
        return variations.random()
    }
    
    // Função alternativa usando palavras aleatórias de um dicionário online (sem API key)
    suspend fun generateSearchesFromOnlineSource(count: Int): List<String> = withContext(Dispatchers.IO) {
        val searches = mutableListOf<String>()
        
        try {
            // Usa API pública de palavras aleatórias (sem necessidade de API key)
            val randomWords = fetchRandomWords(count * 2)
            
            for (i in 0 until count) {
                val word1 = randomWords.getOrNull(i * 2) ?: topics.random()
                val word2 = randomWords.getOrNull(i * 2 + 1) ?: adjectives.random()
                val questionType = questions.random()
                
                val search = when (Random.nextInt(3)) {
                    0 -> "$questionType $word1"
                    1 -> "$word1 $word2"
                    else -> "$word1 e $word2"
                }
                
                searches.add(addNaturalVariation(search))
            }
        } catch (e: Exception) {
            // Fallback para geração local se a API falhar
            return@withContext generateRandomSearches(count)
        }
        
        return@withContext searches.distinctBy { it.lowercase() }.take(count)
    }
    
    private suspend fun fetchRandomWords(count: Int): List<String> {
        return try {
            val request = Request.Builder()
                .url("https://random-words-api.vercel.app/word")
                .build()
            
            val words = mutableListOf<String>()
            repeat(count) {
                try {
                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        // Parse JSON simples para extrair a palavra
                        val word = body?.let { parseWordFromJson(it) }
                        word?.let { words.add(it) }
                    }
                } catch (e: Exception) {
                    // Continua se uma palavra falhar
                }
            }
            words
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseWordFromJson(json: String): String? {
        return try {
            // Parse JSON simples: [{"word":"example","definition":"..."}]
            val wordMatch = Regex("\"word\":\"([^\"]+)\"").find(json)
            wordMatch?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}