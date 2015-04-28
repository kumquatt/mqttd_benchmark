package plantae.citrus.benchmark.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Cancellable, Actor, ActorLogging}
import org.eclipse.paho.client.mqttv3._
import plantae.citrus.benchmark.SystemContainer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

case object Connect

case class Message(message: String, id: Integer)

case object Disconnect

class MqttClientActor(address: String, clientId: String) extends Actor with MqttCallback with ActorLogging {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mqttClient = new MqttClient("tcp://" + address, clientId)
  val topic = "topic_" + clientId
  var messageTimer : Cancellable = null


  override def preStart = {
    var option = new MqttConnectOptions()
    option.setKeepAliveInterval(60)
    option.setWill("test", "test will message".getBytes, 2, true)
    mqttClient.setCallback(this)
  }

  def receive = {
    case Connect => {
      mqttClient.connect()
      log.info("({}) : connect to mqtt broker", clientId)
      mqttClient.subscribe(topic)
      log.info("({}) : subscribe to topic({})", clientId, topic)
      // set timer
      if (messageTimer != null)
        messageTimer.cancel()

      val time = 5 + Random.nextInt(5)
      messageTimer = SystemContainer.system.scheduler.scheduleOnce(FiniteDuration(time, TimeUnit.SECONDS), self , Message("Test Message", 0))


    }
    case Message(message, id) => {
      if (mqttClient.isConnected) {
        val msg = message + "_" + id
        mqttClient.publish(topic, msg.getBytes(), 0, false)
        log.info("({}) : publish topic({}) message({})", clientId, topic, msg)
      } else {
        log.info("({}) : is not connected", clientId)
      }
      // reset timer
      messageTimer.cancel()
      val time = 5 + Random.nextInt(5)
      messageTimer = SystemContainer.system.scheduler.scheduleOnce(FiniteDuration(time, TimeUnit.SECONDS), self , Message("Test Message", id+1))
    }
    case Disconnect => {
      mqttClient.disconnect()
      context.stop(self)
    }
  }

  override def deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken): Unit = {
    log.info("({}) : deliveryComplete", clientId)
  }

  override def messageArrived(s: String, mqttMessage: MqttMessage): Unit = {
    log.info("({}) : messageArrived topic({}) message({})", clientId, s, new String(mqttMessage.getPayload))
  }

  override def connectionLost(throwable: Throwable): Unit = {
    log.info("({}) : connectionLost reason({})", clientId, throwable.getMessage)
  }
}
