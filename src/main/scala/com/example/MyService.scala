package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import com.example.sessionutils.{Session, SessionDirectives}


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService with SessionDirectives {
  val myRoute =
    (get & path("clear") & respondWithMediaType(`text/html`) & clearSession) {
      complete {
        <html>
          <body>
            <h1>The session has been Cleared</h1>
          </body>
        </html>
      }
    } ~
    (get & path("set" / Segment / Segment) & respondWithMediaType(`text/html`)) {
      (param, value) =>
        optionalSession { os =>
            storeSessionParam(os, param, value) & complete {
              <html>
                <body>
                  <h1>The session has been Set</h1>
                </body>
              </html>
            }
        }
    } ~
    (get & path("get" / Segment) & respondWithMediaType(`text/html`)) {
      (param) => session { sessionData:Session =>
        complete {
          <html>
            <body>
              <h1>Session value:
                {sessionData.get(param)}
              </h1>
            </body>
          </html>

        }
      }
    } ~
    (get & path("getAll") & respondWithMediaType(`text/html`)) {
      session { sessionData:Session =>
        complete {
          <html>
            <body>
              <h1>Session value: {sessionData.data.toString}</h1>
            </body>
          </html>
        }
      }
    } ~
    (path("getoptional") & get & respondWithMediaType(`text/html`)) {
      optionalSession { sessionData:Option[Session] =>
        complete {
          <html>
            <body>
              {
                sessionData.map { sess =>
                  <h1>Session value: {sess.data.toString}</h1>
                } getOrElse {
                  <h1>Session not set</h1>
                }
              }
            </body>
          </html>
        }
      }
    }

  /*  def storeSessionParam(param: String, value: String) = optionalSession {
      os => setSession(os.getOrElse(new Session()) + (param -> value))
    }*/

  // TODO: how to have the optionalSession retrieved from the method directly
  def storeSessionParam(os: Option[Session], param: String, value: String): Directive0 = {
    setSession(os.getOrElse(new Session()) + (param -> value))
  }
}