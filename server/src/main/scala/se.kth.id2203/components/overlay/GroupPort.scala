package se.kth.id2203.components.overlay

import se.sics.kompics.PortType

class GroupPort extends PortType {
  {
    indication(classOf[GroupMessage])
  }
}
