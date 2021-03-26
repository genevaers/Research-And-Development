package controllers
// (c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell

import javax.inject._
import org.slf4j.LoggerFactory
import play.api.Logger
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  val LOG = LoggerFactory.getLogger(this.getClass)

  LOG.trace("Starting up the system.")
  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("The Universal Ledger"))
  }

}
