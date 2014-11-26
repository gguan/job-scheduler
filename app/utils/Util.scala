package utils

import awscala.s3.{S3Object, S3}
import com.amazonaws.services.s3.model.{ObjectMetadata, ObjectListing, S3ObjectSummary}
import scala.collection.JavaConversions.{collectionAsScalaIterable => asScala}
import scala.sys.process.Process


case class S3Helper(bucketName: String) {

  implicit val s3 = S3()

  lazy val bucket = s3.bucket(bucketName).get

  def scanPrefix(prefix: String): Seq[String] = s3.keys(bucket, prefix)

  def getFile(key: String): Option[S3Object] = {
    bucket.get(key)
  }

  def readString(srcKey: String): String = {
    scala.io.Source.fromInputStream(bucket.get(srcKey).get.content) .getLines().mkString("\n")
  }

  def writeString(str: String, dstKey: String) = {
    val metadata = new ObjectMetadata()
    metadata.setContentType("text/plain")
    val bytes = str.getBytes("utf-8")
    metadata.setContentLength(bytes.size)
    s3.put(bucket, dstKey, bytes, metadata)
  }

  def exists(path: String): Boolean = {
    map(s3, "pm-archives", path)(_.getKey).exists(_.startsWith(path))
  }

  private def map[T](s3: awscala.s3.S3, bucket: String, prefix: String)
                    (f: (S3ObjectSummary) => T) = {
    def scan(acc: List[T], listing: ObjectListing): List[T] = {
      val summaries = asScala[S3ObjectSummary](listing.getObjectSummaries())
      val mapped = (for (summary <- summaries) yield f(summary)).toList

      if (!listing.isTruncated) mapped.toList
      else scan(acc ::: mapped, s3.listNextBatchOfObjects(listing))
    }
    scan(List(), s3.listObjects(bucket, prefix))
  }

}

object CmdUtil {

  def run(cmd: String): Boolean = {
    val pb = Process(cmd)
    val exitCode = pb.!
    if (exitCode == 0)
      true
    else
      false
  }

  def runWithLog(cmd: String, log: String): Boolean = {
    val pb = Process(cmd + " >> /root/logs/" + log)
    val exitCode = pb.!
    if (exitCode == 0)
      true
    else
      false
  }
}

object PrintUtil {
  def apply(s: String) {
    println("########\t" + s)
  }
}