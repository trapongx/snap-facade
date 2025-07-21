package com.runninglane.facade.test.cases.inherit

import com.runninglane.facade.FacadeClassGenerator
import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class InheritingAnnotatedPropertyTest {
    @Test
    fun `should inherit annotated property correctly`() {
        val delegate = Delegate(1)
        val facadeFactory = FacadeFactory(
            FacadeClassGenerator(annotationForPropertyInheriting = setOf(PropertyToInherit::class))
        )
        val facade = facadeFactory.from(delegate).to(Target::class)

        assertThat(facade.id).isEqualTo(1)
        assertThat(facade.greeting).isEqualTo("Hello")
        assertThrows<UnsupportedOperationException> {
            facade.boolean
        }
    }
}
