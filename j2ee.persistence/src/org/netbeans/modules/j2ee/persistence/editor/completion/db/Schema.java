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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.dbschema.DBException;
import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;
import org.openide.util.Exceptions;

/**
 * 
 * @author Andrei Badea
 */
public class Schema {
    
    private DBMetaDataProvider provider;
    private Catalog catalog;
    private String name;
    private Set/*<String>*/ tableNames;
    
    private ConnectionProvider cp;
    private SchemaElementImpl schemaElementImpl;
    private SchemaElement schemaElement;
    
    // XXX views
    
    Schema(DBMetaDataProvider provider, Catalog catalog, String name) {
        this.provider = provider;
        this.catalog = catalog;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized String[] getTableNames() throws SQLException {
        if (tableNames == null) {
            tableNames = getTableNamesByType("TABLE"); // NOI18N
        }
        
        return (String[])tableNames.toArray(new String[tableNames.size()]);
    }
    
    public TableElement getTable(String tableName) throws SQLException {
        SchemaElement schemaElement;
        synchronized (this) {
            schemaElement = this.schemaElement;
        }
        if (schemaElement == null) {
            cp = new ConnectionProvider(provider.getConnection(), provider.getDriverClass());
            cp.setSchema(name);
            schemaElementImpl = new SchemaElementImpl(cp);
            try {
                schemaElementImpl.setName(DBIdentifier.create("foo")); // XXX set a proper name
            } catch (DBException e) {
                Exceptions.printStackTrace(e);
            }
            schemaElement = new SchemaElement(schemaElementImpl);
            synchronized (this) {
                this.schemaElement = schemaElement;
            }
        }
        
        DBIdentifier tableId = DBIdentifier.create(tableName);
        TableElement tableElement = schemaElement.getTable(tableId);
        if (tableElement == null) {
            LinkedList tableList = new LinkedList();
            tableList.add(tableName);
            LinkedList viewList = new LinkedList();
            schemaElementImpl.initTables(cp, tableList, viewList, false);
            
            tableElement = schemaElement.getTable(tableId);
        }
        
        return tableElement;
    }
    
    public synchronized void refresh() {
        schemaElement = null;
        tableNames = null;
    }
    
    private Set/*<String>*/ getTableNamesByType(String type) throws SQLException {
        Set/*<String>*/ result = new TreeSet();

        ResultSet rs = provider.getMetaData().getTables(catalog.getName(), name, "%", new String[] { type }); // NOI18N
        try {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME"); // NOI18N
                result.add(tableName);
            }
        } finally {
            rs.close();
        }

        return result;
    }
    
    public String toString() {
        return "Schema[catalog=" + catalog + ",name='" + name + "']"; // NOI18N
    }
}
