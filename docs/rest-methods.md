## REST Methods

### Welcome / Health / Check

    curl localhost:8095/
    curl localhost:8095/api/keyService/v1
    curl localhost:8095/api/keyService/v1/check

If healthy the server response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchKeyService ( $GO_PIPELINE_NAME / $GO_PIPELINE_LABEL / $GO_PIPELINE_REVISION )"}

If not healthy the server response is:

    400 {"version":"1.0","status":"NOK","message":"$ERROR_MESSAGE"}

### Deep Check / Server Health

    curl localhost:8095/api/keyService/v1/deepCheck

If healthy the response is:

  F  200 {"status":true,"messages":[]}

If not healthy the status is `false` and the `messages` array not empty:

    503 {"status":false,"messages":["unable to connect to the database"]}


### Public Key

#### Create

This method is idempotent. Hence uploading an existing key does not produce an error but instead behaves as if the
upload had been successful.

All examples are based on the following key pair:

* public key  = MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=
* private key = MC8CAQAwCAYDK2VkCgEBBCBaVXkOGCrGJrrQcfFSOVXTDKJRN5EvFs+UwHVSBIrK6Q==

*Example with all fields set*
 
* previous public key  = MC0wCAYDK2VkCgEBAyEAgH0cf8WwYiLY/LHtLqhtg7pZaaGI1vNHo4jHDrd6KY0=
* previous private key = MC8CAQAwCAYDK2VkCgEBBCC4LZ5r6ueSbFjqM9bUeZKUwWcSyGx2jBs+m5u97adb0g==

```
curl -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '
{
  "pubKeyInfo": {
    "algorithm": "ECC_ED25519",
    "created": "2018-03-15T21:56:20.819Z",
    "hwDeviceId": "44394342-0d06-4e90-9d91-c2e3bd5612a4",
    "previousPubKeyId": "MC0wCAYDK2VkCgEBAyEAgH0cf8WwYiLY/LHtLqhtg7pZaaGI1vNHo4jHDrd6KY0=",
    "pubKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "pubKeyId": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "validNotAfter": "2018-09-15T21:56:20.819Z",
    "validNotBefore": "2018-03-15T21:56:20.819Z"
  },
  "signature": "2Dx11qm9aEcfY3iqrqRsckjP4SRjp4T3P1L3UTPq1eYeOXXb7MLXzM7SfnGIPuXtqZK60vSKe8MSUmk3fa3jDw==",
  "previousPubKeySignature": "YYAif0Wn25+E7Xl+tH00BiwmvCR8ixi1HPrAxOL+1XgebAtlUIqBK5T0uFcdpWzcie0kURCfuHWJgcscH1w0Bw=="
}'
```

*Example with only mandatory fields*

```
curl -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '
{
  "pubKeyInfo": {
    "algorithm": "ECC_ED25519",
    "created": "2018-03-15T21:48:32.373Z",
    "hwDeviceId": "39c023be-9d8f-4a72-a05d-271cb928dbc3",
    "pubKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "pubKeyId": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "validNotBefore": "2018-03-15T21:48:32.373Z"
  },
  "signature": "0aMQdrSBeyGbuZefhhLyWRmW3mJPIK+Tp4AtgKIg8eEXUCTogH23NeOfhw3PB1I82Mmsn8yCNC0cyMEFMMwABQ=="
}'
```

Valid _algorithm_s are:

* RSA4096
* ECC_ED25519

If successful the response is exactly the key from the request.

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

#### Create (MessagePack)

All examples are based on the following key pair:

* public key  = ???
* private key = ???

*Example with all fields set*
 
* previous public key  = ???
* previous private key = ???

// TODO provide a working curl call since none of these is working
```
curl -XPOST localhost:8095/api/keyService/v1/pubkey/mpack -H "Content-Type: application/octet-stream" -d '9512b0000000000000000000000000000000000187a9616c676f726974686dab4543435f45443235353139a763726561746564ce5b7426abaa68774465766963654964b000000000000000000000000000000000a67075624b6579da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caa87075624b65794964da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caad76616c69644e6f744166746572ce5d555a2bae76616c69644e6f744265666f7265ce5b7426abda00409eca7dd20739063735aca17bd0ed4c4b8c2cfd12628e680f2a7b3f68fdeda33d5f2773f62e8182924c3b89ac38202e5d019d8b04f5cc82d262fa0a4100d45f05'
curl -XPOST localhost:8095/api/keyService/v1/pubkey/mpack -H "Content-Type: application/octet-stream" -d '9512b0000000000000000000000000000000000187a9616c676f726974686dab4543435f45443235353139a763726561746564ce5b7426abaa68774465766963654964b000000000000000000000000000000000a67075624b6579da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caa87075624b65794964da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caad76616c69644e6f744166746572ce5d555a2bae76616c69644e6f744265666f7265ce5b7426abda00409eca7dd20739063735aca17bd0ed4c4b8c2cfd12628e680f2a7b3f68fdeda33d5f2773f62e8182924c3b89ac38202e5d019d8b04f5cc82d262fa0a4100d45f05'
curl -XPOST localhost:8095/api/keyService/v1/pubkey/mpack -H "Content-Type: application/octet-stream" --data-binary '9512b0000000000000000000000000000000000187a9616c676f726974686dab4543435f45443235353139a763726561746564ce5b7426abaa68774465766963654964b000000000000000000000000000000000a67075624b6579da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caa87075624b65794964da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caad76616c69644e6f744166746572ce5d555a2bae76616c69644e6f744265666f7265ce5b7426abda00409eca7dd20739063735aca17bd0ed4c4b8c2cfd12628e680f2a7b3f68fdeda33d5f2773f62e8182924c3b89ac38202e5d019d8b04f5cc82d262fa0a4100d45f05'
curl -XPOST localhost:8095/api/keyService/v1/pubkey/mpack --data-binary '9512b0000000000000000000000000000000000187a9616c676f726974686dab4543435f45443235353139a763726561746564ce5b7426abaa68774465766963654964b000000000000000000000000000000000a67075624b6579da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caa87075624b65794964da00209c9f9ffd51bd6f77bebc2e3e0b72457ffdb24b23ebf889a4f91d6008040af0caad76616c69644e6f744166746572ce5d555a2bae76616c69644e6f744265666f7265ce5b7426abda00409eca7dd20739063735aca17bd0ed4c4b8c2cfd12628e680f2a7b3f68fdeda33d5f2773f62e8182924c3b89ac38202e5d019d8b04f5cc82d262fa0a4100d45f05'
curl -XPOST localhost:8095/api/keyService/v1/pubkey/mpack --data-binary '100101010001001010110000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000110000111101010010110000101101100011001110110111101110010011010010111010001101000011011011010101101000101010000110100001101011111010001010100010000110010001101010011010100110001001110011010011101100011011100100110010101100001011101000110010101100100110011100101101101110100001001101010101110101010011010000111011101000100011001010111011001101001011000110110010101001001011001001011000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000101001100111000001110101011000100100101101100101011110011101101000000000001000001001110010011111100111111111110101010001101111010110111101110111101111101011110000101110001111100000101101110010010001010111111111111101101100100100101100100011111010111111100010001001101001001111100100011101011000000000100000000100000010101111000011001010101010000111000001110101011000100100101101100101011110010100100101100100110110100000000000100000100111001001111110011111111111010101000110111101011011110111011110111110101111000010111000111110000010110111001001000101011111111111110110110010010010110010001111101011111110001000100110100100111110010001110101100000000010000000010000001010111100001100101010101101011101100110000101101100011010010110010001001110011011110111010001000001011001100111010001100101011100101100111001011101010101010101101000101011101011100111011001100001011011000110100101100100010011100110111101110100010000100110010101100110011011110111001001100101110011100101101101110100001001101010101111011010000000000100000010011110110010100111110111010010000001110011100100000110001101110011010110101100101000010111101111010000111011010100110001001011100011000010110011111101000100100110001010001110011010000000111100101010011110110011111101101000111111011110110110100011001111010101111100100111011100111111011000101110100000011000001010010010010011000011101110001001101011000011100000100000001011100101110100000001100111011000101100000100111101011100110010000010110100100110001011111010000010100100000100000000110101000101111100000101'
```

#### Trust Keys

This method is idempotent. Hence trusting a key that already has the callers trust is successful.

NOTE: Details about the semantics of the `trustLevel` field have yet to be finalized. So far we at least know that it's
mandatory and will most likely have a range of 1 to 100 with higher values having more weight.

##### Curl Example

All examples are based on the following key pairs:

###### Caller (Key A)

* public key  = MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=
* private key = MC8CAQAwCAYDK2VkCgEBBCBaVXkOGCrGJrrQcfFSOVXTDKJRN5EvFs+UwHVSBIrK6Q==

###### Key To Trust (Key B)

* public key  = MC0wCAYDK2VkCgEBAyEAV4aTMZNuV2bLEy/VwZQTpxbPEVZ127gs88TChgjuq4s=
* private key = MC8CAQAwCAYDK2VkCgEBBCCnZ7tKYA/dzNPqgRRe6yBb+q7cj0AvWA6FVf6nxOtGlg==

```
## upload public keys
# Key A
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{"pubKeyInfo":{"algorithm":"ECC_ED25519","created":"2018-09-12T11:02:17.280Z","hwDeviceId":"6d0157ae-f53e-43a9-ad0d-f3d0a9d56176","pubKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","pubKeyId":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","validNotBefore":"2018-09-12T12:01:17.326Z"},"signature":"RXX0HzCvtuiD6xOQ3Sw/BKLnyfCwgJdDBH7JKkKe7yTXStTlVZOYSyNAPI6uh5IMuXhejxFL6uhU7SAQtcxxDQ=="}'
# Key B
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{"pubKeyInfo":{"algorithm":"ECC_ED25519","created":"2018-09-12T11:02:17.705Z","hwDeviceId":"f02a3429-684a-42e2-b3f7-dc6f546bfed5","pubKey":"MC0wCAYDK2VkCgEBAyEAV4aTMZNuV2bLEy/VwZQTpxbPEVZ127gs88TChgjuq4s=","pubKeyId":"MC0wCAYDK2VkCgEBAyEAV4aTMZNuV2bLEy/VwZQTpxbPEVZ127gs88TChgjuq4s=","validNotBefore":"2018-09-12T12:01:17.706Z"},"signature":"ouXiAXlFoA+6vCgB7uNWlFP0ilbb1r1t9par2f77g7APXjnCXGrh84aRY22ogz6+sdhu2IXLd0WxbnyQffEEBw=="}'

## trusting public keys
# trust(A --trustLevel:50--> B)
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey/trust -H "Content-Type: application/json" -d '{
  "trustRelation": {
    "created":"2018-09-12T12:02:17.717Z",
    "sourcePublicKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "targetPublicKey": "MC0wCAYDK2VkCgEBAyEAV4aTMZNuV2bLEy/VwZQTpxbPEVZ127gs88TChgjuq4s=",
    "trustLevel": 50,
    "validNotAfter": "2018-12-12T12:02:17.717Z"
  },
  "signature":"GfFLMrseDHoq7IcJu7dtNnUB4aHOfpKAQLLya1n7175Hk8uDG3JxEqKUKGWdpGnMc5pqQ+cDn0Horb8eI25iDA=="
}'

# trust(A --trustLevel:80--> B)
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey/trust -H "Content-Type: application/json" -d '{"trustRelation":{"created":"2018-09-12T12:02:17.740Z","sourcePublicKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","targetPublicKey":"MC0wCAYDK2VkCgEBAyEAV4aTMZNuV2bLEy/VwZQTpxbPEVZ127gs88TChgjuq4s=","trustLevel":80,"validNotAfter":"2018-12-12T12:02:17.740Z"},"signature":"MjiYzGDMR1zlShrjoTZa/DTx2GO3Og70Z6i+NBhyNZU8LQpqq5BsQKtjhjsI9SiuHGSRseIQkCDH4zYD7zErBQ=="}'

# trust(B --> A)
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey/trust -H "Content-Type: application/json" -d '{"trustRelation":{"created":"2018-09-12T12:02:17.744Z","sourcePublicKey":"MC0wCAYDK2VkCgEBAyEAV4aTMZNuV2bLEy/VwZQTpxbPEVZ127gs88TChgjuq4s=","targetPublicKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","trustLevel":50,"validNotAfter":"2018-12-12T12:02:17.744Z"},"signature":"HqR6L3jght47vQdCF2019wLC/rOgZ8BSt0j8garBSQ2SqaN0AKxkckTkPROO809bLg/CaGvwLu4MQFiJqkuYBg=="}'
```


#### Query Public Keys by HardwareId (currently active only)

    curl localhost:8095/api/keyService/v1/pubkey/current/hardwareId/$HARDWARE_ID

If no currently valid public keys were found the response is:

    200
    []

If currently valid public keys were found the response is:

    200
    [
      {
        "pubKeyInfo": {
          "algorithm": "ECC_ED25519",
          "created": "2018-03-15T21:48:32.373Z",
          "hwDeviceId": "39c023be-9d8f-4a72-a05d-271cb928dbc3",
          "pubKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
          "pubKeyId": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
          "validNotBefore": "2018-03-15T21:48:32.373Z"
        },
        "signature": "0aMQdrSBeyGbuZefhhLyWRmW3mJPIK+Tp4AtgKIg8eEXUCTogH23NeOfhw3PB1I82Mmsn8yCNC0cyMEFMMwABQ=="
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

#### Query Public Keys by publicKey

    curl localhost:8095/api/keyService/v1/pubkey/$PUB_KEY

If no currently valid public keys were found the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "QueryError",
        "errorMessage": "failed to find public key"
      }
    }

If currently valid public keys were found the response is:

    200
    [
      {
        "pubKeyInfo": {
          "algorithm": "ECC_ED25519",
          "created": "2018-03-15T21:48:32.373Z",
          "hwDeviceId": "39c023be-9d8f-4a72-a05d-271cb928dbc3",
          "pubKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
          "pubKeyId": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
          "validNotBefore": "2018-03-15T21:48:32.373Z"
        },
        "signature": "0aMQdrSBeyGbuZefhhLyWRmW3mJPIK+Tp4AtgKIg8eEXUCTogH23NeOfhw3PB1I82Mmsn8yCNC0cyMEFMMwABQ=="
      }
    ]

In case of an error the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "QueryError",
        "errorMessage": "failed to find public key"
      }
    }

If the server has problems the response is:

    500
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "ServerError",
        "errorMessage": "failed to find public key"
      }
    }

#### Find Trusted Keys

This method allows us to query all keys we trust. To ensure the underlying web-of-trust's privacy we only allow queries
on keys which the caller has full control over which is ensured by a mandatory request signature. As a simple protection
from replay attacks we also check if the `queryDate` is recent from the last few minutes.

The request includes a `depth` field which we'll ignore for now. We'll start to use it only once full control over whom
can see which trust relations has been implemented.

##### Curl Example

The example is based on the Key A:

* public key  = MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=
* private key = MC8CAQAwCAYDK2VkCgEBBCBaVXkOGCrGJrrQcfFSOVXTDKJRN5EvFs+UwHVSBIrK6Q==

```
###### upload public keys
# Key A
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{"pubKeyInfo":{"algorithm":"ECC_ED25519","created":"2018-09-14T14:36:08.536Z","hwDeviceId":"93054176-632f-4298-98d4-3436e7719011","pubKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","pubKeyId":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","validNotBefore":"2018-09-14T15:35:08.588Z"},"signature":"C3mLLb2HBnfW/6mQFtsUmTYKGh7DmxR3WDmkl7xGbrUP2IVredARTWhguGy9lPeroo8Qd0La8+hLtj4YHmVIAQ=="}'
# Key B
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{"pubKeyInfo":{"algorithm":"ECC_ED25519","created":"2018-09-14T14:36:08.921Z","hwDeviceId":"32662320-67ec-4684-a58c-bfd7fc3cb2bc","pubKey":"MC0wCAYDK2VkCgEBAyEAZ68y5f3zwInZVWg2q4eBdfbSzM0UK5l1xroDQpQBF4Y=","pubKeyId":"MC0wCAYDK2VkCgEBAyEAZ68y5f3zwInZVWg2q4eBdfbSzM0UK5l1xroDQpQBF4Y=","validNotBefore":"2018-09-14T15:35:08.921Z"},"signature":"iH/qSxdu8NrnTgBxFYO4jfXvn7no4fNYUsk/5xvzsyvVd06kJxDj1yS1qQ8e+8UN8ec9776jlEO0LKRGP8y0Dw=="}'
# Key C
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{"pubKeyInfo":{"algorithm":"ECC_ED25519","created":"2018-09-14T14:36:08.925Z","hwDeviceId":"f2c467a6-ea37-4d2f-b0c4-132c75c34a69","pubKey":"MC0wCAYDK2VkCgEBAyEAZMPpszVmofwoREiZ07buzXGKx2rdHb4I4yCrO4/7sOI=","pubKeyId":"MC0wCAYDK2VkCgEBAyEAZMPpszVmofwoREiZ07buzXGKx2rdHb4I4yCrO4/7sOI=","validNotBefore":"2018-09-14T15:35:08.926Z"},"signature":"nEOlMc9STR+vmLAPknYdvGGc7gx17N39lMERkEF6EmgIlOojlMjxodloYv0AmZLvF+8JTpVROFSA8zM7BVf6AQ=="}'

###### trust keys
# trust(A --trustLevel:50--> B)
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey/trust -H "Content-Type: application/json" -d '{"trustRelation":{"created":"2018-09-14T15:36:08.936Z","sourcePublicKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","targetPublicKey":"MC0wCAYDK2VkCgEBAyEAZ68y5f3zwInZVWg2q4eBdfbSzM0UK5l1xroDQpQBF4Y=","trustLevel":50,"validNotAfter":"2018-12-14T15:36:08.936Z"},"signature":"wvTg7KSWLuVG/tycst61EglA1W2KQmvMwH3J346V293H7T4MK6NGydKtijtR7LbftnopKetzL8ZdfWm2Oc/wCg=="}'
# trust(A --trustLevel:70--> C)
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey/trust -H "Content-Type: application/json" -d '{"trustRelation":{"created":"2018-09-14T15:36:08.952Z","sourcePublicKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","targetPublicKey":"MC0wCAYDK2VkCgEBAyEAZMPpszVmofwoREiZ07buzXGKx2rdHb4I4yCrO4/7sOI=","trustLevel":70,"validNotAfter":"2018-12-14T15:36:08.952Z"},"signature":"1N+54Ix/O6/967OHWz8Cgcx7b40B9gQhXS5qZqvA9KDYijEOtCibXyGpmImBY66FVbSAsdWNFuBgB4fJzdCBCA=="}'

###### find keys trusted by A
curl -i -XGET localhost:8095/api/keyService/v1/pubkey/trusted -H "Content-Type: application/json" -d '{
  "findTrusted": {
    "depth": 1,
    "minTrustLevel": 50,
    "sourcePublicKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "queryDate": "2018-09-14T12:39:41.329Z"
  },
  "signature": "0guasaWheykTXbhdBrPn7DmcTUFwb6Y1wZrYOakYUHgJbdirXcLHAk4OFLZG17f3yROnZefhY0ryYlyvJPyKAQ=="
}'
```

#### Delete Public Key

Key pair used for this example:

* public key  = MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=
* private key = MC8CAQAwCAYDK2VkCgEBBCBaVXkOGCrGJrrQcfFSOVXTDKJRN5EvFs+UwHVSBIrK6Q==

````
curl -XDELETE localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '
{
  "publicKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=", // base64
  "signature": "XWBWG1y1HWyVqm3a6pwx21G0kaZcJP/NsSXD7KikLvKDbPT19sCQ8CfWe3YuE3VWReSrUsyA33qRsMV3ioaXBA==" // Bae64 encoded signature of field _publicKey_
}'
````

##### Responses

If the public key was deleted (or didn't exist):

    200

If the signature's invalid:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "DeleteError",
        "errorMessage": "failed to delete public key"
      }
    }

If the server has a problem:

    500
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "ServerError",
        "errorMessage": "failed to delete public key"
      }
    }

#### Revoke Key

This method is idempotent. Hence revoking a key that already has been revoked is successful.

##### Curl Example

The example is based on the following key pair:

* public key  = MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=
* private key = MC8CAQAwCAYDK2VkCgEBBCBaVXkOGCrGJrrQcfFSOVXTDKJRN5EvFs+UwHVSBIrK6Q==

```
# upload public key
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey -H "Content-Type: application/json" -d '{"pubKeyInfo":{"algorithm":"ECC_ED25519","created":"2018-09-10T11:10:28.286Z","hwDeviceId":"fcc0fca2-9dc5-4abe-9bc1-fa8221d1d7ef","pubKey":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","pubKeyId":"MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=","validNotBefore":"2018-09-10T12:09:28.365Z"},"signature":"XCSqmCSljuyOAfi2mVVfNj9nkWAs9oJdyFJccUFiYA/D0gxpnAYjJxJGY3Vds27O5KHm4WHOX96oa8LbF52VBw=="}'

# revoke key
curl -i -XPOST localhost:8095/api/keyService/v1/pubkey/revoke -H "Content-Type: application/json" -d '{
  "revokation": {
    "publicKey": "MC0wCAYDK2VkCgEBAyEA+alWF5nfiw7RYbRqH5lAcFLjc13zv63FpG7G2OF33O4=",
    "revokationDate": "2018-09-10T12:10:29.188Z"
  },
  "signature": "4Nlml972DyFdCrVwzrDYnkuY2vcBOHe4txpI4rpTBILeKvtyOnHSi8M1Q00dTdF6VKcyTeWOrsZ0aQeEqY+wDQ=="
}'
```
