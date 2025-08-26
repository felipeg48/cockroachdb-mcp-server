package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Range {
    int rangeId

    String startKey
    String endKey
    String spanStartKey
    String spanEndKey

    // Hot Range
    int nodeId
    int leaseHolderNodeId
    List<String> databases
    List<String> tables
    List<String> indexes
    List<Integer> replicaNodeIds
}
