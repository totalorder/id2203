package se.kth.id2203.components.riwcm

import se.sics.kompics.PortType

class RIWCMPort extends PortType {
  {
    request(classOf[RIWCMRead])
    request(classOf[RIWCMWrite])
    indication(classOf[RIWCMResponse])
  }
}
