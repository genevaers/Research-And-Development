
package controllers
// (c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell

import javax.inject._
import play.api.mvc._
import com.aerospike.client._
import play.api.Play
//import play.api.inject.AerospikeConnection


/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class EnvTestController @Inject()(cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/mockup`.
    */
  def testenv = Action {


    //    val client = new AerospikeClient("ulserver",3000)

    // Initialize policy
//    val policy = new WritePolicy
//    policy.totalTimeout = 50 // 50 millisecond timeout
    // Read a record
    // This records should have been installed during the Vagrant initenv script Aerospike verify
    val key =  new Key("test", "demo", "Aerospike")
    println("Record Key: " + key)
//    val record = ASClient.get(null, key)
//    println("Record Read: " + record)

    Ok(s"Aerospike Database Test Connection Not Coded")

//    Ok(s"Aerospike Database Test Connection Record = ${record}")
  }

}