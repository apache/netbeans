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
package org.netbeans.modules.db.dataview.meta;

import java.util.Collections;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 * Holds DB PrimaryKey meta info of a given table.
 *
 * @author Ahimanikya Satapathy
 */
public final class DBPrimaryKey extends DBObject<DBTable> {
    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
    private static final String RS_KEY_NAME = "PK_NAME"; // NOI18N
    private static final String RS_KEY_SEQ = "KEY_SEQ"; // NOI18N
    private List<String> columnNames;
    private String name;
    private DBTable parent;

    public DBPrimaryKey(ResultSet rs) throws SQLException {
        assert rs != null;

        Map<Integer,String> pkColumns = new HashMap<Integer,String>();

        while (rs.next()) {
            int keySeq = rs.getShort(RS_KEY_SEQ);
            String columName = rs.getString(RS_COLUMN_NAME);

            pkColumns.put(keySeq, columName);

            name = rs.getString(RS_COLUMN_NAME);
            String tmpName = rs.getString(RS_KEY_NAME);
            if (!DataViewUtils.isNullString(tmpName) && name == null) {
                name = tmpName;
            }
        }

        columnNames = new ArrayList<String>();

        List<Integer> columnEntries = new ArrayList<Integer>(pkColumns.keySet());
        Collections.sort(columnEntries);

        for(Integer id: columnEntries) {
            columnNames.add(pkColumns.get(id));
        }
    }

    public boolean contains(DBColumn col) {
        return contains(col.getName());
    }

    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof DBPrimaryKey)) {
            return false;
        }

        DBPrimaryKey ref = (DBPrimaryKey) refObj;
        boolean result = (getName() != null) ? name.equals(ref.name) : (ref.name == null);
        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames != null);
        return result;
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public String getName() {
        if (name == null && parent != null) {
            name = "PK_" + parent.getName(); // NOI18N
        }
        return name;
    }

    @Override
    public int hashCode() {
        int myHash = (getName() != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        return myHash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(100);
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                buf.append(","); // NOI18N
            }
            buf.append((columnNames.get(i)).trim());
        }
        return buf.toString();
    }
}
