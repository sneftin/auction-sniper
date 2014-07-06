package com.wix.slava.sniper

import javax.swing.event.{TableModelEvent, TableModelListener}

import com.wix.slava.sniper.ui.{Column, SnipersTableModel}
import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import org.specs2.mutable.{Before, Specification}
import org.specs2.specification.Scope

class SnipersTableModelTest extends Specification with Mockito {

  trait Context extends Scope with Before {
    val model = new SnipersTableModel
    val listener = mock[TableModelListener]

    override def before {
      model.addTableModelListener(listener)
    }
  }

  "SniperModel" should {
    "have enough columns" in new Context {
      model.getColumnCount must be equalTo Column.values.size
    }
  }

  "SniperModel" should {
    "set up column headings" in new Context {
      for (col <- Column.values) {
        model.getColumnName(col.id) must be equalTo (Column.ColumnTitles(col))
      }
    }
  }

  def withEventOnRow(row: Int): Matcher[TableModelEvent] =
    ((_: TableModelEvent).getFirstRow == row, s"hasn't not correct row $row")

  "SniperModel" should {
    "set sniper values in columns" in new Context {
      val joining = SniperSnapshot.joining("item id")
      val bidding = joining.bidding(555, 666)
      model.addSniper(joining)
      model.sniperStateChanged(bidding)

      assertRowMatchesSnapshot(model, 0, bidding)
      //TODO: why one is not working
      there was atLeastOne(listener).tableChanged(withEventOnRow(0))
    }
  }

  "SniperModel" should {
    "notify listeners when sniper added" in new Context {
      val joining = SniperSnapshot.joining("item123")
      model.getRowCount must be equalTo 0
      model.addSniper(joining)
      model.getRowCount must be equalTo 1
      assertRowMatchesSnapshot(model, 0, joining)

      there was one(listener).tableChanged(any[TableModelEvent])

      //TODO: is it correct was to make work
      //there was one(listener).tableChanged(new TableModelEvent(model, 0))

      there was one(listener).tableChanged(withEventOnRow(0))
    }
  }

  "SniperModel" should {
    "hold snipers in addition order" in new Context {

      model.addSniper(SniperSnapshot.joining("item 0"))
      model.addSniper(SniperSnapshot.joining("item 1"))

      model.getValueAt(0, Column.ItemIdentifier.id) must be equalTo ("item 0")
      model.getValueAt(1, Column.ItemIdentifier.id) must be equalTo ("item 1")
    }
  }

  "SniperModel" should {
    "update correct row for sniper" in new Context {

      model.addSniper(SniperSnapshot.joining("item 0"))

      val joining = SniperSnapshot.joining("item 1")
      model.addSniper(joining)
      val bidding = joining.bidding(1,2)

      model.sniperStateChanged(bidding)
      assertRowMatchesSnapshot(model, 1, bidding)
      there was one(listener).tableChanged(withEventOnRow(1))
    }
  }

  "SniperModel" should {
    "throw defect when if no existing sniper for update" in new Context {

      model.sniperStateChanged( SniperSnapshot.joining("item 1")) must throwA[IllegalArgumentException]
    }
  }

  private def assertRowMatchesSnapshot(model:SnipersTableModel, rowIndex: Int, snapshot: SniperSnapshot) {
    model.getValueAt(rowIndex, Column.ItemIdentifier.id) must be equalTo snapshot.itemId
    model.getValueAt(rowIndex, Column.LastPrice.id) must be equalTo snapshot.lastPrice.toString
    model.getValueAt(rowIndex, Column.LastBid.id) must be equalTo snapshot.lastBid.toString
    model.getValueAt(rowIndex, Column.SniperStatus.id) must be equalTo snapshot.state.toString
  }
}
