package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Node {
    int id
    String address
    String clusterName
    int cpus
    String version
}
