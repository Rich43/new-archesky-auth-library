package com.pynguins.auth.library

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.pynguins.auth.library.CheckTokenQuery.Data
import graphql.GraphQLException
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch

@Service
class TokenService {
    private val apolloClient = ApolloClient.builder()
        .serverUrl("http://localhost:9090/graphql")
        .build()

    fun validateToken(token: String): CheckTokenQuery.CheckToken {
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
