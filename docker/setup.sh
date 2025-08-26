#!/bin/bash

set -euo pipefail

# Ensure the certs directory is clean
rm -rf certs
mkdir -p certs

# Create CA certificate
docker run --rm -v $(pwd)/certs:/certs cockroachdb/cockroach:v25.2.0 cert create-ca --certs-dir=/certs --ca-key=/certs/ca.key --allow-ca-key-reuse

# Create node certificates for a 3-node cluster
# The certs must include all possible hostnames and IP addresses the nodes might use.
docker run --rm -v $(pwd)/certs:/certs cockroachdb/cockroach:v25.2.0 cert create-node localhost roach1 roach2 roach3 --certs-dir=/certs --ca-key=/certs/ca.key --overwrite

# Create a client certificate for the root user
docker run --rm -v $(pwd)/certs:/certs cockroachdb/cockroach:v25.2.0 cert create-client root --certs-dir=/certs --ca-key=/certs/ca.key

echo "Certificates created successfully."
