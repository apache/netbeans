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
package org.netbeans.modules.db.explorer.dlg;

import java.util.Iterator;
import java.util.Set;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.Specification;

/**
 * DDL for creating an index, refactored out of the AddIndex dialog
 *
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public class AddIndexDDL {
    private Specification       spec;
    private String              schema;
    private String              tablename;

    public AddIndexDDL (
            Specification spec, 
            String schema,
            String tablename) {
        this.spec       = spec;
        this.schema     = schema;
        this.tablename  = tablename;
    }
    
    /**
     * Execute the DDL to create an index.  
     * 
     * @param indexName the name of the index
     * @param isUnique set to true if a unique index
     * @param columns - A Vector of ColumnItem representing the columns
     *      in the index
     */
    public boolean execute(String indexName, 
            boolean isUnique, Set columns) throws Exception {
        CreateIndex icmd = spec.createCommandCreateIndex(tablename);
        
        icmd.setObjectOwner(schema);
        icmd.setIndexName(indexName);
        icmd.setIndexType(isUnique ? ColumnItem.UNIQUE : "");
        
        Iterator enu = columns.iterator();
        while (enu.hasNext()) {
            icmd.specifyColumn((String)enu.next());
        }
        
        icmd.execute();

        return icmd.wasException();
    }    
}
