package com.example.ckoa.feature.daily_shape_guess.domain

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateDistanceUseCase {
    private val earthRadiusKm = 6371.0

    operator fun invoke(lat1: Double, lon1: Double, lat2: Double, lon2: Double): GeographicHint {
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val distance = calculateDistanceInKm(lat1Rad, lon1Rad, lat2Rad, lon2Rad)
        val bearing = calculateBearingDegrees(lat1Rad, lon1Rad, lat2Rad, lon2Rad)

        return GeographicHint(
            distanceInKm = distance,
            bearingDegrees = bearing
        )
    }

    private fun calculateDistanceInKm(
        lat1Rad: Double,
        lon1Rad: Double,
        lat2Rad: Double,
        lon2Rad: Double
    ): Long {
        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad
        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return (earthRadiusKm * c).toLong()
    }

    private fun calculateBearingDegrees(
        lat1Rad: Double,
        lon1Rad: Double,
        lat2Rad: Double,
        lon2Rad: Double
    ): Float {
        val dLon = lon2Rad - lon1Rad
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x)).toFloat()
        return (bearing + 360) % 360
    }
}