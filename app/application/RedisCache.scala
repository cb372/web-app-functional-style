package application

import domain.{CacheAlg, Photo}
import infrastructure.AsyncRedisClient

class RedisCache(redisClient: AsyncRedisClient) extends CacheAlg[PhotoServiceOp] {

  override def getPhoto(id: domain.PhotoId): PhotoServiceOp[Option[Photo]] = ignoreRequestId {
    redisClient.get(s"photo:$id").map(_.map(Photo))
  }

  override def putPhoto(id: domain.PhotoId, photo: Photo): PhotoServiceOp[Unit] = ignoreRequestId {
    redisClient.put(s"photo:$id", photo.bytes)
  }

}
