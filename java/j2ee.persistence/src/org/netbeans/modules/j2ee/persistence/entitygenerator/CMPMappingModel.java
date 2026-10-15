/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        joinTableColumnMappings = new HashMap<>();
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
            
            return this.columnName.equals(other.columnName);
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
        
        Iterator<String> keyIt = cmrFieldMapping.keySet().iterator();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            ColumnData[] value = cmrFieldMapping.get(key);
            List<ColumnData> l = Arrays.asList(value);
            Object otherValue = other.cmrFieldMapping.get(key);
            if (otherValue == null || 
                !l.equals(Arrays.asList(other.cmrFieldMapping.get(key)))) {
                return false;
            }
        }
        
        return tableName.equals(other.tableName) &&
            cmrJoinMapping.equals(other.cmrJoinMapping) &&
            cmpFieldMapping.equals(other.cmpFieldMapping);
    }

}
