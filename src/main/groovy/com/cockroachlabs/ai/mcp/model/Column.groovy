package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Column {
    String name
    String type
    boolean nullable
    String defaultValue
}
