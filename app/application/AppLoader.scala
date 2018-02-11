package application

import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}
import controllers.PhotoController
import domain.PhotoServiceAlg
import infrastructure.{AsyncRedisClient, S3Client}
import io.lettuce.core.codec.ByteArrayCodec
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import router.Routes

class AppComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context) {

  private val redisCache = {
    val asyncCommands = io.lettuce.core.RedisClient
      .create("redis://localhost")
      .connect(ByteArrayCodec.INSTANCE)
      .async()
    val redisClient = new AsyncRedisClient(asyncCommands)
    new RedisCache(redisClient)
  }

  private val s3DurableStore = {
    val underlying: AmazonS3 = AmazonS3Client.builder().withRegion("eu-west-1").build()
    val bucketName           = "web-app-functional-style"
    val s3Client             = new S3Client(bucketName, underlying)
    new S3DurableStore(s3Client)
  }

  private val dummyEvents = new DummyEvents(PlayLogging)

  private val photoService = new PhotoServiceAlg[PhotoServiceOp](
    s3DurableStore,
    redisCache,
    LenientValidation,
    dummyEvents,
    PlayLogging
  )

  private val photoController = new PhotoController(controllerComponents, photoService)

  override def router: Router                    = new Routes(httpErrorHandler, photoController)
  override def httpFilters: Seq[EssentialFilter] = Nil
}

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application =
    new AppComponents(context).application
}
