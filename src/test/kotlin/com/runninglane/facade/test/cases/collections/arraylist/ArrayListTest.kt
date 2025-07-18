package com.runninglane.facade.test.cases.collections.arraylist

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ArrayListTest {
    private fun createDelegate() = Delegate(
        arrayToArray = arrayOf(Delegate.Element("A")),
        arrayToArrayList = arrayOf(Delegate.Element("B")),
        arrayListToArrayList = arrayListOf(Delegate.Element("C")),
        arrayListToArray = arrayListOf(Delegate.Element("D")),
    )

    @Test
    fun `should create correct facade from delegate with array list of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.arrayToArray[0]).isSameAs(delegate.arrayToArray[0])
        assertThat(facade.arrayToArrayList[0]).isSameAs(delegate.arrayToArrayList[0])
        assertThat(facade.arrayListToArrayList?.get(0)).isSameAs(delegate.arrayListToArrayList?.get(0))
        assertThat(facade.arrayListToArray?.get(0)).isSameAs(delegate.arrayListToArray?.get(0))
    }

    @Test
    fun `should create correct facade from delegate with array list of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.arrayToArray[0].code).isEqualTo(delegate.arrayToArray[0].code).isEqualTo("A")
        assertThat(facade.arrayToArrayList[0]?.code).isEqualTo(delegate.arrayToArrayList[0]?.code)
            .isEqualTo("B")
        assertThat(facade.arrayListToArrayList?.get(0)?.code).isEqualTo(delegate.arrayListToArrayList?.get(0)?.code)
            .isEqualTo("C")
        assertThat(facade.arrayListToArray?.get(0)?.code).isEqualTo(delegate.arrayListToArray?.get(0)?.code)
            .isEqualTo("D")
    }
}
