package se.kth.id2203.components.riwcm

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.components.beb.{BestEffortBroadcastDeliver, BestEffortBroadcastPort, BestEffortBroadcastRequest}
import se.kth.id2203.components.overlay.{GroupMessage, GroupPort}
import se.kth.id2203.networking.{Message, NetAddress}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Network
import se.sics.kompics.sl.{ComponentDefinition, handle}

import scala.collection.mutable


class ReadImposeWriteConsultMajority extends ComponentDefinition {
  case class ReadData(ts: Int, wr: Int, value: Option[String], pid: Int)

  private val LOG = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajority])

  private val riwcm = provides[RIWCMPort]
  private val bestEffortBroadcast = requires[BestEffortBroadcastPort]
  private val net = requires[Network]
  private val group = requires[GroupPort]
  private val self = config.getValue("id2203.project.address", classOf[NetAddress])

  val store: mutable.Map[String, String] = new mutable.HashMap()
  var ts = 0
  var wr = 0
  var acks = 0
  var writeVal: String = _
  var rid = 0
  var readList: mutable.ListBuffer[ReadData] = mutable.ListBuffer()
  var readVal: Option[String] = _
  var reading = false

  var replicationGroup: List[NetAddress] = _
  var pid: Int = _
  var operationActive: UUID = _
  var queuedOperations: mutable.ListBuffer[KompicsEvent] = mutable.ListBuffer()

  group uponEvent {
    case message: GroupMessage => handle {
      replicationGroup = message.group
      pid = message.pid
      LOG.info(s"Got replication group (pid: $pid): $replicationGroup")
    }
  }

  riwcm uponEvent {
    case read: RIWCMRead => handle {
      queuedOperations += read
      processOperations()
    }
  }

  def processOperations(): Unit = {
    if (operationActive == null && queuedOperations.nonEmpty) {
      queuedOperations.remove(0) match {
        case read: RIWCMRead => handleRead(read)
        case write: RIWCMWrite => handleWrite(write)
      }
      processOperations()
    }
  }

  def handleRead(read: RIWCMRead): Unit = {
    operationActive = read.uuid
    rid += 1
    acks = 0
    readList.clear()
    reading = true
    triggerBeb(RIWCMInternalRead(read.uuid, pid, read.key, rid))
  }

  def handleWrite(write: RIWCMWrite): Unit = {
    operationActive = write.uuid
    rid += 1
    writeVal = write.value
    acks = 0
    readList.clear()
    triggerBeb(RIWCMInternalRead(write.uuid, pid, write.key, rid))
  }

  riwcm uponEvent {
    case write: RIWCMWrite => handle {
      queuedOperations += write
      processOperations()
    }
  }

  bestEffortBroadcast uponEvent {
    case BestEffortBroadcastDeliver(src, internalRead: RIWCMInternalRead) => handle {
      triggerNet(src, RIWCMInternalReadResponse(
        internalRead.uuid, pid, internalRead.key, internalRead.rid, ts, wr, store.get(internalRead.key)))
    }
  }

  net uponEvent {
    case Message(_, _, internalReadResponse: RIWCMInternalReadResponse) => handle {
      LOG.info(s"Received read response")
      if (internalReadResponse.rid == rid) {
        if (readList.exists(readData => readData.pid == internalReadResponse.pid)) {
          throw new RuntimeException(s"Already exists in readList: $internalReadResponse")
        }

        readList += ReadData(ts, wr, internalReadResponse.value, internalReadResponse.pid)

        if (readList.length > replicationGroup.length / 2) {
          val maxReadData = readList.sortBy(readData => (readData.ts, readData.wr)).last
          readVal = maxReadData.value

          readList.clear()
          if (reading) {
            triggerBeb(RIWCMInternalWrite(
              internalReadResponse.uuid, pid, internalReadResponse.key, rid, maxReadData.ts, maxReadData.wr, readVal))
          } else {
            triggerBeb(RIWCMInternalWrite(
              internalReadResponse.uuid, pid, internalReadResponse.key, rid, maxReadData.ts + 1, pid, Option(writeVal)))
          }
        }
      } else {
        LOG.info(s"Received invalid rid: $internalReadResponse (rid: $rid)")
      }
    }
  }


  bestEffortBroadcast uponEvent {
    case BestEffortBroadcastDeliver(src, internalWrite: RIWCMInternalWrite) => handle {
      if (internalWrite.ts > ts || (internalWrite.ts == ts && internalWrite.wr > wr)) {
        ts = internalWrite.ts
        wr = internalWrite.wr
        internalWrite.value.foreach { value =>
          store.put(internalWrite.key, value)
        }
      }

      triggerNet(src, RIWCMInternalAck(internalWrite.uuid, pid, internalWrite.key, internalWrite.rid))
    }
  }

  net uponEvent {
    case Message(_, _, ack: RIWCMInternalAck) => handle {
      if (!ack.uuid.equals(operationActive)) {
        LOG.info("Received INVALID ack")
      } else {
        LOG.info("Received ack")
        acks += 1
        if (acks > replicationGroup.length / 2) {
          acks = 0
          if (reading) {
            reading = false
            trigger(RIWCMReadResponse(ack.uuid, ack.key, readVal), riwcm)
          } else {
            trigger(RIWCMWriteResponse(ack.uuid, ack.key), riwcm)
          }
          operationActive = null
          processOperations()
        }
      }
    }
  }

  private def triggerBeb(event: KompicsEvent): Unit = {
    trigger(BestEffortBroadcastRequest(event, replicationGroup), bestEffortBroadcast)
  }

  private def triggerNet(dst: NetAddress, event: KompicsEvent): Unit = {
    trigger(Message(self, dst, event), net)
  }
}
