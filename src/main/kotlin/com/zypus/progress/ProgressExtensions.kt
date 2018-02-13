package com.zypus.progress

import java.io.File

/**
 * Extension functions to provide progress functionality to collections.
 *
 * @author zypus <zypus@t-online.de>
 *
 * @created 12.02.18
 */

/**
 * Iterates over the collection and provides a way to show progress.
 *
 * @receiver Any collection
 * @param block Callback used to indicate the progress, by default prints to stdout.
 * @return A sequence of the receiver with progress.
 */
inline fun <E> Collection<E>.withProgress(crossinline block: ProgressUpdate<E>.() -> Unit = {
	print("\r($spin) $elapsed [$bar] $percent ($currentTicks/$totalTicks) eta: $eta")
}): Sequence<E> {
	val control = Progress.customControl<E>(size) {
		block()
	}
	return asSequence().onEach {
		control.tick {
			it
		}
	}
}

/**
 * Iterates over the iterable and provides a way to show progress.
 *
 * @receiver Any iterable.
 * @param block Callback used to indicate the progress, by default prints to stdout.
 * @return A sequence of the receiver with progress.
 */
inline fun <T> Iterable<T>.withProgress(crossinline block: ProgressUpdate<T>.() -> Unit = {
	print("\r($spin) $elapsed")
}): Sequence<T> {
	val control = Progress.customControl<T>(startTicks = -1L) {
		block()
	}
	return asSequence().onEach {
		control.tick(0) {
			it
		}
	}
}

/**
 * Iterates over the sequence and provides a way to show progress.
 *
 * @receiver Any sequence.
 * @param block Callback used to indicate the progress, by default prints to stdout.
 * @return An identical sequence with progress.
 */
inline fun <T> Sequence<T>.withProgress(crossinline block: ProgressUpdate<T>.() -> Unit = {
	print("\r($spin) $elapsed")
}): Sequence<T> {
	val control = Progress.customControl<T>(startTicks = -1L) {
		block()
	}
	return onEach {
		control.tick(0) {
			it
		}
	}
}

/**
 * Allows to read a file update by update while providing a convenient way to show progress.
 *
 * @param update Progress update callback (see [ProgressControl.onUpdate]).
 * @param action task to be executed per update.
 */
inline fun File.forEachBlockWithProgress(crossinline update: ProgressUpdate<Nothing>.() -> Unit = {
	print("\r($spin) $elapsed [$bar] $percent ($bytes/$totalBytes @ $rate) eta: $eta")
}, crossinline action: (buffer: ByteArray, bytesRead: Int) -> Unit) {
	val control = Progress.control(length()) {
		update()
	}
	this.forEachBlock { buffer, bytesRead ->
		action(buffer, bytesRead)
		control.tick(bytesRead)
	}
}

/**
 * Allows to read a file update by update while providing a convenient way to show progress.
 *
 * @param blockSize The size of each update in bytes.
 * @param update Progress update callback (see [ProgressControl.onUpdate]).
 * @param action task to be executed per update.
 */
inline fun File.forEachBlockWithProgress(blockSize: Int, crossinline update: ProgressUpdate<Nothing>.() -> Unit = {
	print("\r($spin) $elapsed [$bar] $percent ($bytes/$totalBytes @ $rate) eta: $eta")
}, crossinline action: (buffer: ByteArray, bytesRead: Int) -> Unit) {
	val control = Progress.control(length()) {
		update()
	}
	this.forEachBlock(blockSize) { buffer, bytesRead ->
		action(buffer, bytesRead)
		control.tick(bytesRead)
	}
}
