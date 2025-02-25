/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.openide.nodes.AbstractNode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.BasicStroke;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;



import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.table.DefaultTableModel;

import java.sql.*;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.db.sql.visualeditor.Log;

import org.netbeans.modules.db.sql.visualeditor.querymodel.JoinTable;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Column;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Expression;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Value;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Predicate;
import org.netbeans.modules.db.sql.visualeditor.querymodel.And;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Or;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Table;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Where;
import org.netbeans.modules.db.sql.visualeditor.querymodel.SQLQueryFactory;
import org.netbeans.modules.db.sql.visualeditor.querymodel.ExpressionList;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;

import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;

import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author  Sanjay Dhamankar, Jim Davidson
 */
public class QueryBuilderGraphFrame extends JPanel
        implements ActionListener, TableModelListener, ItemListener,
        KeyListener, DropTargetListener, PopupMenuProvider
{
    // Private/package variables

    private static final boolean        DEBUG = false;

    private boolean                     _disableQBGF = false;

    private QueryBuilder                _queryBuilder;

    private DropTarget                  _dropTarget;

    private JDesktopPane                _desktopPane = null;
    private QBGFJPanel                  _canvas = null;
    private QueryBuilderInputTable      _queryBuilderInputTable;
    private DefaultTableModel           _inputTableModel;
    private JEditorPane			_sqlTextArea;
    private DefaultTableModel           _resultTableModel;

    private QBGraphScene		_scene;

    private JScrollPane 		_desktopScrollPane;
    private JViewport 			viewport;
    
    private FrameSelectionListener      _fsl = null;
    // private FrameComponentListener      _fcl = null;
    private ComponentListener           _cl = null;
    private JPopupMenu                  _backgroundPopup;
    private JPopupMenu                  _tableTitlePopup;
    private AddTableDlg                 _addTableDlg = null;
    
    boolean                             _firstTableInserted = false;
//    private Point                       _location;

    private QBNodeComponent		_selectedNode;
    
    // constants used in the getNextLocation
    private static final int            initX = 40;
    private static final int            initY = 40;
    
    private static final int            offsetX = 20;
    private int                         offsetY = 20;
    
    private static final int            MAX_TABLES_IN_A_ROW = 5;
    
    
    private java.util.Random 		randomVal = new java.util.Random();
    
    private JMenuItem                    runQueryMenuItem;
    private JMenuItem                    groupByMenuItem;
    
    
    private boolean                     _checkTableColumnValidity = false;
    
    private boolean                     _inputTableAddCriteria = false;
    
    // Constructor
    
    // Note that this takes as parameter the models that underlie the other three panes.
    // The QBGF could post listeners on each of those panes; the only one currently
    // listened to is inputTableModel
    public QueryBuilderGraphFrame(
	QueryBuilder queryBuilder,
	QueryBuilderInputTable queryBuilderInputTable,
	JEditorPane sqlTextArea,
	DefaultTableModel resultTableModel)
    {
        super(new BorderLayout());
        
	Log.getLogger().entering("QueryBuilderGraphFrame", "constructor"); // NOI18N
        
        _queryBuilder = queryBuilder;
        _queryBuilderInputTable = queryBuilderInputTable;
        _inputTableModel = (DefaultTableModel) _queryBuilderInputTable.getModel();
        _sqlTextArea = sqlTextArea;
        _resultTableModel = resultTableModel;
        
        // Listen for events in the input table (column list)
        // Unfortunately, this gets triggered with every change to the model.
        // We need a way to make a group of changes to the input table, then invoke the
        // listener once at the end.
        _inputTableModel.addTableModelListener(this);
        
        // Get a list of tables in the DB
        
        JMenu menu, subMenu;
        JMenuItem menuItem;
        
        // Create three listeners
        _fsl = new FrameSelectionListener();
        // _fcl = new FrameComponentListener();
        _cl = new CompListener();
                
        // Create two popup menus
        _backgroundPopup = createBackgroundPopup();
        _tableTitlePopup = createTableTitlePopup();
        
        // Add listener to components that can bring up popup menus.
        // Create a listener that will bring up background menu
        // MouseListener backgroundPopupListener = new BackgroundPopupListener();
        
        // Add it as listener
//        _graph.addMouseListener(backgroundPopupListener);
//         _desktopPane.addMouseListener(backgroundPopupListener);
//         _desktopScrollPane.addMouseListener(backgroundPopupListener);
        
        // Create a new listener for noticing graph selection
//        _graph.addGraphSelectionListener(new GraphSelListener());
        
        // Add the JScrollPane to the QueryBuilderGraphFrame
//        this.add(_desktopScrollPane,BorderLayout.CENTER);
        
        // Make the GraphFrame visible
        setVisible(true);
        
//         _dropTarget = new DropTarget(_queryBuilder.getQueryBuilderPane()._qbSceneView,
// 				     DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
    
    // Set up the links between the QBGraphFrame (old style) and Scene (new style)
    void initScene (QBGraphScene scene, JComponent sceneView) {
	_scene = scene;
	_dropTarget = new DropTarget(sceneView, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    void setDropTarget(Component comp)
    {
	_dropTarget = new DropTarget(comp, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    // Setter for scene
    void setScene (QBGraphScene qbgScene) {
	_scene = qbgScene;
    }
    

     void getCanvasFocus() {
         _canvas.requestFocus(true);
     }
    
    void setQBGFEnabled( boolean value ) {
        _disableQBGF = ! ( value );
        if ( _disableQBGF ) {
            int frameWidth = (int) this.getSize().getWidth();
            int frameHeight = (int) this.getSize().getHeight();
            
//             _canvas.setSize(new Dimension( frameWidth, frameHeight ) );
//             _canvas.updateUI();
//             _desktopPane.revalidate();
        }
    }
    
    void resizeDesktop() {
    }
    
    void setActivatedNode( QueryBuilderInternalFrame currentSelectedFrame ) {
        if ( currentSelectedFrame != null ) {
            TableNode tn = currentSelectedFrame.getNode();
            _queryBuilder.setActivatedNodes(new Node[] {tn});
        } else {
            _queryBuilder.setActivatedNodes(new Node[0]);
        }
        _queryBuilder.activateActions();
    }
    
    class QBGFJPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            if (DEBUG) {
                System.out.println(" paintComponent() called _parseErrorMessage = " + _queryBuilder.getParseErrorMessage() + "\n" ); // NOI18N
            }
            super.paintComponent(g);
            if ( _queryBuilder.getParseErrorMessage() != null ) {
                Graphics2D g2d = (Graphics2D)g;
                paintCenteredText(g2d);
                if (DEBUG) {
                    System.out.println(" paintCenteredText() called " + "\n" ); // NOI18N
                }
            }
        }
        
        /** Paint the "help" text when the page is empty */
        private void paintCenteredText(Graphics2D g) {
            final int PADDING = 5; // Padding around text that's cleared
            final int LINESPACING = 3; // Extra pixels between text lines
            
            // This implementation is slow/inefficient, but since it's only run
            // when the page is empty we know we're not busy
            String text;
            text = _queryBuilder.getParseErrorMessage();
            int textLines = 1;
            if (DEBUG) {
                System.out.println(" paintCenteredText() called _parseErrorMessage = " + _queryBuilder.getParseErrorMessage() + "\n" ); // NOI18N
            }
            for (int i = 0, n = text.length(); i < n; i++) {
                if (text.charAt(i) == '\n') {
                    textLines++;
                }
            }
            
            int width =
                    _desktopScrollPane.getViewport().getWidth();
            int height =
                    _desktopScrollPane.getViewport().getHeight();
            
            this.setSize(new Dimension( width, height ) );
            int center = height/2;
            
            Font font = UIManager.getFont("Label.font"); // NOI18N
            g.setFont(font);
            FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            FontRenderContext frc = g.getFontRenderContext();
            int lineHeight = metrics.getHeight()+LINESPACING;
            
            int top = center-lineHeight*textLines/2;
            
            int minx = width;
            int maxx = 0;
            int nextLine = 0;
            for (int line = 0; line < textLines; line++) {
                int lineEnd = text.indexOf('\n', nextLine);
                String lineText;
                if (lineEnd != -1) {
                    lineText = text.substring(nextLine, lineEnd);
                    nextLine = lineEnd+1;
                } else {
                    lineText = text.substring(nextLine);
                }
                
                Rectangle2D bounds1 = font.getStringBounds(lineText, frc);
                int lx = (width-((int)bounds1.getWidth()))/2;
                if (lx < minx) {
                    minx = lx;
                }
                int xw = lx+(int)bounds1.getWidth();
                if (xw > maxx) {
                    maxx = xw;
                }
            }
            
            
            // Clear background under text
            Color background = null;
            background = getBackground();
            g.setColor(background);
            int miny = top;
            int maxy = top+textLines*lineHeight;
            g.fillRect(minx-PADDING, miny, maxx-minx+2*PADDING, maxy-miny+2*PADDING);
            // Draw text
            g.setColor(java.awt.Color.gray);
            nextLine = 0;
            int y = top+2*PADDING; // XXX change to padding constant
            y += metrics.getHeight() - metrics.getDescent();
            for (int line = 0; line < textLines; line++) {
                int lineEnd = text.indexOf('\n', nextLine);
                String lineText;
                if (lineEnd != -1) {
                    lineText = text.substring(nextLine, lineEnd);
                    nextLine = lineEnd+1;
                } else {
                    lineText = text.substring(nextLine);
                }
                
                Rectangle2D bounds1 = font.getStringBounds(lineText, frc);
                int lx = (width-((int)bounds1.getWidth()))/2;
                
                g.drawString(lineText, lx, y);
                y += lineHeight;
            }
        }
    }
    
    /** Handle the key typed event from the sql text area. */
    public void keyTyped(KeyEvent e) {
        _checkTableColumnValidity = true;
    }
    
    /** Handle the key pressed event. */
    public void keyPressed(KeyEvent e) {
        if ( DEBUG )
            System.out.println(" QBGF : key pressed called. " + "\n" ); // NOI18N
        _checkTableColumnValidity = true;
        if( e.isShiftDown() ) {
            int code = e.getKeyCode();
            switch(code) {
                // diagram pane
                case KeyEvent.VK_F10:
                    _backgroundPopup.show( e.getComponent(), e.getComponent().getX(), e.getComponent().getY() );
                    break;
            }
        }
        _queryBuilder.handleKeyPress(e);
    }
    
    /** Handle the key released event from the sql text area. */
    public void keyReleased(KeyEvent e) {
        _checkTableColumnValidity = true;
    }
    
    public void setTableColumnValidity(boolean value) {
        _checkTableColumnValidity = value;
    }
    
    
    // check the table and column validity only if the user manually
    // changes the sql query.
    public boolean checkTableColumnValidity() {
        return ( _checkTableColumnValidity &&
                 _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().queryChanged() );
    }
    
    // Adding a method to access _graph as per QE request
    
    // Create a popup menu that will appear when the user clicks on the background
    // and similar places.
    JPopupMenu createBackgroundPopup() {
        
        JPopupMenu backgroundPopup;
        JMenu menu, subMenu;
        JMenuItem menuItem;
        JMenuItem subMenuItem;
        
        //Create the popup menu.
        backgroundPopup = new JPopupMenu();
        
        runQueryMenuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderGraphFrame.class, "RUN_QUERY"));       // NOI18N
        runQueryMenuItem.addActionListener(this);
        backgroundPopup.add(runQueryMenuItem);
        
        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderGraphFrame.class, "Add_Table"));       // NOI18N
        menuItem.addActionListener(this);
        backgroundPopup.add(menuItem);
        
        groupByMenuItem =
                new JCheckBoxMenuItem(NbBundle.getMessage(QueryBuilderGraphFrame.class, "GROUP_BY"));       // NOI18N
        groupByMenuItem.addItemListener(this);
        backgroundPopup.add(groupByMenuItem);
        
        return (backgroundPopup);
    }
    
    // Create a menu that will appear when the user clicks on the title bar of a node (table)
    
    JPopupMenu createTableTitlePopup() {
        
        JPopupMenu tableTitlePopup;
        JMenu menu, subMenu;
        JMenuItem menuItem;
        JMenuItem subMenuItem;
        
        // Create the popup menu.
        tableTitlePopup = new JPopupMenu();
        
        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderGraphFrame.class, "REMOVE_FROM_QUERY"));       // NOI18N
        menuItem.addActionListener(this);
        tableTitlePopup.add(menuItem);
        
        return (tableTitlePopup);
    }
    
    // A class for listeners that will bring up the TableTitlePopup menu.
    // An instance of this class will listen on every new InternalFrame
    // (table node)
    
    class TableTitlePopupListener extends MouseAdapter {
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger() && e.getComponent().isEnabled()) {
                _tableTitlePopup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }
    
    // Specified by PopupMenuProvider
    public JPopupMenu getPopupMenu (Widget widget, Point localLocation) {
	if (widget instanceof QBGraphScene) 
		return _backgroundPopup;
	else {
	    // We have a Widget wrapping a QBNodeComponent
	    QBNodeComponent qbNC = (QBNodeComponent)_scene.findObject(widget);
	    this._selectedNode = qbNC;
	    return _tableTitlePopup;
	}
    }


    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf("."); // NOI18N
        return classString.substring(dotIndex+1);
    }
    
    // Specified by TableModelListener.
    // Invoked when a TableModel generates an event; implemented by JTable.
    // Events come from both QueryBuilderTableModel and
    // QueryBuilderInputTableModel,
    // The QueryBuilderTableModel indicates columns that are
    // selected/deselected; that in turn
    // causes them to be added/dropped from the InputTable
    
    public void tableChanged(TableModelEvent e) {
        
        // if the graph is disabled, do not handle any events.
            if ( _disableQBGF )
	    return;
        
        // if the graph is being generated from model, do not handle events.
        if (_queryBuilder._updateModel == false)
	    return;
        
        Log.getLogger().finest("Entering QBGF.tableChanged, source: " + e.getSource()); // NOI18N

        if (e.getSource() instanceof QueryBuilderTableModel)
            tableModelChanged(e);
        
        else if (e.getSource() instanceof QueryBuilderInputTableModel)
            inputTableModelChanged(e);
    }
    
    
    // Called when we have received a change in a graph node
    
    private void tableModelChanged(TableModelEvent e) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "tableModelChanged");

        // We have a mouse click inside a graph table node, indicating select/deselect.
        // Propagate the information to the input table
        
        // Extract some information from the event
        int row = e.getFirstRow();   // the first row that changed
        int column = e.getColumn();  // the column for this event
        
        QueryBuilderTableModel model = (QueryBuilderTableModel) e.getSource();
        String tableSpec = model.getTableSpec();
        
        // DB column name
        String columnName = (String) model.getValueAt(row, column+2);
        
        // boolean - Selected/deselected
        Object value = model.getValueAt(row, column);
        
        if (value==Boolean.TRUE) {      // A column has been selected
            
            // Update the query model if appropriate
            // Do this first so that it's available when adding the row
            if (_queryBuilder._updateModel) {
                _queryBuilder.getQueryModel().addColumn(tableSpec, columnName);
                _queryBuilderInputTable.selectColumn(tableSpec, columnName, Boolean.TRUE);
            }
        }
        
        else if (value==Boolean.FALSE) { // A column has been deselected
            
            // Update the query model, if we're not being driven by it
            // Do this before updating the grid, because we use the model to generate sortorder
            if (_queryBuilder._updateModel) {
                _queryBuilder.getQueryModel().removeColumn(tableSpec, columnName); }
            
            // do not remove the whole row, just deselect the output column.
            _queryBuilderInputTable.selectColumn(tableSpec, columnName, Boolean.FALSE);
        }
        
        // We used to update the text query after every event.  That
        // caused degraded performance.  Now, we check whether we've
        // received a real event, or we're generating the graph as a
        // batch operation.  Also, we trigger only on TableModel events,
        // so InputTableMode must explicitly invoke
        if (_queryBuilder._updateText) {
            // An interactive event -- update the text query
            _queryBuilder.generateText();
        }
    }
    
    
    /**
     * Called when we have received a change in the input table (grid pane)
     * either interactively or because the model was updated from somewhere else
     * @param e the event
     */
    private void inputTableModelChanged(TableModelEvent e) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "inputTableModelChanged");
        
        // if _inputTableAddCriteria is true we should not handle any
        // events. This is set when the events are not directly generated
        // by the user interaction. e.g. when we set value of say
        // Criteria column, CriteriaOrder column will be set a value.
        // To avoid the recursive calls this is used.
        if (_inputTableAddCriteria)
            return;
        
        // Only pay attention to changes to the output column
        // Propagate information to the graph node for this table
        
        // ** We could also do this by listening to the checkbox for
        // the particular column in QueryBuilderInputTable, the same
        // as we do for SortType, SortOrder, and AddCriteria **
        
        // Extract some information from the event, and dispatch on column
        int column = e.getColumn();  // the column for this event
        int row = e.getFirstRow();   // the first row that changed
        QueryBuilderInputTableModel model = (QueryBuilderInputTableModel) e.getSource();
        
        // Column -1 indicates ...
        if (column!=-1) {
            
            String columnName = (String) model.getValueAt(row, QueryBuilderInputTable.Column_COLUMN);
            String tableSpec =  (String) model.getValueAt(row, QueryBuilderInputTable.Table_COLUMN);
            
            if (DEBUG)
                System.out.println("QBGF.iTMC, row: "+row + " columnName: "+columnName + // NOI18N
                        " column: "+column);                                  // NOI18N
            
            if (column==QueryBuilderInputTable.Alias_COLUMN) {
                
                String result = ((String) model.getValueAt(row,
                        QueryBuilderInputTable.Alias_COLUMN )).trim();
                if ( result == null || result.length() == 0 )   // Clear alias
                    _queryBuilder.getQueryModel().removeDerivedColName( tableSpec, columnName );
                else
                    _queryBuilder.getQueryModel().setDerivedColName(tableSpec, columnName, result);
            }
            if (column==QueryBuilderInputTable.Output_COLUMN) {
                
                Boolean value = (Boolean) model.getValueAt(row, column);  // Selected/deselected
		QBNodeComponent node = findGraphNode(tableSpec);
                QueryBuilderTableModel qbtm = node.getQueryBuilderTableModel();
                qbtm.selectColumn(columnName, value);
                // Don't regenerate query, since selectColumn will cause that to happen
                return;
            } else if (column==QueryBuilderInputTable.Criteria_COLUMN) {
                
                String result = ((String) model.getValueAt(row,
                        QueryBuilderInputTable.Criteria_COLUMN )).trim();
                
                // The the value is "" remove the criteria order from the combo box
                if ( result != null && result.length() == 0 ) {
                    // The following lines appear to be unnecessary
                    _inputTableAddCriteria = true;
                    model.setValueAt("", row,QueryBuilderInputTable.CriteriaOrder_COLUMN); // NOI18N
                    _inputTableAddCriteria = false;
                    _queryBuilder.getQueryModel().removeCriteria( tableSpec, columnName, 1 );
                } else if ( result.trim().equals(QueryBuilderInputTable.Criteria_Uneditable_String)) {
                    return;
                } else {
                    Predicate pred = checkCriteria( tableSpec, columnName, result ) ;
                    if ( pred == null )  {
                        _queryBuilderInputTable.getModel().setValueAt("",row, column);      // NOI18N
                        return;
                    }
                    
                    int criteriaCount=
                            _queryBuilder.getQueryModel().getCriteriaCount();
                    String order = (String)model.getValueAt(row,
                            QueryBuilderInputTable.CriteriaOrder_COLUMN);
                    if (order != null && order.trim().length() !=0 &&
                            order.trim().equals(QueryBuilderInputTable.CriteriaOrder_Uneditable_String)) {
                        return;
                    }
                    int orderNum = ((order == null) || (order.trim().length()==0))
                    ? criteriaCount+1
                            : Integer.parseInt(order);
                    _inputTableAddCriteria = true;
                    model.setValueAt(Integer.valueOf(orderNum).toString(), row,
                            QueryBuilderInputTable.CriteriaOrder_COLUMN);
                    _inputTableAddCriteria = false;
                    if ( orderNum < criteriaCount+1 ) {
                        _queryBuilder.getQueryModel().
                                replaceCriteria(tableSpec, columnName, pred, orderNum);
                    } else {
                        _queryBuilder.getQueryModel().
                                addCriteria(tableSpec, columnName, pred);
                    }
                }
                _queryBuilderInputTable.clearSelection();
                _inputTableAddCriteria = true;
                // Regenerate the input table, based on the latest change.
                // Is this necessary?  May be required to get Criteria Order right
                _queryBuilderInputTable.generateTableWhere(_queryBuilder.getQueryModel());
                _inputTableAddCriteria = false;
            } else if (column==QueryBuilderInputTable.CriteriaOrder_COLUMN) {
                
                String criteria = ( (String) model.getValueAt(row,
                        QueryBuilderInputTable.Criteria_COLUMN ) ).trim();
                String order = (String) model.getValueAt(row,column );
                
                if ( (order != null && order.trim().length() !=0) &&
                     (order.trim().equals(QueryBuilderInputTable.CriteriaOrder_Uneditable_String ) ) ) {
                    return;
                }
                // remove the old criteria first anyway
                _queryBuilder.getQueryModel().removeCriteria( tableSpec, columnName, 1 );
                
                if ((order == null) || (order.trim().length()==0)) {
                    _inputTableAddCriteria = true;
                    model.setValueAt("", row,QueryBuilderInputTable.Criteria_COLUMN); // NOI18N
                    _inputTableAddCriteria = false;
                } else {
                    int orderNum = Integer.parseInt(order);
                    Predicate pred = checkCriteria( tableSpec, columnName, criteria ) ;
                    if ( pred == null )  {
                        _queryBuilderInputTable.getModel().setValueAt("",row, column);      // NOI18N
                        return;
                    }
                    if (criteria.trim().length() != 0 ) {
                        _queryBuilder.getQueryModel().addCriteria(tableSpec, columnName, pred, orderNum);
                    }
                }
                _queryBuilderInputTable.clearSelection();
                _inputTableAddCriteria = true;
                _queryBuilderInputTable.generateTableWhere(_queryBuilder.getQueryModel());
                _inputTableAddCriteria = false;
            }
            // Regenerate the query, if someone else isn't doing it
            if (_queryBuilder._updateText) {
                _queryBuilder.generateText();
            }
        }
    }
    
    
    /**
     * Given a criteria string retrun Predicate or null if result is
     * an error. Displays appropriate message.
     */
    private Predicate checkCriteria( String tableSpec, String columnName, String result ) {
        String op=null;
        String val=null;
        if ( result.startsWith(">=") ) { // NOI18N
            op = ">="; // NOI18N
            val = result.substring(2).trim();
        } else if ( result.startsWith("<=") ) { // NOI18N
            op = "<="; // NOI18N
            val = result.substring(2).trim();
        } else if ( result.startsWith("<>") ) { // NOI18N
            op = "<>"; // NOI18N
            val = result.substring(2).trim();
        } else if ( result.startsWith(">") ) { // NOI18N
            op = ">"; // NOI18N
            val = result.substring(1).trim();
        } else if ( result.startsWith("<") ) { // NOI18N
            op = "<"; // NOI18N
            val = result.substring(1).trim();
        } else if ( result.startsWith("=") ) { // NOI18N
            op = "="; // NOI18N
            val = result.substring(1).trim();
        } else if ( result.toUpperCase().startsWith("LIKE" ) ) { // NOI18N
            op = " LIKE "; // NOI18N
            val = result.substring(5).trim();
        } else if ( result.toUpperCase().startsWith("IN" ) ) { // NOI18N
            op = " IN "; // NOI18N
            val = result.substring(3).trim();
        }
        
        // if val or op is still null
        // display an error and clear the cell and return.
        if ( op == null || (op.trim().length() == 0) || val == null || (val.trim().length() == 0 ) ) {
            String msg = NbBundle.getMessage(QueryBuilderGraphFrame.class, "CRITERIA_ERROR");     // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE);  // NOI18N
            DialogDisplayer.getDefault().notify(d);
            _queryBuilderInputTable.clearSelection();
            return null;
        }
        
        Column col1 = SQLQueryFactory.createColumn(tableSpec, columnName);
        Predicate pred = SQLQueryFactory.createPredicate(col1, val, op);
        
        return pred;
    }
    
    public void setCurrentSelectedFrameTitle( String title ) {
        if ((QueryBuilderInternalFrame)_desktopPane.getSelectedFrame() != null )
            ((QueryBuilderInternalFrame)_desktopPane.getSelectedFrame()).setTitle(title);
    }
    
    // Add a node (representing a database table) to the query graph
    // If _updateModel is true, add edges corresponding to any FK relationships
    // ToDo: Add provisions for selecting only some of the columns in the table
    // for inclusion in the query.
    private void insertTableInteractively(String fullTableName) {
	Log.getLogger().entering("QueryBuilderGraphFrame", "insertTableInteractively", fullTableName); // NOI18N
        
        // fix for 6316681 Opening QE on a rowset where command=null throws NPE
        // If the query model is null then the QueryBuilder was not opened
        // before. So for the initial table populate the command
        // This will initiate the QueryBuilder properly.
        if ( ( _queryBuilder.getQueryModel() == null ) ||
	     ( _queryBuilder.getQueryModel().genText() == null ) ) {
            String query = "select * from " + fullTableName;
            _queryBuilder.populate(query, false);
            _queryBuilder.setSqlCommand(query);
            return;
        }
        
        // Disable text query re-generation until we're ready
        QueryBuilder.showBusyCursor( true );
        _queryBuilder._updateText=false;
        try {
            // Extend to handle full tablespec, including schema
            String schemaName=null, tableName, tabName;
            String[] res = fullTableName.split("\\.");      // NOI18N
            if (res.length>1) {
                schemaName=res[0];
                tabName=res[1];
            } else
                tabName=fullTableName;
            
            /*
            if (  !tabName.startsWith("\"") && (tabName.indexOf(' ') != -1) ) {
                tableName = new String ("\"" + tabName + "\"" );
            }
            else {
             */
            tableName = new String(tabName);
            /*
             }
             */
            
            // Create the querymodel object representing the table to be added
            String corrName=_queryBuilder.getQueryModel().genUniqueName(fullTableName);
            Table tbl = (corrName==null) ?
                SQLQueryFactory.createTable(tableName, null, schemaName) :
                SQLQueryFactory.createTable(tableName, corrName, schemaName);
            JoinTable joinTable=SQLQueryFactory.createJoinTable(tbl);
            
            List<String> columnNames = new ArrayList<>();
            columnNames.add("*"); // NOI18N
            
            // Insert the table into the model first, so that column insertions can refer to it
            _queryBuilder.getQueryModel().insertTable(joinTable);
            
            // Insert the node into the graph
	    QBNodeComponent qbNC = insertTable(joinTable, columnNames);
//             if ( internalFrame == null ) {
//                 QueryBuilder.showBusyCursor( false );
//                 return;
//             }
            
            // Add appropriate edges in the graph to connect the new node to previous ones,
            // based entirely on relationships (Foreign Key constraints)
            // This should be refactored, to do computation of edges separately
            List edges = insertFKEdges(qbNC, fullTableName);

            // Add the relationships for this table into the model, based in the edges
            _queryBuilder.getQueryModel().addRelationships(joinTable, edges);
            
            // Redraw the new frame.  Must be done after adding edges, to work around
            // some painting glitches.
            // redrawFrame(currentSelectedFrame);
//            redrawFrame(internalFrame);
            
//             try {
//                 // Make the new frame the selected one
// //                internalFrame.setSelected(true);
//                 // Update the [0,0] cell for the table model; wasn't this already false?
//                 // This will cause an event to fire, but it's not clear what the intent was.
// //                internalFrame.getQueryBuilderTableModel().setValueAt(Boolean.FALSE, 0, 0);
// //                _desktopPane.setSelectedFrame(internalFrame);
// //                setActivatedNode( internalFrame ) ;
//             } catch(PropertyVetoException pve) {
//             }
            
            _firstTableInserted = true;
        } finally {
            _queryBuilder._updateText=true;
            QueryBuilder.showBusyCursor( false );
        }
    }
    
    
    // Add a node to the query graph, based on the parsed query
    // ToDo: Generalize this to support joinTable with conjoined predicates
    
    void insertTableFromModel(JoinTable joinTable, List columnNames) {

        Log.getLogger().entering("QueryBuilderGraphFrame", "insertTableFromModel", joinTable.getTableSpec()); // NOI18N
        
        // Save the state of _updateText
        boolean updateText = _queryBuilder._updateText;
        _queryBuilder._updateText=false;
        try {
	    QBNodeComponent qbNC = insertTable(joinTable, columnNames);
            if ( qbNC == null ) return;
            
            // Insert the edges that are explicitly specified in the query
            insertJoinEdges(joinTable);
            
//            redrawFrame(internalFrame);
            
            // ToDo: This code used to be called twice.  Decide whether there was a reason for it.
//             try {
//                 // Make the new frame the selected one
// //                internalFrame.setSelected(true);
//                 // Update the [0,0] cell for the table model; wasn't this already false?
//                 // This will cause an event to fire, but it's not clear what the intent was.
// //                internalFrame.getQueryBuilderTableModel().setValueAt(Boolean.FALSE, 0, 0);
// //                _desktopPane.setSelectedFrame(internalFrame);
//                 _queryBuilder.enableDelete();
//                 runQueryMenuItem.setEnabled(true);
//                 groupByMenuItem.setEnabled(true);
//             } catch(PropertyVetoException pve) {
//             }
            
            _firstTableInserted = true;
        } finally {
            _queryBuilder._updateText=updateText;
        }
    }
    
    
    // Insert a table into the query graph
    
    QBNodeComponent insertTable(JoinTable joinTable, List columnNames) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "insertTable", new Object[] { joinTable, columnNames }); // NOI18N
        
        // you can not insert a non-existent table.
        String joinTableName = joinTable.getFullTableName();
        
        // Create the internal frame
	QBNodeComponent qbNC = createNode(joinTable, columnNames);
        
//         // Add a listener to the title bar (or outside edge) of the new frame
//         MouseListener tableTitlePopupListener = new TableTitlePopupListener();
//         // trying to handle different LNFs
//         try {
//             if (!System.getProperty("os.name").startsWith("Mac OS")) // NOI18N
//                 ((BasicInternalFrameUI)internalFrame.getUI()).getNorthPane().addMouseListener(tableTitlePopupListener);
//             else
//                 internalFrame.addMouseListener(tableTitlePopupListener);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
        
// 	// Hack - add a listener on the QBNodeComponent, which will replace the internal frame
// 	// _tmpLastInsertedGraphNode.addMouseListener(tableTitlePopupListener);

//         // Insert the cell, without any edges
//         Object insertCells[] = new Object[] {insertCell};
//         _graphModel.insert(insertCells,null,null,null,null);
        
//         // Add listeners that notice when the frame is activated, or moved/resized
//         internalFrame.addInternalFrameListener(_fsl);
//         internalFrame.addComponentListener(_fcl);
        
//         // Add the internal frame to the desktopPane.  Per the JInternalFrame demo this seems to be
//         // necessary (marked VERY IMPORTANT), but needs to be undone if we delete it.
//         _desktopPane.add(internalFrame);
        
        refresh();
        
	return qbNC;
    }
    
    
    // Create an internal frame, which will represent a table in the graph
    // ToDo: Modify this to return a QBNodeComponent
    QBNodeComponent createNode(JoinTable joinTable, List selectColumnNames) {
        
        String fullTableName = joinTable.getFullTableName();
        String[] table = fullTableName.split("\\.");        // NOI18N
        String tableName = (table.length>1) ? table[1] : table[0];
        String corrName = joinTable.getCorrName();
        
        String tableSpec = ((corrName!=null) ? corrName : fullTableName);
        
        Log.getLogger().finest("Entering QBGF.createNode, fullTableName: " + fullTableName + " corrName: " + corrName); // NOI18N
        
        // Correct case of table name...
        try { // TODO JFB shouldn't catch this...
            String newS = _queryBuilder.checkFullTableName(fullTableName) ;
            if ( newS != null & ! fullTableName.equals(newS)) {
                fullTableName = newS ;
                Log.getLogger().finest("  fullTableName corrected to " + fullTableName ) ;
            }
        } catch (SQLException se) {
            Log.getLogger().finest("  fullTableName " + se.getMessage()  ) ;
        }
        
        final String[] columnNames = {"", // "Output",      // NOI18N
				      "", // "KeyIcon"      // NOI18N
				      "" // "Column"        // NOI18N
        };
        
        //Create initial internal frame
        Object [][] dbData ;
        
        List dbColumnNames  ;
        try {
            dbColumnNames = _queryBuilder.getColumnNames(fullTableName);
        } catch(SQLException sqle) {
            dbColumnNames = new ArrayList() ;
        }
        
        // This data will populate the QueryBuilderTableModel, which represents
        // the schema information shown inside the table node
        dbData = new Object[dbColumnNames.size()/* +1 */][3];
        Iterator iterator = dbColumnNames.iterator();
        
        // Primary keys and foreign keys are marked with special icons
        List primaryKeys = null ,foreignKeyCols = null  ;
        try {
            primaryKeys = _queryBuilder.getPrimaryKeys(fullTableName);
            foreignKeyCols = _queryBuilder.getImportedKeyColumns(fullTableName);
        } catch (SQLException sqle) {
            // HACK!  log and dispose
            Log.getLogger().warning("QueryBuilderGraphFrame:  cannot get info " + sqle.getLocalizedMessage()) ;
            primaryKeys = new ArrayList() ;
            foreignKeyCols = new ArrayList() ;
        }
        
        int i = 0;
        
        // Iterate through the column names
        // Put a check by any that are included in the Select clause
        while (iterator.hasNext() && i < dbColumnNames.size() /* +1 */) {
            
            String columnName = new String(iterator.next().toString()) ;
            
            // Mark them all as selected/deselected, based on Select clause
            if (selectColumnNames.contains("*") || selectColumnNames.contains(columnName)) // NOI18N
                dbData[i][0] = Boolean.TRUE;
            else
                dbData[i][0] = Boolean.FALSE;
            
            // Check if this is primary or foreign key and then attach appropriate icon
            // JLabel (String text, Icon image, SwingConstants.LEFT);
            // We used to use toUpperCase() during comparison, but drop that now that we are
            // canonicalizing table/column names.
            if (primaryKeys.contains(columnName.trim())) {
                dbData[i][1] = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/visualeditor/resources/primaryKey.gif", false);
            } else if (foreignKeyCols.contains(columnName.trim())) {
                dbData[i][1] = ImageUtilities.loadImageIcon("org/netbeans/modules/db/sql/visualeditor/resources/foreignKey.gif", false);
            } else {
                dbData[i][1] = null;
            }
            
            dbData[i][2] = columnName;
            _queryBuilderInputTable.addRow(tableSpec, columnName);
            _queryBuilderInputTable.selectColumn(tableSpec, columnName, (Boolean) dbData[i][0]);
            i++;
        }
        
        // Create a model from the column info, and wrap it in a frame
        QueryBuilderTableModel qbtModel =
                new QueryBuilderTableModel(fullTableName, corrName, columnNames, dbData);
        qbtModel.addTableModelListener(this);

        // This action is now incorporated into the initialization loop above
        // We end up doing it again in order to propagate the events
        // Update the internalframe.  By event propagation, will update the input table.
        // ToDo: Replace this with an explicit update to the InputTableModel.
        // This also sets all db columns to display, which is the wrong thing!!!
        // We need some way to update the input table.  fireTableDataChanged will raise
        // an event, but the listener doesn't handle it right.  Instead, explicitly update
        // the table.
        // Add one row to the input table for each column that appears in this node.
        // The current code for adding rows is designed to support interactive mouse clicks,
        // in tableChanged. We may want to break that out into a separate routine for
        // calling from here.
        for (i =0; i < qbtModel.getRowCount(); i++) {
            qbtModel.setValueAt(dbData[i][0], i, 0);
        }
        
        String title = (corrName==null) ? tableName : corrName+": "+tableName; // NOI18N

	// This is specific to the JGraph implementation, now replaced by GraphLib
//         QueryBuilderInternalFrame internalFrame = new QueryBuilderInternalFrame(qbtModel, _queryBuilder);
//         internalFrame.addKeyListener(this);
        
//         // Set the various contents of the internal frame
//         internalFrame.create();
//         internalFrame.setTitle(title);
        
	// TestGraph
	// Do some extra work to create a node in the new graphlib
	Widget widget = _scene.addNode(title, qbtModel);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(this));
	QBNodeComponent qbNC = findComponent(widget);
	widget.setPreferredLocation(getNextGraphLocation(qbNC.getPreferredSize()));
	_scene.validate();

	return qbNC;
    }
    
    /**
     * Returns the next location for placing a node.
     * Right now it uses a standard offset from the previous location.
     */
    Point _gLocation;
    Point getNextGraphLocation (Dimension size) {

        Log.getLogger().entering("QueryBuilderGraphFrame", "getNextGraphLocation", new Object[] { size.getWidth(), size.getHeight() }); // NOI18N
        
        if (!_firstTableInserted ) {
            _gLocation = new Point(initX, initY);
        } else {
            _gLocation =
		new Point(
		    (int)( _gLocation.getX() + size.getWidth() + offsetX + randomVal.nextInt((int)size.getWidth()/2)),
                    (int)( _gLocation.getY() + offsetY + randomVal.nextInt((int)size.getHeight()/2)));
            offsetY *= -1;
            if ( _gLocation.getX() > (int) size.getWidth()*MAX_TABLES_IN_A_ROW ) {
                _gLocation = new Point(initX, (int)_gLocation.getY()+(int)size.getHeight()+2*offsetY);
            }
        }
        return _gLocation;
    }
    
    // Graph: return the NodeComponent associated with this Widget
    QBNodeComponent findComponent(Widget widget) {
	List<Widget> widgets = widget.getChildren();
	for (Widget w : widgets) {
	    if (w instanceof ComponentWidget)
		return (QBNodeComponent)((ComponentWidget)w).getComponent();
	}
	return null;
    }

    // Insert edges that are implied by Foreign Key constraints between existing nodes
    // and the new one.  Return the list of FKs that are used.
    // ToDo: Enable support for multiple edges between a pair of nodes
    
    private List insertFKEdges(QBNodeComponent newNode, String newFullTableName) {
	
        Log.getLogger().entering("QueryBuilderGraphFrame", "insertFKEdges", new Object[] { newNode, newFullTableName }); // NOI18N
        
        // Get foreign key information, for deciding relationship status
        // Do this once, to avoid repeated calls to database
        List foreignKeys ;
        try {
            foreignKeys = _queryBuilder.getForeignKeys(newFullTableName);
        } catch (SQLException sqle) {
            foreignKeys = new ArrayList() ;
        }
        List foreignKeysUsed = new ArrayList();

	// TestGraph
	// Iterate through the nodes in the scene, drawing arcs to the new node as needed

	Collection nodes = _scene.getNodes();
	// Iterate through nodes
	for (Object oldNode : nodes) {

            // Check whether this node is related to the new one
            String[] fk;
	    if (oldNode instanceof QBNodeComponent)	// Make sure it's not a dummy
		// && (root != newCell))                   // how to check this?
            {
                String oldFullTableName=((QBNodeComponent)oldNode).getFullTableName();
                if ((fk=_queryBuilder.findForeignKey(oldFullTableName,newFullTableName,foreignKeys)) != null)
                    // ... and is related to the new cell by an FK?
                {
                    String oldTableSpec=((QBNodeComponent)oldNode).getTableSpec();
                    String newTableSpec=((QBNodeComponent)newNode).getTableSpec();
                    // Insert an edge between the existing cell and the new one
                    // Directionality (old -> new) is just a convention, since we make the choice
                    // fix for 6270428 : querybuilder Issues with SELF JOIN
                    // having self referential integrity (Employee-Manager Scenario)
                    if ( fk[0].equals(fk[2]) ) {
                        fk[0] = oldTableSpec;
                        fk[2] = newTableSpec;
                    }
                    // Default to INNER
                    insertGraphEdge((QBNodeComponent)oldNode, newNode, oldFullTableName, newFullTableName, oldTableSpec,
				    newTableSpec, null, null, fk, "INNER");
                    foreignKeysUsed.add(fk);
                }
            }
        }

	return foreignKeysUsed;
    }
    
    
    // Insert edges connected to this table, which are implied by the join conditions
    // associate with the table in the FROM list
    void insertJoinEdges(JoinTable joinTable) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "insertJoinEdges", joinTable); // NOI18N

        // We're generating the graph from the model.
        // Don't look for new edges to add, but add the edges that are
        // explicitly mentioned in the join condition for this table
        String joinType = joinTable.getJoinType();
        
        // We don't have edges if either
        //   - this is the first table
        //   - this is a cross join
        // Otherwise, there will be an edge
        // Extended 10/22 to support Conditions consisting of conjoined predicates
        
        if ((joinType!=null) && (!joinType.equals("CROSS"))) { // NOI18N
            
            Expression cond = joinTable.getExpression();
            if (cond instanceof Predicate) {
                Predicate pred = (Predicate) cond;
                insertJoinEdge(pred, joinType);
            }
        }
    }
    
    
    // Insert the graph edge corrsponding to this predicate
    void insertJoinEdge(Predicate pred, String joinType) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "insertJoinEdge", new Object[] { pred, joinType }); // NOI18N
        
        Value val1 = pred.getVal1();
        Value val2 = pred.getVal2();
        
        // Only insert an edge if we're comparing columns, rather than literals
        if ((val1 instanceof Column) && (val2 instanceof Column)) {
            
            Column col1=(Column)val1;
            String tableSpec1 = col1.getTableSpec();
            String fullTableName1 = _queryBuilder.getQueryModel().getFullTableName(tableSpec1);
            String colName1 = col1.getColumnName();
	    QBNodeComponent node1 = findGraphNode(tableSpec1);
            
            Column col2=(Column)val2;
            String tableSpec2 = col2.getTableSpec();
            String fullTableName2 = _queryBuilder.getQueryModel().getFullTableName(tableSpec2);
            String colName2 = col2.getColumnName();
	    QBNodeComponent node2 = findGraphNode(tableSpec2);
            
	    // See if there's a foreign key on exactly this combination of tables/columns
	    // If not, we will have no direction label on the join
	    String[] fk = null;
	    try {
		fk = _queryBuilder.findForeignKey(fullTableName1, colName1, fullTableName2, colName2);
	    } catch (SQLException sqle) {
		Log.getLogger().warning("QBGF:  findforeignKey "+sqle.getLocalizedMessage()) ;
	    }
                
	    // Direction is cell1 -> cell2, matching the join
	    // tableName is the table that we are adding
	    insertGraphEdge(node1, node2, fullTableName1, fullTableName2, tableSpec1, tableSpec2,
			    colName1, colName2, fk, joinType);
        }
    }
    
    
    // Insert an edge
    // Like insertEdge, but uses graphLib
    void insertGraphEdge(QBNodeComponent node1, QBNodeComponent node2,
			 String fullTableName1, String fullTableName2,
			 String tableSpec1, String tableSpec2,
			 String colName1, String colName2,
			 String[] fk, String joinType)
    {
	System.out.println("Entering insertGraphEdge, tableSpec1: " + tableSpec1 + "  tableSpec2: " + tableSpec2);

        // Create the node object for the edge (for the Property Sheet)
        AbstractNode an;
        if (colName1==null) {
            // We are adding the edge interactively, based on the FK
            // Use the columns specified in the FK
            if (fullTableName1.equalsIgnoreCase(fk[0]))
                an = createPropertyNode(tableSpec1, fk[1], tableSpec2, fk[3], joinType, _queryBuilder); // NOI18N
            else
                an = createPropertyNode(tableSpec2, fk[1], tableSpec1, fk[3], joinType, _queryBuilder); // NOI18N
        } else {
            // Adding an edge from the model, based on join condition
            an = createPropertyNode(tableSpec1, colName1, tableSpec2, colName2, joinType, _queryBuilder); // NOI18N
        }

	// Now, add an edge to the graph scene, containing the AbstractNode
	Widget widget = _scene.addEdge(an);
	_scene.setEdgeSource (an, node1);
	_scene.setEdgeTarget (an, node2);

	ConnectionWidget connWidget = (ConnectionWidget)widget;
	
	// widget.getActions().addAction(ActionFactory.createSelectAction(new ObjectSelectProvider()));
	connWidget.getActions().addAction(_scene.createSelectAction());

        AnchorShape triangle = AnchorShapeFactory.createTriangleAnchorShape(18, true, false, 17);
        connWidget.setStroke (new BasicStroke (1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
        // If we have an FK, draw the appropriate arrowhead, otherwise don't do anything
        if (fk!=null) {
            if (fk[0].equalsIgnoreCase(fullTableName1)) {
		// ((ConnectionWidget)widget).setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
		connWidget.setTargetAnchorShape (triangle);
            } else {
		connWidget.setSourceAnchorShape (triangle);
            }
        }
        
        // Mark the edge as activated (updates property sheet)
        _queryBuilder.setActivatedNodes(new Node[] {an});
        
	_scene.validate();
    }

    AbstractNode createPropertyNode(String tableSpec1, String fk1, String tableSpec2, String fk3,
            String joinType, QueryBuilder _queryBuilder) {
        if ((joinType==null) || (joinType.equals("")))
            return new CondNode(tableSpec1, fk1, tableSpec2, fk3, _queryBuilder);
        else
            return new JoinNode(tableSpec1, fk1, tableSpec2, fk3, joinType, _queryBuilder);
    }
    
    /**
     * Remove a table (node) from the graph
     */
    void removeTable(QBNodeComponent selectedNode) {

        String tableSpec = selectedNode.getTableSpec();
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "removeTable", tableSpec); // NOI18N

	// Remove the specified node from the GraphLib scene
	// First remove all edges
	Collection edges = _scene.findNodeEdges(selectedNode, true, true);
	for (Object edge : edges) {
	    // Edge is a JoinNode or CondNode
	    _scene.removeEdge(edge);
	}

	// Now remove the node
	_scene.removeNode(selectedNode);

        // Update the input table model, by removing all rows that mention this table
        _queryBuilderInputTable.removeRows(tableSpec);
        
        // Now update the QueryModel
        // Note that we always do this, since the model-driven graph generation never contain deletion
        _queryBuilder.getQueryModel().removeTable(tableSpec);
        
        _queryBuilder.generate();
        
        // update the groupby checkbox menu item.
        setGroupBy(_queryBuilder.getQueryModel().hasGroupBy() );
    }
    
    // Generate the Graph and Table from a query model
    // General algorithm:
    //    - Select clause defines the internal frame (node for each Table)
    //    - From clause defines the structure of the Graph
    //    - Where clause has no effect on Graph, but does affect the tabular version
    //    - Group by, Having, Order by are not shown in the Graph at all
    // Select clause entries in the table are filled in as a side effect during createNode
    
    void generateGraph(QueryModel query) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "generateGraph");
        
        if ( _disableQBGF )
	    return;
        
        // Reset the graph and input table
	// This is also done in QBP.clear()
	// ToDo: Decide whether we need to restore this
        // clearGraph();
        
        _queryBuilder._updateModel = false;
        try {
            generateGraphFrom(query);
            generateGraphWhere(query);
            generateGraphOrderBy(query);
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            _queryBuilder._updateModel = true;
        }
    }
    
    
    // Reset the graph and input table to an empty state
    // This could be implemented in various ways; either retain the same data structure
    // but explicitly empty it, or just create a new instance.  We favor explicit emptying,
    // except for the graph model
    // No longer used, since we just re-create the scene
//    synchronized void clearGraph() {
//        
//        Log.getLogger().entering("QueryBuilderGraphFrame", "clearGraph"); // NOI18N
//        // This is used in certain places
//        _firstTableInserted=false;
//        
//        // Clear the InputTableModel
//        _inputTableModel.setRowCount(0);
//        
//	// Clear the scene, by removing each component
//	Collection nodes = _scene.getNodes();
//	for (Object node : nodes) {
//	    System.out.println("Removing Node: " + node + ((QBNodeComponent)node).getNodeName());
//	    _scene.removeNodeWithEdges(node);
//	}
//// 	Collection edges = _scene.getEdges();
//// 	for (Object edge : edges) {
//// 	    _scene.removeEdge(edge);
//// 	    System.out.println("Removing Edge: " + edge);
//// 	}
//    }
    
    
    // Generate the graph corresponding to the FROM clause.
    // Also updates the table entries corresponding to the SELECT clause;
    // that should be refactored.
    
    private void generateGraphFrom(QueryModel query) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "generateGraphFrom"); // NOI18N
        
        // Start with the From clause
        List tables = query.getFrom().getTableList();
        
        // Iterate through the list of tables, adding them to the graph
        for (int i=0; i<tables.size(); i++) {
            
            JoinTable joinTable = (JoinTable) tables.get(i);
            String tableSpec = joinTable.getTableSpec();
            String fullTableName = joinTable.getFullTableName();
            
            // We also need to use the SELECT list to set the columns in the
            // interior of the table node.  We could pass in the whole FROM list,
            // and scan in with each table, or we could partition it once, and
            // pass in the relevant entries with each table.  If we go the
            // partitioning approach, we could revise the model to use it.
            List columnNames = new ArrayList();
            query.getColumnNames(tableSpec, columnNames);
            insertTableFromModel(joinTable, columnNames);
        }
    }
    
    // check if the where clause has any duplicate columns
    // e.g. if the where caluse is like "where table.c > 10 AND table.c < 20"
    // then this will return true. Current decisoin is not to display multiple
    // columns in the grid pane.
    private boolean whereHasDuplicateColumns( List whereColumns,
            Predicate expr ) {
        List columns = new ArrayList();
        expr.getReferencedColumns(columns);
        for (int i=0; i<columns.size(); i++) {
            if (DEBUG) {
                System.out.println("expr getReferencedColumn i = " + i +
				   " " + ((Column)columns.get(i)).genText(null));
            }
            
            Column c = (Column)columns.get(i);
            int found = 0;
            for (int j=0; j<whereColumns.size(); j++) {
                if (((Column)whereColumns.get(j)).equals(c)) {
                    found++;
                    if (DEBUG) {
                        System.out.println(
                                "where getReferencedColumn j = " + j + " " +
                                " found = " + found +
                                ((Column)whereColumns.get(j)).genText(null));
                    }
                }
                if ( found > 1 )  // more than one instance
                    return true;
            }
        }
        return false;
    }
    
    // Generate the Graph and Table entries corresponding to the WHERE clause
    // Graph: edges  Table: criteria
    
    private void generateGraphWhere(QueryModel query) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "generateGraphWhere"); // NOI18N
        
        // if this is true we should not handle any events in tableChanged.
        _inputTableAddCriteria = true;
        
        // Now handle the WHERE clause
        // For now, assume a single predicate, possibly parameterized
        // Each predicate becomes a row in the InputTable, with an appte entry in Criteria
        // Example code for adding rows to the table is in tableChanged
        // For each predicate (col = [ value | ? ] )
        // ToDo: handle all values, not just *, ?
        // ToDo: introduce a different class for join predicates
        Where where = query.getWhere();
        if (where != null) {
            
            Expression expr = where.getExpression();
            
            List whereColumns = new ArrayList();
            where.getReferencedColumns(whereColumns);
            
            if ( expr == null ) {
                _inputTableAddCriteria = false;
                return;
            } else if ( expr instanceof Predicate ) {
                if ( whereHasDuplicateColumns(whereColumns, (Predicate) expr))  // more than one instance
                    insertPredicate((Predicate)expr, -1);
                else
                    insertPredicate((Predicate)expr, 0);
            } else if ( expr instanceof And  ) {
                insertAndOr(expr, whereColumns, 0);
            } else if ( expr instanceof Or  ) {
                insertAndOr(expr, whereColumns, 0);
            }
        }
        _inputTableAddCriteria = false;
    }
    
    // insert AND / OR expression in the grid pane. if a column appears
    // more than once in the where clause then we display the "****" in the
    // criteria column and the order is displayed as "*".
    private void insertAndOr(Expression expr, List whereColumns, int order) {
        ExpressionList exprList = (ExpressionList)expr;
        for (int i=0; i<exprList.size(); i++) {
            expr = exprList.getExpression(i);
            if ( expr instanceof Predicate ) {
                if ( whereHasDuplicateColumns(whereColumns, (Predicate)expr))
                    // more than one instance
                    insertPredicate((Predicate)expr, -1);
                else
                    insertPredicate((Predicate)expr, order++);
            } else if ( expr instanceof And  ) {
                insertAndOr(expr, whereColumns, order++ );
            } else if ( expr instanceof Or  ) {
                insertAndOr(expr, whereColumns, order++ );
            }
        }
    }
    
    /**
     * Insert the predicate into visual editor, either as a graph edge (for
     * a relationship) or as a table entry (for a criterion).
     * Order is the initial value for the criteria order.
     * if order is -1, then add "*****" to criteria column and "*" to
     * order column and make both of them uneditable.
     */
    private void insertPredicate(Predicate pred, int order) {
        
        Value val1 = pred.getVal1();
        Value val2 = pred.getVal2();
        
        if ((val1 instanceof Column) && (val2 instanceof Column)) {
            // Comparing two columns -- insert an edge
            insertJoinEdge(pred, "");
        } else {
            // Assume that the right hand side is a literal value
            // This will result in an entry into the InputTable
            String marker = pred.getVal2().toString();
            
            // Treat this like any other literal value now
            // if (marker.equals("?")) {}
            
            // We can only count on the tableSpec; tableName might contain corrName
            Column col = (Column)val1;
            String tableSpec=col.getTableSpec();
            String columnName=col.getColumnName();
            
            // Create the value that we're going to put into the table
            String val = pred.getOp() + " " + marker; // NOI18N
            
            // If we're inserting a criterion, get order.  -1 is a special case, meaning ""
            if ( order == -1) {
                _queryBuilderInputTable.addCriterion(tableSpec, columnName,
                        QueryBuilderInputTable.Criteria_Uneditable_String,
                        QueryBuilderInputTable.CriteriaOrder_Uneditable_String);
            } else {
                String orderString = (order == -1) ? "" : Integer.valueOf(order+1).toString();
                
                // Update the appropriate row, or add a new one
                _queryBuilderInputTable.addCriterion(tableSpec, columnName,
                        val, orderString);
            }
        }
    }
    
    // Update the InputTable to show the OrderBy specs
    
    private void generateGraphOrderBy(QueryModel query) {
        
        // Delegate this to the InputTable, which is the only component affected
        _queryBuilderInputTable.generateTableOrderBy(query);
    }
    
    
    /**
     * Finds a graph node with the specified name (tableSpec)
     */
    QBNodeComponent findGraphNode(String tableSpec) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "findGraphNode", tableSpec); // NOI18N
        
	Collection nodes = _scene.getNodes();
	for (Object node : nodes) {
	    if ((node instanceof QBNodeComponent) &&
		((QBNodeComponent)node).getTableSpec().equals(tableSpec))
		return (QBNodeComponent)node;
	}

        return null;
    }
    
    
    // Top-level method for adding a table to the query.
    // Called from the "Add Table" menu item
    public void addTable() {
        Log.getLogger().entering("QueryBuilderGraphFrame", "addTable"); // NOI18N
        
        // if ( _queryBuilder.checkDatabaseAndDisable(null) == false ) return;
        
        QueryBuilder.showBusyCursor( true );
        try {
            
            List tableNames = _queryBuilder.getAllTables();
            String[] tableStrings = new String[tableNames.size()];
            tableNames.toArray(tableStrings);
            _addTableDlg = new AddTableDlg(tableStrings, true);
            if (DEBUG)
                System.out.println("Database tablenames: " + tableNames); // NOI18N
            
            // It's not clear why we need to do this here, since we passed
            // tables into the constructor
            // _addTableDlg.setTableValues(_tableStrings) ;
            if (_addTableDlg.getReturnStatus() == 1) {
                // <change/> Moving to NB winsys.
                //            WindowManager.getDefault().showBusyCursor(true);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Object[] selectedTables = _addTableDlg.getSelectedValues();
                        //                    refresh();
                        //                    QueryBuilder.showBusyCursor ( true );
                        for ( int i=0; i < selectedTables.length; i++ ) {
                            final String finalVal = (String) selectedTables[i];
                            insertTableInteractively(finalVal);
                        }
                        _queryBuilder.generateText();
                        runQueryMenuItem.setEnabled(true);
                        groupByMenuItem.setEnabled(true);
                        _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setRunQueryMenuEnabled(true);
                        _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setParseQueryMenuEnabled(true);
                        //                    QueryBuilder.showBusyCursor ( false );
                    }
                }
                );
                // <change/> Moving to NB winsys.
                //            WindowManager.getDefault().showBusyCursor(false);
            }
            // somehow the graph still thinks we are not changed enough to redraw.
            // this causes the edges not to get drawn, as well as the scroll bars
            // not getting updated.
//             QueryBuilderInternalFrame currentSelectedFrame =
//                     (QueryBuilderInternalFrame)_desktopPane.getSelectedFrame();
//             if ( currentSelectedFrame  != null ) {
//                 redrawFrameWithMove( currentSelectedFrame );
//             }
        } catch (SQLException sqe) {
	    // JDTODO - We need a consistent approach to handling SQL Exceptions.  Best is probably to expose them to user.
            // This comes up a number of places in QBGF.
            // _queryBuilder.checkDatabaseAndDisable(null) ;
        } finally {
            QueryBuilder.showBusyCursor( false );
        }
    }
    
    /**
     * Responds to a menu selection
     *
     * Current choices are "Add Table", "Run Query", "Remove From Query"
     */
    public void actionPerformed(ActionEvent e) {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "actionPerformed"); //NOI18N
        
        JMenuItem source = (JMenuItem)(e.getSource());
        
        if (source.getText().equals(NbBundle.getMessage(QueryBuilderGraphFrame.class, "Add_Table"))) {      // NOI18N
            addTable();
        }
        
        else if (source.getText().equals(NbBundle.getMessage(QueryBuilderGraphFrame.class, "RUN_QUERY")))       // NOI18N
        {
            // Execute the query
            _queryBuilder.executeQuery(_sqlTextArea.getText());
        }
        
        else if (source.getText().equals(NbBundle.getMessage(QueryBuilderGraphFrame.class, "REMOVE_FROM_QUERY"))) {     // NOI18N
            removeTable();
        }
    }
    
//     public boolean isSelectionEmpty() {
//         // if there are no tables, selection should be empty.
//         if ( _firstTableInserted == false ) return true;
//         QueryBuilderInternalFrame currentSelectedFrame =
//                 (QueryBuilderInternalFrame)_desktopPane.getSelectedFrame();
//         return ( currentSelectedFrame == null );
//     }
    
    // Remove the table node from the graph (and model)
    // update delete menu
    public void  removeNode(TableNode node) {
//         QueryBuilder.showBusyCursor( true );
        
//         // remove the selected table to fix
//         // 6253516 : "delete" key doesn't work for same tables in QE.
//         removeTable();
//         QueryBuilder.showBusyCursor( false );
    }
    
    // Remove the condition node from the graph (and model)
    // update delete menu
    public void  removeNode(CondNode node) {
//         QueryBuilder.showBusyCursor( true );
//         String[] rel = new String[4];
//         rel[0] = node.getTable1();
//         rel[1] = node.getColumn1();
//         rel[2] = node.getTable2();
//         rel[3] = node.getColumn2();
        
//         if (DEBUG) {
//             System.out.println(" QBGF.removeNode() table1 = " + rel[0] + " column1 = " + rel[1] + "  table2 = " + rel[2] + " column2 = " + rel[3] + "\n" ); // NOI18N
//         }
        
//         Predicate pred = SQLQueryFactory.createPredicate(rel);
//         _queryBuilder._updateText=false;
//         _queryBuilder.getQueryModel().removeCondition( pred );
//         Object cell = _graph.getSelectionCell();
//         if (cell instanceof DefaultEdge) {
//             _graphModel.remove(new Object[] {cell});
//         }
//         _queryBuilder._updateText=true;
//         _queryBuilder.generateText();
//         _queryBuilder.activateActions();
//         QueryBuilder.showBusyCursor( false );
    }
    
    // Remove the join node from the graph (and model)
    // update delete menu
    public void  removeNode(JoinNode node) {
//         QueryBuilder.showBusyCursor( true );
//         String[] rel = new String[4];
//         rel[0] = node.getTable1();
//         rel[1] = node.getColumn1();
//         rel[2] = node.getTable2();
//         rel[3] = node.getColumn2();
        
//         if (DEBUG) {
//             System.out.println(" QBGF.removeNode() table1 = " + rel[0] + " column1 = " + rel[1] + "  table2 = " + rel[2] + " column2 = " + rel[3] + "\n" ); // NOI18N
//         }
        
//         _queryBuilder._updateText=false;
//         _queryBuilder.getQueryModel().removeJoinNode( rel[0], rel[1], rel[2], rel[3] );
//         Object cell = _graph.getSelectionCell();
//         if (cell instanceof DefaultEdge) {
//             _graphModel.remove(new Object[] {cell});
//         }
//         _queryBuilder._updateText=true;
//         _queryBuilder.generateText();
//         _queryBuilder.activateActions();
//         QueryBuilder.showBusyCursor( false );
    }
    
    // Remove the selected frame from the graph (and model)
    // update delete menu
    public void removeTable() {
        
        Log.getLogger().entering("QueryBuilderGraphFrame", "removeTable"); //NOI18N

        QueryBuilder.showBusyCursor( true );
        // Important: suppress bogus regeneration of the text query
        _queryBuilder._updateText=false;
        try {
	    removeTable(_selectedNode);
        } finally {
            _queryBuilder._updateText=true;
        }
        
        // enable/disable group_by menu item
        if ( _sqlTextArea.getText() == null ) {
            runQueryMenuItem.setEnabled(false);
            groupByMenuItem.setEnabled(false);
            _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setRunQueryMenuEnabled(false);
            _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setParseQueryMenuEnabled(false);
        } else if ( _sqlTextArea.getText().trim().length() == 0 ) {
            runQueryMenuItem.setEnabled(false);
            groupByMenuItem.setEnabled(false);
            _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setRunQueryMenuEnabled(false);
            _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setParseQueryMenuEnabled(false);
        } else {
            runQueryMenuItem.setEnabled(true);
            groupByMenuItem.setEnabled(true);
            _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setRunQueryMenuEnabled(true);
            _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setParseQueryMenuEnabled(true);
        }
        QueryBuilder.showBusyCursor( false );
    }
    
    // Responds to a CheckBoxMenuItem -- Group by
    
    public void itemStateChanged(ItemEvent e) {
        
        JMenuItem source = (JMenuItem)(e.getSource());
        
        if (source.getText().equals(NbBundle.getMessage(QueryBuilderGraphFrame.class, "GROUP_BY"))) {       // NOI18N
            
            // Add or remove a Group by clause
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // Add a Group By to the model
                _queryBuilder.getQueryModel().addGroupBy();
            } else {
                // Remove a Group By from the model
                _queryBuilder.getQueryModel().removeGroupBy();
            }
            _queryBuilder.generateText();
        }
    }
    
    
    // Set the checkbox in the GroupBy menu item
    public void setGroupBy(boolean b) {
        groupByMenuItem.setSelected(b);
    }
    
    
    // somehow the graph still thinks we are not changed enough to redraw.
    // this causes the edges not to get drawn, as well as the scroll bars
    // not getting updated.
    void redrawFrameWithMove(QueryBuilderInternalFrame frame) {
//         if (frame != null)
//             // && frame.isShowing()
//         {
//             HashMap map = new HashMap();
//             Map atts = GraphConstants.createMap();
            
//             GraphConstants.setBounds(atts,frame.getBounds());
//             map.put(frame.getGraphCell(),atts);
//             // Update the graph model with the new attributes, which include frame bounds(?)
//             _graphModel.edit(map,null,null,null);
//         }
        
//         // the 2 lines below need to be there for this to work
//         // but they should not be there in redrawFrame.
//         // That is the only difference between redrawFrame and
//         // redrawFrameWithMove
//         _desktopPane.setBounds(_canvas.getBounds());
//         _desktopPane.updateUI();
        
//         resizeDesktop();
//         _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().requestFocus(true);
    }
    
    
    // Redraw an internal frame?
    
    private void redrawFrame(QueryBuilderInternalFrame frame) {
//         if (frame != null)
//             // && frame.isShowing()
//         {
//             HashMap map = new HashMap();
//             Map atts = GraphConstants.createMap();
            
//             GraphConstants.setBounds(atts,frame.getBounds());
//             map.put(frame.getGraphCell(),atts);
//             // Update the graph model with the new attributes, which include frame bounds(?)
//             _graphModel.edit(map,null,null,null);
//         }
    }
    
    
    // Manually refresh the graph display -- copied from ComponentListener below
    // The forum suggested something like graphDidChange(); revalidate(); but not tried
    // ToDo: Decide whether we need something like this with GraphLib
     void refresh() {
//          _graph.graphDidChange();
//          _graph.revalidate();
//          if (DEBUG) {
//              System.out.println(" refresh() called " + "\n" ); // NOI18N
//              System.out.println(" width = " + _graph.getSize().getWidth() + " Height = " + _graph.getSize().getHeight() + "\n" ); // NOI18N
//          }
//          resizeDesktop();
     }
    
    
    // Inner classes, mostly for Listeners
    
    // A listener that will bring up the background menu
    
//     class BackgroundPopupListener extends MouseAdapter {
        
//         public void mousePressed(MouseEvent e) {
//             maybeShowPopup(e);
//         }
        
//         public void mouseReleased(MouseEvent e) {
//             maybeShowPopup(e);
//         }
        
//         private void maybeShowPopup(MouseEvent e) {
//             if ( _disableQBGF ) return;
//             if (e.isPopupTrigger() ) {
//                 Object cell = _graph.getFirstCellForLocation(e.getX(), e.getY());
//                 if ( ( cell != null ) && ( cell instanceof DefaultEdge ) ) {
//                     // do not show popup.
//                     // bug 4979403 right click and double click the Join edge
//                     return;
//                 }
//                 if (e.getComponent().isEnabled() )
//                     _backgroundPopup.show( e.getComponent(), e.getX(), e.getY() );
//             }
//         }
//     }
    /**
     * An adapter class for receiving internal frame events.  Used
     * to detect when a Frame (graph node representing a table) is selected
     */
    private class FrameSelectionListener extends InternalFrameAdapter {
        
        public void internalFrameActivated(InternalFrameEvent ife) {
            Object source = (Object)(ife.getSource());
            
            // When any internal frame is activated, enable these two listeners?
//          _apifa.setEnabled(true);
//          _acifa.setEnabled(true);
            
            // Finally, bring up property sheet. Ignore event, just check which frame is selected.
            QueryBuilderInternalFrame currentSelectedFrame =
                    (QueryBuilderInternalFrame)_desktopPane.getSelectedFrame();
            setActivatedNode( currentSelectedFrame ) ;
        }
        
        public void internalFrameDeactivated(InternalFrameEvent ife) {
//          _apifa.setEnabled(false);
//          _acifa.setEnabled(false);
        }
    }
    
    
    // An adapter class for receiving component events
    // This listens for events from the desktopPane
    
    private class CompListener extends ComponentAdapter {
        
        public void componentResized(ComponentEvent ce) {
            if ( _disableQBGF ) {
                return;
            } else {
                refresh();
            }
        }
    }
    
    
    // An adapter class for receiving component events
    // This listens for events from nodes (internal frames)
    
//     private class FrameComponentListener extends ComponentAdapter {
        
//         // The following two methods are defined on the ComponentListener interface
//         public void componentResized(ComponentEvent ce) {
//             componentMoved(ce);
//         }
        
//         public void componentMoved(ComponentEvent ce) {
//             HashMap map = new HashMap();
//             Map atts = GraphConstants.createMap();
//             QueryBuilderInternalFrame frame = (QueryBuilderInternalFrame)ce.getComponent();
//             refresh();
            
//             GraphConstants.setBounds(atts,frame.getBounds());
//             map.put(frame.getGraphCell(),atts);
//             _graphModel.edit(map,null,null,null);
            
//             resizeDesktop();
//         }
//     }
    
    /**
     * Listener for detecting changes to the graph selection, to update Property Sheet
     *
     * This only detects selection for edges (joins); nodes are handled elsewhere.
     */
//     private class GraphSelListener implements GraphSelectionListener {
        
//         public void valueChanged(GraphSelectionEvent e) {
            
//             Log.getLogger().finest("Graph selection changed, event: "+e); // NOI18N
//             if (_graph.getSelectionCount() > 0) {
//                 // Use the first selection; should only be one
//                 Object cell = _graph.getSelectionCell();
//                 if ( ( cell != null ) && ( cell instanceof DefaultEdge ) ) {
//                     // We've selected an edge. Update the Property Sheet, by setting
//                     // the activated node to the underlying join
//                     AbstractNode an = (AbstractNode)(((DefaultEdge) cell).getUserObject());
//                     _queryBuilder.setActivatedNodes(new Node[] { an });
//                     QueryBuilderInternalFrame currentSelectedFrame =
//                             (QueryBuilderInternalFrame) _desktopPane.getSelectedFrame();
//                     try {
//                         if ( currentSelectedFrame != null ) {
//                             currentSelectedFrame.setSelected( false );
//                         }
//                     } catch ( java.beans.PropertyVetoException pve ) {
//                         // do nothing
//                     }
//                 }
//             }
//         }
//     }
    
    
    // GraphLib class for handling selection
    
    private class ObjectSelectProvider implements SelectProvider {
        
        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }
        
        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return true;
        }
        
        public void select(Widget widget, Point localLocation, boolean invertSelection) {

            Object object = _scene.findObject(widget);
            if (object instanceof AbstractNode) {
		AbstractNode an = (AbstractNode)object;
		_queryBuilder.setActivatedNodes(new Node[] { an });
                _scene.userSelectionSuggested(Collections.singleton(object), invertSelection);
	    }
        }
    }
    

    // Utility class for timing
    
    public class PerfTimer {
        
        long _time;
        
        public PerfTimer() {
            resetTimer();
        }
        
        // reset Timer
        public void resetTimer(){
            // set current time
            _time = System.currentTimeMillis();
        }
        
        public long elapsedTime() {
            // get elapsed Time
            return (System.currentTimeMillis() - _time);
        }
        
        public void print(String aString) {
            System.out.println(aString + ": " + this.elapsedTime() + " ms"); // NOI18N
        }
    }
    
    public void dragEnter(DropTargetDragEvent e) {
        e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }
    
    public void drop(DropTargetDropEvent e) {
        if ( _disableQBGF ) return;
        try {
            Transferable tr = e.getTransferable();
            DataFlavor[] dataFlavors = tr.getTransferDataFlavors();
            for (int i = 0; i < dataFlavors.length; i++) {
                Object o = tr.getTransferData( dataFlavors[i] );
                if ( o instanceof Node ) {
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    // user should be allowed to drop tables from the
                    // current data source only.
                    List tableNamesArrayList =
                            _queryBuilder.getAllTables();
                    String fullTableName = ( ( Node ) o ).getName();
                    
                     // Reassign fullTableName to just the table name - minus the schema name
                    String tableName;
                    String justTableName;
                    for (int j=0; j < tableNamesArrayList.size(); j++){
                        tableName = (String) tableNamesArrayList.get(j);
                        String[] parts = tableName.split("\\.");
                        if ( parts.length > 1 ) {
                            justTableName = parts[1];
                        } else {
                            justTableName = parts[0];
                        }
                        if (justTableName.equals(fullTableName )){
                            fullTableName = tableName;
                            break;
                        }
                    }
                    
                    if ( tableNamesArrayList.contains( fullTableName ) ) {
                        insertTableInteractively( fullTableName );
                        _queryBuilder.generateText();
                        runQueryMenuItem.setEnabled(true);
                        groupByMenuItem.setEnabled(true);
                        _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setRunQueryMenuEnabled(true);
                        _queryBuilder.getQueryBuilderPane().getQueryBuilderSqlTextArea().setParseQueryMenuEnabled(true);
                        refresh();
                        // _graph.getGraphLayoutCache().reload();
                        resizeDesktop();
                        // somehow the graph still thinks we are not changed
                        // enough to redraw. this causes the edges not to get
                        // drawn, as well as the scroll bars not getting
                        // updated.
                            
                        // 117724  DnD table null pointer exception occurs - removed code to get the currentSelectedFrame
                        // (QueryBuilderInternalFrame)_desktopPane.getSelectedFrame();                        
                    } else {
                        String msg =
                                NbBundle.getMessage(QueryBuilderGraphFrame.class,
                                "DRAG_AND_DROP_FROM_CURRENT_DATASOURCE");
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                msg + ", " +
                                _queryBuilder.getConnectionInfo() +
                                "\n\n", // NOI18N
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                    }
                    e.dropComplete(true);
                    return;
                }
            }
            e.rejectDrop();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void dragExit(DropTargetEvent e) {
        
    }
    
    public void dragOver(DropTargetDragEvent e) {
        
    }
    
    public void dropActionChanged(DropTargetDragEvent e) {
        
    }
    
    public void dragOver(DragSourceDragEvent e) {
        
    }
    
    public void dropActionChanged(DragSourceDragEvent e) {
        
    }
}
