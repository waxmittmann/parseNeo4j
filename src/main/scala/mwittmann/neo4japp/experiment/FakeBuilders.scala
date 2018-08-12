package mwittmann.neo4japp.experiment

import java.util.UUID

import mwittmann.neo4japp.experiment.FakeN4j._
import mwittmann.neo4japp.experiment.Realistic.{Artifact, BindingKey, BindingValue, FileData, Instance}
import mwittmann.neo4japp.parsewitherror.ParseN4j.RecordParser
import mwittmann.neo4japp.parsewitherror.WrappedRecord

object FakeBuilders {

  def fakeArtifactNode =
    FakeWrappedNode(
      atom = Map(
        "uid"         -> FakeWrappedUUID(UUID.randomUUID()),
        "name"        -> FakeWrappedString("blah")
      ),
      atoms = Map(
        "attributes"  -> List(FakeWrappedString("attr1"), FakeWrappedString("attr2"))
      )
    )

  def fakeFileDataNode =
    FakeWrappedNode(
      atom = Map(
        "uid"         -> FakeWrappedUUID(UUID.randomUUID()),
        "size"        -> FakeWrappedLong(334l),
        "hashcode"    -> FakeWrappedString("er3244ds")
      )
    )

  def fakeBindingKeyNode =
    FakeWrappedNode(
      atom = Map(
        "uid"         -> FakeWrappedUUID(UUID.randomUUID()),
        "key"    -> FakeWrappedString("er3244ds")
      )
    )

  def fakeBindingValueNode =
    FakeWrappedNode(
      atom = Map(
        "uid"      -> FakeWrappedUUID(UUID.randomUUID()),
        "value"    -> FakeWrappedString("er3244ds")
      )
    )

  def fakeInputs: FakeWrappedMolecule =
    FakeWrappedMolecule.twoAsNodesAndMolecules(
      fakeArtifactNode, fakeFileDataNode//,
      //     molecules = Some(List(fakeArtifactNode, fakeFileDataNode))
    )

  def fakeOutputs: FakeWrappedMolecule =
    FakeWrappedMolecule.nodesAsMolecules(nodes = List(fakeArtifactNode))

  def fakeBindings: FakeWrappedMolecule =
    FakeWrappedMolecule.twoAsNodesAndMolecules(fakeBindingKeyNode, fakeBindingValueNode)

  def fakeInstanceAttrs: FakeWrappedNode =
    FakeWrappedNode(atom = Map(
      "uid" -> FakeWrappedUUID(UUID.randomUUID()),
      "status" -> FakeWrappedString("Good")
    ))

  def fakeInstance =
    FakeWrappedRecord(
      node = Map("i" -> fakeInstanceAttrs),
      molecules = Map(
        "inputs"  -> List(fakeInputs),
        "outputs" -> List(fakeOutputs),
        "bindings"-> List(fakeBindings)
      )
    )
}
