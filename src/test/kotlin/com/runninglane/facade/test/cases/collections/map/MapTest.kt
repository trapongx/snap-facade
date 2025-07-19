package com.runninglane.facade.test.cases.collections.map

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class MapTest {
    private fun createDelegate() = Delegate(
        mapMutableToMutable = mutableMapOf("1" to Delegate.Element("A")),
        mapImmutableToMutable = mapOf("2" to Delegate.Element("B")),
        mapMutableToImmutable = mutableMapOf(Delegate.Element("C") to "4"),
        mapImmutableToImmutable = mapOf(Delegate.Element("3") to Delegate.Element("D"))
    )

    @Test
    fun `should create correct facade from delegate with maps of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.mapMutableToMutable["1"]).isSameAs(delegate.mapMutableToMutable["1"])
        assertThat(facade.mapImmutableToMutable["2"]).isSameAs(delegate.mapImmutableToMutable["2"])
        run {
            val key = facade.mapMutableToImmutable.keys.find { it.code == "C" }
            assertThat(key).isSameAs(delegate.mapMutableToImmutable?.keys?.find { it.code == "C" })
            assertThat(facade.mapMutableToImmutable[key]).isEqualTo("4")
        }
        run {
            val key = facade.mapImmutableToImmutable.keys.find { it.code == "3" }
            assertThat(key).isSameAs(delegate.mapImmutableToImmutable?.keys?.find { it.code == "3" })
            assertThat(facade.mapImmutableToImmutable[key]?.code).isEqualTo("D")
        }
    }

    @Test
    fun `should create correct facade from delegate with maps of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.mapMutableToMutable["1"]?.code).isEqualTo(delegate.mapMutableToMutable["1"]?.code).isEqualTo("A")
        assertThat(facade.mapImmutableToMutable["2"]?.code).isEqualTo(delegate.mapImmutableToMutable["2"]?.code).isEqualTo("B")
        run {
            val key = facade.mapMutableToImmutable.keys.find { it.code == "C" }
            assertThat(facade.mapMutableToImmutable[key]).isEqualTo("4")
        }
        run {
            val key = facade.mapImmutableToImmutable.keys.find { it.code == "3" }
            assertThat(facade.mapImmutableToImmutable[key]?.code).isEqualTo("D")
        }
    }
}
