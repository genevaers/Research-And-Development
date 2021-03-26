package filters

import javax.inject._
import play.api.mvc._
import scala.concurrent.ExecutionContext

/*  This controller is a sample and not currently used in the project except in the route table **/

/**
 * This is a simple filter that adds a header to all requests. It's
 * added to the application's list of filters by the
 * [[Filters]] class.
 *
 * @param ec This class is needed to execute code asynchronously.
 * It is used below by the `map` method.
 */
@Singleton
class ExampleFilter @Inject()(implicit ec: ExecutionContext) extends EssentialFilter {
  override def apply(next: EssentialAction) = EssentialAction { request =>
    next(request).map { result =>
      result.withHeaders("X-ExampleFilter" -> "foo")
    }
  }
}