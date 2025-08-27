package com.cockroachlabs.ai.mcp.config

import com.cockroachlabs.ai.mcp.service.CockroachDbMoltCalls
import com.cockroachlabs.ai.mcp.service.CockroachDbMcpCalls
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CockroachDbToolCallbacks {

    @Bean
    ToolCallbackProvider cockroachDbMcpTools(CockroachDbMcpCalls cockroachDbMcpCalls,
                                             CockroachDbMoltCalls cockroachDbMoltCalls) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        cockroachDbMcpCalls,
                        cockroachDbMoltCalls)
                .build()
    }

}
