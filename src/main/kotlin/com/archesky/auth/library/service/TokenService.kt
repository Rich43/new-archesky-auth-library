package com.archesky.auth.library.service

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.archesky.auth.library.graphql.CheckTokenQuery
import com.archesky.auth.library.graphql.CheckTokenQuery.Data
import com.archesky.auth.library.security.NullHostNameVerifier
import com.archesky.auth.library.security.NullTrustManager
import graphql.GraphQLException
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.lang.Boolean.valueOf
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

@Service
class TokenService(private val env: Environment) {
    private fun configureSSL() {
        val trustStore = env.getProperty("server.ssl.trust-store")
        val trustStorePassword = env.getProperty("server.ssl.trust-store-password")
        if (trustStore !== null && trustStorePassword !== null) {
            System.setProperty("javax.net.ssl.trustStore", trustStore)
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword)
        }
        if (valueOf(env.getProperty("server.ssl.disable-verification"))) {
            HttpsURLConnection.setDefaultHostnameVerifier(NullHostNameVerifier())
            val context: SSLContext = SSLContext.getInstance("TLS")
            context.init(null, Array(1){ NullTrustManager() }, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(context.socketFactory)
        }
    }

    fun validateToken(token: String, serverUrl: String): CheckTokenQuery.CheckToken {
        configureSSL()
        val apolloClient = ApolloClient.builder()
            .serverUrl(serverUrl)
            .build()
        val resultLatch = CountDownLatch(1)
        var responseData: Response<Data>? = null
        apolloClient.query(
            CheckTokenQuery(token)
        ).enqueue(object : ApolloCall.Callback<Data>() {
            override fun onFailure(e: ApolloException) {
                resultLatch.countDown()
                throw GraphQLException(e.message)
            }

            override fun onResponse(response: Response<Data>) {
                responseData = response
                resultLatch.countDown()
            }
        })
        resultLatch.await()
        responseData!!.errors().forEach {
            throw GraphQLException(it.message())
        }
        return responseData!!.data()!!.checkToken
    }
}
