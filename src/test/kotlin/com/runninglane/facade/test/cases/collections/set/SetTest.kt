package com.runninglane.facade.test.cases.collections.set

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.FacadeGenerationException
import com.runninglane.facade.from
import com.runninglane.facade.test.cases.collections.set.invalid.DelegateWithArrayToSet
import com.runninglane.facade.test.cases.collections.set.invalid.DelegateWithCollectionToSet
import com.runninglane.facade.test.cases.collections.set.invalid.DelegateWithListToSet
import com.runninglane.facade.test.cases.collections.set.invalid.TargetWithOtherTypeToSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class SetTest {
    private fun createDelegate() = Delegate(
        setMutableToMutable = mutableSetOf(Delegate.Element("A")),
        setImmutableToMutable = setOf(Delegate.Element("B")),
        setMutableToImmutable = mutableSetOf(Delegate.Element("C")),
        setImmutableToImmutable = setOf(Delegate.Element("D")),
    )

    @Test
    fun `should create correct facade from delegate with collections of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
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
        assertThat(facade.setMutableToMutable.elementAt(0).code).isEqualTo(delegate.setMutableToMutable.elementAt(0).code)
            .isEqualTo("A")
        assertThat(facade.setImmutableToMutable.elementAt(0).code).isEqualTo(delegate.setImmutableToMutable.elementAt(0)?.code)
            .isEqualTo("B")
        assertThat(facade.setMutableToImmutable.elementAt(0).code).isEqualTo(delegate.setMutableToImmutable?.elementAt(0)?.code)
            .isEqualTo("C")
        assertThat(facade.setImmutableToImmutable.elementAt(0).code).isEqualTo(
            delegate.setImmutableToImmutable?.elementAt(
                0
            )?.code
        ).isEqualTo("D")
    }

    @Test
    fun `should fail when trying to create facade from delegate with properties of type list, array, or collection to set type`() {
        run {
            val delegate = DelegateWithListToSet(otherTypeToSet = mutableListOf(Delegate.Element("A")))
            assertThrows<FacadeGenerationException> {
                FacadeFactory.Companion.default.from(delegate).to(TargetWithOtherTypeToSet::class)
            }
        }

        run {
            val delegate = DelegateWithCollectionToSet(otherTypeToSet = listOf(Delegate.Element("A")))
            assertThrows<FacadeGenerationException> {
                FacadeFactory.Companion.default.from(delegate).to(TargetWithOtherTypeToSet::class)
            }
        }

        run {
            val delegate = DelegateWithArrayToSet(otherTypeToSet = arrayOf(Delegate.Element("A")))
            assertThrows<FacadeGenerationException> {
                FacadeFactory.Companion.default.from(delegate).to(TargetWithOtherTypeToSet::class)
            }
        }
    }
}
