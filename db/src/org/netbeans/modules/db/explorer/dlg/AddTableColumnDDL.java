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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
