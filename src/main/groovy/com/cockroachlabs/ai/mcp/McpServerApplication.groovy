package com.cockroachlabs.ai.mcp

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class McpServerApplication {

	static void main(String[] args) {
		SpringApplication.run(McpServerApplication, args)
	}

}
