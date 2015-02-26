package analysis

import dao.MusicLibraryDAO
import domain.{MusicLibrarySearchParams, MusicLibrary}

import scala.util.Random

object MusicSearch {

  private val SEARCH_FAVORITE = new MusicLibrarySearchParams(None, None, Some(true), None)

  def startSearch(status: String):Either[Failure, MusicLibrary] = searchFavorite

  private def searchFavorite: Either[Failure, MusicLibrary] = {
    val musicLibraryDAO = new MusicLibraryDAO

    val favorites = musicLibraryDAO.search(SEARCH_FAVORITE) match {
      case Right(list) => list
      case Left(e) =>
        println(s"error = ${e.toString}")
        List()
    }

    val random = if (favorites.size > 1) Random.nextInt(favorites.size) else 0

    favorites match {
      case head :: tail => Right(favorites(random))
      case Nil => Left(Failure("すみません。気に入りそうなものを見つけられませんでした・・・。", FailureType.NotFound))
    }
  }

}
