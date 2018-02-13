package com.zypus.progress

import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * Progress control that is used to advance the progress.
 *
 * @param T Type of the custom data.
 * @property totalTicks The number of ticks needed to complete the progress.
 * @property ticksPerStep The number of ticks to advance by default per [tick()][tick] call.
 * @property timeProvider Function to get the current time in milliseconds (needed for testing/debugging).
 * @property defaultCustom The initial/default value of the custom data.
 *
 * @author zypus <zypus@t-online.de>
 *
 * @created 10.02.18
 */
class ProgressControl<T> internal constructor(var totalTicks: Long, startTicks: Long = 0L, var ticksPerStep: Long = 1L, private val timeProvider: () -> Long = System::currentTimeMillis, private val defaultCustom: T? = null) {

	private var currentTicks = max(-1, min(startTicks, totalTicks))
	private var startTime = timeProvider()
	private var state = 0 // used to create the spinner
	private var lastCustom: () -> T? = { defaultCustom }
	private var updater: ProgressUpdate<T>.() -> Unit = {}

	/**
	 * The current progress in the range from 0.0-1.0, can be negative (-1/totalTicks) to indicate undefined state.
	 */
	val value: Double get() = currentTicks.toDouble() / totalTicks
	/**
	 * If the current number of ticks is negative the progress is undefined.
	 */
	val isUndefined: Boolean get() = currentTicks < 0

	/**
	 * The most recent [ProgressUpdate].
	 */
	var progress: ProgressUpdate<T> = ProgressUpdate(
			currentTicks = currentTicks,
			totalTicks = totalTicks,
			duration = Duration.of(timeProvider() - startTime, ChronoUnit.MILLIS),
			state = ProgressUpdate.SpinStates.values()[state],
			customCreator = lastCustom
	)
		private set

	/**
	 * Sets the progress value to a new value.
	 *
	 * @param newValue The new value of the progress (ATTENTION: the actual progress value can differ because it is re-expressed in terms of integer ticks / totalTicks)
	 * @param custom A new value for the custom data, if not set the previous value is retained.
	 */
	fun update(newValue: Double, custom: (() -> T?)? = null) {
		currentTicks = max(-1, min((totalTicks * newValue).toLong(), totalTicks))
		if (custom != null) {
			lastCustom = custom
		}
		internalUpdate()
	}

	/**
	 * Advanced the progress by a set amount of ticks (by default ticksPerStep).
	 *
	 * @param ticks The number of ticks to advance the progress by, can be negative.
	 * @param custom A new value for the custom data, if not set the previous value is retained.
	 */
	fun tick(ticks: Number = ticksPerStep, custom: (() -> T?)? = null) {
		currentTicks = max(-1, min(currentTicks + ticks.toLong(), totalTicks))
		if (custom != null) {
			lastCustom = custom
		}
		internalUpdate()
	}

	private fun internalUpdate() {
		progress = ProgressUpdate(
				currentTicks = currentTicks,
				totalTicks = totalTicks,
				duration = Duration.of(timeProvider() - startTime, ChronoUnit.MILLIS),
				state = ProgressUpdate.SpinStates.values()[state],
				customCreator = lastCustom
		)
		state = (state + 1) % 4
		updater(progress)
	}

	/**
	 * Callback function to be executed on every update/tick call.
	 *
	 * @param updateImmediately If true will call the callback immediately.
	 * @param block The update call back, executed in the context of a ProgressUpdate.
	 */
	fun onUpdate(updateImmediately: Boolean = false, block: ProgressUpdate<T>.() -> Unit) {
		updater = block
		if (updateImmediately) {
			internalUpdate()
		}
	}

	/**
	 * Resets the progress control.
	 *
	 * @param update If true, will call the update callback after the reset.
	 */
	fun reset(update: Boolean = true) {
		currentTicks = 0L
		startTime = System.currentTimeMillis()
		state = 0
		lastCustom = { defaultCustom }
		if (update) internalUpdate()
	}

}