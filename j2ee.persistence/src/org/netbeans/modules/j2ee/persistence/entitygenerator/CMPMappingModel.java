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

package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides the mapping for entity cmp beans to the database table.
 * This class is used by the application server plug-in to facilitate mapping.
 * @author Chris Webster
 */
public class CMPMappingModel {
    private Map<String, String> cmpFieldMapping;
    private Map<String, ColumnData[]> cmrFieldMapping;
    private String tableName;
    private Map<String, String> cmrJoinMapping;
    private Map <String, JoinTableColumnMapping> joinTableColumnMappings;
    
    public CMPMappingModel() {
        cmpFieldMapping = new HashMap<String, String>();
        cmrFieldMapping = new HashMap<String, ColumnData[]>();
        cmrJoinMapping = new HashMap<String, String>();
        joinTableColumnMappings = new HashMap();
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Map<String, String> getCMPFieldMapping() {
        return cmpFieldMapping;
    }
    
    public void setCMPFieldMapping(Map<String, String> m) {
        cmpFieldMapping = m;
    }
    
    public Map<String, ColumnData[]> getCmrFieldMapping() {
        return cmrFieldMapping;
    }
    
    public void setCmrFieldMapping(Map<String, ColumnData[]> m) {
        cmrFieldMapping = m;
    }
    
    public Map<String, String> getJoinTableMapping() {
        return cmrJoinMapping;
    }
    
    public void setJoinTableMapping(Map<String, String> m) {
        cmrJoinMapping = m;
    }
    
    public static class ColumnData {
        private String columnName;
        private boolean nullable;
        
        public ColumnData(String columnName, boolean nullable) {
            this.columnName = columnName;
            this.nullable = nullable;
        }
        
        public String getColumnName() {
            return this.columnName;
        }
        
        public boolean isNullable() {
            return this.nullable;
        }
      
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ColumnData)) {
                return false;
            }

            ColumnData other = (ColumnData) o;
            
            if(this.columnName.equals(other.columnName)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.columnName.hashCode();
        }
    }
    
    public static class JoinTableColumnMapping {
        private ColumnData[] columns;
        private ColumnData[] referencedColumns;
        private ColumnData[] inverseColumns;
        private ColumnData[] referencedInverseColumns;

        public ColumnData[] getColumns() {
            return columns;
        }

        public void setColumns(ColumnData[] columns) {
            this.columns = columns;
        }

        public ColumnData[] getReferencedColumns() {
            return referencedColumns;
        }

        public void setReferencedColumns(ColumnData[] referencedColumns) {
            this.referencedColumns = referencedColumns;
        }

        public ColumnData[] getInverseColumns() {
            return inverseColumns;
        }

        public void setInverseColumns(ColumnData[] inverseColumns) {
            this.inverseColumns = inverseColumns;
        }

        public ColumnData[] getReferencedInverseColumns() {
            return referencedInverseColumns;
        }

        public void setReferencedInverseColumns(ColumnData[] referencedInverseColumns) {
            this.referencedInverseColumns = referencedInverseColumns;
        }
    }
    
    public Map<String, JoinTableColumnMapping> getJoinTableColumnMppings() {
        return joinTableColumnMappings;
    }
    
    public void setJoiTableColumnMppings(Map<String, JoinTableColumnMapping> joinTableColumnMppings) {
        this.joinTableColumnMappings = joinTableColumnMppings;
    }
    
    @Override
    public int hashCode() {
        return tableName.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (! (o instanceof CMPMappingModel)) {
            return false;
        }
        
        CMPMappingModel other = (CMPMappingModel)o;
        
        if (cmrFieldMapping.size() != other.cmrFieldMapping.size()) {
            return false;
        }
        
        Iterator keyIt = cmrFieldMapping.keySet().iterator();
        while (keyIt.hasNext()) {
            String key = (String) keyIt.next();
            ColumnData[] value = (ColumnData[]) cmrFieldMapping.get(key);
            List l = Arrays.asList(value);
            Object otherValue = other.cmrFieldMapping.get(key);
            if (otherValue == null || 
                !l.equals(Arrays.asList((ColumnData[])other.cmrFieldMapping.get(key)))) {
                return false;
            }
        }
        
        return tableName.equals(other.tableName) &&
            cmrJoinMapping.equals(other.cmrJoinMapping) &&
            cmpFieldMapping.equals(other.cmpFieldMapping);
    }

}
