package lightningtalk

class ConcreteEventSource extends EventSource{
  override def registerListener(eventListener:EventListener) = eventListener.onEvent  

}

object ConcreteEventSource {
  def main(args:Array[String]) = {
    val source = new ConcreteEventSource
    new SimpleEventListener(source)
  }
}