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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.util.CommandBuffer;

/**
 * This class factors out the DDL logic from the CreateTableDialog
 * 
 * @author <a href="mailto:david@vancouvering.com>David Van Couvering</a>
 */
public class CreateTableDDL {
    private Specification       spec;
    private String              schema;
    private String              tablename;

    public CreateTableDDL (
            Specification spec, 
            String schema,
            String tablename) {
        this.spec       = spec;
        this.schema     = schema;
        this.tablename  = tablename;
    }
    
    /**
     * Execute the DDL to create a table.  
     * 
     * @param columns - A Vector of ColumnItem representing the columns
     *      in the table
     * 
     * @param pkcols A Vector of ColumnItem representing the columns
     *      which are in the primary key for the table.  Can be null
     */
    public boolean execute(List<ColumnItem> columns, List<ColumnItem> pkcols) throws Exception {

        CommandBuffer cbuff = new CommandBuffer();
        List<CreateIndex> idxCommands = new ArrayList<CreateIndex>();

          CreateTable cmd = spec.createCommandCreateTable(tablename);

          cmd.setObjectOwner(schema);

          /* this variables and operation provide support for
           * creating indexes for primary or unique keys,
           * most of database are creating indexes by myself,
           * support was removed */
          org.netbeans.lib.ddl.impl.TableColumn cmdcol = null;
          CreateIndex xcmd = null;
          Iterator it = columns.iterator();
          while (it.hasNext()) {
              ColumnItem col = (ColumnItem)it.next();
              String name = col.getName();
              if (col.isPrimaryKey()&& !hasPrimaryKeys(pkcols))
                  cmdcol = cmd.createPrimaryKeyColumn(name);
              else if (col.isUnique()&&!col.isPrimaryKey())
                  cmdcol = cmd.createUniqueColumn(name);
              else cmdcol = cmd.createColumn(name);

              //bugfix for #31064
              //combo.setSelectedItem(combo.getSelectedItem());

              cmdcol.setColumnType(Specification.getType(col.getType().getType()));
              cmdcol.setColumnSize(col.getSize());
              cmdcol.setDecimalSize(col.getScale());
              cmdcol.setNullAllowed(col.allowsNull());
              String defval = col.getDefaultValue();
              if (defval != null && defval.length() > 0)
                  cmdcol.setDefaultValue(defval);
              if (col.hasCheckConstraint())
                  // add the TABLE check constraint
                  cmd.createCheckConstraint(name, col.getCheckConstraint());
              if (col.isIndexed()&&!col.isPrimaryKey()&&!col.isUnique()) {
                  xcmd = spec.createCommandCreateIndex(tablename);
                  // This index is referring to a tablename that is being
                  // created now, versus an existing one.  This
                  // means we shouldn't quote it.
                  xcmd.setNewObject(true);
                  xcmd.setIndexName(tablename+ "_" + name + "_idx"); // NOI18N
                  xcmd.setIndexType(new String());
                  xcmd.setObjectOwner(schema);
                  xcmd.specifyNewColumn(name);
                  idxCommands.add(xcmd);
              }
          }
          if( hasPrimaryKeys(pkcols) ) {
              cmdcol = cmd.createPrimaryKeyConstraint(tablename);
              cmdcol.setTableConstraintColumns(new Vector(pkcols));
              cmdcol.setColumnType(0);
              cmdcol.setColumnSize(0);
              cmdcol.setDecimalSize(0);
              cmdcol.setNullAllowed(true);

          }
          cbuff.add(cmd);
          for(int i=0;i<idxCommands.size();i++)
              cbuff.add(idxCommands.get(i));
          // index support removed!
          //if (icmd.getColumns().size()>0) cbuff.add(icmd);

          //execute DDL command
          cbuff.execute();
          
          return cbuff.wasException();
    }
    
    private boolean hasPrimaryKeys(List<ColumnItem> pkcols) {
        return pkcols != null && pkcols.size() > 0;
    }

}
