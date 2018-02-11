import cats.data.Kleisli
import domain.RequestId
import monix.eval.Task

package object application {

  type PhotoServiceOp[A] = Kleisli[Task, RequestId, A]

  def op[A](run: RequestId => Task[A]): PhotoServiceOp[A] = Kleisli(run)

  def ignoreRequestId[A](task: Task[A]): PhotoServiceOp[A] = Kleisli.liftF(task)

  def pure[A](a: A): PhotoServiceOp[A] = Kleisli.pure[Task, RequestId, A](a)

}
