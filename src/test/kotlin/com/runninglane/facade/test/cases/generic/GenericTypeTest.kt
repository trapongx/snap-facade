package com.runninglane.facade.test.cases.generic

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenericTypeTest {
    @Test
    fun `should create correct non-generic facade from non-generic delegate type`() {
        val delegate = Delegate("A")
        val facade = FacadeFactory.default.from(delegate).to(Target::class)

        assertThat(facade.t).isEqualTo("A")
    }

    @Test
    fun `should create correct non-generic facade from generic delegate type`() {
        val delegate = DelegateWithTypeParam("A")
        val facade = FacadeFactory.default.from(delegate).to(Target::class)

        assertThat(facade.t).isEqualTo("A")
    }

    @Test
    fun `should create correct generic facade from non-generic delegate type`() {
        val delegate = Delegate("A")
        val facade = FacadeFactory.default.from(delegate).to(TargetWithTypeParam::class)

        assertThat(facade.t).isEqualTo("A")
    }

    @Test
    fun `should create correct generic facade from generic delegate type`() {
        val delegate = DelegateWithTypeParam("A")
        val facade = FacadeFactory.default.from(delegate).to(TargetWithTypeParam::class)

        assertThat(facade.t).isEqualTo("A")
    }
}