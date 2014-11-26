package controllers

import actors.{StopActor, JobQueueTimerActor, WorkflowQuartzActor}
import akka.actor.Props
import models.{Workflow, ActionConfig, WorkflowConfig}
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import scala.collection.JavaConversions._
import globals._



object AppController extends Controller {

  val jobQueueTimer = Akka.system.actorOf(Props(classOf[JobQueueTimerActor]), "job-queue-timer-actor")

  def index = Action {
    Ok(views.html.main())
  }

  def showWorkflows = Action {

    import play.api.libs.json._
    import play.api.libs.functional.syntax._
    implicit val actionWrites = Json.writes[ActionConfig]
    implicit val workflowWrites = Json.writes[WorkflowConfig]

    val list = globals.workflows.map { wf =>
      Json.toJson(wf._1).asInstanceOf[JsObject] ++ Json.obj("id" -> wf._1.uuid)
    }.toSeq
    Ok(Json.toJson(list))
  }

  def showAllJobs = Action {

    import play.api.libs.json._
    import play.api.libs.functional.syntax._
    implicit val actionCfgWrites = Json.writes[ActionConfig]
    implicit val workflowCfgWrites = Json.writes[WorkflowConfig]
    implicit val actionWrites: Writes[models.Action] = (
      (JsPath \ "command").write[String] and
        (JsPath \ "dataSets").write[List[String]]
      )(unlift(models.Action.unapply))
    implicit val workflowWrites = Json.writes[Workflow]

    val runningJobs = globals.runningJobs.map(_._1).toList
    val pastJobs = globals.pastJobs.toList
    val queuedJobs = globals.jobQueue.toList
    Ok(Json.obj(
      "runningJobs"-> Json.toJson(runningJobs),
      "queuedJobs" -> Json.toJson(queuedJobs),
      "pastJobs" -> Json.toJson(pastJobs)
    ))
  }

  def addWorkflow = Action(parse.json) { implicit request =>

    implicit val formats = org.json4s.DefaultFormats

    try {
      val json = org.json4s.jackson.JsonMethods.parse(request.body.toString)

      val workflowConfig = json.extract[WorkflowConfig]

      val actor = Akka.system.actorOf(Props(classOf[WorkflowQuartzActor], workflowConfig), "actor" + workflowConfig.uuid)

      globals.workflows.put(workflowConfig, actor)

      Ok("success")
    } catch {
      case e: Throwable => BadRequest("failed")
    }

  }

  def addWorkflowBatch = Action(parse.json) { implicit request =>

    implicit val formats = org.json4s.DefaultFormats

    try {
      val json = org.json4s.jackson.JsonMethods.parse(request.body.toString)

      val workflowConfigs = json.extract[List[WorkflowConfig]]

      workflowConfigs.foreach { workflowConfig =>
        val actor = Akka.system.actorOf(Props(classOf[WorkflowQuartzActor], workflowConfig), "actor" + workflowConfig.uuid)
        globals.workflows.put(workflowConfig, actor)
      }

      Ok("success")
    } catch {
      case e: Throwable => BadRequest("failed")
    }

  }

  def runJob(id: String) = Action {
    globals.workflows.find(_._1.uuid == id).map(_._2 ! "Run Job")
    Ok
  }

  def stopJob(id: String) = Action {
    try {
      runningJobs.get(id).destroy()
      runningJobs.remove(id)
      Ok("job[" + id + "] stopped")
    } catch {
      case e: Throwable => BadRequest("failed")
    }
  }

  def removeWorkflow(id: String) = Action {
    try {
      globals.workflows.find(_._1.uuid == id).map { wf =>
        wf._2 ! StopActor
        globals.workflows.remove(wf._1)
      }
      Ok("workflow["+id+"] removed")
    } catch {
      case e: Throwable => BadRequest("failed")
    }
  }

  def status = Action {

    import play.api.libs.json._
    import play.api.libs.functional.syntax._
    implicit val actionCfgWrites = Json.writes[ActionConfig]
    implicit val workflowCfgWrites = Json.writes[WorkflowConfig]
    implicit val actionWrites: Writes[models.Action] = (
      (JsPath \ "command").write[String] and
        (JsPath \ "dataSets").write[List[String]]
      )(unlift(models.Action.unapply))
    implicit val workflowWrites = Json.writes[Workflow]

    val workflows = globals.workflows.map { wf =>
      Json.toJson(wf._1).asInstanceOf[JsObject] ++ Json.obj("id" -> wf._1.uuid)
    }.toSeq

    val runningJobs = globals.runningJobs.map(_._1).toList
    val pastJobs = globals.pastJobs.toList
    val queuedJobs = globals.jobQueue.toList
    Ok(Json.obj(
      "workflows" -> Json.toJson(workflows),
      "runningJobs"-> Json.toJson(runningJobs),
      "queuedJobs" -> Json.toJson(queuedJobs),
      "pastJobs" -> Json.toJson(pastJobs)
    ))
  }

  def clearQueue = Action {
    globals.jobQueue.clear()
    Ok
  }

  def test = Action { implicit request =>

    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.write
    implicit val formats = Serialization.formats(NoTypeHints)


    val act1 = models.ActionConfig("echo aaa", None)
    val act2 = models.ActionConfig("sleep 10", None)
    val act3 = models.ActionConfig("echo bbb", None)
    val wfCfg = WorkflowConfig(None, false, "test", "9-9-2014 1010", "0/20+*+*+*+*+?", List(act1, act2, act3))

    val json = write(wfCfg)
    Ok(json).as("application/json")
  }



  def ttt = Action { implicit request =>
    Ok(views.html.test())
  }

}