import java.util.concurrent.{ConcurrentLinkedQueue, ConcurrentHashMap}
import akka.actor.ActorRef
import models.{WorkflowConfig, JobFailedException, Workflow}
import play.api._
import scala.sys.process.Process

package object globals {

  val workflows = new ConcurrentHashMap[WorkflowConfig, ActorRef]()

  val jobQueue = new ConcurrentLinkedQueue[Workflow]()

  val pastJobs = new ConcurrentLinkedQueue[Workflow]()

  val runningJobs = new ConcurrentHashMap[String, Process]() // (workflowId, pid)

  val isRunningQueueJob = new scala.concurrent.SyncVar[Boolean]()
  isRunningQueueJob.put(false)

  def runQueuedJob() {
    if (!jobQueue.isEmpty && !isRunningQueueJob.get) {
      val workflow = jobQueue.peek
      jobQueue.remove(workflow)
      var status = "success"
      try {
        workflow.runJobs()
      } catch {
        case e: JobFailedException => {
          Logger.error("error: " + e.getMessage)
          status = "failed"
        }
      } finally {
        pastJobs.add(workflow.copy(status=Some(status)))
        if (pastJobs.size() > 100) {
          pastJobs.remove(pastJobs.peek)
        }
      }
    }
  }

  def addToPastJobs(workflow: Workflow) {
    pastJobs.add(workflow)
    if (pastJobs.size() > 100) {
      pastJobs.remove(pastJobs.peek)
    }
  }

}


object Global extends GlobalSettings {

  override def onStart(app: Application) {

    Logger.info("Application has started!!!!")
  }
}
