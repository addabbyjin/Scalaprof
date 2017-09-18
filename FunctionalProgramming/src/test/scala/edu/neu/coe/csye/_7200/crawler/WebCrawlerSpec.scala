package edu.neu.coe.scala.crawler

import java.io.FileNotFoundException
import java.net.{MalformedURLException, URL}

import edu.neu.coe.csye._7200.crawler.WebCrawler
import edu.neu.coe.scala.MonadOps
import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest.time._
import org.scalatest.{FlatSpec, Matchers, _}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

/**
  * @author scalaprof
  */
class WebCrawlerSpec extends FlatSpec with Matchers with Futures with ScalaFutures with TryValues with Inside {

  val goodURL = "http://www.rubecula.com/RobinHillyard.html"
  val badURL = "http://www.rubecula.com/junk"

  "getURLContent" should s"succeed for $goodURL" in {
    val wf = WebCrawler.getURLContent(new URL(goodURL))
    whenReady(wf, timeout(Span(6, Seconds))) { w => w.length shouldBe 2234 }
  }

  "wget(URL)" should s"succeed for $goodURL" in {
    val usfy = for {u <- Try(new URL(goodURL))} yield WebCrawler.wget(u)
    whenReady(MonadOps.flatten(usfy), timeout(Span(6, Seconds))) { us => us.length shouldBe 8 }
  }

  it should s"not succeed for $badURL" in {
    val usfy = for {u <- Try(new URL(badURL))} yield WebCrawler.wget(u)
    val usf = MonadOps.flatten(usfy)
    whenReady(usf.failed, timeout(Span(6, Seconds))) { e => e shouldBe a[FileNotFoundException] }
  }

  it should s"not succeed for x$goodURL" in {
    val usfy = for {u <- Try(new URL("x//www.htmldog.com/examples/"))} yield WebCrawler.wget(u)
    usfy.failure.exception shouldBe a[MalformedURLException]
    usfy.failure.exception should have message "no protocol: x//www.htmldog.com/examples/"
  }

  "wget(Seq[URL])" should s"succeed for $goodURL, http://www.dataflowage.com/" in {
    val ws = List(goodURL, "http://www.dataflowage.com/")
    val uys = for (w <- ws) yield Try(new URL(w))
    val usesfy = for {us <- MonadOps.sequence(uys)} yield WebCrawler.wget(us)
    val usesf = MonadOps.flatten(usesfy)
    whenReady(usesf, timeout(Span(12, Seconds))) { uses =>
      uses.size shouldBe 2
      for (use <- uses) use match {
        case Right(us) => us.length should be >= 8
        case Left(x) => System.err.println(s"ignored error: $x")
      }
    }
  }

  "filterAndFlatten" should "work" in {
    val ws = List(goodURL)
    val uys = for (w <- ws) yield Try(new URL(w))
    MonadOps.sequence(uys) match {
      case Success(us1) =>
        val usefs = WebCrawler.wget(us1)
        val exceptions = mutable.MutableList[Throwable]()
        val usf = MonadOps.flattenRecover(usefs, { x => exceptions += x })
        whenReady(usf, timeout(Span(12, Seconds))) {
          us2 => us2.distinct.size shouldBe 8
            exceptions.size shouldBe 0
        }
      case f@_ => fail(f.toString())
    }
  }

  "crawler(Seq[URL])" should s"succeed for $goodURL, depth 2" in {
    val args = List(s"$goodURL")
    val tries = for (arg <- args) yield Try(new URL(arg))
    //    println(s"tries: $tries")
    val usft = for {us <- MonadOps.sequence(tries)} yield WebCrawler.crawler(2, us)
    whenReady(MonadOps.flatten(usft), timeout(Span(60, Seconds))) { s => Assertions.assert(s.length == 9) }
  }

//  "crawler(Seq[URL])" should "succeed for test.html, depth 2" in {
//    val project = "/Users/scalaprof/ScalaClass/FunctionalProgramming"
//    val dir = "src/test/scala"
//    val pkg = "edu/neu/coe/scala/crawler"
//    val file = "test.html"
//    val args = List(s"file://$project/$dir/$pkg/$file")
//    val tries = for (arg <- args) yield Try(new URL(arg))
//    //    println(s"tries: $tries")
//    val usft = for {us <- MonadOps.sequence(tries)} yield WebCrawler.crawler(2, us)
//    whenReady(MonadOps.flatten(usft), timeout(Span(20, Seconds))) { s => Assertions.assert(s.length == 2) }
//  }
//  it should "succeed for test.html, depth 3" in {
//    val project = "/Users/scalaprof/ScalaClass/FunctionalProgramming"
//    val dir = "src/test/scala"
//    val pkg = "edu/neu/coe/scala/crawler"
//    val file = "test.html"
//    val args = List(s"file://$project/$dir/$pkg/$file")
//    val tries = for (arg <- args) yield Try(new URL(arg))
//    println(s"tries: $tries")
//    val usft = for {us <- MonadOps.sequence(tries)} yield WebCrawler.crawler(3, us)
//    val usf = MonadOps.flatten(usft)
//    whenReady(usf, timeout(Span(60, Seconds))) { us =>
//      println(us)
//      us.size shouldBe 177
//    }
//  }
}