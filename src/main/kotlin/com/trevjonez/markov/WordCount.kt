package com.trevjonez.markov

/**
 * @author TrevJonez
 */
data class WordCount(val word: String, val count: Int) {
    fun increment(): WordCount {
        return WordCount(word, count + 1)
    }
}