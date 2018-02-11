package infrastructure

import java.nio.charset.StandardCharsets

import io.lettuce.core.api.async.RedisAsyncCommands
import monix.eval.Task
import monix.execution.Cancelable

class AsyncRedisClient(commands: RedisAsyncCommands[Array[Byte], Array[Byte]]) {

  def get(key: String): Task[Option[Array[Byte]]] = Task.async {
    case (scheduler, callback) =>
      val redisFuture = commands.get(key.getBytes(StandardCharsets.UTF_8))
      redisFuture.handle[Unit]((bytes: Array[Byte], e: Throwable) => {
        if (e != null) callback.onError(e)
        else callback.onValue(Option(bytes))
      })
      Cancelable(() => redisFuture.cancel(true))
  }

  def put(key: String, value: Array[Byte]): Task[Unit] = Task.async {
    case (scheduler, callback) =>
      val redisFuture = commands.set(key.getBytes(StandardCharsets.UTF_8), value)
      redisFuture.handle[Unit]((reply: String, e: Throwable) => {
        if (e != null) callback.onError(e)
        else callback.onValue(())
      })
      Cancelable(() => redisFuture.cancel(true))
  }

}
