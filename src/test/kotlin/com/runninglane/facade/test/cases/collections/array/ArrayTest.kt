package com.runninglane.facade.test.cases.collections.array

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ArrayTest {
    private fun createDelegate() = Delegate(
        arrayToList = arrayOf(Delegate.Element("A")),
        listToArray = listOf(Delegate.Element("B")),
        arrayToMutableList = arrayOf(Delegate.Element("C")),
        mutableListToArray = mutableListOf(Delegate.Element("D")),
        
        setToArray = setOf(Delegate.Element("E")),
        mutableSetToArray = mutableSetOf(Delegate.Element("F")),
        
        arrayToCollection = arrayOf(Delegate.Element("G")),
        collectionToArray = listOf(Delegate.Element("H")),
        arrayToMutableCollection = arrayOf(Delegate.Element("I")),
        mutableCollectionToArray = mutableListOf(Delegate.Element("J"))
    )

    @Test
    fun `should create correct facade from delegate with array of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.arrayToList[0]).isSameAs(delegate.arrayToList[0])
        assertThat(facade.listToArray[0]).isSameAs(delegate.listToArray[0])
        assertThat(facade.arrayToMutableList[0]).isSameAs(delegate.arrayToMutableList[0])
        assertThat(facade.mutableListToArray[0]).isSameAs(delegate.mutableListToArray[0])

        assertThat(facade.setToArray[0]).isSameAs(delegate.setToArray.elementAt(0))
        assertThat(facade.mutableSetToArray[0]).isSameAs(delegate.mutableSetToArray.elementAt(0))

        assertThat(facade.arrayToCollection.elementAt(0)).isSameAs(delegate.arrayToCollection[0])
        assertThat(facade.collectionToArray[0]).isSameAs(delegate.collectionToArray.elementAt(0))
        assertThat(facade.arrayToMutableCollection.elementAt(0)).isSameAs(delegate.arrayToMutableCollection[0])
        assertThat(facade.mutableCollectionToArray[0]).isSameAs(delegate.mutableCollectionToArray.elementAt(0))
    }

    @Test
    fun `should create correct facade from delegate with collections of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.arrayToList[0].code).isEqualTo(delegate.arrayToList[0].code).isEqualTo("A")
        assertThat(facade.listToArray[0].code).isEqualTo(delegate.listToArray[0].code).isEqualTo("B")
        assertThat(facade.arrayToMutableList[0].code).isEqualTo(delegate.arrayToMutableList[0].code).isEqualTo("C")
        assertThat(facade.mutableListToArray[0].code).isEqualTo(delegate.mutableListToArray[0].code).isEqualTo("D")

        assertThat(facade.setToArray[0].code).isEqualTo(delegate.setToArray.elementAt(0).code).isEqualTo("E")
        assertThat(facade.mutableSetToArray[0].code).isEqualTo(delegate.mutableSetToArray.elementAt(0).code).isEqualTo("F")

        assertThat(facade.arrayToCollection.elementAt(0)?.code).isEqualTo(delegate.arrayToCollection.elementAt(0)?.code).isEqualTo("G")
        assertThat(facade.collectionToArray[0]?.code).isEqualTo(delegate.collectionToArray.elementAt(0)?.code).isEqualTo("H")
        assertThat(facade.arrayToMutableCollection.elementAt(0)?.code).isEqualTo(delegate.arrayToMutableCollection.elementAt(0)?.code).isEqualTo("I")
        assertThat(facade.mutableCollectionToArray[0]?.code).isEqualTo(delegate.mutableCollectionToArray.elementAt(0)?.code).isEqualTo("J")
    }
}
