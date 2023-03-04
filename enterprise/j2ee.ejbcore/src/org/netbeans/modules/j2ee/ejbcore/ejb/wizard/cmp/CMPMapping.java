/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel.ColumnData;
import org.openide.filesystems.FileObject;

/**
 * This class provides the mapping for entity cmp beans to the database table.
 * This class is used by the application server plug-in to facilitate mapping.
 * 
 * @author Chris Webster
 */
public class CMPMapping implements OriginalCMPMapping {
    
    private String ejbName;
    private CMPMappingModel model;
    private FileObject schema;


    public CMPMapping(String ejbName, CMPMappingModel m, FileObject schema) {
        this.ejbName = ejbName;
        model = m;
        this.schema = schema;
    }

    public String getEjbName() {
        return ejbName;
    }
    
    public String getFieldColumn(String cmpFieldName) {
        return model.getCMPFieldMapping().get(cmpFieldName);
    }
    
    public String[] getRelationshipColumn(String cmrFieldName) {
        ColumnData[] columns =  model.getCmrFieldMapping().get(cmrFieldName);
        String[] names = new String[columns.length];
        for(int i = 0; i < columns.length; i ++) {
            names[i] = columns[i].getColumnName();
        }
        return names;
    }
    
    public FileObject getSchema() {
        return schema;
    }
    
    public void setTableName(String tableName) {
        model.setTableName(tableName);
    }
    
    public String getTableName() {
        return model.getTableName();
    }
    
    public String getRelationshipJoinTable(String cmrFieldName) {
        return model.getJoinTableMapping().get(cmrFieldName);
    }
    
    public CMPMappingModel getMappingModel() {
        return model;
    }
}
