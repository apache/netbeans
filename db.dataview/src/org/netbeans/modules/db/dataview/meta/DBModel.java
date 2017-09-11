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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container object for database tables.
 *
 * @author Ahimanikya Satapathy
 */
public final class DBModel extends DBObject<Object> {

    private static final String FQ_TBL_NAME_SEPARATOR = "."; // NOI18N
    private Map<String, DBTable> tables;
    private int dbType;

    public DBModel() {
        tables = new HashMap<String, DBTable>();
    }

    public synchronized void addTable(DBTable table) {
        if (table != null) {
            String fqName = getFullyQualifiedTableName(table);
            table.setParentObject(this);
            tables.put(fqName, table);
        }
    }

    @Override
    public boolean equals(Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof DBModel) {
            DBModel aSrc = (DBModel) refObj;

            if (tables != null && aSrc.tables != null) {
                Set<String> objTbls = aSrc.tables.keySet();
                Set<String> myTbls = tables.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                boolean tblCheck = myTbls.containsAll(objTbls) && objTbls.containsAll(myTbls);
                result &= tblCheck;
            }
        }
        return result;
    }

    public String getFullyQualifiedTableName(DBTable tbl) {
        if (tbl != null) {
            String tblName = tbl.getName();
            String schName = tbl.getSchema();
            String catName = tbl.getCatalog();

            StringBuilder buf = new StringBuilder(50);

            if (catName != null && catName.trim().length() != 0) {
                buf.append(catName.trim());
                buf.append(FQ_TBL_NAME_SEPARATOR);
            }

            if (schName != null && schName.trim().length() != 0) {
                buf.append(schName.trim());
                buf.append(FQ_TBL_NAME_SEPARATOR);
            }

            buf.append(tblName.trim());
            return buf.toString();
        }

        return null;
    }

    public DBTable getTable(String fqTableName) {
        return this.tables.get(fqTableName);
    }

    public int getDBType() {
        return dbType;
    }

    @Override
    public int hashCode() {
        int myHash = 0;
        if (tables != null) {
            myHash += tables.keySet().hashCode();
        }
        return myHash;
    }

    @Override
    public String toString() {
        return this.getDisplayName();
    }

    void setDBType(int dbType) {
        this.dbType = dbType;
    }
}
