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

import org.netbeans.lib.ddl.impl.AddColumn;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.lib.ddl.util.CommandBuffer;

/**
 * This class factors out the logic of actually adding a column to
 * the database.  It is responsible for interacting with the DDL package.
 * 
 * This refactoring is done to both separate the view from the underlying
 * logic, and also to make it more possible to unit test this logic
 */
public class AddTableColumnDDL {
    private Specification       spec;
    private String              schema;
    private String              tablename;
    

    public AddTableColumnDDL(
            Specification spec, 
            String schema,
            String tablename) {
        this.spec       = spec;
        this.schema     = schema;
        this.tablename  = tablename;
    }
        
    public boolean execute(String colname, ColumnItem citem) throws Exception {
        assert citem != null;
        assert colname != null;
        
        CommandBuffer cbuff = new CommandBuffer();

        AddColumn cmd = spec.createCommandAddColumn(tablename);
        cmd.setObjectOwner(schema);
        org.netbeans.lib.ddl.impl.TableColumn col = null;
        if (citem.isPrimaryKey()) {
          col = cmd.createPrimaryKeyColumn(colname);
        } else if (citem.isUnique()) {
          col = cmd.createUniqueColumn(colname);
        } else col = (TableColumn)cmd.createColumn(colname);
        col.setColumnType(Specification.getType(citem.getType().getType()));
        col.setColumnSize(citem.getSize());
        col.setDecimalSize(citem.getScale());
        col.setNullAllowed(citem.allowsNull());
        if (citem.hasDefaultValue()) col.setDefaultValue(citem.getDefaultValue());

        if (citem.hasCheckConstraint()) {
            // add COLUMN constraint (without constraint name)
            col.setCheckCondition(citem.getCheckConstraint());
        }

        cbuff.add(cmd);

        if (citem.isIndexed() && !citem.isPrimaryKey() && !citem.isUnique()) {
            CreateIndex xcmd = spec.createCommandCreateIndex(tablename);
            xcmd.setIndexName(tablename + "_" + colname + "_idx"); // NOI18N
            xcmd.setIndexType(new String());
            xcmd.setObjectOwner(schema);
            xcmd.specifyNewColumn(colname);
            cbuff.add(xcmd);
        }

        cbuff.execute();

        return cbuff.wasException();
    }
}
