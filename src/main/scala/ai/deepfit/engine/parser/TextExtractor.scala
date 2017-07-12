package ai.deepfit.engine.parser

import ai.deepfit.engine.config.Config
import java.io._

import scala.collection.JavaConversions.asScalaIterator
import org.apache.commons.io.{FileUtils, FilenameUtils, IOUtils}
import org.apache.commons.io.filefilter.{DirectoryFileFilter, WildcardFileFilter}
import org.apache.tika.exception.TikaException
import org.apache.tika.metadata.{Metadata, TikaCoreProperties}
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.sax.WriteOutContentHandler


/**
  * Created by alvinjin on 2017-06-29.
  */
object TextExtractor extends App with Config {

  val extractor = new TextExtractor()
  val idir = new File(cvInputPath)
  val odir = new File(cvStagePath)

  extractor.extractDirToFiles(idir, odir, None)

}

class TextExtractor {

  object FileType extends Enumeration {
    type FileType = Value
    val Text, Xml, Pdf, MsWord, Undef = Value
  }

  object DocPart extends Enumeration {
    type DocPart = Value
    val Title, Author, Body, Error = Value
  }

  val parsers = Map[FileType.Value, Parser](
    (FileType.Text, new TXTParser()),
    (FileType.Pdf, new PDFParser()),
    (FileType.MsWord, new OfficeParser()),
    (FileType.Undef, new AutoDetectParser())
  )

  /** Extract single file to Text */
  def extractText(file: File): String = {

    var istream: InputStream = null

    try {
      istream = new FileInputStream(file)
      val handler = new WriteOutContentHandler(-1)
      val metadata = new Metadata()
      val parser = parsers(detectFileType(file))
      val ctx = new ParseContext()
      parser.parse(istream, handler, metadata, ctx)
      handler.toString
    } catch {
      case e: TikaException => e.getMessage()
    } finally {
      IOUtils.closeQuietly(istream)
    }
  }

  def extract(file: File): Map[DocPart.Value, String] = {

    var istream: InputStream = null

    try {
      istream = new FileInputStream(file)
      val handler = new WriteOutContentHandler(-1)//ToXMLContentHandler
      val metadata = new Metadata()
      val parser = parsers(detectFileType(file))
      val ctx = new ParseContext()
      parser.parse(istream, handler, metadata, ctx)
      Map[DocPart.Value, String](
        (DocPart.Author, metadata.get(TikaCoreProperties.CREATOR)),
        (DocPart.Title, metadata.get(TikaCoreProperties.TITLE)),
        (DocPart.Body, handler.toString))
    } catch {
      case e: TikaException => Map[DocPart.Value, String](
        (DocPart.Error, e.getMessage()))
    } finally {
      IOUtils.closeQuietly(istream)
    }
  }

  /** Detect FileType based on file name suffix */
  def detectFileType(file: File): FileType.Value = {

    val suffix = FilenameUtils.getExtension(file.getName()).toLowerCase()

    suffix match {
      case "text" | "txt" => FileType.Text
      case "pdf" => FileType.Pdf
      case "doc" | "docx" => FileType.MsWord
      case _ => FileType.Undef
    }
  }

  /** Extract all files in directory with specified file name
    * pattern. Accepts a renderer function to convert name-
    * value pairs into an output file (or files). */
  def extract(dir: File, pattern: Option[String], odir: File,
              renderer: (File, File, Map[DocPart.Value, String]) => Unit): Unit ={
    val fileFilter = pattern match {
      case None => new WildcardFileFilter("*.*")
      case _ => new WildcardFileFilter(pattern.get)
    }

    FileUtils.iterateFiles(dir, fileFilter,
      DirectoryFileFilter.DIRECTORY).foreach(file => {
      val data = extract(file)
      renderer(file, odir, data)
      }
    )
  }

  /** Convenience method to write out text extracted from a file
    * into the specified directory as filename.txt */
  def extractDirToFiles(dir: File, odir: File, pattern: Option[String]): Unit = {

    extract(dir, pattern, odir, renderDirToFiles)
  }

  def renderDirToFiles(file: File, odir: File, data: Map[DocPart.Value, String]): Unit = {

    val ofname = FilenameUtils.removeExtension(file.getName) + ".txt"

    val writer = new PrintWriter(new FileWriter(new File(odir, ofname)), true)

    writer.println(data(DocPart.Body))
    writer.flush()
    writer.close()
  }

}
