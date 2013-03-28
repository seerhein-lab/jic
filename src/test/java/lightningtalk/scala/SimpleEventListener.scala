package lightningtalk

import java.lang.AssertionError

class SimpleEventListener(source:EventSource) extends EventListener{
	source.registerListener(this)
	val state=42
	
	override def onEvent = if (state !=42) throw new AssertionError("SimpleEventListener in invalid state.")
}