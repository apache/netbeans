/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.text.DefaultEditorKit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ParameterMetaData;
import java.sql.Connection;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.sql.visualeditor.QueryEditorUILogger;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx ;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.db.sql.visualeditor.querymodel.ColumnProvider;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Column;
import org.netbeans.modules.db.sql.visualeditor.querymodel.JoinTable;
import org.netbeans.modules.db.sql.visualeditor.querymodel.OrderBy;
import org.netbeans.modules.db.sql.visualeditor.parser.ParseException;
import org.netbeans.modules.db.sql.visualeditor.parser.TokenMgrError;

import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditor;

import org.netbeans.modules.db.sql.visualeditor.Log;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.openide.util.Exceptions;

/**
 * The top-level class for the QueryBuilder.
 *
 * This is the class that gets invoked from outside, to create QueryBuilder
 * instances.  It also handles communication between the QueryBuilder panes and the
 * data source (RowSet, Connection), including retrieving metadata, executing
 * queries, and retrieving result sets.
 *
 * @author  Sanjay Dhamankar, Jim Davidson, then hacked apart, chewed up, by jfbrown, but still needs more!
 */
public class QueryBuilder extends TopComponent
        implements
        ClipboardOwner,
        KeyListener,
        ColumnProvider {
    

    // Provide package access on these, for use by other classes in the Query Builder
    
    QueryBuilderPane                    _queryBuilderPane;
    QueryModel                          _queryModel;
    boolean                             _updateModel = true;
    boolean                             _graphicsEnabled = true;
    
    // Used to delay updating text pane until all changes have been made
    boolean                             _updateText = true;
    SQLIdentifiers.Quoter               quoter;

    // Private variables
    private String                      lastQuery;

    // the boolean below is used to determine if we need to store the query in the 
    // backing file. The first time that generateText is called, we avoid saving
    // the query in the backing file as this unnecessarily sets the save buttons ON.
    private boolean                     firstTimeGenerateText = true;

    private String                      _parseErrorMessage = null ;
    private boolean                     DEBUG = false;
    private DatabaseConnection          dbconn;
    private String                      statement;
    private QueryBuilderMetaData	qbMetaData;
    // private VisualSQLEditorMetaData         metaDataCache = null ;
    private VisualSQLEditor         	vse ;
    private SQLException                lastException = null ;


    
    /**
     * Static factory method, added for access from RowSet
     * @Return a QueryBuilder instance, either new or retrieved from the Map
     */
    public static Component open( DatabaseConnection dbconn, String statement, VisualSQLEditorMetaData metadata, VisualSQLEditor vse)
    {
        Log.getLogger().entering("QueryBuilder", "open"); // NOI18N
        QueryEditorUILogger.logEditorOpened();

        Connection conn = dbconn.getJDBCConnection();
        if (conn == null) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
        }
        // A database connection is required.  If not connected then don't create a QueryBuilder
        if (dbconn.getJDBCConnection() == null) {
            return null;
        }

        showBusyCursor( true );
        QueryBuilder qb ;
        try {
            qb = new QueryBuilder(dbconn, statement, metadata, vse);
        } catch (SQLException sqle ) {
            qb = null ;
            // JDTODO: restore this dialog
//            // TODO:  popup an error dialog.
//            ConnectionStatusPanel csp = new ConnectionStatusPanel() ;
//            csp.configureDisplay(sqlStatement.getConnectionInfo(), false,sqle.getLocalizedMessage(),  "", 0, false ) ;
//            // csp.setGeneralInfo("") ;
//            csp.displayDialog( sqlStatement.getConnectionInfo() ) ;
        }
        final QueryBuilder queryBuilder = qb ;
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if ( queryBuilder != null) {
                    queryBuilder.open();
                    queryBuilder.requestActive();
                }
                showBusyCursor( false );
            }
        }) ;

        queryBuilder.getTextAreaFocusInvokeLater();
        return queryBuilder;
    }

    /**
     * Private constructor, invoked only from the open() factory method
     */ 
    private QueryBuilder(DatabaseConnection dbconn, String statement, VisualSQLEditorMetaData metadata, VisualSQLEditor vse)
            throws  SQLException
    {
	Log.getLogger().entering("QueryBuilder", "constructor");
        this.dbconn = dbconn;
        this.statement = statement;
	this.vse = vse;
        
	// Either pass in metadata, or have it created from db
	this.qbMetaData = 
 	    (metadata==null) ?
            new QueryBuilderMetaData(dbconn, this) :
	    new QueryBuilderMetaData(metadata, this) ;
        
        // Create a quoter to be used for delimiting identifiers
        this.quoter = SQLIdentifiers.createQuoter(getConnection().getMetaData());

	// It would be nice to have a short title, but there isn't a convenient one
        String title = dbconn.getName();
     
        // Set the name to display
        setName(title);
        setDisplayName(title);

        setLayout(new java.awt.BorderLayout());

        ImageIcon imgIcon =
                new ImageIcon(getClass().getResource("/org/netbeans/modules/db/sql/visualeditor/resources/query-editor-tab.png")); // NOI18N
        if (imgIcon != null)
            setIcon(imgIcon.getImage());

        _queryBuilderPane = new QueryBuilderPane(this);

        // Add the pane to the end of the QueryBuilder container
        add(_queryBuilderPane);

        addKeyListener(this);
    }
    
    
    // used for syntax highlighting
    public boolean isSchemaName( String schemaName ) {
	return qbMetaData.isSchemaName( schemaName ) ;
    }

    public boolean isTableName( String tableName ) {
	return qbMetaData.isTableName( tableName );
    }

    public boolean isColumnName( String columnName ) {
	return qbMetaData.isColumnName( columnName );
    }


    /////////////////////////////////////////////////////////////////////////
    // Delete support
    /////////////////////////////////////////////////////////////////////////

    /** delete action performer */
    private final transient DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();

    /** copy action performer */
    final transient CopyCutActionPerformer copyActionPerformer =
        new CopyCutActionPerformer(true);

    /** cut action performer */
    final transient CopyCutActionPerformer cutActionPerformer =
        new CopyCutActionPerformer(false);

    
    // Implements ClipboardOwner
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    boolean isSelectionEmpty() {
        Node[] nodes = getActivatedNodes();
        if ( (nodes != null) && (nodes.length != 0) )
            return false;
        else
            return true;
    }

    private boolean isActivated() {
        return this == TopComponent.getRegistry().getActivated();
    }

    /*** do not save across IDE session ***/
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER ;
    }

    public String preferredID() {
        return "QueryEditor" ; //NOI18N
    }



    /** Called when this window is activated: make delete
     * sensitive based on whether or not anything is selected and whether
     * the clipboard contains something we can absorb. */
    public void activateActions() {
        if (isSelectionEmpty()) {
            disableDelete();
        } else {
            enableDelete();
        }
        // for now cut / copy is always disabled.
        disableCutCopy();
    }

    /** Called when the when the component is deactivated. We no longer
     * allow our paste types to be invoked so clear it - get rid of
     * the action performers as well. */
    public void deactivateActions() {
        if (deleteActionPerformer != null) {
//            delete.setActionPerformer(null);
        }
    }

    /** Called when the selection is non zero and the component is active:
     * enable cut, copy and delete */
    void enableDelete() {
        if (!isActivated()) {
            return;
        }
        deleteActionPerformer.setEnabled(true);
    }

    /** Called when the selection is removed: disable delete */
    void disableDelete() {
        if (!isActivated()) {
            return;
        }
        deleteActionPerformer.setEnabled(false);
        DeleteAction da = SystemAction.get(DeleteAction.class);
        da.setEnabled(false);
    }

    /** Called when the selection is removed: disable cut/copy */
    void disableCutCopy() {
        if ( DEBUG )
            System.out.println(" disableCutCopy called. " + "\n" ); // NOI18N
        // Fix 6265915 Copy/Cut menu items are always enabled
        cutActionPerformer.setEnabled(false);
        copyActionPerformer.setEnabled(false);
    }

    /** Class which performs delete action */
    class DeleteActionPerformer extends AbstractAction implements ActionPerformer {

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        // Perform delete action.
        public void performAction(SystemAction action) {
            // We run into deadlocks without this; !#$!@#!@ ModuleActions thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    deleteSelection();
                }
            }
            );
        }
    }

    /** Class which performs copy and cut actions */
    class CopyCutActionPerformer extends AbstractAction implements ActionPerformer {
        /** determine if adapter is used for copy or cut action. */
        boolean isCopy;

        /** Create new adapter */
        public CopyCutActionPerformer(boolean b) {
            isCopy = b;
        }

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        /** Perform copy or cut action. */
        public void performAction(SystemAction action) {
            // for now do nothing
        }
    }


    /** Remove the currently selected components */
    private void deleteSelection() {
        if ( DEBUG )
            System.out.println(" deleteSelection called. " + "\n" ); // NOI18N
        java.awt.KeyboardFocusManager kbfm = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager();
        java.awt.Component c = kbfm.getFocusOwner();
        if ( c != null ) {
            java.awt.Container p = c.getParent(); 
            while ( p != null ) {
                if ( p instanceof QueryBuilderGraphFrame ) {

                    Node[] nodes = getActivatedNodes();
                    if (nodes == null || nodes.length == 0) {
                        return;
                    }
                    for (int i = 0; i < nodes.length; i++) {
                        if ( ( nodes[i] instanceof CondNode ) ) {
                            _queryBuilderPane.getQueryBuilderGraphFrame().removeNode((CondNode)nodes[i]);
                        } else if ( ( nodes[i] instanceof JoinNode ) ) {
                            _queryBuilderPane.getQueryBuilderGraphFrame().removeNode((JoinNode)nodes[i]);
                        } else if ( ( nodes[i] instanceof TableNode ) ) {
                            _queryBuilderPane.getQueryBuilderGraphFrame().removeNode((TableNode)nodes[i]);
                        }
                    }
                }
            p = p.getParent();
            }
        }
    }

    private void installActions(ActionMap map, InputMap keys) {
       /*
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);
        // Paste still done the old way...
        //map.put(DefaultEditorKit.pasteAction, pasteActionPerformer);
        */
        map.put("delete", deleteActionPerformer); // or false
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer); 

        /*
        // Popup menu from the keyboard
        map.put ("org.openide.actions.PopupAction",
                new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showKeyboardPopup();
                    }
                });
            }
        });

        keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);
         */
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    }



    void getGraphFrameCanvasFocus() {
        _queryBuilderPane.getQueryBuilderGraphFrame().getCanvasFocus ();
    }

    void getTextAreaFocusInvokeLater() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus( true );
                _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocusInWindow ();
            }
        }) ;
    }
    
    /** ignore */
    public void keyTyped(KeyEvent e) {
    }

    /** ignore */
    public void keyReleased(KeyEvent e) {
    }

    /** Handle the key pressed event and change the focus if a particular
     * key combination is pressed. */
    public void keyPressed(KeyEvent e) {
        handleKeyPress(e);
    }

    public void handleKeyPress(KeyEvent e) {
        if( e.isAltDown() ) {
            int code = e.getKeyCode();
            switch(code) {
                // diagram pane
                case KeyEvent.VK_1:
                    if ( DEBUG )
                        System.out.println(" Alt + 1 pressed. "); // NOI18N
		    // ToDo: Decide whether this needs to be duplicated in the GraphLib version
		    // _queryBuilderPane.getQueryBuilderGraphFrame().getFocus ();
                    getGraphFrameCanvasFocus();
                    break;
                    // grid pane
                case KeyEvent.VK_2:
                    if ( DEBUG )
                        System.out.println(" Alt + 2 pressed. "); // NOI18N
                    if ( _queryBuilderPane.getQueryBuilderInputTable().getRowCount() > 0 ) {
                        _queryBuilderPane.getQueryBuilderInputTable().setRowSelectionInterval(0, 0);
                        _queryBuilderPane.getQueryBuilderInputTable().requestFocus( true );
                    }
                    break;
                    // SQL text pane
                case KeyEvent.VK_3:
                    if ( DEBUG )
                        System.out.println(" Alt + 3 pressed. "); // NOI18N
                    _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus( true );
                    break;
                    // Result Pane
                case KeyEvent.VK_4:
                    if ( DEBUG )
                        System.out.println(" Alt + 4 pressed. "); // NOI18N
                    _queryBuilderPane.getQueryBuilderResultTable().requestFocus( true );
                    break;
            }
        }
    }

    String checkTableName( String tableName ) throws SQLException {
	return qbMetaData.checkTableName( tableName );
    }

    String checkFullTableName( String fullTableName ) throws SQLException {
	return qbMetaData.checkFullTableName( fullTableName );
    }

    String checkColumnName( String tableName, String columnName ) throws SQLException {
	return qbMetaData.checkColumnName( tableName, columnName );
    }

	
    boolean checkColumnNameForTable( Column col, String tableName ) {
	return qbMetaData.checkColumnNameForTable( col, tableName );
    }

    
    boolean checkTableColumnName( Column col ) throws SQLException {
	return qbMetaData.checkTableColumnName( col );
    }
	

    /****
     * Check the database connection.
     * If no connection, ask to retry.
     * if user doesn't want to retry,
     * then disable the query editor
     */
//     public boolean checkDatabaseAndDisable(String query) {
//         if ( query == null )
// 	    query = _queryBuilderPane.getQueryBuilderSqlTextArea().getText() ;
//         if ( checkDatabaseConnection() == false ) {
// 	    Log.getLogger().finest("checkDatabaseConnection returns false ... \n " ); // NOI18N
//             // If we don't have a valid connection, disable all visual editing.
//             disableVisualEditing(query);
//             return false;
//         }
//         return true ;
//     }

    /**
     * Parse the query and regenerate all the panes
     * If parsing fails, raise an notification and do nothing else
     * If parsing succeeds, return true, false otherwise.
     * @param text query to parse
     * @param forceParse force a parse even if query hasn't changed
     */
    boolean populate(String query, boolean forceParse ) {

        Log.getLogger().entering("QueryBuilder", "populate", query); // NOI18N

        if ( ! forceParse ) {
            if ( query.trim().equals( _queryBuilderPane.getQueryBuilderSqlTextArea().getText().trim())) {
                // no change, just return.
		Log.getLogger().finest("  skipping populate(), no change") ; //NOI18N
                return true ;
            }
        }

        // Fix CR 6275870 Error when parsing invalid SQL
        if ( query.trim().equals( lastQuery ) ) {
            // no change, just return.
	    Log.getLogger().finest("  skipping populate(), no change") ; //NOI18N
            return true ;
        }
        else {
            lastQuery = new String (query.trim());
        }

        // if ( ! checkDatabaseAndDisable( query )) return false ;

        // First parse the query, and report any exception
        try {
            parseQuery(query);

            // if the parsing is successful and if the user modified query
            // by hand, then check for all the table names and column names.
            // if there is an error, give a message to the user and return false
            // else if there is only error in case and/or ommission of
            // tablename etc fix the query model with the correct values.

            if (!checkQuery()) 
                return false;

            _queryBuilderPane.getQueryBuilderGraphFrame().setQBGFEnabled( true ) ;
            _queryBuilderPane.setQueryBuilderInputTableEnabled( true ) ;
            _queryBuilderPane.getQueryBuilderGraphFrame().setTableColumnValidity(false) ;
            _queryBuilderPane.getQueryBuilderGraphFrame().setGroupBy(_queryModel.hasGroupBy() );
            _graphicsEnabled=true;
            // Will be done later by generate()
            // setSqlText(query);
        } catch (ParseException pe)	{
            Log.getLogger().severe("Parse error: " + pe.getLocalizedMessage());  // NOI18N
            promptForContinuation(pe.getMessage(), query);
            return false;
	} catch (TokenMgrError tme)	{
            Log.getLogger().severe("Parse error: " + tme.getLocalizedMessage());  // NOI18N
            promptForContinuation(tme.getMessage(), query);
            return false;

        } catch (SQLException sqe) {
            lastException = sqe ;
            Log.getLogger().severe("Parse error: " + sqe.getLocalizedMessage());  // NOI18N
            promptForContinuation(sqe.getMessage(), query);
            return false;
        }

        _parseErrorMessage = null ;
        
        // If parsing was successful...

        // ...generate the editor panes
        this.generate();

        // ...save the sql command.
        saveSqlCommand();

        _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus();

        return true;
    }


    /**
     * Ask the user whether to Retry&Continue or Cancel&Continue
     */
    private boolean promptForContinuation(String msg, String query) {

        // There could be an error or the typed SQL may not be SQL-92
        // compliant. Give the user an option to keep the query and test by
        // running it. If the user is satisfied then this could reflect in the
        // backing file. In that case the tables may not be displayed in the
        // graph properly. If the user thinks that there is a genuine error,
        // then the previous good query will be restored.

        Object[] options = {
            NbBundle.getMessage(QueryBuilder.class, "CONTINUE"), // "Continue"
            NbBundle.getMessage(QueryBuilder.class, "CANCEL")    // "Cancel"
        };
        if ( _queryBuilderPane.getQueryBuilderSqlTextArea().queryChanged() ) {
            int val = JOptionPane.showOptionDialog(
                    this,
                    (msg + "\n\n" +
                    NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE")
                    +
                    NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE_PROMPT")),
                    NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (val==JOptionPane.NO_OPTION) {    // Cancel - Revert to previous
                Log.getLogger().info("Query execution canceled"); // NOI18N
                _queryBuilderPane.getQueryBuilderSqlTextArea().restoreLastGoodQuery();
                _queryBuilderPane.getQueryBuilderGraphFrame().setQBGFEnabled( true ) ;
                _queryBuilderPane.setQueryBuilderInputTableEnabled( true ) ;
                _graphicsEnabled=true;
                _parseErrorMessage = null ;
            } else {    // Continue - Disable visual editing
                _parseErrorMessage = NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE"); // NOI18N
                disableVisualEditing(query);
            }
        } else {
            // display the message in the graph area
            _parseErrorMessage = NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE"); // NOI18N
            disableVisualEditing(query);
        }
        return false;
    }


    // Disable the graph and grid panes, leaving only the text pane.
    // Used when either a parse fails, or the database is down
    private void disableVisualEditing(String query)    {
        _graphicsEnabled=false;
        _queryBuilderPane.clear();
        _queryBuilderPane.getQueryBuilderGraphFrame().setQBGFEnabled( false ) ;
        _queryBuilderPane.setQueryBuilderInputTableEnabled( false ) ;

        String command = getSqlCommand();
        if ( query != null && query.trim().length() != 0) {
            setSqlText(query);
            setSqlCommand(query) ;
        }
        else {
            setSqlText(command);
        }
    }
    
    

    /***************************************************************************
     * Code for checking validity of queries
     **************************************************************************/

    /**
     * Check the various parts of the query for valid table/column names
     */
    private boolean checkQuery()  throws SQLException {

        if ( ( getSqlText()  != null ) ||
             ( _queryBuilderPane.getQueryBuilderGraphFrame().checkTableColumnValidity() ) ) 
        {

            // from
            if ( ! checkFrom() ) return false;

            // We  were calling a function to replace the "*" with the column 
            // names of the tables immediately after parsing the query. With the
            // introduction of routines to check the table and column names and 
            // to resolve them properly, if the from clause contains the table 
            // names not as they appear in database, we change them to match 
            // with those in the database. e.g. trip becomes "TRAVEL.TRIP". This
            // is done in function checkFrom which gets called after parsing is 
            // done. In the case of replaceStar being called immediately after 
            // parsing, the column names and table names were resolved without 
            // checking for their validity in the database. I have changed this 
            // sequence as follows:
            // 1. First parse the query.
            // 2. Check for table names in the from clause. If the names
            //    only differ in case or if they are missing schema name
            //    then they are corrected in the datamodel.
            // 3. These corrected table names are used to resolve the column 
            // names in SQLs like "select * from trip" when we call replaceStar()
            // function.  This should fix the problem where "select * from TRIP" 
            // was parsed properly but "select * from trip" used to give errors.
            // Bug Id : 4962093

            // we need to replace star after we validate all the table names
            // in the from list.
            _queryModel.replaceStar(this);

            // select
            if ( ! checkSelect() ) return false;

            // where
            if ( ! checkWhere() ) return false;
            
            // groupby
            if ( ! checkGroupBy() ) return false;

            // having
            if ( ! checkHaving() ) return false;

            // orderby
            if ( ! checkOrderBy() ) return false;

        } else {
            // we are not validating the fromList as the query has not been
            // changed by the user.
            _queryModel.replaceStar(this);
        }
        return true;
    }


    /**
     * Check the tables specified in the FROM clause (plus any columns
     * specified in join conditions), against the DB Schema
     */
    private boolean checkFrom() throws SQLException {

        if (DEBUG)
            System.out.println("checkFrom called... \n " ); // NOI18N
        
        // we could reuse this to find the tablename if the user
        // only specifies "select 'column_name' from 'table_name'"
        List<JoinTable> fromTables;
        // from
        if ( _queryModel.getFrom() != null ) {

            fromTables = _queryModel.getFrom().getTableList();
            for ( int i = 0; i < fromTables.size(); i++ ) {

                String fromTableName = ( (JoinTable) fromTables.get(i) ).getFullTableName();
                String fromTableSpec = ( (JoinTable) fromTables.get(i) ).getTableSpec();
                String checkedFullTableName = checkFullTableName( fromTableName ) ;

                if (DEBUG)
                    System.out.println("checkFullTableName called, fromTableName: " + fromTableName + " returns: " +
                                       checkedFullTableName + " \n " ); // NOI18N
                if ( checkedFullTableName == null ) {
                    // table not found, give an error
                    showTableColumnNameError( fromTableName ) ;
                    return false;
                } else if ( ! checkedFullTableName.equals( fromTableName ) ) {
                    // table found but maybe in a wrong case, replace
                    // it in the querymodel's from
                    if (DEBUG)
                        System.out.println(
                                " fromTableName = " + fromTableName  +  // NOI18N
                                " fromTableSpec = " + fromTableSpec  +  // NOI18N
                                " \n" ); // NOI18N
                    _queryModel.getFrom().setTableSpec(
                            fromTableSpec, checkedFullTableName );
                }

                // now check the columns in the condition if any.
                List<Column> fromColumns = new ArrayList<>();
                ( (JoinTable) fromTables.get(i) ).getReferencedColumns(fromColumns);
                for ( int j = 0; j < fromColumns.size(); j++ ) {
                    Column fromColumn = fromColumns.get(j);
                    if (! checkTableColumnName( fromColumn)) {
                        showTableColumnNameError(  fromColumn.getColumnName() );
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Check the tables specified in the SELECT clause, against the DB Schema
     */
    private boolean checkSelect()  throws SQLException  {
        if (DEBUG)
            System.out.println("checkSelect called. _queryModel.getSelect() = " + _queryModel.getSelect() ); // NOI18N
        if ( _queryModel.getSelect() != null ) {
            List<Column> selectColumns = new ArrayList<>();
            _queryModel.getSelect().getReferencedColumns(selectColumns);
            if ( ! checkColumns( selectColumns ) )
                return false;
        }
        return true;
    }

    /**
     * Check the tables specified in the WHERE clause against the DB schema
     */
    private boolean checkWhere()  throws SQLException {
        if (DEBUG)
            System.out.println("checkWhere called... " ); // NOI18N
        if ( _queryModel.getWhere() != null ) {
            List<Column> whereColumns = new ArrayList<>();
            _queryModel.getWhere().getReferencedColumns(whereColumns);
            if ( ! checkColumns( whereColumns ) ) 
                return false;
        }
        return true;
    }

    /**
     * Check the tables specified in the GROUP BY clause against the DB schema
     */
    private boolean checkGroupBy()  throws SQLException {
        if (DEBUG)
            System.out.println("checkGroupBy called... " ); // NOI18N
        if ( _queryModel.getGroupBy() != null ) {
            List<Column> groupByColumns = new ArrayList<>();
            _queryModel.getGroupBy().getReferencedColumns(groupByColumns);
            if ( ! checkColumns( groupByColumns ) ) return false;
        }
        return true;
    }

    /**
     * Check the tables specified in the HAVING clause against the DB schema
     */
    private boolean checkHaving()  throws SQLException {
        if (DEBUG)
            System.out.println("checkHaving called... " ); // NOI18N
        if ( _queryModel.getHaving() != null ) {
            List<Column> havingColumns = new ArrayList<>();
            _queryModel.getHaving().getReferencedColumns(havingColumns);
            if ( ! checkColumns( havingColumns ) ) return false;
        }
        return true;
    }

    /**
     * Check the tables specified in the ORDER BY clause against the DB schema
     */
    private boolean checkOrderBy()  throws SQLException {
        if (DEBUG)
            System.out.println("checkOrderBy called... " ); // NOI18N
        OrderBy orderBy = _queryModel.getOrderBy();
        if ( orderBy != null ) {
            List<Column> orderByColumns = new ArrayList<>();
            for ( int i = 0; i < orderBy.getSortSpecificationCount(); i++ ) {
                Column sortColumn = orderBy.getSortSpecification(i).getColumn();
                orderByColumns.add(sortColumn);
            }
            if ( ! checkColumns( orderByColumns ) ) return false;
        }
        return true;
    }

    // Check and correct any columns that may have wrong or missing table
    // specifications. If the column is not found in the database it displays
    // an error message and returns false. If there is just case mismatch
    // this function corrects the column name.
    // The column name could be :
    //    case 1 : <schema_name>.<table_name>.<column_name>
    //    case 2 : <table_name>.<column_name>
    //    case 3 : <alias_table_name>.<column_name>
    //    case 4 : <column_name>
    //
    private boolean checkColumns(List<Column> columns)  throws SQLException {
	Log.getLogger().entering("QueryBuilder", "checkColumns"); // NOI18N
        for ( int i = 0; i < columns.size(); i++ ) {
            Column column = columns.get(i);
            String columnTableSpec = column.getTableSpec();
            String columnFullTableName = column.getFullTableName();

            // If the user has specified a column without tablename resolve it
            // from the from_table_list.
            // Reversed the first test, switched || to &&.
            if ( ( columnFullTableName == null ) 	&&
                 ( _queryModel.getFrom() != null ) )
            {
                // Check every table in the From list, to see if any have
                // this column
                List<JoinTable> fromTables = _queryModel.getFrom().getTableList();
                boolean found=false;
                for ( int j = 0; j < fromTables.size(); j++ ) {
                    String fromTableName = ( (JoinTable) fromTables.get(j) ).getFullTableName();
                    // this could be an alias
                    String fromTableSpec = ( (JoinTable) fromTables.get(j) ).getTableSpec();

                    if (DEBUG)
                        System.out.println(
                                " checkColumns called " +  // NOI18N
                                " fromTableName = " + fromTableName  +  // NOI18N
                                " fromTableSpec = " + fromTableSpec ) ;  // NOI18N
                    // use the following function to check if fromTableSpec
                    // is in the database. If it is found update the column.
                    if ( checkColumnNameForTable( column, fromTableSpec )) {
                        found=true;
                        break;
                    }
                }
                // Give an error only if all the columns have been checked in all tables
                if (!found) {
                    // table not found, give an error
                    showTableColumnNameError( column.getColumnName() ) ;
                    return false;
                }
            }

            if (! checkTableColumnName(column)
                // Not clear what this test was for; it meant that we only reported an error if
                // the column that failed was the last one
                // && ( i == ( columns.size() - 1 ) )
                )
            {
                showTableColumnNameError( column.getColumnName() ) ;
                return false;
            }
            // the table has an alias, do not check the table name, just
            // check column name
        }
        return true;
    }

    /**
     * Report an unrecognized table/column name
     */
    private void showTableColumnNameError( String error ) {
        String msg = NbBundle.getMessage(QueryBuilder.class, "TABLE_COLUMN_NAME_ERROR");
        NotifyDescriptor d =
            new NotifyDescriptor.Message( error + " : " + msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        _parseErrorMessage = error + " : " + msg + "\n\n" ;
        String query = getSqlText();
        disableVisualEditing(query);
    }
    

    /**
     * Parse the current query (obtained from the RowSet).
     * @param the current query
     */
    private void parseQuery(String query) throws ParseException {

        Log.getLogger().entering("QueryBuilder", "parseQuery", query); // NOI18N

        // Initialize the QueryModel object if necessary
        if (_queryModel==null)
            _queryModel = new QueryModel(quoter);

        _queryModel.parse(query);
    }


    /**
     * Update the command property of the calling VSE, using the current
     * contents of the Sql Text.
     * Also, record this as the last valid query, for rollback purposes.
     */
    private void saveSqlCommand() {
        
        // Done in setText now?
        // _queryBuilderPane.getQueryBuilderSqlTextArea().saveLastGoodQuery();

        // Comapare the current SQL text to the existing Command value; don't do the
        // setValue operation if there has been no change
        String query = getSqlText();
        if (!query.equals( getSqlCommand())) {
	    Log.getLogger().finest("QB:  setting sql command to: " + query) ; //NOI18N
            setSqlCommand( query ) ;
        }
    }


    // Wrappers for SqlStatement methods, which are now handled by a combination of 
    // VisualSqlEditor and DatabaseConnection

    void setSqlCommand(String query) {
//        sqlStatement.setCommand(query) ;
	vse.setStatement(query);
    }

    String getSqlCommand() {
//	return sqlStatement.getCommand();
	return vse.getStatement();
    }

    // JDTODO - use dbconn
    String getConnectionInfo() {
//	return sqlStatement.getConnectionInfo();
        String conn = "";
        try {
            conn = getConnection().getMetaData().getURL();
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return conn;
    }
    
    /**
     * Return the java.sql.Connection associated with this dbconn, after opening 
     * it if necessary.
     */
    Connection getConnection() {

        Connection conn = dbconn.getJDBCConnection();
        if (conn== null) {
            // Try to (re-) open the connection
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
            conn = dbconn.getJDBCConnection();
            if (conn==null) {
                // Connection failed for some reason.  User may have cancelled.
                // If there's an exception, ConnectionDialog already reported it.
                String msg = NbBundle.getMessage(QueryBuilder.class, "CANNOT_ESTABLISH_CONNECTION");     // NOI18N
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);
            }
        }
        return conn;
    }
    
     
    // JDTODO - use dbconn, which requires ConnectionManager.disconnect
    void closeQB() {
//	sqlStatement.close();
    }
    

    // Wrappers for schema methods that are used by other classes in the query builder

    List<String> getColumnNames(String fullTableName) throws SQLException {
	return qbMetaData.getColumnNames( fullTableName );
    }

    public void getColumnNames(String fullTableName, List columnNames ) {
	qbMetaData.getColumnNames( fullTableName, columnNames );
    }
	
    List getImportedKeyColumns(String fullTableName) throws SQLException {
	return qbMetaData.getImportedKeyColumns( fullTableName );
    }

    List<String> getAllTables() throws SQLException {
	return qbMetaData.getAllTables();
    }

    List getPrimaryKeys(String fullTableName) throws SQLException {
	return qbMetaData.getPrimaryKeys( fullTableName );
    }
    
    List getForeignKeys(String fullTableName) throws SQLException {
	return qbMetaData.getForeignKeys( fullTableName );
    }
	
    String[] findForeignKey(String oldFullTableName, String newFullTableName, List foreignKeys) {
	return qbMetaData.findForeignKey( oldFullTableName, newFullTableName, foreignKeys );
    }

    String[] findForeignKey(String fullTableName1, String colName1, String fullTableName2, String colName2)
	throws SQLException
    {
	return qbMetaData.findForeignKey( fullTableName1, colName1, fullTableName2, colName2 );
    }

    /**
     * Execute the specified query against the database
     * @param query the query to execute
     */
    void executeQuery(String query) {

        Log.getLogger().entering("QueryBuilder", "executeQuery", query); // NOI18N

        String sqlCommand = getSqlText() ; // why not "query"?
        ResultSet result = null ;  // value to be returned.

        Connection connection = null ;
        PreparedStatement myStatement = null ;
        
        showBusyCursor ( true );
        
        boolean canExecute = true ;

        ParameterMetaData pmd = null;
        int paramCount =  0;
        try {
            connection = getConnection() ;
            if (connection==null) {
                canExecute=false;
               
            } else {

                myStatement = connection.prepareStatement(sqlCommand) ;
                pmd = myStatement.getParameterMetaData();
                paramCount =  pmd.getParameterCount();
                if (DEBUG) {
                    System.out.println(" Parameter Count  = " + paramCount);
                    for (int i = 1; i <= paramCount; i++) {
                        System.out.println(" Parameter Type  = " + pmd.getParameterType(i));
                        System.out.println(" Parameter Type Name = " + pmd.getParameterTypeName(i));
                    }
                }
            }
        } catch ( SQLException e) {
            reportDatabaseError(e); // NOI18N
            canExecute = false ;
        } catch ( AbstractMethodError e) {
            // Certain drivers (e.g., Sybase 5.5) can throw Errors because of incompatibility.  Catch and report.
	    Log.getLogger().severe("Error occurred when trying to retrieve table information: " + e); // NOI18N
            String title = NbBundle.getMessage(QueryBuilder.class, "PROCESSING_ERROR");
            JOptionPane.showMessageDialog( this, e.toString() + "\n\n", title, JOptionPane.ERROR_MESSAGE );
            canExecute = false ;
        }

        // Deal with any query parameters if we know about them.
        if ( canExecute && ( _queryModel != null ) )
        {
            if ( getParseErrorMessage() == null && _queryModel.isParameterized()) {

                List<String>  list = new ArrayList<>();
                _queryModel.getParameterizedPredicates(list);
                String[] parameters = new String[list.size()];
                String[] values = new String[list.size()];

                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = new String(list.get(i));
                }
                ParameterizedQueryDialog pqDlg =
		    new ParameterizedQueryDialog( parameters, true);
                if (pqDlg.getReturnStatus() == ParameterizedQueryDialog.RETURNED_OK) {

                    values = pqDlg.getParameterValues();
                    try {
                        for (int i = 0; i < values.length; i++) {
                            if (DEBUG) {
                                System.out.println(" command  = " + sqlCommand);
                                System.out.println("PreparedStatement i = " + i + " values = " + values[i]);
                            }
                            myStatement.setObject(i+1, values[i], pmd.getParameterType(i+1) );
                        }
    
                    } catch ( SQLException e) {
                        if ( isUnsupportedFeature(e)) {
                            Log.getLogger().log(Level.FINE, null, e);
                            String msg = NbBundle.getMessage(QueryBuilder.class,
                                    "PARAMETERS_NOT_SUPPORTED");
                            reportProcessingError(msg);
                        } else {
                            reportDatabaseError(e); // NOI18N
                        }
                        canExecute = false ;

                    }
                } else {
                    // cancelled the dialog.
                    canExecute = false ;
                }
            }
            else if (paramCount != 0) {
                // we have a query which can not be parsed.
                if (DEBUG) {
                    System.out.println(" param count = " + paramCount);
                }
                String[] parameters = new String[paramCount];
                String[] values = new String[paramCount];
                if (DEBUG) {
                    System.out.println(" parameters size  = " + parameters.length);
                    System.out.println(" values size  = " + values.length);
                }
    
                for (int i = 0; i < paramCount; i++) {
                    parameters[i] = new String( "Parameter " + Integer.valueOf(i).toString());
                }
                if (DEBUG) {
                    for (int i = 0; i < parameters.length; i++) {
                        System.out.println(" parameter  = " + parameters[i]);
                    }
                }
                ParameterizedQueryDialog pqDlg = new ParameterizedQueryDialog(
                    parameters, true);
                // System.out.println(pqDlg.getReturnStatus());
                if (pqDlg.getReturnStatus() == ParameterizedQueryDialog.RETURNED_OK) {
                    values = pqDlg.getParameterValues();
    
                    try {
                        for (int i = 0; i < values.length; i++) {
                            if (DEBUG) {
                                System.out.println(" command  = " + sqlCommand );
                                System.out.println("PreparedStatement i = " + i + " values = " + values[i] );
                            }
                            myStatement.setObject(i+1, values[i], pmd.getParameterType(i+1) );
                        }
    
                    } catch ( SQLException e) {
                        reportDatabaseError(e); // NOI18N
                        canExecute = false ;
                    }
                } else {
                    // cancelled the dialog.
                    canExecute = false ;
                }
            }
        }

        // Now execute the query
        if ( canExecute ) {

            try {
                boolean hasResults = myStatement.execute() ;
                if ( hasResults ) {
                    result = myStatement.getResultSet() ;
                    _queryBuilderPane.getQueryBuilderResultTable().displayResultSet(result);
                    result.close() ;
                }
                
            }  catch (SQLException e) {
                reportDatabaseError(e); // NOI18N
            }
        }
        
        // clean up stuff.
        try {
            if ( myStatement != null ) {
                myStatement.close() ;
            }
        } catch( SQLException se) {
            Log.getLogger().finest("Error Closing statement: " + se.getLocalizedMessage()); // NOI18N
        }
        
        showBusyCursor ( false );
        Log.getLogger().finest("Returning from QueryBuilder.executeQuery"); // NOI18N

    }


    /**
     * Initialize all the panes based on a new query passed in from the RowSet
     */
    void generate() {

        Log.getLogger().entering("QueryBuilder", "generate"); // NOI18N

        // Suppress updating of the text pane until we're ready
        _updateText = false;
        try {

            // Clear the current state.  It might be easier to recreate each of
            // the panes, as we did at startup.
            _queryBuilderPane.clear();
            // Generate the Diagram Pane and Grid Pane
            this.generateGraph();
        } finally {
            _updateText = true;
        }

        // Generate the Text Pane
        // It's not clear whether we should use the passed-in command,
        // or generate it from the model
        // _queryBuilderSqlTextArea.setText(command);
        this.generateText();
    }


    /**
     * Generate the query graph and tables that correspond to the current query model
     */
    private void generateGraph() {
        // If this is false, graphics and model are disabled.  Don't build graph/table.
        if (_graphicsEnabled)
            _queryBuilderPane.getQueryBuilderGraphFrame().generateGraph(_queryModel);
    }

    /**
     * Build the SQL query from the current model, and update the text pane
     */
    void generateText() {
        // If this is false, graphics and model are disabled.  Don't generate text.
        if (_graphicsEnabled) {
            setSqlText(_queryModel.genText());
            
            // Update command property except on first time through
            if ( ! firstTimeGenerateText ) {
                saveSqlCommand();
            } else  {
                firstTimeGenerateText = false;
            }
        }
    }


//     /**
//      * Disable everything except the SQL Text Pane
//      */
//     void disable() {
//     }

//     /**
//      * Re-enable the panes that were disabled
//      */
//     void enable() {
//     }


    /***************************************************************************
     * Accessors/Mutators
     **************************************************************************/

    /**
     * Return the current query model
     */
    QueryModel getQueryModel() {
        return _queryModel;
    }

    /**
     *  Restore the last good query.
     */

    QueryBuilderPane getQueryBuilderPane ()
    {
        return ( _queryBuilderPane );        
    }
    
    public VisualSQLEditor getVisualSQLEditor () {
        return vse;
    }

    /**
     * Get the current SQL Text from the SqlTextArea
     */
    private String getSqlText() {
        return _queryBuilderPane.getQueryBuilderSqlTextArea().getText() ;
    }

    /**
     * Set text in the SqlTextArea
     * @param text the new text
     */
    private void setSqlText(String text) {
        _queryBuilderPane.getQueryBuilderSqlTextArea().setText(text) ;
    }
    
    /**
     * Returns true if the current query is parameterized
     */
    private boolean isParameterized() {
        return _queryModel.isParameterized();
    }
    
    // Provide access to metadata object (currently used during text generation)
    public QueryBuilderMetaData getMetaData() {
	return qbMetaData;
    }
    

    // Methods inherited from org.openide.windows.TopComponent

    /**
     * About to show.  Could have been previously hidden ( e.g.,
     * when the user tabs back to the QueryEditor) or on first showing.
     *
     * We need to flush all the current information (model, graph, metadata),
     * and read the command, as if we were starting for the first time.
     */
    protected void componentShowing() {

        Log.getLogger().entering("QueryBuilder", "componentShowing"); // NOI18N

        String command = getSqlCommand();

        if (_queryModel==null)
            _queryModel = new QueryModel(quoter);

        Log.getLogger().finest("  * command=" + command) ;

        // Parse the current query, in case it has changed

        // Special case for handling null queries -- prompt for an initial table
        // We should probably allow this, since the user can delete the last table in the
        // editor anyway, so we need to be able to deal with empty queries as a special case.
        if ((command==null) || (command.trim().length()==0)) {
            Log.getLogger().finest("QBShowing command is null") ;
            setVisible(true);
            this.repaint();
            String msg = NbBundle.getMessage(QueryBuilder.class, "EMPTY_QUERY_ADD_TABLE");
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
            DialogDisplayer.getDefault().notify(d);

            _queryBuilderPane.getQueryBuilderGraphFrame().addTable() ;

        } else {

            String queryText = getSqlText();
            // parse and populate only if the query has changed.
            if ( queryText == null || (! command.trim().equalsIgnoreCase( queryText.trim() ) ) ) {
                this.populate(command, false);
                setVisible(true);
                this.repaint();
            }
        }
        activateActions();

        _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus();

        if ( DEBUG )
            System.out.println(" _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus () called. " ); // NOI18N

    }

    /**
     * Component is about to be shown.
     * Called when the user moves to another tab.
     * If we have an associated rowset, update it with current text query.
     */
    @Override
    protected void componentHidden() {
	Log.getLogger().entering("QueryBuilder", "componentHidden");
        String command = getSqlCommand();
        if ((command!=null) && (command.trim().length()!=0)) {
            String queryText = getSqlText();

            // parse and populate only if the query has changed.
            if ( queryText == null || (! command.trim().equalsIgnoreCase( queryText.trim() ) ) ) {
                if ( _graphicsEnabled ) {
                    boolean good = this.populate(queryText, true);
		    if ( !good) {
                        setSqlCommand(queryText); //HACK, temporary (jfb)
                    }
                } else {
                    setSqlCommand(queryText) ;
                }
            }
        }
        deactivateActions();
    }

    
    /** Opened for the first time */
    @Override
    protected void componentOpened() {

	Log.getLogger().entering("QueryBuilder", "componentOpened");

        activateActions();
        ActionMap map = getActionMap();
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        installActions(map, keys);
        
        // QueryBuilder does not need to listen to VSE, because it will notify us
        // directly if something changes. The SqlCommandCustomizer needs to listen 
        // to VSE, because that's the only way it is notified of changes to the command 
        // sqlStatement.addPropertyChangeListener(sqlStatementListener) ;
        // vse.addPropertyChangeListener(sqlStatementListener) ;

        // do NOT force a parse here.  It's done in componentShowing().
        // populate( sqlStatement.getCommand()) ;
    }

    /* closed - not visible anywhere)
     */
    @Override
    protected void componentClosed() {
	Log.getLogger().entering("QueryBuilder", "componentClosed");

        deactivateActions();

	// JDTODO - use dbconn?
	this.closeQB() ;
        lastQuery = null;
    }

    /*****
     * listener for changes in the sqlStatement - either the
     * command changed or the connection changed (e.g., datasource changed).
     */
    // No longer needed, since the customizer will just call vse.setStatement,
    // which can in turn update the QueryEditor
//    private PropertyChangeListener sqlStatementListener = new PropertyChangeListener() {
//        public void propertyChange(PropertyChangeEvent evt) {
//            // what property?
//            String propName = evt.getPropertyName() ;
//            Log.getLogger().finest("QB sqlStatement property change: " + propName ) ;
//            if ( propName.equals(VisualSQLEditor.PROP_STATEMENT)) {
//                Log.getLogger().finest(" newValue=" + getSqlCommand()) ;
//                populate( getSqlCommand() ) ;
//                _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus();
//
//             } else if ( propName.equals(SqlStatement.TITLE) ) { 
//                 Log.getLogger().finest(" title to " + sqlStatement.getTitle()) ; // NOI18N
//                 SwingUtilities.invokeLater(new Runnable() {
//                     public void run() {
//                         setDisplayName(sqlStatement.getTitle()) ;
//                     }
//                 }) ;

//             } else if ( propName.equals(SqlStatement.CLOSING)) {
//                 Log.getLogger().finest(" closing...") ; // NOI18N
//                 SwingUtilities.invokeLater(new Runnable() {
//                     public void run() {
//                         close() ;
//                     }
//                 }) ;
//             }
//        }
//    } ;

    /***
    private final JButton retryButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "RETRY_AND_CONTINUE")) ;
    private final JButton cancelButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "CANCEL_AND_CONTINUE")) ;
    **/
    /******
     * show a dialog with the a message saying the database connection is hosed.
     * It has a Retry and Cancel button.
     * returns true if the Retry is the closing action.
     */
//    int ii = 0 ;
//    private boolean showRetryDialog() {
//	Log.getLogger().entering("QueryBuilder", "showRetryDialog", ii++);
//        ConnectionStatusPanel csp = new ConnectionStatusPanel() ;
//        csp.configureDisplay(getConnectionInfo(), false, lastException.getLocalizedMessage(),  "", 0, false ) ;
//        csp.setGeneralInfo(NbBundle.getMessage(QueryBuilder.class, "DATABASE_CONNECTION_ERROR") ) ;    // NOI18N
//        csp.setFooterInfo(NbBundle.getMessage(QueryBuilder.class, "NO_DATABASE_CONNECTION") ) ;      // NOI18N
//
//        final JButton retryButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "RETRY_AND_CONTINUE")) ;
//        final JButton cancelButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "CANCEL_AND_CONTINUE")) ;
//
//        // this listener is for the dialog.
//        final Object[] retVal = new Object[1] ;
//        ActionListener listener = new ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                Log.getLogger().finest("  retry dialog event: " + evt) ;
//                retVal[0] = evt.getSource() ;
//            }
//
//        };
//
//        DialogDescriptor dlg = new DialogDescriptor(csp,
//                NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_title", getConnectionInfo()), // NOI18N
//                true/*modal*/,
//                new Object[] {retryButton, cancelButton}, cancelButton,
//                        DialogDescriptor.DEFAULT_ALIGN, null, listener);
//
//        dlg.setClosingOptions( null );
//
//        Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
//        dialog.setResizable(true);
//        dialog.pack() ;
//
//        // present dialog, waits for it to be disposed.
//        dialog.show();
//
//        boolean val = ( retVal[0] == retryButton ) ;
//        Log.getLogger().finest("  * dlg says:  Retry=" + val ) ;
//        return val ;
//    }


    private boolean isUnsupportedFeature(SQLException e) {
        return e.getErrorCode() == 17023 ||      // Oracle "Unsupported feature" error
	     "S1C00".equals(e.getSQLState());  // MySQL "Optional feature not supported" error
    }
    
    private void reportProcessingError(String msg) {
        // NbBundle.getMessage(QueryBuilder.class, key);
        String title = NbBundle.getMessage(QueryBuilder.class, "PROCESSING_ERROR");

        JOptionPane.showMessageDialog( this, msg + "\n\n", title, JOptionPane.ERROR_MESSAGE );        
    }
    private void reportDatabaseError(SQLException e) {

        Log.getLogger().log(Level.FINE, null, e);
        
        String msg = isUnsupportedFeature(e)
	    ? NbBundle.getMessage(QueryBuilder.class, "UNSUPPORTED_FEATURE")
	    : e.getLocalizedMessage();
        
        reportProcessingError(msg);

        /*
        ConnectionStatusPanel csp = new ConnectionStatusPanel() ;
        csp.configureDisplay(sqlStatement.getConnectionInfo(), false, e.getLocalizedMessage(),  "", 0, false ) ;
        csp.setGeneralInfo(msg) ;
        csp.displayDialog( sqlStatement.getConnectionInfo() ) ;
        */

    }

    String getParseErrorMessage() {
        return _parseErrorMessage;
    }

    /**
     * Showing/hiding busy cursor, before this funcionality was in Rave winsys,
     * the code is copied from that module.
     * It needs to be called from event-dispatching thread to work synch,
     * otherwise it is scheduled into that thread. */
    static void showBusyCursor(final boolean busy) {
        if(SwingUtilities.isEventDispatchThread()) {
            doShowBusyCursor(busy);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doShowBusyCursor(busy);
                }
            });
        }
    }

    private static void doShowBusyCursor(boolean busy) {
        JFrame mainWindow = (JFrame)WindowManager.getDefault().getMainWindow();
        if(busy){
            RepaintManager.currentManager(mainWindow).paintDirtyRegions();
            mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            mainWindow.getGlassPane().setVisible(true);
            mainWindow.repaint();
        } else {
            mainWindow.getGlassPane().setVisible(false);
            mainWindow.getGlassPane().setCursor(null);
            mainWindow.repaint();
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( "projrave_ui_elements_editors_about_query_editor" );        // NOI18N
    }
    
}
