package com.zypus.progress

/**
 * Object for creating progress controls.
 *
 * @author zypus <zypus@t-online.de>
 *
 * @created 10.02.18
 */
object Progress {

	/**
	 * Basic progress control without custom data.
	 *
	 * @param totalTicks The total amount of progress ticks this control will be initialized with (can be changed afterwards).
	 * @param ticksPerStep The number of ticks to be used by default per tick() call.
	 * @param timeProvider A function that returns the current time, mainly useful for testing.
	 * @param updateImmediately If set to true, will call the onUpdate callback of the control immediately before returning the control.
	 * @param updater Progress update callback (can be altered later).
	 *
	 * @return Progress control.
	 */
	fun control(
			totalTicks: Number = 100L,
			startTicks: Number = 0L,
			ticksPerStep: Number = 1L,
			timeProvider: () -> Long = System::currentTimeMillis,
			updateImmediately: Boolean = false,
			updater: ProgressUpdate<Nothing>.() -> Unit = {}
	): ProgressControl<Nothing> = ProgressControl<Nothing>(totalTicks.toLong(), startTicks.toLong(), ticksPerStep.toLong(), timeProvider).also { it.onUpdate(updateImmediately, updater) }

	/**
	 * Progress control that also handles custom data.
	 *
	 * @param totalTicks The total amount of progress ticks this control will be initialized with (can be changed afterwards).
	 * @param ticksPerStep The number of ticks to be used by default per tick() call.
	 * @param timeProvider A function that returns the current time, mainly useful for testing.
	 * @param updateImmediately If set to true, will call the onUpdate callback of the control immediately before returning the control.
	 * @param default Initial value of the custom data object.
	 * @param updater Progress update callback (can be altered later).
	 *
	 * @return Progress control.
	 */
	fun <T> customControl(
			totalTicks: Number = 100L,
			startTicks: Number = 0L,
			ticksPerStep: Number = 1L,
			timeProvider: () -> Long = System::currentTimeMillis,
			updateImmediately: Boolean = false,
			default: T? = null,
			updater: ProgressUpdate<T>.() -> Unit = {}
	): ProgressControl<T> = ProgressControl(totalTicks.toLong(), startTicks.toLong(), ticksPerStep.toLong(), timeProvider, default).also {
		it.onUpdate(updateImmediately, updater)
	}

}