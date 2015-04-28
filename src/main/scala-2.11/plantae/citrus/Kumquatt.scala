package plantae.citrus

import akka.actor.{Props, ActorSystem}
import plantae.citrus.actors.{Connect, MqttClientActor}


object SystemContainer {
  val system = ActorSystem()
}
object Kumquatt {

  def main(args: Array[String]): Unit ={
    val usage = """
    Usage: <server address> <port> <count> <time_wait>
    """

    if (args.length != 4) println(usage)
    else {
      val address = args(0) + ":" + args(1)
      val count = args(2).toInt
      val time_wait = args(3).toInt

      Range(0, count).foreach(x => {
        val clientId = "clientId_" + x
        val mqttClient = SystemContainer.system.actorOf(Props(classOf[MqttClientActor], address, clientId), clientId)
        mqttClient ! Connect
        Thread.sleep(time_wait)

      })
    }
  }

}
