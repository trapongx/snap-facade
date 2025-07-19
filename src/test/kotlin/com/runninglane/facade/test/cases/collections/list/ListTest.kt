package com.runninglane.facade.test.cases.collections.list

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ListTest {
    private fun createDelegate() = Delegate(
        listMutableToMutable = mutableListOf(Delegate.Element("A")),
        listImmutableToMutable = listOf(Delegate.Element("B")),
        listMutableToImmutable = mutableListOf(Delegate.Element("C")),
        listImmutableToImmutable = listOf(Delegate.Element("D")),
    )

    @Test
    fun `should create correct facade from delegate with collections of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.listMutableToMutable[0]).isSameAs(delegate.listMutableToMutable[0])
        assertThat(facade.listImmutableToMutable[0]).isSameAs(delegate.listImmutableToMutable[0])
        assertThat(facade.listMutableToImmutable[0]).isSameAs(delegate.listMutableToImmutable?.get(0))
        assertThat(facade.listImmutableToImmutable[0]).isSameAs(delegate.listImmutableToImmutable?.get(0))
    }

    @Test
    fun `should create correct facade from delegate with collections of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.listMutableToMutable[0].code).isEqualTo(delegate.listMutableToMutable[0].code).isEqualTo("A")
        assertThat(facade.listImmutableToMutable[0].code).isEqualTo(delegate.listImmutableToMutable[0]?.code)
            .isEqualTo("B")
        assertThat(facade.listMutableToImmutable[0].code).isEqualTo(delegate.listMutableToImmutable?.get(0)?.code)
            .isEqualTo("C")
        assertThat(facade.listImmutableToImmutable[0].code).isEqualTo(delegate.listImmutableToImmutable?.get(0)?.code)
            .isEqualTo("D")
    }
}
