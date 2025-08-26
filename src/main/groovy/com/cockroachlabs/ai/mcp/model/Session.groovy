package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Session {
    int nodeId
    String username
    String clientAddress
    String applicationName
}
