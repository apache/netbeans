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

/**
 * Represents database table column
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBColumn extends DBObject<DBTable> implements Comparable<DBColumn> {

    public static final int POSITION_UNKNOWN = Integer.MIN_VALUE;
    private boolean foreignKey;
    private int jdbcType;
    private String typeName;
    private String columnName;
    private boolean nullable;
    private boolean editable = true;
    private int ordinalPosition = POSITION_UNKNOWN;
    private boolean primaryKey;
    private int precision;
    private int scale;
    private boolean generated;
    private int displaySize;
    private String defaultValue;

    public DBColumn(DBTable table, String colName, int sqlJdbcType, String dbTypeName, int colScale, int colPrecision, boolean isNullable, boolean isGenerated) {
        super();

        setParentObject(table);
        columnName = colName;
        jdbcType = sqlJdbcType;
        typeName = dbTypeName;

        precision = colPrecision;
        scale = colScale;

        nullable = isNullable;
        generated = isGenerated;
        editable = (!table.getName().equals("") && !isGenerated);
    }

    @Override
    public int compareTo(DBColumn refColumn) {
        if (refColumn == null) {
            return -1;
        }

        if (refColumn == this) {
            return 0;
        }

        String myName = getDisplayName();
        myName = (myName == null) ? columnName : myName;

        if (!(refColumn instanceof DBColumn)) {
            return -1;
        }

        String refName = refColumn.getName();

        // compare primary keys
        if (this.isPrimaryKey() && !refColumn.isPrimaryKey()) {
            return -1;
        } else if (!this.isPrimaryKey() && refColumn.isPrimaryKey()) {
            return 1;
        }

        // compare foreign keys
        if (this.isForeignKey() && !refColumn.isForeignKey()) {
            return -1;
        } else if (!this.isForeignKey() && refColumn.isForeignKey()) {
            return 1;
        }

        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
    }

    @Override
    public boolean equals(Object refObj) {
        if (!(refObj instanceof DBColumn)) {
            return false;
        }

        DBColumn refMeta = (DBColumn) refObj;
        boolean result = super.equals(refObj);
        result &= (columnName != null) ? columnName.equals(refMeta.getName()) : (refMeta.getName() == null);
        result &= (jdbcType == refMeta.getJdbcType()) && (primaryKey == refMeta.isPrimaryKey()) && (foreignKey == refMeta.isForeignKey()) && (nullable == refMeta.isNullable()) && (scale == refMeta.getScale()) && (precision == refMeta.getPrecision()) && (ordinalPosition == refMeta.getOrdinalPosition());

        return result;
    }

    @Override
    public String getDisplayName() {
        return (displayName != null && displayName.trim().length() != 0) ? displayName.trim() : columnName.trim();
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getName() {
        return this.columnName;
    }

    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    public int getPrecision() {
        return precision;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }

    public String getQualifiedName(boolean quoteAlways) {
        StringBuilder buf = new StringBuilder(50);
        DBTable table = this.getParentObject();
        buf.append(quoteAlways ? table.getQuoter().quoteAlways(columnName) : table.getQuoter().quoteIfNeeded(columnName));
        return buf.toString();
    }

    public int getScale() {
        return scale;
    }

    @Override
    public int hashCode() {
        int myHash = super.hashCode();

        myHash += (columnName != null) ? columnName.hashCode() : 0;
        myHash += ordinalPosition;

        myHash += jdbcType + (10 * scale) + (100 * precision);
        myHash += primaryKey ? 1 : 0;
        myHash += foreignKey ? 2 : 0;
        myHash += generated ? 4 : 0;
        myHash += nullable ? 8 : 0;

        return myHash;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean hasDefault() {
        return defaultValue != null && defaultValue.trim().length() != 0;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    void setForeignKey(boolean newFlag) {
        foreignKey = newFlag;
    }

    void setOrdinalPosition(int cardinalPos) {
        this.ordinalPosition = cardinalPos;
    }

    void setPrimaryKey(boolean newFlag) {
        primaryKey = newFlag;
    }

    void setEditable(boolean editable) {
        this.editable = editable;
    }
}
