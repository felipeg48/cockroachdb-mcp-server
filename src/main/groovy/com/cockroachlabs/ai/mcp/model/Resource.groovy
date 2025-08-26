package com.cockroachlabs.ai.mcp.model

import org.springframework.http.HttpMethod

enum Resource {
    DATABASES("/databases/"),
    DATABASE("/databases/{database}/","{database}"),
    GRANTS("/databases/{database}/grants/","{database}"),
    TABLES("/databases/{database}/tables/","{database}"),
    TABLE("/databases/{database}/tables/{table}/", "{database}","{table}"),
    EVENTS("/events/"),
    NODES("/nodes/"),
    RANGES("/nodes/{node_id}/ranges/","{node_id}"),
    RANGE("/ranges/{range_id}/","{range_id}"),
    HOT_RANGES("/ranges/hot/"),
    SESSIONS("/sessions/"),
    USERS("/users/"),
    LOGIN("/login/"),
    LOGOUT("/logout/")

    final String endpoint
    final String[] replace = []

    Resource(String endpoint, String... replace) {
        this.endpoint = endpoint
        this.replace = replace
    }

    String getEndpoint(String... values) {
        String result = this.endpoint
        values.eachWithIndex { String entry, int i ->
            result = result.replace(replace[i], entry)
        }
        result
    }

    @Override
    String toString() {
        endpoint
    }
}