package com.cockroachlabs.ai.mcp

import com.cockroachlabs.ai.mcp.model.Database
import com.cockroachlabs.ai.mcp.model.Event
import com.cockroachlabs.ai.mcp.model.Grant
import com.cockroachlabs.ai.mcp.model.Login
import com.cockroachlabs.ai.mcp.model.Logout
import com.cockroachlabs.ai.mcp.model.Node
import com.cockroachlabs.ai.mcp.model.Range
import com.cockroachlabs.ai.mcp.model.Session
import com.cockroachlabs.ai.mcp.model.Table
import com.cockroachlabs.ai.mcp.model.User
import com.cockroachlabs.ai.mcp.service.CockroachDbMcpCalls
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class McpRestApiTests {

    @Autowired
    CockroachDbMcpCalls cockroachDbMcpCalls

    @Test
    void getNodeInfoTest() {
        def result = cockroachDbMcpCalls.nodesInfo

        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Node)
    }

    @Test
    void getDatabasesTest() {
        def result = cockroachDbMcpCalls.databases
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Database)
    }

    @Test
    void getDatabaseDescriptorTest() {
        def result = cockroachDbMcpCalls.getDatabaseInfo("postgres")
        assertNotNull result
        assertTrue(result instanceof Database)
    }

    @Test
    void getDatabaseGrantsTest() {
        def result = cockroachDbMcpCalls.getDatabaseGrants("postgres")
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Grant)
    }

    @Test
    void getDatabaseTablessTest() {
        def result = cockroachDbMcpCalls.getDatabaseTables("system")
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Table)
    }

    @Test
    void getDatabaseTableDetailTest() {
        def result = cockroachDbMcpCalls.getDatabaseTableDetail("system","public.users")
        assertNotNull result
        assertTrue(result instanceof Table)
    }

    @Test
    void getEventsTest() {
        def result = cockroachDbMcpCalls.events
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Event)
    }

    @Test
    void getNodeRangesTest(){
        def result = cockroachDbMcpCalls.getNodeRanges("1")
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Range)
    }

    @Test
    void getHotRangesTest(){
        def result = cockroachDbMcpCalls.hotRanges
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Range)
    }

    @Test
    void getRangeDetailTest(){
        def result = cockroachDbMcpCalls.getRangeDetail("42")
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof Range)
    }

    @Test
    void getSessionsTest(){
        def result = cockroachDbMcpCalls.sessions
        assertNotNull result
        assertTrue result.size() >= 0
        assertTrue(result[0] instanceof Session)
    }

    @Test
    void getUserTest(){
        def result = cockroachDbMcpCalls.users
        assertNotNull result
        assertTrue result.size() > 0
        assertTrue(result[0] instanceof User)
    }

    @Value('${cockroachdb.username}')
    private String username

    @Value('${cockroachdb.password}')
    String password

    @Test
    void loginTest(){
        def result = cockroachDbMcpCalls.login(username,password)
        assertNotNull result
        assertTrue (result instanceof Login)
        assertNotNull result.token
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void logoutTest(){
        def result = cockroachDbMcpCalls.logout
        assertNotNull result
        assertTrue (result instanceof Logout)
    }
}
