package frc.team5499.monkeyLib.path

import frc.team5499.monkeyLib.math.splines.QuinticHermiteSpline
import frc.team5499.monkeyLib.math.splines.SplineGenerator
import frc.team5499.monkeyLib.math.geometry.Pose2d
import frc.team5499.monkeyLib.math.geometry.Rotation2d
import frc.team5499.monkeyLib.math.geometry.Pose2dWithCurvature

object PathGenerator {

    private const val kMaxVelocity = 100.0 // i / s
    private const val kMaxAcceleration = 80.0 // in / s / s
    private const val kDefaultStartPathVelocity = 10.0 // i / s
    private const val kDefaultEndPathVelocity = 0.0 // i / s

    @Suppress("LongParameterList", "ComplexMethod")
    public fun generatePath(
        reversed: Boolean,
        waypoints: Array<Pose2d>,
        maxVelo: Double,
        maxAccel: Double,
        startVelo: Double,
        endVelo: Double
    ): Path {
        val waypointsMaybeFlipped = waypoints.toMutableList()
        val flip = Pose2d.fromRotation(Rotation2d(-1.0, 0.0, false))
        if (reversed) {
            waypointsMaybeFlipped.clear()
            for (pose in waypoints) {
                waypointsMaybeFlipped.add(pose.transformBy(flip))
            }
        }

        val splines: MutableList<QuinticHermiteSpline> = mutableListOf()
        for (i in 0..waypointsMaybeFlipped.size - 2) {
            splines.add(QuinticHermiteSpline(waypointsMaybeFlipped.get(i), waypointsMaybeFlipped.get(i + 1)))
        }
        QuinticHermiteSpline.optimizeSpline(splines)

        var samples = SplineGenerator.parameterizeSplines(splines)
        if (reversed) {
            val flipped = samples.toMutableList()
            flipped.clear()
            for (i in 0..samples.size - 1) {
                flipped.add(Pose2dWithCurvature(
                    samples.get(i).pose.transformBy(flip),
                    -samples.get(i).curvature,
                    samples.get(i).dCurvature
                ))
            }
            samples = flipped
        }

        // extend last segment by lookahead distance
        val lastNorm = (samples.get(samples.size - 1).translation -
            samples.get(samples.size - 2).translation).normalized
        val newSegment = samples.get(samples.size - 1).translation + (lastNorm * 12.0)
        samples.set(samples.size - 1, Pose2dWithCurvature(
            Pose2d(newSegment, samples.get(samples.size - 1).rotation),
            samples.get(samples.size - 1).curvature,
            samples.get(samples.size - 1).dCurvature)
        )

        val velocities = mutableListOf<Double>()
        for (i in 0..samples.size - 1) {
            velocities.add(Math.min(maxVelo, Math.abs(3.0 / samples.get(i).curvature)))
        }
        velocities.set(velocities.size - 1, endVelo)
        for (i in (samples.size - 2).downTo(0)) {
            val distance = samples.get(i).translation.distanceTo(samples.get(i + 1).translation)
            val value = Math.min(
                velocities.get(i),
                Math.sqrt(Math.pow(velocities.get(i + 1), 2.0) + 2.0 * maxAccel * distance)
            )
            velocities.set(i, value)
        }

        velocities.set(0, startVelo)
        for (i in 0..samples.size - 2) {
            val distance = samples.get(i).translation.distanceTo(samples.get(i + 1).translation)
            val value = Math.sqrt(Math.pow(velocities.get(i), 2.0) + 2.0 * maxAccel * distance)
            if (value < velocities.get(i + 1))
                velocities.set(i + 1, value)
        }

        return Path(samples, velocities, reversed)
    }

    public fun generatePath(reversed: Boolean, waypoints: Array<Pose2d>): Path {
        return generatePath(
            reversed, waypoints,
            kMaxVelocity,
            kMaxAcceleration,
            kDefaultStartPathVelocity,
            kDefaultEndPathVelocity
        )
    }
}
