package models

import controllers.AppController
import utils.{S3Helper, PrintUtil}
import scala.collection._
import scala.sys.process.Process

case class JobFailedException(msg: String) extends Exception(msg)


case class Workflow(
                     name: String,
                     actions: List[Action] = List[Action](),
                     remains: mutable.Queue[Action] = mutable.Queue[Action](),
                     status: Option[String] = None
                     ) {

  val uuid: String = {
    val regex = """[^A-Za-z0-9_]""".r
    regex.replaceAllIn(name, "-") + "-" + System.currentTimeMillis()
  }

  def runJobs() {
    while (remains.length > 0) {
      // get first action in the list
      val action = remains.front
      val exitCode = if (action.dataAvailable) {
        // run action command and store process in global map
        val pb = Process(action.command).run()
        globals.runningJobs.put(uuid, pb)
        // block until return exit code
        pb.exitValue()
      } else {
        // data set is not available, fail the job
        globals.runningJobs.remove(uuid)
        throw JobFailedException(s"Job $name failed, input data set is not available.")
      }
      if (exitCode == 0) {
        remains.dequeue()
      } else {
        globals.runningJobs.remove(uuid)
        throw JobFailedException(s"Job $name failed with exit code $exitCode.")
      }
    }
    globals.runningJobs.remove(uuid)
  }

  override def toString: String = {
    s"""
      |================== WORKFLOW ==================
      | PROPERTY:     $name
      | TOTAL:        ${actions.size}
      | REMAINS:      ${remains.size} (next: ${remains.headOption.getOrElse("")})
      |==============================================
     """.stripMargin
  }
}

case class Action(
                   command: String,
                   dataSets: List[String] = List()
                   ) {

  def dataAvailable: Boolean = {

    if (dataSets.length > 0) {

      lazy val fmt = org.joda.time.format.DateTimeFormat.forPattern("MM-dd-YYYY").withZone(org.joda.time.DateTimeZone.forID("US/Pacific"))

      lazy val s3 = S3Helper("pm-archives")

      dataSets.forall { dataPath =>
        val exist = if (dataPath.endsWith("/*")) {
          s3.exists(dataPath.substring(0, dataPath.length - 2))
        } else {
          s3.exists(dataPath)
        }
        if (!exist) {
          PrintUtil("\"" + dataPath + "\" not available.")
        }
        exist
      }
    } else {
      true
    }
  }

  override def toString: String = command

}

