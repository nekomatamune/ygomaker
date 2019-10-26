package me.nekomatamune.ygomaker

import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import me.nekomatamune.ygomaker.EventName.LOAD_PACK
import me.nekomatamune.ygomaker.EventName.SAVE_PACK
import org.spekframework.spek2.Spek

object EventDispatcherSpec : Spek({

	listOf(
		arrayOf(listOf(LOAD_PACK), 1, 0, 1),
		arrayOf(listOf(LOAD_PACK, SAVE_PACK), 1, 1, 2),
		arrayOf(listOf(LOAD_PACK, SAVE_PACK, LOAD_PACK), 2, 1, 3)

	).forEach { (events,
		expectedLoadPackCount,
		expectedSavePackCount,
		expectedCatchAllCount) ->

		test("Should invoke event handler for: $events") {
			val dispatcher = EventDispatcher()

			val spyLoadPackHandler: EventHandler = spyk({ _ -> Result.success(Unit) })
			val spySavePackHandler: EventHandler = spyk({ _ -> Result.success(Unit) })
			val spyCatchAllHandler: EventHandler = spyk({ _ -> Result.success(Unit) })

			dispatcher.register(LOAD_PACK, spyLoadPackHandler)
			dispatcher.register(SAVE_PACK, spySavePackHandler)
			dispatcher.register(LOAD_PACK, spyCatchAllHandler)
			dispatcher.register(SAVE_PACK, spyCatchAllHandler)

			(events as List<EventName>).forEach { dispatcher.dispatch(Event(it)) }

			verify(exactly = expectedLoadPackCount as Int) {
				spyLoadPackHandler(Event(LOAD_PACK))
			}

			verify(exactly = expectedSavePackCount as Int) {
				spySavePackHandler(Event(SAVE_PACK))
			}

			verify(exactly = expectedCatchAllCount as Int) {
				spyCatchAllHandler(allAny())
			}

			confirmVerified(
				spyLoadPackHandler, spySavePackHandler, spyCatchAllHandler
			)
		}
	}
})