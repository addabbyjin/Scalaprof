package edu.neu.coe.csye._7200

/**
 * @author scalaprof
 */
object Prime extends App {
  val start = System.currentTimeMillis()
//  def nthPrime(n: Int) = Stream.from(2).filter(isPrime(_))(n) // 46 mSec
  def nthPrime(n: Int) = (2 to 200000).filter(isPrime)(n) // 104 mSec
  def isPrime(x: Int): Boolean = (2 to math.floor(math.sqrt(x)).toInt) find {x % _ ==0} match {case Some(z) => false; case None => true}
  val n = 9591
  val m = 100
  // TODO this looks wrong...
  val pN = for(i <- 1 to m) nthPrime(n)
  val end = System.currentTimeMillis()
  println(s"${n}th prime: "+pN)
  println(s"time taken: ${(end-start)/m} mSec")
}
