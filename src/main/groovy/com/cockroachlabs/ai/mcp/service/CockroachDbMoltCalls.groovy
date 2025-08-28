package com.cockroachlabs.ai.mcp.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service

/**
 * A Spring service that provides tools for interacting with the CockroachDB Molt migration utility.
 * This class exposes a single tool to generate a 'molt fetch' and 'molt verify' commands.
 */
@Service
class CockroachDbMoltCalls {

    /**
     * Generates a 'molt fetch' command for data migration.
     * <p>
     * This tool is designed to assist in creating the correct CLI command for migrating
     * data from a source database to a CockroachDB target using the `molt` tool.
     *
     * - Full documentation: https://www.cockroachlabs.com/docs/molt/molt-fetch
     * - Additional Flags: https://www.cockroachlabs.com/docs/molt/molt-fetch#global-flags
     *
     * @param sourceConnectionString The connection string for the source database (e.g., PostgreSQL or MySQL).
     *        This should include the schema name if applicable.
     *        Example: 'postgresql://user:pass@host:port/database?sslmode=require'
     * @param targetConnectionString The connection string for the target CockroachDB instance.
     *         Example: 'cockroachdb://user:pass@host:port/database?sslmode=require'
     * @param tables A list of table names to be migrated. Use the format 'schema.table' if a schema is specified.
     *        If the list is empty, all tables will be migrated.
     * @param unsafeDisableVersionCheck When true, disables the version check. This flag should be used with caution.
     * @param logSeverity Sets the logging level. Valid values are 'NONE', 'INFO', 'WARN', 'ERROR', and 'FATAL'.
     * @param verbose When true, increases the verbosity of the output.
     * @param logDir The directory to which logs should be written.
     * @param tableHandling Controls how the tool interacts with tables on the target.
     *        Valid values include "drop-on-target-and-recreate", "truncate-and-recreate", and "no-op".
     *        The default is "no-op".
     * @return A complete 'molt fetch' command string that can be copied and executed in a terminal.
     */
    @Tool(name="molt_generate_fetch_command",description = "Generates a 'molt fetch' command for data migration from a source database to CockroachDB.")
    String generateMoltFetchCommand(
            String sourceConnectionString,
            String targetConnectionString,
            List<String> tables,
            Boolean unsafeDisableVersionCheck,
            String logSeverity,
            Boolean verbose,
            String logDir,
            String tableHandling
    ) {
        // Use a GString for cleaner command building.
        def command = "molt fetch --source=${sourceConnectionString} --target=${targetConnectionString}"

        // Append tables if the list is not null and not empty.
        if (tables) {
            command += " --tables=${tables.join(',')}"
        }

        // Append optional global and command-specific flags if they are provided.
        if (unsafeDisableVersionCheck) {
            command += " --unsafe-disable-version-check"
        }
        if (logSeverity) {
            command += " --log-severity=${logSeverity}"
        }
        if (verbose) {
            command += " --verbose"
        }
        if (logDir) {
            command += " --log-dir=${logDir}"
        }
        if (tableHandling) {
            command += " --table-handling=${tableHandling}"
        }

        return command
    }

    /**
     * Generates a 'molt verify' command for data validation.
     * <p>
     * This tool is designed to assist in creating the correct CLI command for validating
     * data integrity between a source database and a CockroachDB target.
     *
     * - Documentation: https://www.cockroachlabs.com/docs/molt/molt-verify
     * - Flags: https://www.cockroachlabs.com/docs/molt/molt-verify#flags
     *
     * @param sourceConnectionString The connection string for the source database (e.g., PostgreSQL or MySQL).
     * @param targetConnectionString The connection string for the target CockroachDB instance.
     * @param tables A list of table names to be validated. Use the format 'schema.table' if a schema is specified.
     *        If the list is empty, all tables will be verified.
     * @param unsafeDisableVersionCheck When true, disables the version check. This flag should be used with caution.
     * @param live When true, re-checks rows that don't match before reporting them as problematic. This is useful during continuous replication.
     * @param concurrency The number of concurrent verification workers to run.
     * @return A complete 'molt verify' command string that can be copied and executed in a terminal.
     */
    @Tool(name = "molt_generate_verify_command", description = "Generates a 'molt verify' command for data validation between a source and target database.")
    String generateMoltVerifyCommand(
            String sourceConnectionString,
            String targetConnectionString,
            List<String> tables,
            Boolean unsafeDisableVersionCheck,
            Boolean live,
            Integer concurrency
    ) {
        def command = "molt verify --source=${sourceConnectionString} --target=${targetConnectionString}"

        // Append tables if the list is not null and not empty.
        if (tables) {
            command += " --tables=${tables.join(',')}"
        }

        // Append optional flags for verification.
        if (unsafeDisableVersionCheck) {
            command += " --unsafe-disable-version-check"
        }
        if (live) {
            command += " --live"
        }
        if (concurrency) {
            command += " --concurrency=${concurrency}"
        }

        return command
    }
}
