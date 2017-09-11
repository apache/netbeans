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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a generalized table
 */
public class TableNode implements Table {

    // Fields

    private Identifier      _tableName;

    private Identifier      _corrName;

    private Identifier      _schemaName;


    // Constructors

    public TableNode(String tableName, String corrName, String schemaName) {
        _tableName = new Identifier(tableName);
        _corrName = corrName==null ? null : new Identifier(corrName);
        _schemaName = schemaName==null ? null : new Identifier(schemaName);
    }

    public TableNode(String tableName, String corrName) {
        this(tableName, corrName, null);
    }

    public TableNode(String tableName) {
        this(tableName, null, null);
    }

    public TableNode() {
    }

    // PseudoConstructor
    // Use static methods because we can't overload Identier, String
    public static TableNode make(Identifier tableName, Identifier corrName, Identifier schemaName) {
        TableNode t = new TableNode();
        t._tableName = tableName;
        t._corrName = corrName;
        t._schemaName = schemaName;
        return t;
    }


    // Methods
    public String genText(SQLIdentifiers.Quoter quoter) {
        return genText(quoter, false);
    }

    // Return the SQL string that corresponds to this Table
    // This was originally called only in FROM clauses, but is now used as
    // part of column specifications.
    // For now, assume no joins
    public String genText(SQLIdentifiers.Quoter quoter, boolean from) {
        if (from)       // Calling from within a FROM clause
            return
                ((_schemaName==null) ? "" : _schemaName.genText(quoter)+".") +  // NOI18N
                _tableName.genText(quoter) +
                // remove AS to fix CR5097412
                ((_corrName==null) ? "" : " " + _corrName.genText(quoter));  // NOI18N
        else            // Calling from within a column
            return
                ((_corrName!=null)
                 ? _corrName.genText(quoter)
                 : ((_schemaName==null) ? "" : _schemaName.genText(quoter)+".") +  // NOI18N
                 _tableName.genText(quoter));
    }


    // Accessors/Mutators

    public String getTableName() {
        return _tableName.getName();
    }

    public String getFullTableName() {
        return
            ((_schemaName==null) ? "" : _schemaName.getName()+".") +   // NOI18N
            _tableName.getName();
    }

    public String getCorrName() {
        return (_corrName==null) ? null : _corrName.getName();
    }

    public String getSchemaName() {
        return (_schemaName==null) ? null : _schemaName.getName();
    }

    public String getTableSpec() {
        return (_corrName!=null) ?
               _corrName.getName() :
               getFullTableName();
    }

    // if oldTableSpec is null, just replace the table schema and name.
    public void renameTableSpec(String oldTableSpec, String corrName) {
        // If we really have a correlation name, there cannot be any schema specified to split
        if (oldTableSpec == null) {
            String[] table = corrName.split("\\.");    // NOI18N
            if (table.length>1) {
                _schemaName= new Identifier(table[0]);
                _tableName = new Identifier(table[1]);
            } else
                _tableName= new Identifier(table[0]);
        }
        else if (getTableSpec().equals(oldTableSpec))
            _corrName=(corrName==null) ? null : new Identifier(corrName);
    }

    void setTableName (String tableName) {
        String[] table = tableName.split("\\.");    // NOI18N
        if (table.length>1) {
            _schemaName= new Identifier(table[0]);
            _tableName = new Identifier(table[1]);
        } else
            _tableName=new Identifier(table[0]);
    }

    void setCorrName (String corrName) {
        _corrName=new Identifier(corrName);
    }

    void setTableSpec(String oldTableSpec, String newTableSpec) {
        if (getTableSpec().equals(oldTableSpec)) {
            String[] table = newTableSpec.split("\\.");    // NOI18N
            if (table.length>1) {
                _schemaName= new Identifier(table[0]);
                _tableName = new Identifier(table[1]);
            } else
                _tableName=new Identifier(table[0]);
        }

    }

    public void getReferencedColumns(Collection columns) {}
}
