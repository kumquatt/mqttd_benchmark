package plantae.citrus.benchmark

import akka.actor.{ActorSystem, Props}
import plantae.citrus.benchmark.actors.{Connect, MqttClientActor}
import plantae.citrus.mqttclient.KumquattClient


object SystemContainer {
  val system = ActorSystem()
}
object Kumquatt {

  def main(args: Array[String]): Unit ={

    val usage = """
    Usage: <server address> <port> <count> <time_wait>
    """
    KumquattClient.printAll

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
