package actors

import akka.camel.Consumer

class JobQueueTimerActor extends Consumer {

  def endpointUri = "quartz://jobQueue?cron=0/10+*+*+*+*+?"

  override def receive = {
    case msg => {
      globals.runQueuedJob()
    }
  }
}