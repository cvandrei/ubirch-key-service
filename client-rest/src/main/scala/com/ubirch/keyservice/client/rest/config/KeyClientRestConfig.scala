package com.ubirch.keyservice.client.rest.config

import com.ubirch.keyservice.util.server.RouteConstants
import com.ubirch.util.config.ConfigBase

/**
  * author: cvandrei
  * since: 2017-06-20
  */
object KeyClientRestConfig extends ConfigBase {

  /**
    * The host the REST API runs on.
    *
    * @return host
    */
  private def host = config.getString(KeyClientRestConfigKeys.HOST)

  val urlCheck = s"$host${RouteConstants.pathCheck}"

  val urlDeepCheck = s"$host${RouteConstants.pathDeepCheck}"

  val pubKey = s"$host${RouteConstants.pathPubKey}"

  def currentlyValidPubKeys(hardwareId: String) = s"$host${RouteConstants.pathPubKeyCurrentHardwareId(hardwareId)}"

}
