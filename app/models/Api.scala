package models

import play.api.mvc._
import play.api.db._
import play.api.Play.current
import anorm._
import com.github.tototoshi.base62._
import token_generator.BearerTokenGenerator

object Api {
  val base62 = new Base62

  def isAuthorized(request: RequestHeader) = {
    val token = request.headers.get("X-Auth-Token")

    DB.withConnection { implicit c =>
      val query = SQL("select * from users where token = {token}").
        on("token" -> token)

      !query().toList.isEmpty
    }
  }

  def getToken(userid: Int) = {
    val tokenList =
      DB.withConnection { implicit c =>
        val query = SQL("select * from users where userid = {userid}").
          on("userid" -> userid)

        query().map(row => row[String]("token")).toList
      }

    if (tokenList.isEmpty)
      createToken(userid)
    else
      tokenList.head
  }

  def createToken(userid: Int) = {
    val token = BearerTokenGenerator.generateToken

    DB.withConnection { implicit c =>
      val result: Int =
        SQL("insert into users values ({userid}, {token})").
          on("userid" -> userid, "token" -> token).
          executeUpdate()
    }

    token
  }

  def getShortUrlCode(url: String) =
    DB.withConnection { implicit c =>
      val query = SQL("select * from links where url = {url}").
        on("url" -> url)

      val result = query().map(row => row[Int]("id")).toList

      if (result.toList.isEmpty) {
        val query =
          SQL("insert into links (url) values ({url}) returning id").
            on("url" -> url)
        val result = query().map(row => row[Int]("id")).toList

        val linkId   = result.head
        val linkCode = base62.encode(linkId)

        SQL("update links set code = {code} where id = {linkId}").
          on("linkId" -> linkId, "code" -> linkCode).executeUpdate()

        linkCode
      }
      else {
        base62.encode(result.head) //encode url id to get short url code
      }
    }

  def addClickForLink(linkId: Long, referer: String, remoteIP: String) =
    DB.withConnection { implicit c =>
      val result: Int = SQL(
        """
          insert into clicks (referer, remote_ip, link_id)
          values ({referer}, {remoteIP}, {linkId})
        """
      ).on("referer" -> referer, "remoteIP" -> remoteIP, "linkId" -> linkId).
        executeUpdate()
    }

  def getLinkUrl(linkId: Long) =
    DB.withConnection { implicit c =>
      val query = SQL("select * from links where id = {linkId}").
        on("linkId" -> linkId)

      val result = query().map(row => row[String]("url")).toList

      if (result.isEmpty)
        Nil
      else
        result.head
    }

  def getLinkInfo(linkId: Long) =
    DB.withConnection { implicit c =>
      val query = SQL(
        """
          select l.url, count(c.link_id) as clicks from
          links l left outer join clicks c on (l.id = c.link_id)
          where l.id = {linkId}
          group by 1
        """).
        on("linkId" -> linkId)

      val result = query().
        map(row => (row[String]("url"), row[Long]("clicks"))).toList

      if (result.isEmpty)
        Nil
      else
        result.head
    }

  def getLinks(limit: Int, offset: Int) =
    DB.withConnection { implicit c =>
      val query = SQL("select * from links limit {limit} offset {offset}").
        on("limit" -> limit, "offset" -> offset)

      query().map(row => row[String]("url") -> row[String]("code")).toList
    }

  def getClicks(linkId: Long, limit: Int, offset: Int) =
    DB.withConnection { implicit c =>
      val query = SQL(
        """
          select * from clicks
          where link_id = {linkId} limit {limit} offset {offset}
        """
      ).on("limit" -> limit, "offset" -> offset, "linkId" -> linkId)

      query().map(row => row[String]("referer") -> row[String]("remote_ip")).
        toList
    }
}
