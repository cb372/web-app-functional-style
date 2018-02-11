package application

import domain.{CacheAlg, LoggingAlg, Photo}
import infrastructure.AsyncRedisClient

class RedisCache(redisClient: AsyncRedisClient, logging: LoggingAlg[PhotoServiceOp])
    extends CacheAlg[PhotoServiceOp] {

  override def getPhoto(id: domain.PhotoId): PhotoServiceOp[Option[Photo]] =
    for {
      bytes <- ignoreRequestId(redisClient.get(s"photo:$id"))
      hitOrMiss = if (bytes.isDefined) "hit" else "miss"
      _ <- logging.info(s"Cache $hitOrMiss for photo $id")
      photo = bytes.map(Photo)
    } yield photo

  override def putPhoto(id: domain.PhotoId, photo: Photo): PhotoServiceOp[Unit] = ignoreRequestId {
    redisClient.put(s"photo:$id", photo.bytes)
  }

}
