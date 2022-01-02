package com.archesky.auth.library.service;


//@Service
//class TokenService {
//    fun validateToken(token: String, serverUrl: String, hostName: String): Token {
//        val apolloClient = ApolloClient.builder()
//                .serverUrl(serverUrl)
//                .okHttpClient(OkHttpService.unsafeOkHttpClient)
//                .build()
//        val resultLatch = CountDownLatch(1)
//        var responseData: Response<Data>? = null
//        apolloClient.query(CheckTokenQuery(token))
//                .requestHeaders(builder().addHeader("hostname", hostName).build())
//                .enqueue(object : ApolloCall.Callback<Data>() {
//            override fun onFailure(e: ApolloException) {
//                resultLatch.countDown()
//                throw GraphQLException(e.message)
//            }
//
//            override fun onResponse(response: Response<Data>) {
//                responseData = response
//                resultLatch.countDown()
//            }
//        })
//        resultLatch.await()
//        responseData!!.errors().forEach {
//            throw GraphQLException(it.message())
//        }
//        return responseData!!.data()!!.checkToken
//    }
//}

import com.archesky.auth.library.model.Role;
import com.archesky.auth.library.model.Token;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TokenService {
    public Token validateToken(String token, String serverUrl, String hostName) {
        return new Token("", "", "", "", "", Collections.<Role>emptyList());
    }
}
