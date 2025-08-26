#!/bin/bash

set -euo pipefail

rm -rf certs/
docker volume rm -f docker_roach1 docker_roach2 docker_roach3
