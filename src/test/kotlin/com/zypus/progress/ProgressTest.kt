package com.zypus.progress

import io.kotlintest.matchers.beGreaterThan
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import org.junit.Test
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Unit tests for the progress.
 *
 * @author zypus <zypus@t-online.de>
 *
 * @created 10.02.18
 */
class ProgressTest {

	private fun oneSecondLater(): () -> Long {
		var seconds = 1
		return {
			seconds++ * 1000L
		}
	}

	@Test
	fun `simple test`() {
		val control = Progress.control(
				totalTicks = 100L,
				ticksPerStep = 2L,
				timeProvider = System::currentTimeMillis,
				updateImmediately = false,
				updater = {}
		)
		control.ticksPerStep = 1L
		control.value shouldEqual 0.0
		control.progress.currentTicks shouldEqual 0L
		control.update(0.5)
		control.value shouldEqual 0.5
		control.progress.apply {
			currentTicks shouldEqual 50L
			current shouldBe " 50"
			total shouldBe "100"
			percentValue shouldEqual 0.5
			percent shouldBe " 50.0%"
			spin shouldBe "|"
		}
	}

	@Test
	fun `ticks`() {
		val control = Progress.control(10L)
		control.totalTicks shouldEqual 10L
		control.progress.totalTicks shouldEqual 10L
		for (i in 1L..10L) {
			control.tick()
			control.progress.currentTicks shouldEqual i
		}
	}

	data class NamedData(val name: String)

	@Test
	fun `custom data`() {

		val names = listOf("Hugo", "Peter", "Karl").map { NamedData(it) }

		val control = Progress.customControl<NamedData>(
				totalTicks = names.size,
				ticksPerStep = 1L,
				timeProvider = System::currentTimeMillis,
				updateImmediately = false,
				default = null,
				updater = {}
		)
		control.totalTicks shouldEqual names.size.toLong()
		control.progress.apply {
			totalTicks shouldEqual names.size.toLong()
			custom shouldBe null
		}
		for (d in names) {
			control.tick {
				d
			}
			control.progress.custom shouldBe d
		}

		for (d in names) {
			control.update(0.0) {
				d
			}
			control.progress.custom shouldBe d
		}
	}

	@Test
	fun `update callback`() {

		var updateCount = 0

		val stringWriter = StringWriter()
		val writer = PrintWriter(stringWriter)

		val control = Progress.control {
			updateCount++
			writer.println("$currentTicks")
		}

		val ticks = 0..9
		for (t in ticks) {
			control.tick(t)
		}

		updateCount shouldBe 10

		// accumulate ticks and convert Int to String
		val expected = ticks.fold(listOf<Int>()) { acc, next ->
			acc + ((acc.lastOrNull() ?: 0) + next)
		}.map(Int::toString)

		stringWriter.buffer.toString().split("\n").dropLast(1) shouldEqual expected
	}

	@Test
	fun `undefined`() {
		with(Progress.control()) {
			isUndefined shouldBe false
			progress.isUndefined shouldBe false

			update(-1.0)
			isUndefined shouldBe true
			progress.apply {
				isUndefined shouldBe true
				currentTicks shouldBe -1L
				current shouldBe "  ~"
				bar shouldBe "~".repeat(20)
				percent shouldBe "   ~ %"
			}

			tick()
			isUndefined shouldBe false
			progress.isUndefined shouldBe false
			progress.bar() shouldBe "-".repeat(20)
		}

	}

	@Test
	fun `spin`() {

		val spinStates = "|/-\\".repeat(4)

		with(Progress.control()) {
			for (s in spinStates) {
				tick()
				progress.spin shouldBe s.toString()
			}
		}

	}

	@Test
	fun `update immediately`() {
		var called = false

		Progress.control(updateImmediately = true) {
			called = true
		}

		called shouldBe true

	}

	@Test
	fun `reset`() {

		with(Progress.control()) {

			tick()

			progress.currentTicks shouldBe 1L
			progress.spin shouldBe "|"

			reset()

			progress.currentTicks shouldBe 0L
			progress.spin shouldBe "|"

		}

	}

	@Test
	fun `durations`() {

		var nextTime = 1000L

		fun customTime() = nextTime

		with(Progress.control(10, timeProvider = ::customTime)) {

			progress.elapsed shouldBe "  0.0s"
			progress.eta shouldBe "   ~ s"

			nextTime += 1000
			tick()

			progress.elapsed shouldBe "  1.0s"
			progress.eta shouldBe "  9.0s"

			nextTime += 60_000
			tick()

			progress.elapsed shouldBe " 1m01s"
			progress.eta shouldBe " 4m04s"

			nextTime += 60 * 60_000
			tick()

			progress.elapsed shouldBe " 1h01m"
			progress.eta shouldBe " 2h22m"

			nextTime += 24 * 60 * 60_000
			tick()

			progress.elapsed shouldBe " 1d01h"
			progress.eta shouldBe " 1d13h"
		}

	}

	@Test
	fun `bytes`() {

		with(Progress.control(1e18, timeProvider = oneSecondLater())) {

			progress.totalBytes shouldBe "1000.00PB"

			tick(-1)

			progress.bytes shouldBe "     ~ B"
			progress.rate shouldBe "   ~  B/s"

			tick(1)

			progress.bytes shouldBe "     0 B"
			progress.rate shouldBe "  0.0 B/s"

			tick(1000)

			progress.bytes shouldBe "  1.00kB"
			progress.rate shouldBe "250.0 B/s"

			tick(1e6)

			progress.bytes shouldBe "  1.00MB"
			progress.rate shouldBe "200.2kB/s"

			tick(1e9)

			progress.bytes shouldBe "  1.00GB"
			progress.rate shouldBe "166.8MB/s"

			tick(1e12)

			progress.bytes shouldBe "  1.00TB"
			progress.rate shouldBe "143.0GB/s"

			tick(1e15)

			progress.bytes shouldBe "  1.00PB"
			progress.rate shouldBe "125.1TB/s"

			tick(1e18)

			progress.bytes shouldBe "1000.00PB"
			progress.rate shouldBe "111.1PB/s"
		}

	}

	@Test
	fun `extensions`() {

		var callCount = 0

		listOf(1, 2, 4, 5, 6).withProgress { callCount++ }.map { it * it }.toList()
		callCount shouldBe 5
		callCount = 0

		listOf(1, 2, 4, 5, 6).asIterable().withProgress { callCount++ }.map { it * it }.toList()
		callCount shouldBe 5
		callCount = 0

		generateSequence(1) { it + 1 }.withProgress { callCount++ }.take(10).toList()
		callCount shouldBe 10
		callCount = 0

		val tmpFile = File.createTempFile("Progress-", ".tmp")
		tmpFile.deleteOnExit()

		tmpFile.writeText((1..10_000).map { (Math.random() * 256).toChar() }.joinToString(separator = ""))

		var blockCount = 0

		tmpFile.forEachBlockWithProgress({ callCount++ }) { _, _ ->
			blockCount++
		}
		callCount should beGreaterThan(0)
		callCount shouldBe blockCount
		callCount = 0
		blockCount = 0


		tmpFile.forEachBlockWithProgress(1000, { callCount++ }) { _, _ ->
			blockCount++
		}
		callCount should beGreaterThan(0)
		callCount shouldBe blockCount

	}

}