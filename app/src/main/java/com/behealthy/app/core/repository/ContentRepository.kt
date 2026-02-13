package com.behealthy.app.core.repository

import android.content.Context
import com.behealthy.app.core.database.dao.DailyHistoryDao
import com.behealthy.app.core.database.dao.PoemDao
import com.behealthy.app.core.database.dao.QuoteDao
import com.behealthy.app.core.database.entity.DailyHistoryEntity
import com.behealthy.app.core.database.entity.PoemEntity
import com.behealthy.app.core.database.entity.QuoteEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.InputStreamReader
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ContentRepository @Inject constructor(
    private val quoteDao: QuoteDao,
    private val poemDao: PoemDao,
    private val dailyHistoryDao: DailyHistoryDao,
    @ApplicationContext private val context: Context
) {
    fun getAllQuotes(): Flow<List<QuoteEntity>> = quoteDao.getAllQuotes()
    fun getAllPoems(): Flow<List<PoemEntity>> = poemDao.getAllPoems()

    suspend fun getDailyQuote(date: LocalDate, forceRefresh: Boolean = false): QuoteEntity? {
        val dateStr = date.toString()
        if (!forceRefresh) {
            val history = dailyHistoryDao.getHistoryForDate("quote", dateStr)
            
            if (history != null) {
                val quotes = quoteDao.getQuotesSnapshot()
                val historyQuote = quotes.find { it.id == history.itemId }
                if (historyQuote != null) return historyQuote
            }
        }

        // Generate new daily quote
        var allQuotes = quoteDao.getQuotesSnapshot()
        if (allQuotes.isEmpty()) {
            initializeDataIfNeeded()
            allQuotes = quoteDao.getQuotesSnapshot()
        }
        if (allQuotes.isEmpty()) return null

        val recentHistoryIds = dailyHistoryDao.getRecentItemIds("quote", 30).toSet()
        
        // Check for consecutive Tao Te Ching (or same source)
        // Get last 2 history items
        val recentHistory = dailyHistoryDao.getRecentHistory("quote", 2)
        val bannedSources = mutableSetOf<String>()
        
        if (recentHistory.size >= 2) {
             // Need to find the actual quotes to check source
             val h1 = allQuotes.find { it.id == recentHistory[0].itemId }
             val h2 = allQuotes.find { it.id == recentHistory[1].itemId }
             
             if (h1 != null && h2 != null) {
                 // Check if both are Taoism/Tao Te Ching
                 val isH1Taoism = h1.source.contains("道德经") || h1.source.contains("老子") || h1.category.contains("Taoism") || h1.tags.contains("taoism")
                 val isH2Taoism = h2.source.contains("道德经") || h2.source.contains("老子") || h2.category.contains("Taoism") || h2.tags.contains("taoism")
                 
                 if (isH1Taoism && isH2Taoism) {
                     bannedSources.add("Taoism")
                 }
                 
                 // Also check for identical source generally
                 if (h1.source.isNotEmpty() && h1.source == h2.source) {
                     bannedSources.add(h1.source)
                 }
             }
        }

        // Filter for Chinese content ONLY (must contain Chinese characters)
        // and exclude recent history (last 30 days)
        // and exclude banned sources
        val availableQuotes = allQuotes.filter { 
            !recentHistoryIds.contains(it.id) && 
            it.content.matches(Regex(".*[\\u4e00-\\u9fa5].*")) &&
            (!bannedSources.contains("Taoism") || !(it.source.contains("道德经") || it.source.contains("老子") || it.category.contains("Taoism") || it.tags.contains("taoism"))) &&
            (!bannedSources.contains(it.source))
        }
        
        // Fallback: If strict filtering leaves no quotes, use Least Recently Used strategy
        val candidates = if (availableQuotes.isEmpty()) {
             // Get full history for LRU
             val allHistoryIds = dailyHistoryDao.getRecentItemIds("quote", 1000)
             val lruQuotes = getLeastRecentlyUsed(allQuotes, allHistoryIds)
             // Still must be Chinese
             lruQuotes.filter { it.content.matches(Regex(".*[\\u4e00-\\u9fa5].*")) }
        } else {
             availableQuotes
        }

        if (candidates.isEmpty()) return null // No Chinese quotes found

        // Weighted Random Algorithm
        // Tao Te Ching (Daoism) -> 25% probability
        // BUT if Taoism is banned, probability is 0
        val taoismQuotes = candidates.filter { 
            it.source.contains("道德经") || it.source.contains("老子") || it.category.contains("Taoism") || it.tags.contains("taoism")
        }
        val otherQuotes = candidates.filter { !taoismQuotes.contains(it) }

        val selectedQuote = if (!bannedSources.contains("Taoism") && taoismQuotes.isNotEmpty() && Random.nextFloat() < 0.25) {
            taoismQuotes.random()
        } else {
            if (otherQuotes.isNotEmpty()) otherQuotes.random() else candidates.random()
        }

        // Save history
        dailyHistoryDao.insert(DailyHistoryEntity(date = dateStr, type = "quote", itemId = selectedQuote.id))
        
        return selectedQuote
    }

    suspend fun getDailyPoem(date: LocalDate, forceRefresh: Boolean = false): PoemEntity? {
        val dateStr = date.toString()
        if (!forceRefresh) {
            val history = dailyHistoryDao.getHistoryForDate("poem", dateStr)
            
            if (history != null) {
                val poems = poemDao.getPoemsSnapshot()
                val historyPoem = poems.find { it.id == history.itemId }
                if (historyPoem != null) return historyPoem
            }
        }

        // Generate new daily poem
        var allPoems = poemDao.getPoemsSnapshot()
        if (allPoems.isEmpty()) {
            initializeDataIfNeeded()
            allPoems = poemDao.getPoemsSnapshot()
        }
        if (allPoems.isEmpty()) return null

        // 1. Get recent history to analyze patterns
        val recentHistory = dailyHistoryDao.getRecentHistory("poem", 10)
        val todayStr = LocalDate.now().toString()
        
        // Map history to actual Poem entities
        val historyPoems = recentHistory.mapNotNull { historyItem ->
            allPoems.find { it.id == historyItem.itemId }?.let { poem ->
                historyItem to poem
            }
        }

        // Rule 1: Prevent consecutive author repeats (max 2 in a row)
        // Check the last 2 items. If they are from the same author, that author is banned.
        val bannedAuthorsConsecutive = if (historyPoems.size >= 2) {
            val (h1, p1) = historyPoems[0]
            val (h2, p2) = historyPoems[1]
            if (p1.author == p2.author) setOf(p1.author) else emptySet()
        } else {
            emptySet()
        }

        // Rule 2: Limit 1 author to <= 3 poems/24h (today)
        val todayHistory = historyPoems.filter { it.first.date == todayStr }
        val authorCountsToday = todayHistory.groupingBy { it.second.author }.eachCount()
        val bannedAuthorsFrequency = authorCountsToday.filter { it.value >= 3 }.keys

        val bannedAuthors = bannedAuthorsConsecutive + bannedAuthorsFrequency

        // Filter available poems
        // Also exclude recent specific poems (avoid exact duplicates recently - 30 days)
        val recentHistoryIds = dailyHistoryDao.getRecentItemIds("poem", 30).toSet()
        
        val availablePoems = allPoems.filter { 
            !recentHistoryIds.contains(it.id) && !bannedAuthors.contains(it.author)
        }
        
        // Fallback: Use Least Recently Used, but try to respect Author Ban if possible
        val candidates = if (availablePoems.isEmpty()) {
             // Try LRU but still exclude banned authors if possible
             val allHistoryIds = dailyHistoryDao.getRecentItemIds("poem", 1000)
             val lruPoems = getLeastRecentlyUsed(allPoems, allHistoryIds)
             
             val lruFiltered = lruPoems.filter { !bannedAuthors.contains(it.author) }
             if (lruFiltered.isNotEmpty()) lruFiltered else lruPoems
        } else {
             availablePoems
        }
        
        // If still empty (e.g. only 1 poem in DB), use all
        val finalCandidates = if (candidates.isEmpty()) allPoems else candidates

        // Weighted Random Algorithm
        // Wang Wei -> 15%
        // Su Shi -> 15%
        // Others -> 70%
        val wangWeiPoems = finalCandidates.filter { it.author == "王维" }
        val suShiPoems = finalCandidates.filter { it.author == "苏轼" }
        val otherPoems = finalCandidates.filter { it.author != "王维" && it.author != "苏轼" }

        val rand = Random.nextFloat()
        val selectedPoem = when {
            wangWeiPoems.isNotEmpty() && rand < 0.15 -> wangWeiPoems.random()
            suShiPoems.isNotEmpty() && rand < 0.30 -> suShiPoems.random() // 0.15 + 0.15 = 0.30 cumulative
            otherPoems.isNotEmpty() -> otherPoems.random()
            else -> finalCandidates.random()
        }

        dailyHistoryDao.insert(DailyHistoryEntity(date = dateStr, type = "poem", itemId = selectedPoem.id))
        
        return selectedPoem
    }
    
    /**
     * Helper to sort items by Least Recently Used (LRU).
     * Items not in history (never shown) come first.
     * Items shown longest ago come next.
     * Items shown recently come last.
     */
    private fun <T : Any> getLeastRecentlyUsed(allItems: List<T>, historyIds: List<Long>): List<T> {
        // Map ID to Index in history (0 = most recent, N = oldest)
        // historyIds is ordered DESC (Recent -> Old)
        // So smaller index = More Recent
        
        // We want:
        // 1. Not in history (Priority 1)
        // 2. In history, but larger index (Oldest shown) (Priority 2)
        
        // Let's create a map: ID -> RecencyScore
        // Not in history -> MaxValue (Sort Descending -> First)
        // In history -> Index (Sort Descending -> Larger index (Older) -> First)
        
        // Wait, if I sort Descending:
        // Item A (Not in history) -> Score 1,000,000
        // Item B (Index 100 - Old) -> Score 100
        // Item C (Index 0 - Recent) -> Score 0
        // Result: A, B, C. This is correct.
        
        val historyMap = historyIds.withIndex().associate { it.value to it.index }
        
        return allItems.sortedByDescending { item ->
            val id = when (item) {
                is QuoteEntity -> item.id
                is PoemEntity -> item.id
                else -> -1L
            }
            if (historyMap.containsKey(id)) {
                historyMap[id]!!
            } else {
                Int.MAX_VALUE // Never shown, highest priority
            }
        }
    }

    suspend fun initializeDataIfNeeded() {
        // Load Quotes from Assets
        try {
            val quotesJson = readAssetFile("quotes.json")
            val quoteType = object : TypeToken<List<QuoteEntity>>() {}.type
            val initialQuotes: List<QuoteEntity> = Gson().fromJson(quotesJson, quoteType)

            if (initialQuotes.isNotEmpty()) {
                val existingQuotes = quoteDao.getQuotesSnapshot()
                val existingQuoteContents = existingQuotes.map { it.content }.toSet()
                val newQuotes = initialQuotes.filter { !existingQuoteContents.contains(it.content) }
                
                if (newQuotes.isNotEmpty()) {
                    quoteDao.insertAll(newQuotes)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Load Poems from Assets
        try {
            val poemsJson = readAssetFile("poems.json")
            val poemType = object : TypeToken<List<PoemEntity>>() {}.type
            val initialPoems: List<PoemEntity> = Gson().fromJson(poemsJson, poemType)

            if (initialPoems.isNotEmpty()) {
                val existingPoems = poemDao.getPoemsSnapshot()
                val existingPoemContents = existingPoems.map { it.content }.toSet()
                val newPoems = initialPoems.filter { !existingPoemContents.contains(it.content) }
                
                if (newPoems.isNotEmpty()) {
                    poemDao.insertAll(newPoems)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readAssetFile(fileName: String): String {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }
}
