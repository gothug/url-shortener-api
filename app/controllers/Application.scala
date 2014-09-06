package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def token(userid: Int, secret: String) = Action {
    val secretKey = "AqDKS7EKnE5wq820"

    if (secret == secretKey) {
      val token = Api.getToken(userid)
      Ok(toJson(Map("token" -> token)))
    }
    else
      Ok(toJson(Map("error" -> "Incorrect secret")))
  }

  def link = Action(parse.json) {
    implicit request => {
      if (Api.isAuthorized(request)) {
        val url = (request.body \ "url").toString.replace("\"","")
        val shortUrlCode = Api.getShortUrlCode(url)
        val response =
          Map("url" -> url, "code" -> shortUrlCode,
            "shortUrl" -> ("http://one.sh/" + shortUrlCode))

        Ok(toJson(response))
      }
      else
        nonAuthorizedResponse
    }
  }

  def linkClick(code: String) = Action(parse.json) {
    implicit request => {
      if (Api.isAuthorized(request)) {
        val referer  = (request.body \ "referer").toString.replace("\"","")
        val remoteIP = (request.body \ "remote_ip").toString.replace("\"","")
        val linkId   = Api.base62.decode(code)

        val url = Api.getLinkUrl(linkId)

        val response =
          url match {
            case s: String => {
              Api.addClickForLink(linkId, referer, remoteIP)
              Json.toJson(Map("url" -> s))
            }
            case _         => Json.obj("error" -> "Unknown short link code")
          }

        Ok(Json.prettyPrint(response))
      }
      else
        nonAuthorizedResponse
    }
  }

  def linkInfo(code: String) = Action {
    implicit request => {
      if (Api.isAuthorized(request)) {
        val linkId = Api.base62.decode(code)

        val info = Api.getLinkInfo(linkId)

        val response =
          info match {
            case (url: String, clicks: Long) =>
              Json.obj(
                "url"    -> url,
                "code"   -> code,
                "clicks" -> clicks
              )
            case _ => Json.obj("error" -> "Unknown short link code")
          }

        Ok(Json.prettyPrint(response))
      }
      else
        nonAuthorizedResponse
    }
  }

  def linksList(limit: Option[Int], offset: Option[Int]) = Action {
    implicit request => {
      if (Api.isAuthorized(request)) {
        val links = Api.getLinks(limit.getOrElse(3), offset.getOrElse(0))

        val response = links.map(x => Map("url" -> x._1, "code" -> x._2))

        Ok(toJson(response))
      }
      else
        nonAuthorizedResponse
    }
  }

  def clicksList(code: String, limit: Option[Int], offset: Option[Int]) =
    Action {
      implicit request => {
        if (Api.isAuthorized(request)) {
          val linkId = Api.base62.decode(code)
          val clicks =
            Api.getClicks(linkId, limit.getOrElse(3), offset.getOrElse(0))

          val response =
            clicks.map(x => Map("referer" -> x._1, "remote_ip" -> x._2))

          Ok(toJson(response))
        }
        else
          nonAuthorizedResponse
      }
    }

  def toJson(x: Map[String, String]) =
    Json.prettyPrint(Json.toJson(x))

  def toJson(x: List[Map[String, String]]) =
    Json.prettyPrint(Json.toJson(x))

  def nonAuthorizedResponse =
    Ok(toJson(Map("error" -> "No valid token passed")))
}
