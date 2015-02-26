package domain

import scala.slick.driver.MySQLDriver.simple._

case class MusicLibrary(id: Option[Long], title: String, artists: String, youtubeId: String, favorite: Boolean, category: String)

case class MusicLibrarySearchParams(title: Option[String], artists: Option[String], favorite: Option[Boolean], category: Option[String])

object MusicLibraryTag extends TableQuery(new MusicLibraryTag(_)) {
  val findById = this.findBy(_.id)
}

class MusicLibraryTag(tag: Tag) extends Table[MusicLibrary](tag, "music_library") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def title = column[String]("title", O.NotNull)

  def artists = column[String]("artists", O.NotNull)

  def youtubeId = column[String]("youtube_id", O.NotNull)

  def favorite = column[Boolean]("favorite", O.NotNull)

  def category = column[String]("category", O.NotNull)

  def * = (id.?, title, artists, youtubeId, favorite, category) <>(MusicLibrary.tupled, MusicLibrary.unapply)

}