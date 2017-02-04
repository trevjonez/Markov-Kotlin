package com.trevjonez.markov

import io.reactivex.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

/**
 * @author TrevJonez
 */
class MapChainTest {
    @Test
    fun groupStep() {
        val chain = MapChain(2)
        val testSub = chain.groupStep(Observable.just("one", "two", "three", "four", "five", "six"))
                .concatMap { Observable.fromIterable(it) }.test()

        assertThat(testSub.values()).containsExactly(
                "one", "two", "three",
                "two", "three", "four",
                "three", "four", "five",
                "four", "five", "six")
    }

    @Test
    fun seedChain() {
        val chain = MapChain(2)
        chain.seedChain(Observable.just("one", "two", "three", "four", "five", "six"))

        val expecting = LinkedHashMap<String, LinkedHashMap<String, WordCount>>()

        val sub1 = LinkedHashMap<String, WordCount>()
        sub1.put("three", WordCount("three", 1))
        expecting.put("one two", sub1)

        val sub2 = LinkedHashMap<String, WordCount>()
        sub2.put("four", WordCount("four", 1))
        expecting.put("two three", sub2)

        val sub3 = LinkedHashMap<String, WordCount>()
        sub3.put("five", WordCount("five", 1))
        expecting.put("three four", sub3)

        val sub4 = LinkedHashMap<String, WordCount>()
        sub4.put("six", WordCount("six", 1))
        expecting.put("four five", sub4)

        assertThat(chain.countMap).isEqualTo(expecting)

        chain.readChain().take(20).subscribe(::print, ::print, ::println)
    }
}