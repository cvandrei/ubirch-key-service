# ubirch-key-service


## General Information

This project serves as a keyserver but unlike PGP keyservers it has additional features (for example, uploading pub keys
is only possible if you control the private key, too).


## Release History

### Version 0.1.0 (tbd)

* initial release


## Scala Dependencies

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.key" %% "cmdtools" % "0.1.0-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.key" %% "config" % "0.1.0-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "anormcypher" at "http://repo.anormcypher.org/", // needed by dependency org.anormcypher:anormcypher
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/" // needed by dependency org.anormcypher:anormcypher
)
libraryDependencies ++= Seq(
  "com.ubirch.key" %% "core" % "0.1.0-SNAPSHOT"
)
```

### `model`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.key" %% "model" % "0.1.0-SNAPSHOT"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.key" %% "server" % "0.1.0-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.key" %% "util" % "0.1.0-SNAPSHOT"
)
```


## REST Methods

### Welcome / Health

    curl localhost:8095/
    curl localhost:8095/api/keyService/v1

If healthy the server response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchKeyService"}

If not healthy the server response is:

    400 {"version":"1.0","status":"NOK","message":"$ERROR_MESSAGE"}


### Public Key

#### Create

    curl -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{
      "pubkeyInfo": {
        "hwDeviceId": "some-id-asdf", // String (not always a UUID)
        "pubKey": "string", // base64
        "algorithm": "RSA4096", // check X.509 re: constants
        "previousPubKey": "...", // String - full pub key (optional)
        "created": "2017-04-26T17:18:00.000Z+02:00",
        "validNotBefore": "2017-04-26T17:18:00.000Z+02:00",
        "validNotAfter": "2019-04-26T17:18:00.000Z+02:00" // (optional)
      },
      "signature": "string", // base64 (self signed)
      "previousPubKeySignature": "..." // (optional)
    }'

If successful the response is:

    200
    {
      "pubkeyInfo": {
        "hwDeviceId": "some-id-asdf", // String (not always a UUID)
        "pubKey": "string", // base64
        "algorithm": "RSA4096", // check X.509 re: constants
        "previousPubKey": "...", // String - full pub key (optional)
        "created": "2017-04-26T17:18:00.000Z+02:00",
        "validNotBefore": "2017-04-26T17:18:00.000Z+02:00",
        "validNotAfter": "2019-04-26T17:18:00.000Z+02:00" // (optional)
      },
      "signature": "string", // base64 (self signed)
      "previousPubKeySignature": "..." // (optional)
    }

In case of an error the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "CreateError",
        "errorMessage": "failed to create public key"
      }
    }

If the server has problems the response is:

    500
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "ServerError",
        "errorMessage": "failed to create public key"
      }
    }

#### Query Public Keys by HardwareId (currently active only)

    curl localhost:8095/api/keyService/v1/pubkey/current/hardwareId/$HARDWARE_ID

If no currently valid public keys were found the response is:

    200
    []

If currently valid public keys were found the response is:

    200
    [
      {
        "pubkeyInfo": {
          "hwDeviceId": "some-id-asdf", // String (not always a UUID)
          "pubKey": "string", // base64
          "algorithm": "RSA4096", // check X.509 re: constants
          "previousPubKey": "...", // String - full pub key (optional)
          "created": "2017-04-26T17:18:00.000Z+02:00",
          "validNotBefore": "2017-04-26T17:18:00.000Z+02:00",
          "validNotAfter": "2019-04-26T17:18:00.000Z+02:00" // (optional)
        },
        "signature": "string", // base64 (self signed)
        "previousPubKeySignature": "..." // (optional)
      }
    ]

In case of an error the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "QueryError",
        "errorMessage": "failed to query public keys"
      }
    }

If the server has problems the response is:

    500
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "ServerError",
        "errorMessage": "failed to query public keys"
      }
    }

## Configuration

TODO


## Deployment Notes

TODO


## Automated Tests

run all tests

    ./sbt test

### generate coverage report

    ./sbt coverageReport

more details here: https://github.com/scoverage/sbt-scoverage


## Local Setup

(**NOTE** we might go with the embedded server for loca development)

1) download and install [Neo4j 3.1.x](https://neo4j.com/download/community-edition/) (community edition)

2) start Neo4j


## Create Docker Image

    ./sbt server/docker
