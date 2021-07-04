/*
 * Created on 2021-07-04T10:20:54.949+0800 by CoderYellow
 * Copied from Copyright (C) 2021 Alibaba, Inc.
 * Modified by Copyright (C) 2020 CoderYellow under Apache-2.0 License.
 *
 */
package org.microjservice.rsocket.alibaba

import io.micronaut.context.annotation.ConfigurationProperties
import com.alibaba.rsocket.route.RoutingEndpoint
import com.alibaba.rsocket.transport.NetworkUtil
import org.microjservice.rsocket.alibaba.Contants.Environment.PREFIX
import java.lang.Exception
import java.net.URI

/**
 * RSocket Properties
 *
 * @author leijuan
 */
@ConfigurationProperties(PREFIX)
class RSocketProperties {
    /**
     * schema, such as tcp, local
     */
    var schema = "tcp"

    /**
     * listen port, default is 42252, 0 means to disable listen
     */
    var port = 0

    /**
     * broker url, such tcp://127.0.0.1:42252
     */
    var brokers: List<String>? = null
        private set

    /**
     * topology, intranet or internet
     */
    var topology = "intranet"

    /**
     * metadata
     */
    var metadata: Map<String, String>? = null

    /**
     * group for exposed service
     */
    var group = ""

    /**
     * version for exposed services
     */
    var version = ""

    /**
     * JWT token
     */
    var jwtToken: String? = null

    /**
     * request/response timeout, and default value is 3000 and unit is millisecond
     */
    var timeout = 3000

    /**
     * endpoints: interface full name to endpoint url
     */
    var routes: List<RoutingEndpoint>? = null

    var p2pServices: List<String>? = null

    fun setBrokers(brokers: List<String>) {
        this.brokers = brokers
        for (broker in brokers) {
            try {
                val uri = URI.create(broker)
                if (!NetworkUtil.isInternalIp(uri.host)) {
                    topology = "internet"
                }
            } catch (ignore: Exception) {
            }
        }
    }
}