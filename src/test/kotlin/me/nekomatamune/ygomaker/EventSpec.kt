package me.nekomatamune.ygomaker

import org.spekframework.spek2.Spek
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object EventSpec : Spek({
	group("EventDispatcher#dispatchEvent") {
		var invocations = 0
		registerEventHandler(
			EventName.LOAD_PACK) {
			invocations++
			Result.success(Unit)
		}

		beforeEachTest {
			invocations = 0
		}

		test("Should invoke event handlers multiple times") {
			dispatchEvent(Event(
				name = EventName.LOAD_PACK))
			expectThat(invocations).isEqualTo(1)

			dispatchEvent(Event(
				name = EventName.LOAD_PACK))
			expectThat(invocations).isEqualTo(2)
		}

		test("Should not invoke handler with non-matching EventName") {
			dispatchEvent(Event(
				name = EventName.SAVE_PACK))
			expectThat(invocations).isEqualTo(0)
		}

		afterEachTest {
			unregisterAllEventHandlers()
		}
	}
})