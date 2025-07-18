package com.runninglane.facade.test.cases.map

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class MapTest {
    private fun createDelegate() = Delegate(
        mapMutableToMutable = mutableMapOf("1" to Delegate.Element("I")),
        mapImmutableToMutable = mapOf("2" to Delegate.Element("J")),
        mapMutableToImmutable = mutableMapOf(Delegate.Element("L") to "4"),
        mapImmutableToImmutable = mapOf(Delegate.Element("3") to Delegate.Element("K"))
    )

    @Test
    fun `should create correct facade from delegate with maps of exact element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithExactElementType::class)

        // Assert equality
        assertThat(facade.mapMutableToMutable["1"]).isSameAs(delegate.mapMutableToMutable["1"])
        assertThat(facade.mapImmutableToMutable["2"]).isSameAs(delegate.mapImmutableToMutable["2"])
        run {
            val key = facade.mapMutableToImmutable.keys.find { it.code == "L" }
            assertThat(key).isSameAs(delegate.mapMutableToImmutable?.keys?.find { it.code == "L" })
            assertThat(facade.mapMutableToImmutable[key]).isEqualTo("4")
        }
        run {
            val key = facade.mapImmutableToImmutable.keys.find { it.code == "3" }
            assertThat(key).isSameAs(delegate.mapImmutableToImmutable?.keys?.find { it.code == "3" })
            assertThat(facade.mapImmutableToImmutable[key]?.code).isEqualTo("K")
        }
    }

    @Test
    fun `should create correct facade from delegate with maps of different element types`() {
        val delegate = createDelegate()
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithDifferentElementType::class)

        // Assert equality
        assertThat(facade.mapMutableToMutable["1"]?.code).isEqualTo(delegate.mapMutableToMutable["1"]?.code).isEqualTo("I")
        assertThat(facade.mapImmutableToMutable["2"]?.code).isEqualTo(delegate.mapMutableToMutable["2"]?.code).isEqualTo("J")
        run {
            val key = facade.mapMutableToImmutable.keys.find { it.code == "L" }
            assertThat(facade.mapMutableToImmutable[key]).isEqualTo("4")
        }
        run {
            val key = facade.mapImmutableToImmutable.keys.find { it.code == "3" }
            assertThat(facade.mapImmutableToImmutable[key]?.code).isEqualTo("K")
        }
    }
}
