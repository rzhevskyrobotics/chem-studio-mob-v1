package com.example.smart_labs.domain.search

import com.example.smart_labs.domain.model.TopicItem
import kotlin.math.min

object NGramSearch {

    fun search(items: List<TopicItem>, query: String): List<TopicItem> {
        val q = normalize(query)
        if (q.isBlank()) return items

        val n = if (q.length <= 4) 2 else 3
        val qGrams = ngrams(q, n)

        return items
            .map { item ->
                val hay = normalize(item.title + " " + item.topic + " " + (item.description ?: ""))
                val hGrams = ngrams(hay, n)
                val score = overlapScore(qGrams, hGrams)
                item to score
            }
            .filter { (_, score) -> score > 0 }
            .sortedWith(compareByDescending<Pair<TopicItem, Int>> { it.second }
                .thenBy { it.first.title })
            .map { it.first }
    }

    private fun normalize(s: String): String =
        s.lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace("ё", "е")

    private fun ngrams(s: String, n: Int): Set<String> {
        if (s.length < n) return emptySet()
        val res = HashSet<String>(min(64, s.length))
        for (i in 0..(s.length - n)) {
            res.add(s.substring(i, i + n))
        }
        return res
    }

    private fun overlapScore(a: Set<String>, b: Set<String>): Int {
        if (a.isEmpty() || b.isEmpty()) return 0
        val small = if (a.size <= b.size) a else b
        val big = if (a.size <= b.size) b else a
        var c = 0
        for (g in small) if (g in big) c++
        return c
    }
}