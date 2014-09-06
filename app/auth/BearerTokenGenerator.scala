package token_generator

import scala.util._
import java.security.SecureRandom

/*
 * Generates a Bearer Token with the length of 32 characters according to the
 * specification RFC6750 (http://http://tools.ietf.org/html/rfc6750)
 */
object BearerTokenGenerator {

  val TOKEN_LENGTH = 32
  val TOKEN_CHARS =
     "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._"
  val secureRandom = new SecureRandom()

  def generateToken:String =
    generateToken(TOKEN_LENGTH)

  def generateToken(tokenLength: Int): String =
    if (tokenLength == 0) ""
    else TOKEN_CHARS(secureRandom.nextInt(TOKEN_CHARS.length())) +
      generateToken(tokenLength - 1)
}
