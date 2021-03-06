//package mwittmann.neo4japp.parsewitherror
//
//import java.util.UUID
//import scala.collection.JavaConverters._
//import scala.collection.immutable
//
//import cats.data.{IndexedStateT, State, StateT}
//import cats.syntax._
//import cats.implicits._
//import mwittmann.neo4japp.parsewitherror.ParseN4j._
//import org.neo4j.driver.v1.{Record, Value}
//
//object ParseN4j {
//
//  object ParseState {
//    def empty = ParseState(List.empty)
//  }
//
//  case class ParseState(actions: List[(String, String)]) {
//    def appendAction(str: String): ParseState = this.copy(actions = (str, str) :: actions)
//
//    def appendAction(str: String, full: String): ParseState = this.copy(actions = (str, full) :: actions)
//  }
//
//  //  type ResultState[S] = State[ParseState, S]
//
//  case class Error(message: String, exception: Option[Exception], state: ParseState)
//
//  object Result {
//
//    def failureF[S](message: String, ex: Option[Exception] = None): Result[S] = StateT[ErrorEither, ParseState, S] { (s: ParseState) =>
//      val x: ErrorEither[(ParseState, S)] = Left[Error, (ParseState, S)](Error(message, ex, s))
//      x
//    }
//
//    def successF[S](s: S, sfn: ParseState => ParseState = identity): Result[S] = StateT[ErrorEither, ParseState, S] { (ps: ParseState) =>
//      val x: ErrorEither[(ParseState, S)] = Right[Error, (ParseState, S)]((sfn(ps), s))
//      x
//    }
//
//    def failure[S](error: Error): ErrorEither[(ParseState, S)] = {
//      val x: ErrorEither[(ParseState, S)] = Left[Error, (ParseState, S)](error)
//      x
//    }
//
//    def success[S](ps: ParseState)(s: S): ErrorEither[(ParseState, S)] = {
//      val x: ErrorEither[(ParseState, S)] = Right[Error, (ParseState, S)]((ps, s))
//      x
//    }
//
//  }
//
//  // Result type for parse
////  type Result[S] = Either[(String, Option[Exception]), ResultState[S]]
//
//
//  type ErrorEither[S] = Either[Error, S]
//  type Result[S] = StateT[ErrorEither, ParseState, S]
//
////  type Result[S] = Either[(String, Option[Exception]), S]
//
//  // Parsers for the different wrapped neo4j objects
////  type NodeParser[S] = WrappedNode => Result[S]
//  type AtomParser[S] = WrappedAtom => Result[S]
////  type MoleculeParser[S] = WrappedMolecule => Result[S]
//  type RecordParser[S] = WrappedRecord => Result[S]
//
//  type RecordsParser[S] = List[WrappedRecord] => Result[S]
//
////  implicit class NodeGetDirect(wn: WrappedNode) {
////    def get[S](field: String)(implicit atomParser: AtomParser[S]): Result[S] = for {
////      atom <- wn.getAtom(field)
////      value <- atomParser(atom)
////    } yield value
////
////    def getList[S](field: String)(implicit atomParser: AtomParser[S]): Result[List[S]] = for {
////      atoms <- wn.getAtoms(field)
////      value <- atoms.map(atomParser)
////    } yield value
////  }
//
//  object Implicits {
//    // Convert a node parser to a molecule parser
////    implicit def moleculeFromNodeI[S](implicit np: NodeParser[S]): MoleculeParser[S] = moleculeFromNode(np)
////    implicit def recordFromNodeI[S](implicit np: NodeParser[S]): MoleculeParser[S] = moleculeFromNode(np)
//
//    // Optionally match what the input parser matches
////    implicit def optionalI[S](implicit s: MoleculeParser[S]): MoleculeParser[Option[S]] = optional(s)
//    implicit def optionalI[S](implicit s: RecordParser[S]): RecordParser[Option[S]] =
//      optional(s)
//
//    // Combine two molecule parsers
////    implicit def twoI[S, T](implicit
////      s: MoleculeParser[S],
////      t: MoleculeParser[T]
////    ): MoleculeParser[(S, T)] = two(s, t)
//    def twoI[S, T](name: String)(implicit
//      s: RecordParser[S],
//      t: RecordParser[T]
//    ): RecordParser[(S, T)] = two(name)(s, t)
//
////    implicit def threeI[S, T, U](implicit
////      s: MoleculeParser[S], t: MoleculeParser[T], u: MoleculeParser[U]
////    ): MoleculeParser[(S, T, U)] = three(s, t, u)
//    def threeI[S, T, U](name: String)(implicit
//      s: RecordParser[S], t: RecordParser[T], u: RecordParser[U]
//    ): RecordParser[(S, T, U)] = three(name)(s, t, u)
//
//  }
//
////  def tryCatchC[S](fn: => S)(error: String): Result[S] = tryCatch(fn, error)
//
//  // Util for implementations to catch neo4j conversion errors
////  def tryCatch[S](fn: => S, error: String): Result[S] = {
////    try {
////      Right(fn)
////    } catch {
////      case e: Exception => Left((error, Some(e)))
////    }
////  }
//
//  def tryCatch[S](fn: => S, error: String, sFn: ParseState => ParseState = identity): Result[S] = {
//    StateT  { (state: ParseState) =>
//      try {
//        Result.success(sFn(state))(fn)
//      } catch {
//        case e: Exception =>
//          Result.failure(Error(error, Some(e), state))
//      }
//    }
//  }
//
//  // Convert a node parser to a molecule parser
////  def moleculeFromNode[S](np: NodeParser[S]): MoleculeParser[S] = { molecule =>
////    for {
////      n <- molecule.asNode
////      n2 <- np(n)
////    } yield n2
////  }
//
//  // Optionally match what the input parser matches
//  def optional[S](s: RecordParser[S]): RecordParser[Option[S]] = { molecule =>
////  def optional[S](s: MoleculeParser[S]): MoleculeParser[Option[S]] = { molecule =>
//    if (molecule.nonNull)
//      s(molecule).map((v: S) => Some(v))
//    else
//      Result.successF(None, (v: ParseState) => v.appendAction(s"Got optional from $molecule"))
//  }
//
//  // Combine two molecule parsers
//  def two[S, T](name: String)(
//    s: RecordParser[S],
//    t: RecordParser[T]
//  ): RecordParser[(S, T)] = { molecule =>
//    for {
//      items <- molecule.getRecords(name) //.asMolecules
//
//      _ = pprint.pprintln(items)
//
//      _ <-
//        if (items.size < 2)       Result.failureF(s"Fewer than 2 items (${items.size}) in:\n$items", None)
//        else if (items.size > 2)  Result.failureF(s"More than 2 items in $items", None)
//        else                      Result.successF((), _.appendAction("Correct number of times"))
//
//      si <- s(items(0))
//      ti <- t(items(1))
//    } yield (si, ti)
//  }
//
//  // Combine three molecule parsers
////  def three[S, T, U](
////    s: MoleculeParser[S],
////    t: MoleculeParser[T],
////    u: MoleculeParser[U]
////  ): MoleculeParser[(S, T, U)] = { molecule =>
////    for {
////      items <- molecule.asMolecules
////      _ <-
////        if (items.size < 3)       Result.failureF("Fewer than 3 items", None)
////        else if (items.size > 3)  Result.failureF("More than 3 items", None)
////        else                      Result.successF((), _.appendAction("Correct number of times"))
////
////      si <- s(items(0))
////      ti <- t(items(1))
////      ui <- u(items(2))
////    } yield (si, ti, ui)
////  }
//  def three[S, T, U](name: String)(
//    s: RecordParser[S],
//    t: RecordParser[T],
//    u: RecordParser[U]
//  ): RecordParser[(S, T, U)] = { molecule =>
//    for {
//      items <- molecule.getRecords(name)
//      _ <-
//        if (items.size < 3)       Result.failureF("Fewer than 3 items", None)
//        else if (items.size > 3)  Result.failureF("More than 3 items", None)
//        else                      Result.successF((), _.appendAction("Correct number of times"))
//
//      si <- s(items(0))
//      ti <- t(items(1))
//      ui <- u(items(2))
//    } yield (si, ti, ui)
//  }
//
//  def parseRecords[S](alias: String, parser: NodeParser[S]): List[Record] => Result[List[S]] = { (records: List[Record]) =>
//    val p: RecordParser[S] = parseNodeList(alias, parser)
//    records.map(r => p(WrappedRecordImpl(r))).sequence[Result, S]
//  }
//
//  // Make a record parser from node parser
////  def parseNodeList[S](alias: String, parser: NodeParser[S]): RecordParser[List[S]] = { record =>
//  def parseNodeList[S](alias: String, parser: NodeParser[S]): RecordParser[S] = { record =>
//    for {
////      nodes <- record.getNodes(alias)
//      nodes <- record.getNode(alias)
////      result <- nodes.map(parser).sequence[Result, S]
//      result <- parser(nodes)
//    } yield result
//    ///record.getNodes(alias).flatMap().sequence[Result, WrappedNode]
//  }
//}
//
//sealed trait ParseN4j
//
//// A wrapped Record, which may contain WrappedNode(s), WrappedMolecule(s) or WrappedAtom(s)
//trait WrappedRecord extends ParseN4j {
//  def nonNull: Boolean
//
//
//  //  def getMoleculesAs[S](name: String)(implicit moleculeParser: MoleculeParser[S]): Result[List[S]] =
////    getMolecules(name).flatMap(_.map(moleculeParser).sequence[Result, S])
//
////  def getNodesAs[S](name: String)(implicit nodeParser: NodeParser[S]): Result[List[S]] =
////    getNodes(name).flatMap(_.map(nodeParser).sequence[Result, S])
////
////  def getNodeAs[S](name: String)(implicit nodeParser: NodeParser[S]): Result[S] =
////    getNode(name).flatMap(nodeParser)
//
//  def getRecordsAs[S](name: String)(implicit nodeParser: RecordParser[S]): Result[List[S]] =
//    getRecords(name).flatMap(_.map(nodeParser).sequence[Result, S])
//
//  def getNodeAs[S](name: String)(implicit nodeParser: RecordParser[S]): Result[S] =
//    getRecord(name).flatMap(nodeParser)
//
//  def getAtomAs[S](name: String)(implicit atomParser: AtomParser[S]): Result[S] =
//    getAtom(name).flatMap(atomParser)
//
////  def getNode(name: String): Result[WrappedNode]
////  def getNodes(name: String): Result[List[WrappedNode]]
//
//  def getRecord(name: String): Result[WrappedRecord]
//  def getRecords(name: String): Result[List[WrappedRecord]]
//
//  def getAtom(name: String): Result[WrappedAtom]
//  def getAtoms(name: String): Result[List[WrappedAtom]]
//
//
////  def getMolecule(name: String): Result[WrappedMolecule]
////  def getMolecules(name: String): Result[List[WrappedMolecule]]
//}
//
//// A wrapped Node, which may contain atomic values or lists of atomic values
////trait WrappedNode extends ParseN4j {
////  def getAtomAs[S](field: String)(implicit atomParser: AtomParser[S]): Result[S] = for {
////    atom  <- getAtom(field)
////    value <- atomParser(atom)
////  } yield value
////
////  def getAtomsAs[S](field: String)(implicit atomParser: AtomParser[S]): Result[List[S]] = for {
////    atoms <- getAtoms(field)
////    value <- atoms.map(atomParser).sequence
////  } yield value
////
////  def getAtom(name: String): Result[WrappedAtom]
////  def getAtoms(name: String): Result[List[WrappedAtom]]
////}
//
//// A wrapped Value that can be either a node, an atomic value or a list of Values
////trait WrappedMolecule extends ParseN4j {
////  def nonNull: Boolean
////
////  def asNode: Result[WrappedNode]
////  def asNodes: Result[List[WrappedNode]]
////
////  def asAtom: Result[WrappedAtom]
////  def asAtoms: Result[List[WrappedAtom]]
////
////  def asMolecule: Result[WrappedMolecule]
////  def asMolecules: Result[List[WrappedMolecule]]
////}
//
//// A wrapped Value that is an atomic value; lists of values come from the wrapped parent via a plural (ends in s) method
//trait WrappedAtom extends ParseN4j {
//  def as[S](implicit atomParser: AtomParser[S]): Result[S] = atomParser(this)
//
//  def value: Value
//}
