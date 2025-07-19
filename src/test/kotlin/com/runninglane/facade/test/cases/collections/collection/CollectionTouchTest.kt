package com.runninglane.facade.test.cases.collections.collection

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.collections.Map
import kotlin.collections.listOf
import kotlin.collections.mapOf
import kotlin.collections.toList

class CollectionTouchTest {

    class Delegate(
        dummyCollection: Collection<String>,
        dummyMap: Map<String, String>
    ) {
        val collectionToHashSet: Collection<String> = dummyCollection
        val collectionToLinkedHashSet: Collection<String> = dummyCollection
        val collectionToSortedSet: Collection<String> = dummyCollection
        val collectionToTreeSet: Collection<String> = dummyCollection
        val collectionToLinkedList: Collection<String> = dummyCollection
        val collectionToArrayDeque: Collection<String> = dummyCollection
        val collectionToJavaArrayDeque: Collection<String> = dummyCollection
        val collectionToDeque: Collection<String> = dummyCollection
        val collectionToJavaDeque: Collection<String> = dummyCollection
        val collectionToQueue: Collection<String> = dummyCollection
        val collectionToStack: Collection<String> = dummyCollection
        val collectionToVector: Collection<String> = dummyCollection
        val mapToHashMap: Map<String, String> = dummyMap
        val mapToTreeMap: Map<String, String> = dummyMap
        val mapToSortedMap: Map<String, String> = dummyMap
    }

    interface Target {
        val collectionToHashSet: HashSet<String>
        val collectionToLinkedHashSet: LinkedHashSet<String>
        val collectionToSortedSet: SortedSet<String>
        val collectionToTreeSet: TreeSet<String>
        val collectionToLinkedList: LinkedList<String>
        val collectionToArrayDeque: ArrayDeque<String>
        val collectionToJavaArrayDeque: java.util.ArrayDeque<String>
        val collectionToDeque: Deque<String>
        val collectionToJavaDeque: Deque<String>
        val collectionToQueue: Queue<String>
        val collectionToStack: Stack<String>
        val collectionToVector: Vector<String>
        val mapToHashMap: HashMap<String, String>
        val mapToTreeMap: TreeMap<String, String>
        val mapToSortedMap: SortedMap<String, String>
    }

    @Test
    fun `should create correct facade from delegate with misc collection types of exact element types`() {
        val dummyCollection = listOf("1", "2", "3")
        val dummyMap = mapOf("1" to "1", "2" to "2", "3" to "3")
        val delegate = Delegate(dummyCollection, dummyMap)
        val facade = FacadeFactory.Companion.default.from(delegate).to(Target::class)

        assertThat(facade.collectionToHashSet.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToLinkedHashSet.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToSortedSet.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToTreeSet.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToLinkedList.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToArrayDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToJavaArrayDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToJavaDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToQueue.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToStack.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToVector.toList()).isEqualTo(dummyCollection)
        assertThat(facade.mapToHashMap).isEqualTo(dummyMap)
        assertThat(facade.mapToTreeMap).isEqualTo(dummyMap)
        assertThat(facade.mapToSortedMap).isEqualTo(dummyMap)
    }

    @Test
    fun `should fail converting non-unique collection to set`() {
        val dummyCollection = listOf("1", "2", "3", "2")
        val dummyMap = mapOf("1" to "1", "2" to "2", "3" to "3")
        val delegate = Delegate(dummyCollection, dummyMap)
        val facade = FacadeFactory.Companion.default.from(delegate).to(Target::class)

        assertThrows<IllegalArgumentException> { facade.collectionToHashSet }
        assertThrows<IllegalArgumentException> { facade.collectionToLinkedHashSet }
        assertThrows<IllegalArgumentException> { facade.collectionToSortedSet }
        assertThrows<IllegalArgumentException> { facade.collectionToTreeSet }
        assertThat(facade.collectionToLinkedList.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToArrayDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToJavaArrayDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToJavaDeque.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToQueue.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToStack.toList()).isEqualTo(dummyCollection)
        assertThat(facade.collectionToVector.toList()).isEqualTo(dummyCollection)
        assertThat(facade.mapToHashMap).isEqualTo(dummyMap)
        assertThat(facade.mapToTreeMap).isEqualTo(dummyMap)
        assertThat(facade.mapToSortedMap).isEqualTo(dummyMap)
    }
    
}