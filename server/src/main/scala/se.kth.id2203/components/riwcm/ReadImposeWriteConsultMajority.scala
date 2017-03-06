package se.kth.id2203.components.riwcm

import org.slf4j.LoggerFactory
import se.kth.id2203.components.beb.BestEffortBroadcastPort
import se.sics.kompics.sl.{ComponentDefinition, NegativePort}

import scala.collection.mutable


class ReadImposeWriteConsultMajority extends ComponentDefinition {
  case class ReadData(ts: Int, wr: Int, value: String)


  private val LOG = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajority])

  private val riwcm: NegativePort[RIWCMPort] = provides[RIWCMPort]
  private val bestEffortBroadcast = requires[BestEffortBroadcastPort]

  var ts = 0
  var wr = 0
  var acks = 0
  var writeVal: String = _
  var rid = 0
  var readList: mutable.Seq[ReadData] = mutable.ListBuffer()
  var readVal: String = _
  var reading = false

  riwcm uponEvent {
    case read: RIWCMRead => handle {
    }
  }
}
