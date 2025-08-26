package com.cockroachlabs.ai.mcp

import com.cockroachlabs.ai.mcp.service.CockroachDbMcpCalls
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate

import static org.junit.jupiter.api.Assertions.*

@SpringBootTest
class MockMcpServerApplicationTests {

    @Autowired
    CockroachDbMcpCalls cockroachDbMcpCalls

    @Autowired
    RestTemplate restTemplate

    @Value('${cockroachdb.api.url}')
    String apiUrl

    MockRestServiceServer server

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restTemplate)
                .ignoreExpectOrder(true)
                .build()
    }

    @Test
    void mockGetNodesInfoTests() {
        String expected = '{"nodes":[{"node_id":1},{"node_id":2}]}'

        server.expect(MockRestRequestMatchers.requestTo("${apiUrl}/api/v2/nodes/"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(expected, MediaType.APPLICATION_JSON))

        String result = cockroachDbMcpCalls.rawNodesInfo

        assertNotNull result
        assertTrue result.contains('"nodes"')
        server.verify()
    }
}
