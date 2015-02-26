package implicits

object Conversions {

  implicit class ArrayImprovements[T](val array: Array[T]) {

    import scala.util.control.Exception._

    def getElemOpt(elemNum: Int) = catching(classOf[IndexOutOfBoundsException]) opt array(elemNum)

    def getElemOrElse(elemNum: Int, default: T) = catching(classOf[IndexOutOfBoundsException]).opt(array(elemNum)).getOrElse(default)
  }

  implicit def tList2Map[A](tList: List[(A, A)]): Map[A, A] = tList map { case (key, value) => key -> value} toMap

}
