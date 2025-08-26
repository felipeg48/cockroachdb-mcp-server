package com.cockroachlabs.ai.mcp.service

import com.cockroachlabs.ai.mcp.model.Column
import com.cockroachlabs.ai.mcp.model.Database
import com.cockroachlabs.ai.mcp.model.Event
import com.cockroachlabs.ai.mcp.model.Grant
import com.cockroachlabs.ai.mcp.model.Index
import com.cockroachlabs.ai.mcp.model.Login
import com.cockroachlabs.ai.mcp.model.Logout
import com.cockroachlabs.ai.mcp.model.Node
import com.cockroachlabs.ai.mcp.model.Range
import com.cockroachlabs.ai.mcp.model.Resource
import com.cockroachlabs.ai.mcp.model.Session
import com.cockroachlabs.ai.mcp.model.Table
import com.cockroachlabs.ai.mcp.model.User
import groovy.json.JsonSlurper

import org.springframework.ai.tool.annotation.Tool
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import java.time.Instant
import java.time.ZoneOffset
import java.time.LocalDateTime

@Service
class CockroachDbMcpCalls {
    // Global / Constants
    private final String API_BASE = "/api/v2"
    private final JsonSlurper jsonSlurper = new JsonSlurper()

    // Constructor
    private final RestTemplate restTemplate
    private final String apiUrl
    private String sessionCookie


    CockroachDbMcpCalls(@Value('${cockroachdb.api.url}') String apiUrl,
                        @Value('${cockroachdb.api.session-cookie:}') String sessionCookie,
                        RestTemplate restTemplate) {
        this.apiUrl = Objects.requireNonNull(apiUrl, "cockroachdb.api.url must not be null")
        this.sessionCookie = sessionCookie
        this.restTemplate = restTemplate
    }

    @Tool(description = "List nodes. Get details on all nodes in the cluster, including node IDs, software versions, and hardware.")
    List<Node> getNodesInfo() {
        def json = jsonSlurper.parseText(this.rawNodesInfo)
        List<Node> cluster = []
        json.nodes.each { node ->
            cluster << new Node(
                    id: node.node_id,
                    address: node.address.address_field,
                    clusterName: node?.cluster_name ?: 'unknown',
                    cpus: node.num_cpus,
                    version: node.build_tag
            )
        }
        cluster
    }

    @Tool(description = "List databases. Get all databases in the cluster")
    List<Database> getDatabases() {
        def raw = getResource(Resource.DATABASES.endpoint)
        def json = jsonSlurper.parseText(raw)
        ArrayList<Database> dbs = []
        json.databases.each { db ->
            dbs << new Database(
                   name: db
            )
        }
        dbs
    }

    @Tool(description = "Get database details. Get the descriptor ID of a specified database.")
    Database getDatabaseInfo(String database) {
        def raw = getResource(Resource.DATABASE.getEndpoint(database))
        def json = jsonSlurper.parseText(raw)
        new Database(name: database, descriptorId: json.descriptor_id)
    }

    @Tool(description = "List database grants. List all privileges granted to users for a specified database.")
    List<Grant> getDatabaseGrants(String database) {
        def raw = getResource(Resource.GRANTS.getEndpoint(database))
        def json = jsonSlurper.parseText(raw)
        ArrayList<Grant> grants = []
        json.grants.each { grant ->
            grants << new Grant(
                    user: grant.user,
                    privileges: grant.privileges
            )
        }
        grants
    }

    @Tool(description = "List database tables. List all tables in a specified database.")
    List<Table> getDatabaseTables(String database) {
        def raw = getResource(Resource.TABLES.getEndpoint(database))
        def json = jsonSlurper.parseText(raw)
        List<Table> tableList = []
        json.table_names.each { name ->
            tableList << new Table(name: name)
        }
        tableList
    }

    @Tool(description = "Get table details. Get details on a specified table, including schema, grants, indexes, range count, and zone configurations.")
    Table getDatabaseTableDetail(String database, String table) {
        def raw = getResource(Resource.TABLE.getEndpoint(database,table))
        def json = jsonSlurper.parseText(raw)

        List<Grant> grants = []
        json.grants.each { grant ->
            grants << new Grant(
                    user: grant.user,
                    privileges: grant.privileges
            )
        }

        List<Column> columns = []
        json.columns.each { column ->
            columns << new Column(
                    name: column.name,
                    type: column.type,
                    nullable: column?.nullablem,
                    defaultValue: column?.default_value
            )
        }

        List<Index> indexes = []
        json.indexes.each { index ->
            indexes << new Index(
                    name: index.name,
                    unique: index?.unique,
                    sequence: index?.seq,
                    column: index.column,
                    direction: index?.direction,
                    storing: index?.storing,
                    implicit: index?.implicit
            )
        }

        new Table(
                name: table,
                grants: grants,
                columns: columns,
                indexes: indexes,
                rangeCount: json.range_count,
                replicas: json.zone_config.num_replicas,
                descriptorId: json.descriptor_id
        )
    }

    @Tool(description = "List events. List the latest events on the cluster, in descending order..")
    List<Event> getEvents() {
        def raw = getResource(Resource.EVENTS.endpoint)
        def json = jsonSlurper.parseText(raw)
        List<Event> events = []
        Instant instant = null
        json.events.each { event ->
            instant = Instant.parse(event.timestamp)
            events << new Event(
                    id: event.unique_id,
                    eventType: event.event_type,
                    timestamp: LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
            )
        }
        events
    }

    @Tool(description = "List nodes ranges. Get details on the ranges on a specified node.")
    List<Range> getNodeRanges(String nodeId) {
        def raw = getResource(Resource.RANGES.getEndpoint(nodeId))
        def json = jsonSlurper.parseText(raw)
        List<Range> ranges = []
        json.ranges.each { range ->
            ranges << new Range(
                    rangeId: range?.desc?.range_id,
                    endKey: range?.desc?.end_key,
                    spanStartKey: range?.span?.start_key,
                    spanEndKey: range?.span?.end_key
            )
        }
        ranges
    }

    @Tool(description = "List hot ranges. Get information on ranges receiving a high number of reads or writes..")
    List<Range> getHotRanges() {
        def raw = getResource(Resource.HOT_RANGES.endpoint)
        def json = jsonSlurper.parseText(raw)
        List<Range> ranges = []
        json.ranges.each { range ->
            ranges << new Range(
                    rangeId: range?.range_id,
                    nodeId: range?.node_id,
                    leaseHolderNodeId: range?.leaseholder_node_id,
                    databases: range?.databases,
                    tables: range?.tables,
                    indexes: range?.indexes,
                    replicaNodeIds: range?.replica_node_ids
            )
        }
        ranges
    }

    @Tool(description = "Get range details. Get detailed technical information on a range. Typically used by Cockroach Labs engineers.")
    List<Range> getRangeDetail(String rangeId) {
        def raw = getResource(Resource.RANGE.getEndpoint(rangeId))
        def json = jsonSlurper.parseText(raw)
        List<Range> ranges = []

        json.responses_by_node_id.each { key, range ->
            ranges << new Range(
                    nodeId: key.toInteger(),
                    rangeId: range?.range_info?.desc?.range_id,
                    startKey: range?.range_info?.desc?.start_key,
                    endKey: range?.range_info?.desc?.end_key,
                    spanStartKey: range?.range_info?.span?.start_key,
                    spanEndKey: range?.range_info?.span?.end_key
            )
        }

        ranges
    }

    @Tool(description = "List sessions. Get SQL session details of all current users or a specified user.")
    List<Session> getSessions() {
        def raw = getResource(Resource.SESSIONS.endpoint)
        def json = jsonSlurper.parseText(raw)
        List<Session> sessions = []
        json.sessions.each { session ->
            sessions << new Session(
                    nodeId: session?.node_id,
                    username: session?.username,
                    clientAddress: session?.client_address,
                    applicationName: session?.application_name
            )
        }
        sessions
    }

    @Tool(description = "List users. List all SQL users on the cluster.")
    List<User> getUsers() {
        def raw = getResource(Resource.USERS.endpoint)
        def json = jsonSlurper.parseText(raw)
        List<User> users = []
        json.users.each { user ->
            users << new User(
                    username: user?.username
            )
        }
        users
    }

    @Tool(description = "Log in. Authenticate as a SQL role that is a member of the admin role to retrieve a session token to use with further API calls.")
    Login login(String username, String password){
        def raw = postLoginForm(Resource.LOGIN.endpoint, username, password)
        def json = jsonSlurper.parseText(raw)
        new Login(token: json.session)
    }

    @Tool(description = "Log out. Invalidate the session token.")
    Logout getLogout(){
        def raw = getResource(Resource.LOGOUT.endpoint,HttpMethod.POST)
        def json = jsonSlurper.parseText(raw)
        new Logout(status: json.logged_out)
    }

    String getRawNodesInfo() {
        getResource(Resource.NODES.endpoint)
    }

    private String getResource(String endpoint,HttpMethod httpMethod = HttpMethod.GET){
        String path = "${API_BASE}${endpoint}"
        HttpHeaders headers = defaultJsonHeadersWithSession()
        HttpEntity<Void> entity = new HttpEntity<>(headers)
        ResponseEntity<String> response = restTemplate.exchange(
                "${apiUrl}${path}",
                httpMethod,
                entity,
                String.class
        )
        response.body
    }

    private HttpHeaders defaultJsonHeadersWithSession() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        String cookie = resolveSessionCookie()
        if (cookie != null && !cookie.isBlank()) {
            headers.add("X-Cockroach-API-Session", cookie)
        }
        headers
    }

    private String postLoginForm(String endpoint, String username, String password) {
        String path = "${API_BASE}${endpoint}"
        HttpHeaders headers = createFormEncodedHeaders()

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>()
        map.add("username", username)
        map.add("password", password)

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers)

        ResponseEntity<String> response = restTemplate.exchange(
                "${apiUrl}${path}",
                HttpMethod.POST,
                request,
                String.class
        )
        response.body
    }

    private HttpHeaders createFormEncodedHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        headers
    }

    private String resolveSessionCookie() {
        String env = System.getenv("SESSION_COOKIE")
        (env != null && !env.isBlank()) ? env : this.sessionCookie
    }
}
