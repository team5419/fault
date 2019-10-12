package tests.math.units

import org.junit.Test
import org.team5419.fault.math.epsilonEquals
import org.team5419.fault.math.units.feet
import org.team5419.fault.math.units.inch
import org.team5419.fault.math.units.kilogram
import org.team5419.fault.math.units.meter
import org.team5419.fault.math.units.native.NativeUnitLengthModel
import org.team5419.fault.math.units.native.nativeUnits
import org.team5419.fault.math.units.native.nativeUnitsPer100ms
import org.team5419.fault.math.units.native.nativeUnitsPer100msPerSecond
import org.team5419.fault.math.units.native.toNativeUnitAcceleration
import org.team5419.fault.math.units.native.toNativeUnitVelocity
import org.team5419.fault.math.units.second

class UnitTest {

    private val settings = NativeUnitLengthModel(
            1440.nativeUnits,
            3.0.inch
    )

    @Test
    fun testVelocitySTU() {
        val one = 1.meter / 1.second

        val two = one.toNativeUnitVelocity(settings)

        val three = two.nativeUnitsPer100ms

        assert(three.toInt() == 300)
    }

    @Test
    fun testAccelerationSTU() {
        val one = 1.meter / 1.second / 1.second

        val two = one.toNativeUnitAcceleration(settings)

        assert(two.nativeUnitsPer100msPerSecond.toInt() == 300)
    }

    @Test
    fun testFeetToMeter() {
        val one = 1.feet

        assert(one.meter epsilonEquals 0.3048)
    }

    @Test
    fun testKgToPound() {
        val kg = 2.kilogram
        assert(kg.lb epsilonEquals 4.409248840367555)
    }

    @Test
    fun testNativeUnits() {
        val nativeUnits = 360.nativeUnits.fromNativeUnit(settings)
        assert(nativeUnits.inch epsilonEquals 4.71238898038469)
    }
}
