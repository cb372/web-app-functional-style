package application

import domain.{LoggingAlg, RequestId}
import monix.eval.Task
import org.slf4j.MDC
import play.api.Logger

object PlayLogging extends LoggingAlg[PhotoServiceOp] {

  override def info(msg: String): PhotoServiceOp[Unit] = op { requestId =>
    Task {
      withMDC(requestId)(Logger.info(msg))
    }
  }

  override def warn(msg: String, e: Throwable): PhotoServiceOp[Unit] = op { requestId =>
    Task {
      withMDC(requestId)(Logger.warn(msg, e))
    }
  }

  private def withMDC[A](requestId: RequestId)(f: => A): A = {
    MDC.put("requestId", requestId.value)
    try {
      f
    } finally {
      MDC.remove("requestId")
    }
  }

}
