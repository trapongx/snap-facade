package com.runninglane.facade.test.cases.collections.collection

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CollectionTest {
    private fun createDelegate() = Delegate(
        collectionMutableToMutable = mutableListOf(Delegate.Element("A")),
        collectionImmutableToMutable = listOf(Delegate.Element("B")),
        collectionMutableToImmutable = mutableListOf(Delegate.Element("C")),
        collectionImmutableToImmutable = listOf(Delegate.Element("D")),
    )

    @Test
    fun `should create correct facade from delegate with collections of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.collectionMutableToMutable.elementAt(0)).isSameAs(delegate.collectionMutableToMutable.elementAt(0))
        assertThat(facade.collectionImmutableToMutable.elementAt(0)).isSameAs(delegate.collectionImmutableToMutable.elementAt(0))
        assertThat(facade.collectionMutableToImmutable.elementAt(0)).isSameAs(delegate.collectionMutableToImmutable?.elementAt(0))
        assertThat(facade.collectionImmutableToImmutable.elementAt(0)).isSameAs(delegate.collectionImmutableToImmutable?.elementAt(0))
    }

    @Test
    fun `should create correct facade from delegate with collections of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.collectionMutableToMutable.elementAt(0).code).isEqualTo(delegate.collectionMutableToMutable.elementAt(0).code).isEqualTo("A")
        assertThat(facade.collectionImmutableToMutable.elementAt(0).code).isEqualTo(delegate.collectionImmutableToMutable.elementAt(0)?.code)
            .isEqualTo("B")
        assertThat(facade.collectionMutableToImmutable.elementAt(0).code).isEqualTo(delegate.collectionMutableToImmutable?.elementAt(0)?.code)
            .isEqualTo("C")
        assertThat(facade.collectionImmutableToImmutable.elementAt(0).code).isEqualTo(delegate.collectionImmutableToImmutable?.elementAt(0)?.code)
            .isEqualTo("D")
    }
}
