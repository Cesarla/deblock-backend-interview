package org.deblock.exercise.infraestructure.adapters

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.util.TimeValue
import org.apache.hc.core5.util.Timeout
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.client.RestTemplate

@Configuration
@EnableScheduling
class HttpClientConfig {

    private var crazyAirConnectionManager: PoolingHttpClientConnectionManager? = null
    private var toughJetConnectionManager: PoolingHttpClientConnectionManager? = null

    @Bean("crazyAirPoolingConnectionManager")
    fun crazyAirPoolingConnectionManager(): PoolingHttpClientConnectionManager {
        val manager = PoolingHttpClientConnectionManager()
        manager.maxTotal = 200
        manager.defaultMaxPerRoute = 100

        val connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(300))
            .setSocketTimeout(Timeout.ofMilliseconds(500))
            .build()

        manager.setDefaultConnectionConfig(connectionConfig)
        this.crazyAirConnectionManager = manager
        return manager
    }

    @Bean("toughJetPoolingConnectionManager")
    fun toughJetPoolingConnectionManager(): PoolingHttpClientConnectionManager {
        val manager = PoolingHttpClientConnectionManager()
        manager.maxTotal = 200
        manager.defaultMaxPerRoute = 100

        val connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(300))
            .setSocketTimeout(Timeout.ofMilliseconds(500))
            .build()

        manager.setDefaultConnectionConfig(connectionConfig)
        this.toughJetConnectionManager = manager
        return manager
    }

    @Bean
    fun connectionKeepAliveStrategy(): ConnectionKeepAliveStrategy {
        return ConnectionKeepAliveStrategy { _, _ ->
            TimeValue.ofMilliseconds(10_000L)
        }
    }

    @Bean("crazyAirHttpClient")
    fun crazyAirCloseableHttpClient(): CloseableHttpClient {
        val requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(300))
            .build()

        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(crazyAirPoolingConnectionManager())
            .setKeepAliveStrategy(connectionKeepAliveStrategy())
            .build()
    }

    @Bean("toughJetHttpClient")
    fun toughJetCloseableHttpClient(): CloseableHttpClient {
        val requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(300))
            .build()

        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(toughJetPoolingConnectionManager())
            .setKeepAliveStrategy(connectionKeepAliveStrategy())
            .build()
    }

    @Bean("crazyAirRestTemplate")
    fun crazyAirRestTemplate(@Qualifier("crazyAirHttpClient") httpClient: CloseableHttpClient): RestTemplate {
        val factory = HttpComponentsClientHttpRequestFactory(httpClient)
        return RestTemplate(factory)
    }

    @Bean("toughJetRestTemplate")
    fun toughJetRestTemplate(@Qualifier("toughJetHttpClient") httpClient: CloseableHttpClient): RestTemplate {
        val factory = HttpComponentsClientHttpRequestFactory(httpClient)
        return RestTemplate(factory)
    }

    @Scheduled(fixedDelay = 5_000)
    fun idleConnectionMonitor() {
        try {
            crazyAirConnectionManager?.let {
                LOG.debug("Closing expired and idle connections for CrazyAir")
                it.closeExpired()
                it.closeIdle(TimeValue.ofSeconds(30))
            }
            toughJetConnectionManager?.let {
                LOG.debug("Closing expired and idle connections for ToughJet")
                it.closeExpired()
                it.closeIdle(TimeValue.ofSeconds(30))
            }
        } catch (e: Exception) {
            LOG.error("Error during idle connection cleanup", e)
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(HttpClientConfig::class.java)

    }
}