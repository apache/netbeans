/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.table.DefaultTableModel;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.sql.visualeditor.querymodel.Predicate;
import org.netbeans.modules.db.sql.visualeditor.querymodel.SQLQueryFactory;

import org.netbeans.api.visual.widget.Scene;

/**
 * A class that implements a graph node, representing a DB table
 * @author  Sanjay Dhamankar, Jim Davidson
 */
public class QueryBuilderInternalFrame extends JInternalFrame
        implements  ActionListener, KeyListener,
                    DragGestureListener,
                    DragSourceListener,
                    DropTargetListener
{

    // Private variables

    private boolean                     DEBUG = false;

    private Object                      _dragObject;
    private DropTarget                  _dropTarget;

    private JPopupMenu                  _tableColumnPopup;
    private TableNode                   _node;          // for property sheet

    private QueryBuilder                _queryBuilder;

    private QueryBuilderTable           _qbTable;
    private QueryBuilderTableModel      _queryBuilderTableModel = null;

    // Record location of last placement, to aid layout
    private static int                  _lastX = 0, _lastY = 0;


    // Constructor

    public QueryBuilderInternalFrame(QueryBuilderTableModel queryBuilderTableModel,
                                     QueryBuilder queryBuilder)
    {
        _dragObject = null;
        // Set some private variables
        _queryBuilderTableModel = queryBuilderTableModel;
        _queryBuilder = queryBuilder;

        // Create the node that will be used for the property sheet
        _node = new TableNode(queryBuilderTableModel.getFullTableName(),
                              queryBuilderTableModel.getCorrName(),
                              _queryBuilder);
        setResizable(true);
        setFrameIcon(null);
        setIconifiable(false);

        // Add an anonymous listener for the internalFrameOpened event
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                // As soon as a frame is opened select it.
                // This doesn't seem to work.
                try {
                    QueryBuilderInternalFrame.this.setSelected(true);
                } catch(PropertyVetoException pve) {
                }
            }});

        this.setBackground(Color.white);

    }


    /** Handle the key typed event */
    public void keyTyped(KeyEvent e) {
    }

    /** Handle the key pressed event from the internal frame. */
    public void keyPressed(KeyEvent e) {
        if ( DEBUG )
            System.out.println(" QBIF : key pressed called. " + "\n" ); // NOI18N
        _queryBuilder.handleKeyPress(e);
    }

    /** Handle the key released event from the sql text area. */
    public void keyReleased(KeyEvent e) {
    }

    // Initialize various aspects of the internal frame:
    //   main panel, model, popup listeners, scrollpane...
    
    public void create() {
        
        JPanel mainPanel = new JPanel();
        //      mainPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Create a JTable component, with the specified TableModel behind it
        _qbTable = new QueryBuilderTable(_queryBuilderTableModel);
        _qbTable.setBackground(Color.white); 
        
        // Wrap the JTable in a JScrollPane
        JScrollPane sp = new JScrollPane(_qbTable);
        sp.getViewport().setBackground(Color.white); 
        
        // Wrap the JScrollPane in a JPanel
        mainPanel.add(sp,BorderLayout.CENTER);
        mainPanel.setBackground(Color.white); 
        
        // And add the JPanel to the content pane of the internal frame 
        getContentPane().add(mainPanel);
        getContentPane().setBackground(Color.white); 
        
        DragSource dragSource = DragSource.getDefaultDragSource();

	dragSource.createDefaultDragGestureRecognizer(
	    _qbTable, // component where drag originates
	    DnDConstants.ACTION_MOVE, // actions
	    this); // drag gesture recognizer

        _dropTarget = new DropTarget ( _qbTable, 
				       DnDConstants.ACTION_MOVE,
				       this );

        // Per JInternalFrame tutorial, it's importnat to set the size of the internal frame
        // (with pack, setSize, or setBounds) and make it visible (with setVisible or show)
        pack();
        setSize(175,120);
        setVisible(true);
    }

    /**
     * Create a popup menu - XXX - all items are NOP 
     */
    JPopupMenu createTableColumnPopup() {

        JPopupMenu tableColumnPopup;
        JMenu menu, subMenu;
        JMenuItem menuItem;
        JMenuItem subMenuItem;
        
        // Create the popup menu.
        tableColumnPopup = new JPopupMenu();
        
        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInternalFrame.class, "ADD_TO_QUERY") );      // NOI18N
        menuItem.addActionListener(this);
        tableColumnPopup.add(menuItem);
        
        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInternalFrame.class, "SORT_ASCENDING") );        // NOI18N
        menuItem.addActionListener(this);
        tableColumnPopup.add(menuItem);
        
        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInternalFrame.class, "SORT_DESCENDING") );       // NOI18N
        menuItem.addActionListener(this);
        tableColumnPopup.add(menuItem);
        
        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInternalFrame.class, "REMOVE_FILTER") );     // NOI18N
        menuItem.addActionListener(this);
        tableColumnPopup.add(menuItem);
        
        return tableColumnPopup;
    }


    // Accessors/Mutators

    public QueryBuilderTableModel getQueryBuilderTableModel()
    {
        return(_queryBuilderTableModel);
    }

    public TableNode getNode() {
        return _node;
    }
    
    /**
     * Suppress label printing in graph
     */
    public String toString() {
        return ""; // NOI18N
    }
    
    // Convenience methods -- return tablename/tablespec from associated model

    String getTableName() {
        return _queryBuilderTableModel.getTableName();
    }

    String getTableSpec() {
        return _queryBuilderTableModel.getTableSpec();
    }

    String getFullTableName() {
        return _queryBuilderTableModel.getFullTableName();
    }

    static Point getLastLocation() {
        return new Point(_lastX, _lastY);
    }

    static void resetLocation() {
        _lastX=0;
        _lastY=0;
    }
    
    // Record the new location, before passing on the set method

    public void setLocation(int x, int y) {
        _lastX = x;
        _lastY = y;
        super.setLocation(x, y);
    }
    

    // Returns just the class name -- no package info.

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf("."); // NOI18N
        return classString.substring(dotIndex+1);
    }


    // Event handlers
    
    /** 
     * ActionListener interface implementation
     * This would presumably handle menu selections, but none of them actually
     * do anything now.
     */
    public void actionPerformed(ActionEvent e) {
        if ( DEBUG ) {
            JMenuItem source = (JMenuItem)(e.getSource());
            String s = "Action event detected." // NOI18N
            + "\n" // NOI18N
            + "    Event source: " + source.getText() // NOI18N
            + " (an instance of " + getClassName(source) + ")"; // NOI18N
            System.out.println (s + "\n"); // NOI18N
        }
    }


    // Inner class for popup menu
    
    class TableColumnPopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                _tableColumnPopup.show(e.getComponent(),
                                       e.getX(), e.getY());
            }
        }
    }

    public void dragGestureRecognized(DragGestureEvent e) {

        if (DEBUG) {
            System.out.println (" Component point " + e.getDragOrigin() + "\n"); // NOI18N
        }
        int row =  _qbTable.rowAtPoint ( e.getDragOrigin() );
        int column =  _qbTable.columnAtPoint ( e.getDragOrigin() );

        _dragObject = this;

        if ( ( row < 0 ) || ( column < 2 ) ) {
            return;
        }

        if (DEBUG) {
            System.out.println (" Table row " + row + " Table column " + column +
                                " Object " + _qbTable.getValueAt ( row, column ) + "\n"); // NOI18N
        }
        // drag anything ...
        String dragTableColumn =
            // used to be _queryBuilderTableModel.getFullTableName(), but that failed dragging aliased tables
            _queryBuilderTableModel.getTableSpec() + "." + ( (String) _qbTable.getValueAt ( row, column ) );
        e.startDrag ( DragSource.DefaultCopyDrop, // cursor
                      new StringSelection (dragTableColumn),
                      this );  // drag source listener
    }

    public void dragDropEnd(DragSourceDropEvent e) {}
    public void dragEnter(DragSourceDragEvent e) {}
    public void dragExit(DragSourceEvent e) {}
    public void dragOver(DragSourceDragEvent e) {}
    public void dropActionChanged(DragSourceDragEvent e) {}


    public void drop(DropTargetDropEvent e) {

        try {
            if (DEBUG) {
                System.out.println (" Component point " + e.getLocation() + "\n"); // NOI18N
            }
            int row =   _qbTable.rowAtPoint ( e.getLocation() );
            int column =  _qbTable.columnAtPoint ( e.getLocation() );

            if ( ( row < 0 ) || ( column < 2 ) || ( _dragObject == this ) ) {
                String msg = 
                    NbBundle.getMessage(QueryBuilderInternalFrame.class,
                                        "DRAG_AND_DROP_COLUMNS");
                NotifyDescriptor d = new NotifyDescriptor.Message (
                    msg + 
                    "\n\n", // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            
                _dragObject = null;
                return;
            }

            if (DEBUG) {
                System.out.println (" Table row " + row + " Table column " + column +
                                    " Object " + _qbTable.getValueAt ( row, column ) + "\n"); // NOI18N
            }

            // drag anything ...
            String dropTableColumn =
                _queryBuilderTableModel.getTableSpec() + "." + ( (String) _qbTable.getValueAt ( row, column ) );

            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            Transferable tr = e.getTransferable();

            if(e.isDataFlavorSupported(stringFlavor) && e.isLocalTransfer()) {
                String dragTableColumn = 
                    (String)tr.getTransferData(stringFlavor);

                if (DEBUG) {
                    System.out.println ( "dragTableColumnName = " + dragTableColumn + "\n"); // NOI18N
                    System.out.println ( "dropTableColumnName = " + dropTableColumn + "\n"); // NOI18N
                }
                String[] rel = new String[4];
                String[] res = dragTableColumn.split("\\.");      // NOI18N

                // it has to be a column in the format
                // schema.table.column, if not reject it.
                if ( res.length == 2 ) {
                    // table
                    rel[0] = res[0] ;
                    // column
                    rel[1] = res[1];
                }
                else if ( res.length == 3 ) {
                    // schema.table
                    rel[0] = res[0] + "." + res[1];
                    // column
                    rel[1] = res[2];
                }
                else  {
                    String msg = 
                        NbBundle.getMessage(QueryBuilderInternalFrame.class, "DRAG_AND_DROP_COLUMNS");
                    NotifyDescriptor d =
                        new NotifyDescriptor.Message ( msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                    DialogDisplayer.getDefault().notify(d);
                    _dragObject = null;
             
                    return;
                }

                // 'schema.table'  OR just 'table'
                rel[2] = _queryBuilderTableModel.getTableSpec(); 
                // 'column'
                rel[3] = ( (String) _qbTable.getValueAt ( row, column ) );

                if (DEBUG) {
                    System.out.println ( 
                        " rel[0] = " + rel[0] +
                        " rel[1] = " + rel[1] +
                        " rel[2] = " + rel[2] +
                        " rel[3] = " + rel[3] + "\n"); // NOI18N
                }
                Predicate pred = SQLQueryFactory.createPredicate(rel);
                _queryBuilder._queryModel.addOrCreateAndExpression ( pred );
                if (DEBUG) {
                    System.out.println ( 
                        _queryBuilder._queryModel.getWhere ().genText (null) );
                }
                _queryBuilder.generate();
                e.acceptDrop(DnDConstants.ACTION_MOVE);
                e.dropComplete(true);

            }
            else {
                e.rejectDrop();
            }
            _dragObject = null;
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }
    }
    public void dragEnter(DropTargetDragEvent e) { }
    public void dragExit(DropTargetEvent e) { }
    public void dragOver(DropTargetDragEvent e) { }
    public void dropActionChanged(DropTargetDragEvent e) { }
}
