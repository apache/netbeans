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
package org.netbeans.modules.db.dataview.api;

import java.awt.Component;
import java.sql.SQLWarning;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * DataView to show data of a given sql query string, provides static method to create 
 * the DataView Pannel from a given sql query string and a connection. 
 *
 * @author Ahimanikya Satapathy
 */
public class DataView {

    org.netbeans.modules.db.dataview.output.DataView delegate;

    /**
     * Create and populate a DataView Object. Populates 1st data page of default size.
     * The caller can run this in background thread and then create the GUI components 
     * to render to render the DataView by calling DataView.createComponent()
     * 
     * @param dbConn instance of DBExplorer DatabaseConnection 
     * @param queryString SQL query string
     * @param pageSize default page size for this data view
     * @return a new DataView instance
     */
    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize) {
        DataView dataView = new DataView();
        dataView.delegate = org.netbeans.modules.db.dataview.output.DataView.create(dbConn, sqlString, pageSize);
        return dataView;
    }

    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize, boolean nbOutputComponent) {
        DataView dataView = new DataView();
        dataView.delegate = org.netbeans.modules.db.dataview.output.DataView.create(dbConn, sqlString, pageSize, nbOutputComponent);
        return dataView;
    }

    /**
     * Create the UI component and renders the data fetched from database on create()
     * 
     * @param dataView DataView Object created using create()
     * @return a JComponent that after rending the given dataview
     */
    public List<Component> createComponents() {
        return delegate.createComponents();
    }

    /**
     * Returns true if there were any expection in the last database call.
     * 
     * @return true if error occurred in last database call, false otherwise.
     */
    public boolean hasExceptions() {
        return delegate.hasExceptions();
    }

    /**
     * Returns true if there were any warnings in the last database call.
     * 
     * @return true if a warning resulted from the last database call, false otherwise.
     */
    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }
    
    /**
     * Returns true if the statement executed has ResultSet.
     * 
     * @return true if the statement executed has ResultSet, false otherwise.
     */
    public boolean hasResultSet() {
        return delegate.hasResultSet();
    }

    /**
     * Returns Collection of a error messages of Throwable type, if there were any 
     * expection in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<Throwable> getExceptions() {
        return delegate.getExceptions();
    }

    /**
     * Returns Collection of SQLWarnings, if there were any 
     * warnings in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<SQLWarning> getWarnings() {
        return delegate.getWarnings();
    }
    
    /**
     * If exception is reportet this indicates the position of the error.
     * 
     * <p>The reported position is the zero-based index into the supplied SQL</p>
     * 
     * @return position of reported error, -1 if not available
     */
    public int getErrorPosition() {
        return delegate.getErrorPosition();
    }
    
    /**
     * Get updated row count for the last executed sql statement.
     * 
     * @return number of rows updated in last execution, -1 if no rows updated
     */
    public int getUpdateCount() {
        return delegate.getUpdateCount();
    }

    /**
     * Get modified row counts for the last executed sql statement.
     * 
     * @return number of rows updated in last execution, index is the xth result for this statement
     */
    public List<Integer> getUpdateCounts() {
        return delegate.getUpdateCounts();
    }
    
    /**
     * Get fetchtimes for the resultsets of the last executed sql statement.
     * 
     * @return fetchtime in ms, index is the xth result for this statement
     */
    public List<Long> getFetchTimes() {
        return delegate.getFetchTimes();
    }
    
    /**
     * Get execution time for the last executed sql statement
     * 
     * @return execution time for last executed sql statement in milliseconds
     */
    public long getExecutionTime() {
        return delegate.getExecutionTime();
    }

    /**
     * Returns editing tool bar items.
     * 
     * @return an array of JButton
     */
    public JButton[] getEditButtons() {
        return delegate.getEditButtons();
    }

    /**
     * Sets editable mode to true/false for this component
     *
     * @param editable if set to false, all the editing functionality will be disabled
     */
    public void setEditable(boolean editable) {
        delegate.setEditable(editable);
    }

    private DataView() {
    }
}
