package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Grant {
    String user
    List<String> privileges
}
