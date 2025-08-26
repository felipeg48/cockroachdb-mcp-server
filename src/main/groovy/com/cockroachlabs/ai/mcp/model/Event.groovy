package com.cockroachlabs.ai.mcp.model

import groovy.transform.ToString

import java.time.LocalDateTime

@ToString
class Event {
    String id
    String eventType
    LocalDateTime timestamp
}
