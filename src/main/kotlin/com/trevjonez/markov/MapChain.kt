package com.trevjonez.markov

import io.reactivex.Observable
import java.util.*

/**
 * @author TrevJonez
 */
class MapChain(val groupSize: Int) {
    val countMap: MutableMap<String, MutableMap<String, WordCount>> = HashMap()
    val totalMap: MutableMap<String, Int> = HashMap()

    fun seedChain(words: Observable<String>) {
        groupStep(words).subscribe({
            Observable.fromIterable(it)
                    .take(groupSize.toLong())
                    .reduce(StringBuilder(), { builder, word ->
                        if (builder.isNotEmpty()) builder.append(" ")
                        builder.append(word)
                    })
                    .map(StringBuilder::toString)
                    .subscribe({ key ->
                        val map: MutableMap<String, WordCount> = countMap[key] ?: HashMap()
                        val count: WordCount = map[it[groupSize]] ?: WordCount(it[groupSize], 0)

                        map.put(it[groupSize], count.increment())
                        countMap.put(key, map)

                        val total: Int = totalMap[key] ?: 0
                        totalMap.put(key, total + 1)
                    })
        })
    }

    internal fun groupStep(words: Observable<String>): Observable<ArrayList<String>> {
        return words.scan(ArrayList<String>(), { list, nextWord ->
            if (list.size == groupSize + 1) list.removeAt(0)
            list.add(nextWord)
            list
        }).filter { it.size == groupSize + 1 }
    }

    fun readChain(): Observable<String> {
        return Observable.create({
            val rand = Random()

            var next = randomStart(rand)
            it.onNext(next)

            while (!it.isDisposed) {
                val potentialNextWords = countMap[next]

                if (potentialNextWords == null) {
                    next = randomStart(rand)
                    it.onNext(".")
                    continue
                }

                val iter = potentialNextWords.iterator()
                var offset = rand.nextInt(99) / 100.0

                while (offset > 0.0 && iter.hasNext()) {
                    val candidate = iter.next()
                    offset -= (candidate.value.count / totalMap[next]!!)
                    if (offset <= 0.0) {
                        next = Observable.fromIterable(next.split(" ").subList(1, groupSize))
                                .reduce(StringBuilder(), { builder, token ->
                                    if (builder.isNotEmpty()) builder.append(" ")
                                    builder.append(token)
                                    builder
                                })
                                .map { it.append(" ").append(candidate.key) }
                                .map(StringBuilder::toString)
                                .blockingGet()
                        it.onNext(" ${candidate.key}")
                    }
                }
            }
        })
    }

    internal fun randomStart(rand: Random): String {
        return countMap.entries.toTypedArray()[rand.nextInt(countMap.values.size)].key
    }
}