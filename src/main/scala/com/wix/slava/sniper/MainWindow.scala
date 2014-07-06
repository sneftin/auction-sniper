package com.wix.slava.sniper

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{FlowLayout, BorderLayout, Color}
import java.util.EventListener
import javax.swing.border.LineBorder
import javax.swing.table.AbstractTableModel
import javax.swing._


trait UserRequestListener extends EventListener {
  def joinAuction(itemId:String)
}

class MainWindow (snipers:SnipersTableModel) extends JFrame("Auction Sniper") {

  var userRequestListener: UserRequestListener = null

  setName(MainWindow.MAIN_WINDOW_NAME)
  fillContentPane(makeSnipersTable, makeControls)
  pack()
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  setVisible(true)

  def addUserRequestListener(listener: UserRequestListener) {
    userRequestListener = listener
  }

  private def makeSnipersTable : JTable = {
    val t = new JTable(snipers)
    t.setName(MainWindow.SNIPERS_TABLE_NAME)
    return t
  }

  private def fillContentPane(snipersTable:JTable, controlPanel: JPanel) {
    val contentPane = getContentPane

    contentPane.setLayout(new BorderLayout())
    contentPane.add(controlPanel, BorderLayout.PAGE_START)
    contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER)
  }

  private def makeControls : JPanel = {
    val controls = new JPanel(new FlowLayout())
    val itemIdField = new JTextField()
    itemIdField.setColumns(25)
    itemIdField.setName(MainWindow.NEW_ITEM_ID_NAME)
    controls.add(itemIdField)

    val joinAuctionButton = new JButton("Join Auction")
    joinAuctionButton.setName(MainWindow.JOIN_BUTTON_NAME)
    joinAuctionButton.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {
        userRequestListener.joinAuction(itemIdField.getText)
      }
    })
    controls.add(joinAuctionButton)
    controls
  }

  /*private def createLabel(initialText: String): JLabel = {
    val label = new JLabel(initialText)
    label.setName(MainWindow.SNIPER_STATUS_NAME)
    label.setBorder(new LineBorder(Color.BLACK))
    return label
  }
  */
  def sniperStatusChanged(sniperState:SniperSnapshot) {
    snipers.sniperStateChanged(sniperState)
  }


}

class SnipersTableModel extends AbstractTableModel with SniperListener {

  val STARTING_UP = new SniperSnapshot("344", 0, 0, SniperState.Joining)
  val STATUS_TEXT = Array("Joining", "Bidding", "Winning", "Lost", "Won")

  var snapshots = Array[SniperSnapshot]()

  def addSniper(snapshot: SniperSnapshot) {
    val newSnapshots = new Array[SniperSnapshot](snapshots.size+1)
    Array.copy(snapshots, 0, newSnapshots, 0, snapshots.size)
    snapshots = newSnapshots
    snapshots(snapshots.size-1) = snapshot
    fireTableRowsInserted(snapshots.size-1, snapshots.size-1)
  }

  override def getRowCount: Int = snapshots.size
  override def getColumnCount: Int = Column.values.size
  override def getColumnName(colIndex:Int) : String = Column.ColumnTitles(Column(colIndex))
  override def getValueAt(rowIndex: Int, columnIndex: Int)  = {
    val snapshot = snapshots(rowIndex)
    Column(columnIndex) match {
      case Column.ItemIdentifier => snapshot.itemId
      case Column.LastPrice => snapshot.lastPrice.toString
      case Column.LastBid => snapshot.lastBid.toString
      case Column.SniperStatus => textFor(snapshot.state)
    }
    //println("getValueAt called " + snapshot.state + " for " + rowIndex + " " + columnIndex + " ret=" + ret)
    //ret
  }
  private def textFor(state:SniperState.Value) = STATUS_TEXT(state.id)

  override def sniperStateChanged(newSnapshot: SniperSnapshot) {
    val snapshotIndex = snapshots.indexWhere((s:SniperSnapshot) => s.itemId == newSnapshot.itemId)
    if (snapshotIndex == -1)
      throw new IllegalArgumentException("Cannot find match for " + newSnapshot.itemId)
    snapshots(snapshotIndex) = newSnapshot
    fireTableRowsUpdated(0,0)
  }
}

object Column extends Enumeration {
  val ItemIdentifier = Value
  val LastPrice = Value
  val LastBid = Value
  val SniperStatus = Value

  val ColumnTitles = Map(
    ItemIdentifier -> "Item",
    LastPrice-> "Last Price",
    LastBid -> "Last Bid",
    SniperStatus -> "State"
  )
}


object MainWindow {
  val MAIN_WINDOW_NAME = "Auction Sniper"
  val SNIPER_STATUS_NAME = "Status"
  val SNIPERS_TABLE_NAME = "Snipers Table"
  val NEW_ITEM_ID_NAME = "New Item"
  val JOIN_BUTTON_NAME = "Join"
}
