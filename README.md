# ID2203 Project 2017 Starter Code

## Compiling
```bash
mvn package -DskipTests
```

## Running tests
```bash
mvn test
```
## Booting the servers
```bash
./bootstrap.sh  # Listens on 5000
./server.sh 6000  # Listens on 6000, connects to 6000
./server.sh 7000  # Listens on 7000, connects to 6000
```
The cluster is ready when this is output
```
03/10 06:03:06 INFO [Kompics-worker-2] s.k.i.c.r.ReadImposeWriteConsultMajority - Got replication group (pid: 2046831430): List(/127.0.0.1:5000, /127.0.0.1:6000)
```

## Connecting a client
```bash
./client 6000  # Listens on RANDOM(), connects to 6000
```

## Using a client
Read the key "asd"
```
op asd
```

Put the value "qwe" to the key "asd"
```
op asd qwe
```