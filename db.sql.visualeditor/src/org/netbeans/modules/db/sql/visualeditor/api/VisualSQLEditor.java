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

