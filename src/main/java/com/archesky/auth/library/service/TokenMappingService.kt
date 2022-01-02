package com.archesky.auth.library.service

import com.archesky.auth.library.graphql.CheckTokenQuery
import org.springframework.stereotype.Service

@Service
data class TokenMappingService(val userTokenMap: MutableMap<String, CheckTokenQuery.CheckToken> = HashMap())
