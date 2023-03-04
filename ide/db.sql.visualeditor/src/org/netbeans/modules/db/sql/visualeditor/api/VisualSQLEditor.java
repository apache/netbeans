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

package org.netbeans.modules.db.sql.visualeditor.api;

import java.awt.Component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder;

/**
 * Class to encapsulate a visual SQL editor.
 *
 * @author Jim Davidson
 */
public final class VisualSQLEditor {

    // Private fields
    
    /**
     * Property corresponding to the SQL statement; for listening.
     */
    public static final String		PROP_STATEMENT="STATEMENT";

    private String 			statement;
    private VisualSQLEditorMetaData 	metadata;
    private DatabaseConnection		dbconn;
    private Component			queryBuilder=null;
    
    private PropertyChangeSupport 	changeSupport = new PropertyChangeSupport(this);


    /*
     * Constructor for VisualSQLEditor.
     * Package protected, used only by Factory class.
     */
    VisualSQLEditor(DatabaseConnection dbconn, String statement, VisualSQLEditorMetaData metadata) {
	this.dbconn = dbconn;
	this.statement = statement;
	this.metadata = metadata;
    }

    /**
     * Create and open the QueryBuilder that backs up this VisualSQLEditor instance.
     *
     * @return the new QueryBuilder component
     */
    public Component open() {
        // return QueryBuilder.open(dbconn, statement, metadata);
	queryBuilder = QueryBuilder.open(dbconn, statement, metadata, this);
	return queryBuilder;
    }


    /**
     * Returns the current value of the SQL statement
     *
     * @return the statement
     */
    public String getStatement(){
	return this.statement;
    }

    /**
     * Sets the value of the SQL statement
     *
     * @param statement - the new statement value
     */
    public void setStatement(String statement) {
	String oldValue = this.statement;
	this.statement = statement;
        changeSupport.firePropertyChange(PROP_STATEMENT, oldValue, statement);
    }


    /**
     * Adds a property change listener.  The only property of interest is PROP_STATEMENT,
     * which contains the SQL query.
     *
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener){
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.  
     *
     * @param listener - the listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener){
        changeSupport.removePropertyChangeListener(listener);
    }

}

