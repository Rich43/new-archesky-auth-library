package com.pynguins.auth.library.service

import com.pynguins.auth.library.graphql.CheckTokenQuery
import org.springframework.stereotype.Service

@Service
data class TokenMappingService(val userTokenMap: MutableMap<String, CheckTokenQuery.CheckToken> = HashMap())
