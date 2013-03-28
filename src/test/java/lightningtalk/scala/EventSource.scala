package lightningtalk

trait EventSource {
  def registerListener(eventListener:EventListener)

}