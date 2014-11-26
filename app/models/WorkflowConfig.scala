package models

import org.joda.time.DateTime

case class WorkflowConfig(disabled: Option[Boolean], isBlocked: Boolean, name: String, start: String, cron: String, actions: List[ActionConfig]) {

  val uuid: String = {
    val regex = """[^A-Za-z0-9_]""".r
    regex.replaceAllIn(name, "-") + "-" + System.currentTimeMillis()
  }

  def toWorkflow(): Option[Workflow] = {
    val fmt = org.joda.time.format.DateTimeFormat.forPattern("dd-MM-YYYY kkmm").withZone(org.joda.time.DateTimeZone.forID("US/Pacific"))
    val startTime = fmt.parseDateTime(start)

    if (disabled == Some(true)) None
    else if (startTime.minusHours(3).isAfterNow) {println(startTime);println("ssss");None}
    else {
      val acts = scala.collection.mutable.Queue[Action]()
      actions.foreach(x => acts.enqueue(x.toAction(startTime)))
      Some(Workflow(name = name, actions = acts.toList, remains = acts).copy())
    }
  }
}

case class ActionConfig(command: String, datasets: Option[String]) {
  def toAction(date: DateTime): Action = {
    val fmt = org.joda.time.format.DateTimeFormat.forPattern("dd-MM-YYYY").withZone(org.joda.time.DateTimeZone.forID("US/Pacific"))
    val Ptrn1 = """[^\{]+\{DATE\}[^\}]+""".r
    val Ptrn2 = """[^\{]+\{([0-9,-]+)\}[^\}]+""".r
    val ReplacePtrn = """\{[0-9,-]+\}""".r

    val cmd = command.replace("{DATE}", fmt.print(date))

    datasets match {
      case Some(ds) =>  {
        ds match {
          case Ptrn1() => {  // e.g. activities/wikihow/{DATE}/*
          val dataPath = ds.replace("{DATE}", fmt.print(date))
            Action(cmd, List(dataPath))
          }
          case Ptrn2(m) => {  // e.g. activities/wikihow/{-6,0}/*
          val start = m.split(",")(0).toInt
            val end = m.split(",")(1).toInt
            val dataPath = (start to end).map { i =>
              ReplacePtrn.replaceAllIn(ds, fmt.print(date.plusDays(i)))
            }.toList
            Action(cmd, dataPath)
          }
          case _ => Action(cmd, List(ds))
        }
      }
      case None => Action(cmd, List())
    }
  }
}
