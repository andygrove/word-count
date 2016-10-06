package wc

import java.io.{BufferedReader, FileReader}
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.convert.Wrappers.JConcurrentMapWrapper

object WordCount {

  val MIN_WORD_LENGTH = 6

  val map = new JConcurrentMapWrapper[String, AtomicInteger](new ConcurrentHashMap[String,AtomicInteger]())

  def main(arg: Array[String]): Unit = {

    val filename = "/Users/andy/Documents//shakespeare.txt"

    val r = new BufferedReader(new FileReader(filename), 64*1024)
    var line : String = r.readLine()
    var word = new StringBuilder(1024)

    while (line != null) {

      word.setLength(0)
      var i = 0
      for (i <- 0 until line.length) {
        val ch = line.charAt(i)
        if (Character.isAlphabetic(ch)) {
          word.append(ch)
        } else {
          if (word.length >= WordCount.MIN_WORD_LENGTH) {
            processWord(word.toString())
          }
          word.setLength(0)
        }

      }

      // final word?
      if (word.length >= WordCount.MIN_WORD_LENGTH) {
        processWord(word.toString())
      }

      line = r.readLine()
    }
    r.close()

    val list: List[(String, AtomicInteger)] = map.toList.sortBy(entry => -entry._2.get())
    list.take(10).foreach(System.out.println)

  }

  def processWord(word: String): Unit = {
    map.getOrElseUpdate(word.toLowerCase, new AtomicInteger()).incrementAndGet()
  }

}


