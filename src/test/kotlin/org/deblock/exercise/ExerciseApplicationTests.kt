package org.deblock.exercise

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ExerciseApplicationTests {

    @Test
    fun `should dummily pass`() {
        assertThat(true).isTrue()
    }

    @Disabled
    fun `should dummily fail`() {
        assertThat(true).isFalse()
    }
}
