package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

@ToString
class Table {
    String name

    List<Column> columns
    List<Index> indexes
    List<Grant> grants

    int rangeCount
    int replicas
    int descriptorId
}
