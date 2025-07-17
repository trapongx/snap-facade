package com.runninglane.facade.test.cases.collection

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import com.runninglane.facade.test.cases.collection.delegate.Delegate
import com.runninglane.facade.test.cases.collection.target.TargetWithDifferentElementType
import com.runninglane.facade.test.cases.collection.target.TargetWithExactElementType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class CollectionTest {

    @Test
    fun `should create correct facade from delegate with collections of exact element types`() {
        val delegate = Delegate(
            listMutableToMutable = mutableListOf(Delegate.Element('A')),
            listImmutableToMutable = listOf(Delegate.Element('B')),
            listMutableToImmutable = mutableListOf(Delegate.Element('C')),
            listImmutableToImmutable = listOf(Delegate.Element('D')),
            mapMutableToMutable = mutableMapOf("1" to Delegate.Element('E')),
            mapImmutableToMutable = mapOf("2" to Delegate.Element('F')),
            mapMutableToImmutable = mutableMapOf("3" to Delegate.Element('G')),
            mapImmutableToImmutable = mapOf("4" to Delegate.Element('H')),
            setMutableToMutable = mutableSetOf(Delegate.Element('I')),
            setImmutableToMutable = setOf(Delegate.Element('J')),
            setMutableToImmutable = mutableSetOf(Delegate.Element('K')),
            setImmutableToImmutable = setOf(Delegate.Element('L')),
            array = arrayOf(Delegate.Element('M')),
            arrayList = arrayListOf(Delegate.Element('N')),
            linkedHashSet = linkedSetOf(Delegate.Element('O')),
            hashSet = hashSetOf(Delegate.Element('P')),
            hashMap = hashMapOf("5" to Delegate.Element('Q')),
            linkedHashMap = linkedMapOf("6" to Delegate.Element('R')),
            iterator = listOf(Delegate.Element('S')).iterator(),
            sequence = sequenceOf(Delegate.Element('T')),
            pair = "U" to Delegate.Element('V'),
            triple = Triple("W", "X", Delegate.Element('Y'))
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.listMutableToMutable[0].char).isEqualTo(delegate.listMutableToMutable[0].char).isEqualTo('A')
        assertThat(facade.listImmutableToMutable[0].char).isEqualTo(delegate.listImmutableToMutable[0].char).isEqualTo('B')
        assertThat(facade.listMutableToImmutable[0].char).isEqualTo(delegate.listMutableToImmutable[0].char).isEqualTo('C')
        assertThat(facade.listImmutableToImmutable[0].char).isEqualTo(delegate.listImmutableToImmutable[0].char).isEqualTo('D')
        assertThat(facade.mapMutableToMutable["1"]?.char).isEqualTo(delegate.mapMutableToMutable["1"]?.char).isEqualTo('E')
        assertThat(facade.mapImmutableToMutable["2"]?.char).isEqualTo(delegate.mapImmutableToMutable["2"]?.char).isEqualTo('F')
        assertThat(facade.mapMutableToImmutable["3"]?.char).isEqualTo(delegate.mapMutableToImmutable["3"]?.char).isEqualTo('G')
        assertThat(facade.mapImmutableToImmutable["4"]?.char).isEqualTo(delegate.mapImmutableToImmutable["4"]?.char).isEqualTo('H')
        assertThat(facade.setMutableToMutable.elementAt(0).char).isEqualTo(delegate.setMutableToMutable.elementAt(0).char).isEqualTo('I')
        assertThat(facade.setImmutableToMutable.elementAt(0).char).isEqualTo(delegate.setImmutableToMutable.elementAt(0).char).isEqualTo('J')
        assertThat(facade.setMutableToImmutable.elementAt(0).char).isEqualTo(delegate.setMutableToImmutable.elementAt(0).char).isEqualTo('K')
        assertThat(facade.setImmutableToImmutable.elementAt(0).char).isEqualTo(delegate.setImmutableToImmutable.elementAt(0).char).isEqualTo('L')
        assertThat(facade.array[0].char).isEqualTo(delegate.array[0].char).isEqualTo('M')
        assertThat(facade.arrayList[0].char).isEqualTo(delegate.arrayList[0].char).isEqualTo('N')
        assertThat(facade.linkedHashSet.elementAt(0).char).isEqualTo(delegate.linkedHashSet.elementAt(0).char).isEqualTo('O')
        assertThat(facade.hashSet.elementAt(0).char).isEqualTo(delegate.hashSet.elementAt(0).char).isEqualTo('P')
        assertThat(facade.hashMap["5"]?.char).isEqualTo(delegate.hashMap["5"]?.char).isEqualTo('Q')
        assertThat(facade.linkedHashMap.entries.elementAt(0).value.char).isEqualTo(delegate.linkedHashMap.entries.elementAt(0).value.char).isEqualTo('R')
        assertThat(facade.iterator.next().char).isEqualTo(delegate.iterator.next().char).isEqualTo('S')
        assertThat(facade.sequence.elementAt(0).char).isEqualTo(delegate.sequence.elementAt(0).char).isEqualTo('T')
        assertThat(facade.pair.first).isEqualTo(delegate.pair.first).isEqualTo("U")
        assertThat(facade.pair.second.char).isEqualTo(delegate.pair.second.char).isEqualTo('V')
        assertThat(facade.triple.first).isEqualTo(delegate.triple.first).isEqualTo("W")
        assertThat(facade.triple.second).isEqualTo(delegate.triple.second).isEqualTo("X")
        assertThat(facade.triple.third.char).isEqualTo(delegate.triple.third.char).isEqualTo('Y')
        
        // Assert mutating prevention
        val dummyElement = Delegate.Element('Z')
        assertThrows<UnsupportedOperationException> { facade.listMutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.listImmutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.mapMutableToMutable["7"] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.mapImmutableToMutable["8"] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.setMutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.setImmutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.array[0] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.arrayList[0] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.linkedHashSet.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.hashSet.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.hashMap["9"] = dummyElement }
    }

    @Test
    fun `should create correct facade from delegate with collections of different element types`() {
        val delegate = Delegate(
            listMutableToMutable = mutableListOf(Delegate.Element('A')),
            listImmutableToMutable = listOf(Delegate.Element('B')),
            listMutableToImmutable = mutableListOf(Delegate.Element('C')),
            listImmutableToImmutable = listOf(Delegate.Element('D')),
            mapMutableToMutable = mutableMapOf("1" to Delegate.Element('E')),
            mapImmutableToMutable = mapOf("2" to Delegate.Element('F')),
            mapMutableToImmutable = mutableMapOf("3" to Delegate.Element('G')),
            mapImmutableToImmutable = mapOf("4" to Delegate.Element('H')),
            setMutableToMutable = mutableSetOf(Delegate.Element('I')),
            setImmutableToMutable = setOf(Delegate.Element('J')),
            setMutableToImmutable = mutableSetOf(Delegate.Element('K')),
            setImmutableToImmutable = setOf(Delegate.Element('L')),
            array = arrayOf(Delegate.Element('M')),
            arrayList = arrayListOf(Delegate.Element('N')),
            linkedHashSet = linkedSetOf(Delegate.Element('O')),
            hashSet = hashSetOf(Delegate.Element('P')),
            hashMap = hashMapOf("5" to Delegate.Element('Q')),
            linkedHashMap = linkedMapOf("6" to Delegate.Element('R')),
            iterator = listOf(Delegate.Element('S')).iterator(),
            sequence = sequenceOf(Delegate.Element('T')),
            pair = "U" to Delegate.Element('V'),
            triple = Triple("W", "X", Delegate.Element('Y'))
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.listMutableToMutable[0].char).isEqualTo(delegate.listMutableToMutable[0].char).isEqualTo('A')
        assertThat(facade.listImmutableToMutable[0].char).isEqualTo(delegate.listImmutableToMutable[0].char).isEqualTo('B')
        assertThat(facade.listMutableToImmutable[0].char).isEqualTo(delegate.listMutableToImmutable[0].char).isEqualTo('C')
        assertThat(facade.listImmutableToImmutable[0].char).isEqualTo(delegate.listImmutableToImmutable[0].char).isEqualTo('D')
        assertThat(facade.mapMutableToMutable["1"]?.char).isEqualTo(delegate.mapMutableToMutable["1"]?.char).isEqualTo('E')
        assertThat(facade.mapImmutableToMutable["2"]?.char).isEqualTo(delegate.mapImmutableToMutable["2"]?.char).isEqualTo('F')
        assertThat(facade.mapMutableToImmutable["3"]?.char).isEqualTo(delegate.mapMutableToImmutable["3"]?.char).isEqualTo('G')
        assertThat(facade.mapImmutableToImmutable["4"]?.char).isEqualTo(delegate.mapImmutableToImmutable["4"]?.char).isEqualTo('H')
        assertThat(facade.setMutableToMutable.elementAt(0).char).isEqualTo(delegate.setMutableToMutable.elementAt(0).char).isEqualTo('I')
        assertThat(facade.setImmutableToMutable.elementAt(0).char).isEqualTo(delegate.setImmutableToMutable.elementAt(0).char).isEqualTo('J')
        assertThat(facade.setMutableToImmutable.elementAt(0).char).isEqualTo(delegate.setMutableToImmutable.elementAt(0).char).isEqualTo('K')
        assertThat(facade.setImmutableToImmutable.elementAt(0).char).isEqualTo(delegate.setImmutableToImmutable.elementAt(0).char).isEqualTo('L')
        assertThat(facade.array[0].char).isEqualTo(delegate.array[0].char).isEqualTo('M')
        assertThat(facade.arrayList[0].char).isEqualTo(delegate.arrayList[0].char).isEqualTo('N')
        assertThat(facade.linkedHashSet.elementAt(0).char).isEqualTo(delegate.linkedHashSet.elementAt(0).char).isEqualTo('O')
        assertThat(facade.hashSet.elementAt(0).char).isEqualTo(delegate.hashSet.elementAt(0).char).isEqualTo('P')
        assertThat(facade.hashMap["5"]?.char).isEqualTo(delegate.hashMap["5"]?.char).isEqualTo('Q')
        assertThat(facade.linkedHashMap.entries.elementAt(0).value.char).isEqualTo(delegate.linkedHashMap.entries.elementAt(0).value.char).isEqualTo('R')
        assertThat(facade.iterator.next().char).isEqualTo(delegate.iterator.next().char).isEqualTo('S')
        assertThat(facade.sequence.elementAt(0).char).isEqualTo(delegate.sequence.elementAt(0).char).isEqualTo('T')
        assertThat(facade.pair.first).isEqualTo(delegate.pair.first).isEqualTo("U")
        assertThat(facade.pair.second.char).isEqualTo(delegate.pair.second.char).isEqualTo('V')
        assertThat(facade.triple.first).isEqualTo(delegate.triple.first).isEqualTo("W")
        assertThat(facade.triple.second).isEqualTo(delegate.triple.second).isEqualTo("X")
        assertThat(facade.triple.third.char).isEqualTo(delegate.triple.third.char).isEqualTo('Y')

        // Assert mutating prevention
        val dummyElement = TargetWithDifferentElementType.Element('Z')
        assertThrows<UnsupportedOperationException> { facade.listMutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.listImmutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.mapMutableToMutable["7"] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.mapImmutableToMutable["8"] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.setMutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.setImmutableToMutable.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.array[0] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.arrayList[0] = dummyElement }
        assertThrows<UnsupportedOperationException> { facade.linkedHashSet.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.hashSet.add(dummyElement) }
        assertThrows<UnsupportedOperationException> { facade.hashMap["9"] = dummyElement }
    }
}
