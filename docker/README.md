# CockroachDB 3 Node Cluster with Docker Compose

1. Run the `setup.sh` to create the certificates.
2. Run `docker-compose up` or `docker compose up`
3. Initialize the cluster with `docker-compose exec roach1 cockroach init --certs-dir=/certs` or `docker exec -it roach1 cockroach init --certs-dir=/certs`
4. In another terminal you can use connect with: `docker exec -it roach1 cockroach sql --certs-dir=/certs --host=localhost:26257`
5. Altert the root password with `ALTER USER root WITH PASSWORD 'password';`, then you can use the https://localhost:8080.
6. You can quit with `exit`.

## Requirements

- Docker
- Docker Compose
- https://jqlang.org/[jq^]

## Using Curl

```shell
# Login with root to get the Token
export SESSION=$(curl -s --cacert certs/ca.crt \
--cert certs/client.root.crt \
--key certs/client.root.key \
-X POST -d 'username=root&password=password' \
-H 'Content-Type: application/x-www-form-urlencoded' \
https://localhost:8080/api/v2/login/ | jq -r .session)
```

```shell
 curl -s --cacert certs/ca.crt \
 --cert certs/client.root.crt \
 --key certs/client.root.key \
 -H "X-Cockroach-API-Session: ${SESSION}" \
 https://localhost:8080/api/v2/nodes/ | jq .
 ```





