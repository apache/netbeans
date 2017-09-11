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
package org.netbeans.modules.db.dataview.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBTable;

/**
 * Wrapper class provides ordered columns and tooltips
 *
 * @author Ahimanikya Satapathy
 */
public class DataViewDBTable {

    private final DBTable[] dbTables;
    private final List<DBColumn> columns;

    public DataViewDBTable(Collection<DBTable> tables) {
        assert tables != null;

        dbTables = new DBTable[tables.size()];
        List<DBColumn> cols = new ArrayList<>();

        for (DBTable tbl : tables.toArray(dbTables)) {
            cols.addAll(tbl.getColumnList());
        }
        Collections.sort(cols, new ColumnOrderComparator());
        columns = Collections.unmodifiableList(cols);
    }

    public DBTable getTable(int index) {
        return dbTables[index];
    }

    public int getTableCount() {
        return dbTables.length;
    }

    public boolean hasOneTable() {
        return dbTables != null && dbTables.length == 1 && !dbTables[0].getName().equals("");
    }

    public String getFullyQualifiedName(int index, boolean quoteAlways) {
        return dbTables[index].getFullyQualifiedName(quoteAlways);
    }

    public DBColumn getColumn(int index) {
        return columns.get(index);
    }

    public int getColumnType(int index) {
        return columns.get(index).getJdbcType();
    }

    public String getColumnName(int index) {
        return columns.get(index).getName();
    }

    public String getQualifiedName(int index, boolean quoteAlways) {
        return columns.get(index).getQualifiedName(quoteAlways);
    }

    public int getColumnCount() {
        return columns.size();
    }

    public List<DBColumn> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public synchronized Map<String,DBColumn> getColumnMap() {
        Map<String, DBColumn> colMap = new HashMap<>();
        for (DBTable tbl : dbTables) {
            colMap.putAll(tbl.getColumns());
        }
        return Collections.unmodifiableMap(colMap);
    }

    private final class ColumnOrderComparator implements Comparator<DBColumn> {

        private ColumnOrderComparator() {
        }

        @Override
        public int compare(DBColumn col1, DBColumn col2) {
            return col1.getOrdinalPosition() - col2.getOrdinalPosition();
        }
    }
}
