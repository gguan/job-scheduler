package actors

import akka.camel.Consumer
import models.{JobFailedException, WorkflowConfig}
import play.api.Logger

/**
 * When add a new scheduled workflow config,
 * create a new actor for that workflow config
 */
class WorkflowQuartzActor(wfCfg: WorkflowConfig) extends Consumer {

  def endpointUri = "quartz://" + wfCfg.uuid + "?cron=" + wfCfg.cron.replaceAll(" ", "+")

  def receive = {
    case StopActor => {
      context.stop(self)
      globals.workflows.remove(wfCfg)
    }
    case _ => {
      Logger.info("run " + wfCfg.uuid + "...")
      wfCfg.toWorkflow match {
        case Some(workflow) => {
          if (wfCfg.isBlocked) {
            globals.jobQueue.add(workflow)
          } else {
            var status = "success"
            try {
              workflow.runJobs()
            } catch {
              case e: JobFailedException => {
                Logger.error(e.getMessage)
                status = "failed"
              }
            } finally {
              globals.addToPastJobs(workflow.copy(status=Some(status)))
            }
          }
        }
        case None => println("None!")
      }
    }
  }
}
