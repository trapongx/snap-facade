package com.runninglane.facade.test.cases.collection

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import com.runninglane.facade.test.cases.collection.delegate.Delegate
import com.runninglane.facade.test.cases.collection.target.TargetWithDifferentElementType
import com.runninglane.facade.test.cases.collection.target.TargetWithExactElementType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CollectionTest {
    private fun createDelegate() = Delegate(
        listMutableToMutable = mutableListOf(Delegate.Element('A')),
        listImmutableToMutable = listOf(Delegate.Element('B')),
        listMutableToImmutable = mutableListOf(Delegate.Element('C')),
        listImmutableToImmutable = listOf(Delegate.Element('D')),

        setMutableToMutable = mutableSetOf(Delegate.Element('E')),
        setImmutableToMutable = setOf(Delegate.Element('F')),
        setMutableToImmutable = mutableSetOf(Delegate.Element('G')),
        setImmutableToImmutable = setOf(Delegate.Element('H'))
    )

    @Test
    fun `should create correct facade from delegate with collections of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.listMutableToMutable[0]).isSameAs(delegate.listMutableToMutable[0])
        assertThat(facade.listImmutableToMutable[0]).isSameAs(delegate.listImmutableToMutable[0])
        assertThat(facade.listMutableToImmutable[0]).isSameAs(delegate.listMutableToImmutable?.get(0))
        assertThat(facade.listImmutableToImmutable[0]).isSameAs(delegate.listImmutableToImmutable?.get(0))

        assertThat(facade.setMutableToMutable.elementAt(0)).isSameAs(delegate.setMutableToMutable.elementAt(0))
        assertThat(facade.setImmutableToMutable.elementAt(0)).isSameAs(delegate.setImmutableToMutable.elementAt(0))
        assertThat(facade.setMutableToImmutable.elementAt(0)).isSameAs(delegate.setMutableToImmutable?.elementAt(0))
        assertThat(facade.setImmutableToImmutable.elementAt(0)).isSameAs(delegate.setImmutableToImmutable?.elementAt(0))
    }

    @Test
    fun `should create correct facade from delegate with collections of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.listMutableToMutable[0].char).isEqualTo(delegate.listMutableToMutable[0].char).isEqualTo('A')
        assertThat(facade.listImmutableToMutable[0].char).isEqualTo(delegate.listImmutableToMutable[0]?.char)
            .isEqualTo('B')
        assertThat(facade.listMutableToImmutable[0].char).isEqualTo(delegate.listMutableToImmutable?.get(0)?.char)
            .isEqualTo('C')
        assertThat(facade.listImmutableToImmutable[0].char).isEqualTo(delegate.listImmutableToImmutable?.get(0)?.char)
            .isEqualTo('D')

        assertThat(facade.setMutableToMutable.elementAt(0).char).isEqualTo(delegate.setMutableToMutable.elementAt(0).char)
            .isEqualTo('E')
        assertThat(facade.setImmutableToMutable.elementAt(0).char).isEqualTo(delegate.setImmutableToMutable.elementAt(0)?.char)
            .isEqualTo('F')
        assertThat(facade.setMutableToImmutable.elementAt(0).char).isEqualTo(delegate.setMutableToImmutable?.elementAt(0)?.char)
            .isEqualTo('G')
        assertThat(facade.setImmutableToImmutable.elementAt(0).char).isEqualTo(
            delegate.setImmutableToImmutable?.elementAt(
                0
            )?.char
        ).isEqualTo('H')
    }
}
