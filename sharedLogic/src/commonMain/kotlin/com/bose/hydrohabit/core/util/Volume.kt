package com.bose.hydrohabit.core.util

import kotlin.jvm.JvmInline

/**
 * A volume of water, stored internally as whole milliliters.
 *
 * Wrapping the raw [Int] in a value class prevents the classic unit bug (passing liters where
 * milliliters are expected) at zero runtime cost.
 */
@JvmInline
value class Volume(val milliliters: Int) : Comparable<Volume> {

    init {
        require(milliliters >= 0) { "Volume cannot be negative: $milliliters" }
    }

    val liters: Double get() = milliliters / 1000.0
    /** US fluid ounces, for the IMPERIAL unit system. */
    val fluidOunces: Double get() = milliliters / ML_PER_FL_OZ

    operator fun plus(other: Volume) = Volume(milliliters + other.milliliters)
    operator fun minus(other: Volume) = Volume((milliliters - other.milliliters).coerceAtLeast(0))
    operator fun times(factor: Double) = Volume((milliliters * factor).toInt().coerceAtLeast(0))

    override fun compareTo(other: Volume): Int = milliliters.compareTo(other.milliliters)

    companion object {
        const val ML_PER_FL_OZ: Double = 29.5735
        val ZERO = Volume(0)
        fun ofMilliliters(ml: Int) = Volume(ml)
        fun ofLiters(liters: Double) = Volume((liters * 1000).toInt())
        fun ofFluidOunces(oz: Double) = Volume((oz * ML_PER_FL_OZ).toInt())
    }
}
