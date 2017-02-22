package v1

import play.api.i18n.Messages

/**
  * Package object for news.  This is a good place to put implicit conversions.
  */
package object news {

  /**
    * Converts between NewsRequest and Messages automatically.
    */
  implicit def requestToMessages[A](implicit r: NewsRequest[A]): Messages = {
    r.messages
  }
}
