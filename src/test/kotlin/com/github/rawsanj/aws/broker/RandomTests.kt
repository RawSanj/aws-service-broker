package com.github.rawsanj.aws.broker

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RandomTests {

    @Test
    fun removeNewLineInString (){

        val lines = """This lines ends here.
            |This is new lines.
        """.trimMargin()

        val sinleLine = lines.replace("\n".toRegex(), "")

        assertThat(sinleLine).isEqualTo("This lines ends here.This is new lines.")

    }
}