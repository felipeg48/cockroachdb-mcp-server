package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Index {
    String name
    boolean unique
    String column
    String direction
    boolean storing
    boolean implicit
    int sequence
}
