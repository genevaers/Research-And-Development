package controllers
// (c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell

import javax.inject._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class MockupsController @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/mockup`.
    */
  def mockups(image: String) = Action {
    Ok(views.html.mockups("Universal Ledger Mockups", image))
  }

}