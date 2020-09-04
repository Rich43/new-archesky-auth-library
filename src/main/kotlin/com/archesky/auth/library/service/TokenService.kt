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
import okhttp3.OkHttpClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

@Service
class TokenService(private val env: Environment) {
    private fun configureSSL(): SSLSocketFactory {
        HttpsURLConnection.setDefaultHostnameVerifier(NullHostNameVerifier())
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, Array(1) { NullTrustManager() }, SecureRandom())
        return requireNotNull(context.socketFactory, { "socket factory is null" })
    }

    fun validateToken(token: String, serverUrl: String): CheckTokenQuery.CheckToken {
        val httpClient = OkHttpClient
                .Builder()
                .sslSocketFactory(configureSSL(), NullTrustManager())
                .build()
        val apolloClient = ApolloClient.builder()
                .serverUrl(serverUrl)
                .okHttpClient(httpClient)
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
