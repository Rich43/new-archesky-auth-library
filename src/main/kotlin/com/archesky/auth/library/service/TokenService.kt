package com.archesky.auth.library.service

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.archesky.auth.library.graphql.CheckTokenQuery
import com.archesky.auth.library.graphql.CheckTokenQuery.Data
import graphql.GraphQLException
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch

@Service
class TokenService(private val env: Environment) {
    fun validateToken(token: String, serverUrl: String): CheckTokenQuery.CheckToken {
        val apolloClient = ApolloClient.builder()
                .serverUrl(serverUrl)
                .okHttpClient(OkHttpService.unsafeOkHttpClient)
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
