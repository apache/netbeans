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

import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;

import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;


/**
 * For use by the Query Editor, this describes methods for
 * getting/setting the sql commnad (in whatever bean it exists) and
 * obtaining sql connections.
 *
 * The Query Editor will initialize itself from the <code>command</code> property
 * and possible set it's value by calling setCommand().
 *
 * The Query Editor will listen for PropertyChangeEvents
 * for the <code>connectionInfo</code> and <code>command</code> properties .
 *
 * A connectionInfo change means the connection info changed and the
 * query editor should close and then obtain new connections.  The query editor
 * does not care what the old and new values are.
 * Do a  firePropertyChange(CONNECTION_INFO, ...) if the old connections
 * are no longer valid (data source changed, schema list changed, etc.)
 *
 * If the command (sql text) is changed somewhere else, notify the query editor
 * by changing the <code>command</code> property.
 *
 * @author jfbrown
 */
public interface SqlStatement {

    /**
     * The attribute for a human readable form describing the connection.
     * A change of this property tells the QueryBuilder that any connections
     * it holds should be closed.
     */
    public static final String CONNECTION_INFO = "connectionInfo" ;
    /**
     * the attribute holding the SQL Statement
     */
    public static final String COMMAND = "command" ;
    /**
     * A property change of this signifies that this object is closing.
     * Users of this instance of SqlStatment should clean up their references.
     */
    public static final String CLOSING = "closing" ;
    /**
     * The attibute signifies what QueryBuilder should use for it's
     * window title.
     **/
    public static final String TITLE = "title" ;

    /**
     * Property nam
    /**
     * Obtain a read only connection to the database.  Improved performance
     * may be obtained when using a read only connection.
     * If you need to update the database, @see #getConnection()
     *
     * It is the caller's responsibility to close this connection
     * when finished.
     *
     * @return a valid connection to a database.
     **/
    public Connection getReadOnlyConnection()  throws SQLException ;

    /**
     * Obtains a connection to the database.
     * If the caller does not plan to update the database, the caller
     * should use @see #getReadOnlyConnection()
     *
     * It is the caller's responsibility to close this connection
     * when finished.
     *
     * @return a valid connection to a database.
     **/
    public Connection getConnection() throws SQLException ;


    /**
     * Checks to see if the connection is still valid.
     *
     * Throws an SQLException if the connection is not not valid.
     **/
    public void validateConnection(Connection connection) throws SQLException ;


    /**
     * Get the value of the SQL command.
     */
    public String getCommand() ;

    /**
     * Set the value of the SQL command.  This method should fire a
     * propertyChangeEvent for COMMAND.
     */
    public void setCommand(String command) ;

    /**
     * Gets the connection info.  Query Editor uses this value for error dialogs
     * and debugging messages.  Trigger a propertyChangeEvent on the connectionInfo
     * property whenever the connection changes.
     */
    public String getConnectionInfo() ;

    /**
     * Add a PropertyChangeListener to the listener list.  The listener is registered
     * for all properties.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) ;

    /**
     * Add a PropertyChangeListener for a specific property.  The listener is will be invoked
     * only for property changes on that specific property.
     * for all properties.
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) ;

    /**
     * Remove a PropertyChangeListener from the listener list.  This removes a
     * PropertyChangeListener that was registered for all properties.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) ;

    /**
     * Remove a PropertyChangeListener for a specific property.
     */
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) ;
    
    /**
     * Close out all resources because the QueryEditor is closing and
     * this object is no longer needed.
     * Use this method to deregister any listeners to other objects, close db connections, etc.
     */
    public void close() ;
   
    /**
     * QueryBuilder window title for this statement.
     */
    public String getTitle() ;
    
    /**
     * gets an instance of the database's meta data cache to be used for
     * this sql statement.
     */
    public VisualSQLEditorMetaData getMetaDataCache() throws SQLException ;
  
}
