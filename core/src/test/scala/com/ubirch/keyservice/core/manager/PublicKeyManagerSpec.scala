package com.ubirch.keyservice.core.manager

import java.util.Base64

import com.ubirch.crypto.ecc.EccUtil
import com.ubirch.key.model.db.{PublicKey, PublicKeyDelete}
import com.ubirch.keyService.testTools.data.generator.TestDataGeneratorDb
import com.ubirch.keyService.testTools.db.neo4j.Neo4jSpec
import com.ubirch.util.date.DateUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-05-09
  */
class PublicKeyManagerSpec extends Neo4jSpec {

  feature("create()") {

    scenario("public key does not exist (PublicKey with all fields set)") {

      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      // prepare
      val publicKey = TestDataGeneratorDb.createPublicKey(
        privateKey = privKey1,
        infoPubKey = pubKey1
      )

      // test
      PublicKeyManager.create(publicKey) map { result =>

        // verify
        result shouldBe Right(Some(publicKey))

      }

    }

    scenario("invalid public key does not exist (PublicKey with all fields set)") {

      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded

      // prepare
      val publicKey = TestDataGeneratorDb.createPublicKey(
        privateKey = privKey1,
        infoPubKey = pubKey1
      )

      val invalidPublicKey = publicKey.copy(pubKeyInfo = publicKey.pubKeyInfo.copy(pubKey = pubKey2))

      // test
      PublicKeyManager.create(invalidPublicKey) map {

        // verify
        case Left(e: Exception) =>

          e.getMessage should startWith("unable to create public key if signature is invalid")

        case Right(_) =>

          fail("should have resulted in Exception")

      }

    }

    scenario("public key exists (PublicKey with all fields set)") {

      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      // prepare
      val publicKey = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1)
      PublicKeyManager.create(publicKey) flatMap {

        case Left(t) => fail(s"failed to create key", t)

        case Right(None) => fail(s"failed to create existing key: $publicKey")

        case Right(Some(result: PublicKey)) =>

          result shouldBe publicKey

          // test
          PublicKeyManager.create(publicKey) map { result =>

            // verify
            result shouldBe Right(Some(publicKey))

          }

      }

    }

    scenario("publicKey.info.pubKey already exists (PublicKey with all fields set)") {

      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      // prepare
      val publicKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1)

      PublicKeyManager.create(publicKey1) flatMap { createResult =>

        createResult shouldBe Right(Some(publicKey1))

        val publicKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1)
        publicKey2.pubKeyInfo.pubKey shouldBe publicKey1.pubKeyInfo.pubKey

        // test
        PublicKeyManager.create(publicKey2) map {

          // verify
          case Left(e: Exception) =>

            e.getMessage should startWith("unable to create publicKey if it already exists")

          case Right(_) =>

            fail("should have resulted in Exception")

        }

      }

    }

    scenario("publicKey.info.pubKeyId already exists (PublicKey with all fields set)") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val publicKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1)

      PublicKeyManager.create(publicKey1) flatMap { createResult =>

        createResult shouldBe Right(Some(publicKey1))

        val publicKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1)
        publicKey2.pubKeyInfo.pubKeyId shouldBe publicKey1.pubKeyInfo.pubKeyId

        // test
        PublicKeyManager.create(publicKey2) map {

          // verify
          case Left(e: Exception) =>

            e.getMessage should startWith("unable to create publicKey if it already exists")

          case Right(_) =>

            fail("should have resulted in Exception")

        }

      }

    }

    scenario("public key does not exist (PublicKey with only mandatory fields set)") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val publicKey = TestDataGeneratorDb.publicKeyMandatoryOnly(privateKey = privKey1, infoPubKey = pubKey1)

      // test
      PublicKeyManager.create(publicKey) map { result =>

        // verify
        result shouldBe Right(Some(publicKey))

      }

    }

    scenario("public key exists (PublicKey with only mandatory fields set)") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val publicKey = TestDataGeneratorDb.publicKeyMandatoryOnly(privateKey = privKey1, infoPubKey = pubKey1)
      PublicKeyManager.create(publicKey) flatMap { createResult =>

        createResult shouldBe Right(Some(publicKey))

        // test
        PublicKeyManager.create(publicKey) map { result =>

          result shouldBe Right(Some(publicKey))

        }

      }

    }

  }

  feature("update()") {

    scenario("public key does not exist --> UpdateException") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()

      // test
      PublicKeyManager.update(keyPair.publicKey) map {

        // verify
        case Right(_) =>

          fail("update should have failed")

        case Left(updateException: UpdateException) =>

          updateException.getMessage shouldBe "failed to update public key as it does not exist"

      }

    }

    scenario("public key exists; try to update to same --> updated public key") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val pubKeyDb = keyPair.publicKey

      PublicKeyManager.create(pubKeyDb) flatMap { createResult =>

        createResult shouldBe Right(Some(pubKeyDb))

        // test
        PublicKeyManager.update(pubKeyDb) flatMap { updateResult =>

          // verify
          updateResult shouldBe Right(pubKeyDb)
          PublicKeyManager.findByPubKey(pubKeyDb.pubKeyInfo.pubKey) map (_ shouldBe Some(pubKeyDb))

        }

      }

    }

    scenario("public key exists; update with changed version --> updated public key") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val pubKeyDb = keyPair.publicKey

      PublicKeyManager.create(pubKeyDb) flatMap { createResult =>

        createResult shouldBe Right(Some(pubKeyDb))
        pubKeyDb.raw shouldBe empty
        val updatedPubKey = pubKeyDb.copy(raw = Some("raw"))

        // test
        PublicKeyManager.update(updatedPubKey) flatMap { updateResult =>

          // verify
          updateResult shouldBe Right(updatedPubKey)
          PublicKeyManager.findByPubKey(pubKeyDb.pubKeyInfo.pubKey) map (_ shouldBe Some(updatedPubKey))

        }

      }

    }

    scenario("add revokation --> success") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val pubKeyDb = keyPair.publicKey

      PublicKeyManager.create(pubKeyDb) flatMap { createResult =>

        createResult shouldBe Right(Some(pubKeyDb))

        val signedRevoke = TestDataGeneratorDb.signedRevoke(
          publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
          privateKey = keyPair.privateKeyString
        )
        val pubKeyDbRevoked = pubKeyDb.copy(signedRevoke = Some(signedRevoke))

        // test
        PublicKeyManager.update(pubKeyDbRevoked) flatMap { result =>

          // verify
          result shouldBe Right(pubKeyDbRevoked)

        }

      }

    }

    scenario("remove revokation --> error") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val pubKeyDb = keyPair.publicKey

      val signedRevoke = TestDataGeneratorDb.signedRevoke(
        publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
        privateKey = keyPair.privateKeyString
      )
      val pubKeyDbRevoked = pubKeyDb.copy(signedRevoke = Some(signedRevoke))

      PublicKeyManager.create(pubKeyDbRevoked) flatMap { createResult =>

        createResult shouldBe Right(Some(pubKeyDbRevoked))

        // test
        PublicKeyManager.update(pubKeyDb) flatMap {

          // verify
          case Left(updateException: UpdateException) =>

            updateException.getMessage shouldBe "unable to remove revokation from public key"
            PublicKeyManager.findByPubKey(pubKeyDb.pubKeyInfo.pubKey) map (_ shouldBe Some(pubKeyDbRevoked))

          case Right(_) =>

            fail("update should have failed")

        }

      }

    }

  }

  feature("currentlyValid()") {

    scenario("two keys: both currently valid (with notValidAfter) --> find both") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId)
      pKey1.pubKeyInfo.validNotAfter should be('isDefined)

      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = hardwareId)
      pKey2.pubKeyInfo.validNotAfter should be('isDefined)

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        // test
        PublicKeyManager.currentlyValid(hardwareId) map { result =>

          // verify
          result shouldBe Set(pKey1, pKey2)

        }

      }

    }

    scenario("two keys: both currently valid (without notValidAfter) --> find both") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.publicKeyMandatoryOnly(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId)
      pKey1.pubKeyInfo.validNotAfter should be('isEmpty)

      val pKey2 = TestDataGeneratorDb.publicKeyMandatoryOnly(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = hardwareId)
      pKey2.pubKeyInfo.validNotAfter should be('isEmpty)

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        // test
        PublicKeyManager.currentlyValid(hardwareId) map { result =>

          // verify
          result shouldBe Set(pKey1, pKey2)

        }

      }

    }

    scenario("two keys: first currently valid, second not valid (validNotBefore > now) --> find first") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId)
      val pKey2 = TestDataGeneratorDb.createPublicKey(
        privateKey = privKey2,
        infoPubKey = pubKey2,
        infoHwDeviceId = hardwareId,
        infoValidNotBefore = DateTime.now.plusDays(1)
      )

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        // test
        PublicKeyManager.currentlyValid(hardwareId) map { result =>

          // verify
          result shouldBe Set(pKey1)

        }

      }

    }

    scenario("two keys: first currently valid, second not valid (validNotAfter < now) --> find first") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId)
      val pKey2 = TestDataGeneratorDb.createPublicKey(
        privateKey = privKey2,
        infoPubKey = pubKey2,
        infoHwDeviceId = hardwareId,
        infoValidNotAfter = Some(DateTime.now(DateTimeZone.UTC).minusMillis(100))
      )

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        // test
        PublicKeyManager.currentlyValid(hardwareId) map { result =>

          // verify
          result shouldBe Set(pKey1)

        }

      }

    }

    scenario("two keys: both currently valid (with different hardware ids) --> find first") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId1 = UUIDUtil.uuidStr
      val hardwareId2 = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId1)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = hardwareId2)

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        // test
        PublicKeyManager.currentlyValid(hardwareId1) map { result =>

          // verify
          result shouldBe Set(pKey1)

        }

      }

    }

    scenario("one keys currently valid (except it's been revoked) --> find nothing") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val pubKey = keyPair.publicKey

      createKeys(pubKey) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        val hardwareId = pubKey.pubKeyInfo.hwDeviceId
        PublicKeyManager.currentlyValid(hardwareId) flatMap { result =>

          result shouldBe Set(pubKey)

          val signedRevoke = TestDataGeneratorDb.signedRevoke(
            publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
            privateKey = keyPair.privateKeyString
          )
          val pubKeyRevoked = pubKey.copy(signedRevoke = Some(signedRevoke))

          PublicKeyManager.revoke(signedRevoke) flatMap { revokeResult =>

            revokeResult shouldBe Right(pubKeyRevoked)

            // test
            PublicKeyManager.currentlyValid(hardwareId) map { result =>

              // verify
              result shouldBe empty

            }

          }

        }

      }

    }

  }

  feature("findByPubKey()") {

    scenario("database empty; pubKey doesn't exist --> None") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val hardwareId1 = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId1)

      // test & verify
      PublicKeyManager.findByPubKey(pKey1.pubKeyInfo.pubKey) map (_ shouldBe empty)

    }

    scenario("database not empty; pubKey doesn't exist --> None") {

      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId1 = UUIDUtil.uuidStr
      val hardwareId2 = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId1)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = hardwareId2)

      createKeys(pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        // test & verify
        PublicKeyManager.findByPubKey(pKey1.pubKeyInfo.pubKey) map (_ shouldBe empty)

      }

    }

    scenario("database not empty; pubKey exists --> Some") {

      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId1 = UUIDUtil.uuidStr
      val hardwareId2 = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId1)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = hardwareId2)
      println(s"publicKey1=$pKey1")
      println(s"publicKey2=$pKey2")

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        val pubKeyString = pKey1.pubKeyInfo.pubKey

        // test & verify
        PublicKeyManager.findByPubKey(pubKeyString) map (_ shouldBe Some(pKey1))

      }

    }

  }

  feature("deleteByPubKey()") {

    scenario("database empty; pubKey doesn't exist; valid signature --> true") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = UUIDUtil.uuidStr)
      val pubKeyString = pKey1.pubKeyInfo.pubKey
      val decodedPubKey = Base64.getDecoder.decode(pubKeyString)
      val signature = EccUtil.signPayload(privKey1, decodedPubKey)
      val pubKeyDelete = PublicKeyDelete(
        publicKey = pubKey1,
        signature = signature
      )
      EccUtil.validateSignature(pubKeyString, signature, decodedPubKey) shouldBe true

      // test & verify
      PublicKeyManager.deleteByPubKey(pubKeyDelete) map (_ shouldBe true)

    }

    scenario("database empty; pubKey doesn't exist; invalid signature --> false") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val (_, privKey2) = EccUtil.generateEccKeyPairEncoded
      val hardwareId1 = UUIDUtil.uuidStr

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = hardwareId1)
      val pubKeyString = pKey1.pubKeyInfo.pubKey
      val signature = EccUtil.signPayload(privKey2, pubKeyString)
      val pubKeyDelete = PublicKeyDelete(
        publicKey = pubKeyString,
        signature = signature
      )
      EccUtil.validateSignature(pubKeyString, signature, pubKeyString) shouldBe false

      // test & verify
      PublicKeyManager.deleteByPubKey(pubKeyDelete) map (_ shouldBe false)

    }

    scenario("database not empty; pubKey doesn't exist; valid signature --> true") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = UUIDUtil.uuidStr)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = UUIDUtil.uuidStr)

      createKeys(pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        val pubKeyString = pKey1.pubKeyInfo.pubKey
        val decodedPubKey = Base64.getDecoder.decode(pubKeyString)
        val signature = EccUtil.signPayload(privKey1, decodedPubKey)
        val pubKeyDelete = PublicKeyDelete(
          publicKey = pubKeyString,
          signature = signature
        )
        EccUtil.validateSignature(pubKeyString, signature, decodedPubKey) shouldBe true

        // test
        PublicKeyManager.deleteByPubKey(pubKeyDelete) map { result =>

          // verify
          PublicKeyManager.findByPubKey(pKey1.pubKeyInfo.pubKey) map (_ shouldBe empty)
          PublicKeyManager.findByPubKey(pKey2.pubKeyInfo.pubKey) map (_ shouldBe defined)

          result shouldBe true

        }

      }

    }

    scenario("database not empty; pubKey doesn't exist; invalid signature --> false and don't delete key") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = UUIDUtil.uuidStr)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = UUIDUtil.uuidStr)

      createKeys(pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        val pubKeyString = pKey1.pubKeyInfo.pubKey
        val signature = EccUtil.signPayload(privKey2, pubKeyString)
        val pubKeyDelete = PublicKeyDelete(
          publicKey = pubKeyString,
          signature = signature
        )
        EccUtil.validateSignature(pubKeyString, signature, pubKeyString) shouldBe false

        // test
        PublicKeyManager.deleteByPubKey(pubKeyDelete) map { result =>

          // verify
          PublicKeyManager.findByPubKey(pKey1.pubKeyInfo.pubKey) map (_ shouldBe defined)
          PublicKeyManager.findByPubKey(pKey2.pubKeyInfo.pubKey) map (_ shouldBe defined)

          result shouldBe false

        }

      }

    }

    scenario("database not empty; pubKey exists; valid signature --> true and delete key") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = UUIDUtil.uuidStr)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = UUIDUtil.uuidStr)

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        val pubKeyString = pKey1.pubKeyInfo.pubKey
        val decodedPubKey = Base64.getDecoder.decode(pubKeyString)
        val signature = EccUtil.signPayload(privKey1, decodedPubKey)
        val pubKeyDelete = PublicKeyDelete(
          publicKey = pubKeyString,
          signature = signature
        )
        EccUtil.validateSignature(pubKeyString, signature, decodedPubKey) shouldBe true

        // test
        PublicKeyManager.deleteByPubKey(pubKeyDelete) flatMap { result =>

          // verify
          PublicKeyManager.findByPubKey(pubKeyDelete.publicKey) map (_ shouldBe empty)
          PublicKeyManager.findByPubKey(pKey2.pubKeyInfo.pubKey) map (_ shouldBe defined)

          result shouldBe true

        }

      }

    }

    scenario("database not empty; pubKey exists; invalid signature --> false and don't delete key") {

      // prepare
      val (pubKey1, privKey1) = EccUtil.generateEccKeyPairEncoded
      val (pubKey2, privKey2) = EccUtil.generateEccKeyPairEncoded

      val pKey1 = TestDataGeneratorDb.createPublicKey(privateKey = privKey1, infoPubKey = pubKey1, infoHwDeviceId = UUIDUtil.uuidStr)
      val pKey2 = TestDataGeneratorDb.createPublicKey(privateKey = privKey2, infoPubKey = pubKey2, infoHwDeviceId = UUIDUtil.uuidStr)

      createKeys(pKey1, pKey2) flatMap { createKeysResult =>

        createKeysResult shouldBe true

        val pubKeyString = pKey1.pubKeyInfo.pubKey
        val signature = EccUtil.signPayload(privKey2, pubKeyString)
        val pubKeyDelete = PublicKeyDelete(
          publicKey = pubKeyString,
          signature = signature
        )
        EccUtil.validateSignature(pubKeyString, signature, pubKeyString) shouldBe false

        // test
        PublicKeyManager.deleteByPubKey(pubKeyDelete) flatMap { result =>

          // verify
          PublicKeyManager.findByPubKey(pubKeyDelete.publicKey) map (_ shouldBe defined)
          PublicKeyManager.findByPubKey(pKey2.pubKeyInfo.pubKey) map (_ shouldBe defined)

          result shouldBe false

        }

      }

    }

  }

  feature("revoke()") {

    scenario("key does not exist --> error") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val signedRevoke = TestDataGeneratorDb.signedRevoke(
        publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
        privateKey = keyPair.privateKeyString
      )

      // test
      PublicKeyManager.revoke(signedRevoke) map {

        // verify
        case Right(_) =>

          fail("revokation should have failed")

        case Left(error: KeyRevokeException) =>

          error.getMessage shouldBe "unable to revoke public key if it does not exist"

      }

    }

    scenario("invalid signature --> error") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()

      val now = DateUtil.nowUTC
      val signedRevoke1 = TestDataGeneratorDb.signedRevoke(
        publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
        privateKey = keyPair.privateKeyString,
        created = now
      )
      val signedRevoke2 = TestDataGeneratorDb.signedRevoke(
        publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
        privateKey = keyPair.privateKeyString,
        created = now.plusMinutes(1)
      )
      val withInvalidSignature = signedRevoke1.copy(signature = signedRevoke2.signature)

      // test
      PublicKeyManager.revoke(withInvalidSignature) map {

        // verify
        case Right(_) =>

          fail("revokation should have failed")

        case Left(error: KeyRevokeException) =>

          error.getMessage shouldBe "signature verification failed"

      }

    }

    scenario("key exists --> revoked key") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val signedRevoke = TestDataGeneratorDb.signedRevoke(
        publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
        privateKey = keyPair.privateKeyString
      )
      val pubKey = keyPair.publicKey

      PublicKeyManager.create(pubKey) flatMap { createResult =>

        createResult shouldBe Right(Some(pubKey))

        // test
        PublicKeyManager.revoke(signedRevoke) map { revokeResult =>

          // verify
          val pubKeyRevoked = pubKey.copy(signedRevoke = Some(signedRevoke))
          revokeResult shouldBe Right(pubKeyRevoked)

        }

      }

    }

    scenario("key has been revoked already --> error") {

      // prepare
      val keyPair = TestDataGeneratorDb.generateOneKeyPair()
      val signedRevoke = TestDataGeneratorDb.signedRevoke(
        publicKey = keyPair.publicKey.pubKeyInfo.pubKey,
        privateKey = keyPair.privateKeyString
      )
      val pubKeyRevoked = keyPair.publicKey.copy(signedRevoke = Some(signedRevoke))

      PublicKeyManager.create(pubKeyRevoked) flatMap { createResult =>

        createResult shouldBe Right(Some(pubKeyRevoked))

        // test
        PublicKeyManager.revoke(signedRevoke) map {

          // verify
          case Right(_) =>

            fail("revokation should have failed")

          case Left(error: KeyRevokeException) =>

            error.getMessage shouldBe "unable to revoke public key if it has been revoked already"

        }

      }

    }

  }


  private def createKeys(pubKeys: PublicKey*): Future[Boolean] = {

    // TODO copy to test-tools-ext?
    val resultsFuture = pubKeys map { pubKey =>
      PublicKeyManager.create(pubKey = pubKey) map {
        case Left(t) => false
        case Right(None) => false
        case Right(Some(_: PublicKey)) => true
      }
    }

    Future.sequence(resultsFuture.toList) map { results =>
      results.forall(b => b)
    }

  }

}
