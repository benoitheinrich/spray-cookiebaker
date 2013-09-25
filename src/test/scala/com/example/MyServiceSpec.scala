package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import spray.http.HttpHeaders.Cookie


class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  val expectedCookie = "6d0a84f8c9eddb11e503925f26aee5f6968909f7-p1%3Av1"

  "MyService" should {

    "set some value in the session and allow the user to retrieve it" in {
      Get("/set/p1/v1") ~> myRoute ~> check {
        entityAs[String] must contain("The session has been Set")
        header("Set-Cookie") must beSome
        header("Set-Cookie").get.value must equalTo(s"SPRAY_SESSION=$expectedCookie; Path=/; HttpOnly")
      }
      Get("/get/p1").withHeaders(Cookie(HttpCookie("SPRAY_SESSION", expectedCookie))) ~> myRoute ~> check {
        entityAs[String] must contain("Some(v1)")
      }
    }

    "clear session when needed" in {
      Get("/set/p1/v1") ~> myRoute ~> check {
        entityAs[String] must contain("The session has been Set")
        header("Set-Cookie") must beSome
        header("Set-Cookie").get.value must equalTo(s"SPRAY_SESSION=$expectedCookie; Path=/; HttpOnly")
      }
      Get("/clear") ~> myRoute ~> check {
        entityAs[String] must contain("The session has been Cleared")
        header("Set-Cookie") must beSome
        header("Set-Cookie").get.value must equalTo(s"SPRAY_SESSION=; Max-Age=-1; Path=/")
      }
    }
  }
}