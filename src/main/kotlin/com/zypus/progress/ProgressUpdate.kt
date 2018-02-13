package com.zypus.progress

import java.time.Duration

/**
 * Encapsulate a progress status update and for convenience various pre-formatted info.
 *
 * @param T The type of the custom data.
 * @property currentTicks The current number of ticks.
 * @property totalTicks The total number of ticks.
 * @property duration The elapsed time since the creation/reset of the progress control.
 * @property customCreator Custom data creator based on either the [tick()][ProgressControl.tick] or [update()][ProgressControl.update] function.
 *
 * @author zypus <zypus@t-online.de>
 *
 * @created 10.02.18
 */
data class ProgressUpdate<out T> internal constructor(val currentTicks: Long, val totalTicks: Long, val duration: Duration, private val state: SpinStates = SpinStates.N, private val customCreator: (() -> T?)) {

	internal enum class SpinStates(val representation: String) {
		N("|"), NO("/"), O("-"), SO("\\")
	}

	/**
	 * True, if the progress is undefined.
	 */
	val isUndefined: Boolean get() = currentTicks < 0

	/**
	 * Custom data which only gets created if requested.
	 */
	val custom: T? by lazy {
		customCreator()
	}

	/**
	 * The current number of ticks as string.
	 */
	val current: String by lazy {
		val length = Math.log10(totalTicks.toDouble()).toInt() + 1
		if (this.isUndefined) {
			"~".padStart(length)
		}
		else {
			"$currentTicks".padStart(length)
		}
	}

	/**
	 * The total number of ticks as string.
	 */
	val total: String by lazy {
		totalTicks.toString()
	}

	/**
	 * The current progress in percent.
	 */
	val percentValue: Double by lazy {
		currentTicks.toDouble() / totalTicks
	}

	/**
	 * The current progress in percent as formatted string.
	 */
	val percent: String by lazy {
		if (this.isUndefined) {
			"~ %".padStart(6)
		}
		else {
			"%.1f%%".format(Math.floor(10000 * percentValue) / 100).padStart(6)
		}
	}

	/**
	 * Default bar of length 20 characters, using # and blanks.
	 */
	val bar: String by lazy {
		bar()
	}

	/**
	 * Creates a progress bar of specified length and components.
	 *
	 * @param length The length of the progress bar in characters.
	 * @param filled The string element used for the filled part of the bar.
	 * @param empty The string element used for the empty part of the bar.
	 * @param undefined The element used for the the bar when it is in an undefined progress state.
	 */
	fun bar(length: Int = 20, filled: Char = '=', empty: Char = '-', undefined: Char = '~'): String {
		return if (percentValue in 0.0..1.0) {
			val n = Math.floor(percentValue / (1.0 / length)).toInt()
			filled.toString().repeat(n) + empty.toString().repeat(length - n)
		}
		else {
			undefined.toString().repeat(length)
		}

	}

	/**
	 * The elapsed time as string since the progress bar was created!! or [reset][ProgressControl.reset].
	 */
	val elapsed: String by lazy {
		formatDuration(duration).padStart(6)
	}

	/**
	 * Estimated time as string until the process will be done, based on the [elapsed time][elapsed] and current progress (via linear interpolation).
	 */
	val eta: String by lazy {
		when {
			currentTicks <= 0L -> " ~ s"
			else               -> formatDuration(duration.dividedBy(currentTicks).multipliedBy(totalTicks - currentTicks))
		}.padStart(6)
	}

	/**
	 * The number of ticks formatted as bytes.
	 */
	val bytes: String by lazy {
		formatBytes(currentTicks).padStart(8)
	}

	/**
	 * The number of total ticks formatted as bytes.
	 */
	val totalBytes: String by lazy {
		formatBytes(totalTicks).trim()
	}

	/**
	 * The current bytes/ticks per second.
	 */
	val rate: String by lazy {
		if (duration.seconds < 1 || this.isUndefined) {
			return@lazy "~  B/s".padStart(9)
		}
		val bytesPerSecond = currentTicks.toDouble() / duration.seconds
		when {
			bytesPerSecond < 1e3  -> "%.1f B/s".format(bytesPerSecond)
			bytesPerSecond < 1e6  -> "%.1fkB/s".format(bytesPerSecond / 1e3)
			bytesPerSecond < 1e9  -> "%.1fMB/s".format(bytesPerSecond / 1e6)
			bytesPerSecond < 1e12 -> "%.1fGB/s".format(bytesPerSecond / 1e9)
			bytesPerSecond < 1e15 -> "%.1fTB/s".format(bytesPerSecond / 1e12)
			else                  -> "%.1fPB/s".format(bytesPerSecond / 1e15)
		}.padStart(9)
	}

	/**
	 * String which will change with each update and can be used as a spinner to indicate activity.
	 */
	val spin: String by lazy {
		state.representation
	}

	private fun formatDuration(duration: Duration): String {

		val seconds = duration.seconds

		return when {
			seconds < 60           -> "%d.0s".format(seconds)
			seconds < 60 * 60      -> "%dm%02ds".format(seconds / 60, seconds % 60)
			seconds < 60 * 60 * 24 -> "%dh%02dm".format(seconds / 60 / 60, seconds / 60 % 60)
			else                   -> "%dd%02dh".format(seconds / 60 / 60 / 24, seconds / 60 / 60 % 24)
		}
	}

	private fun formatBytes(ticks: Long): String {
		return when {
			this.isUndefined -> " ~ B"
			ticks < 1e3      -> "%d B".format(ticks)
			ticks < 1e6      -> "%.2fkB".format(ticks.toDouble() / 1e3)
			ticks < 1e9      -> "%.2fMB".format(ticks.toDouble() / 1e6)
			ticks < 1e12     -> "%.2fGB".format(ticks.toDouble() / 1e9)
			ticks < 1e15     -> "%.2fTB".format(ticks.toDouble() / 1e12)
			else             -> "%.2fPB".format(ticks.toDouble() / 1e15)
		}
	}
}