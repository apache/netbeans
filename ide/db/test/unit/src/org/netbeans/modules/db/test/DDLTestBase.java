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

package org.netbeans.modules.db.test;

import java.sql.Types;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.impl.AddColumn;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.CreateView;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.lib.ddl.util.CommandBuffer;

/**
 *
 * @author David
 */
public class DDLTestBase extends DBTestBase {
    private static SpecificationFactory specfactory;
    private Specification spec;
    private DriverSpecification drvspec;

    private static Logger LOGGER = Logger.getLogger(DDLTestBase.class.getName());

    public DDLTestBase(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (specfactory == null) {
            specfactory = new SpecificationFactory();
        }
        spec = (Specification)specfactory.createSpecification(getConnection());

        // All this is copied from ConnectionNodeInfo.java
        drvspec = specfactory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
        if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
            //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
            drvspec.setMetaData(getConnection().getMetaData());
        else
            drvspec.setMetaData(spec.getMetaData());
        drvspec.setCatalog(getConnection().getCatalog());
        drvspec.setSchema(getSchema());
    }

    protected final Specification getSpecification() throws Exception {
        return spec;
    }
    
    protected final DriverSpecification getDriverSpecification() throws Exception {
        return drvspec;
    }
        
    protected void createBasicTable(String tablename, String pkeyName)
            throws Exception {
        dropTable(tablename);
        CommandBuffer cbuff = new CommandBuffer();

        // Uncomment this if you want to see what the SQL looks like
        // cbuff.setDebugMode(true);

        CreateTable cmd = getSpecification().createCommandCreateTable(tablename);
        cmd.setObjectOwner(getSchema());

        // primary key
        TableColumn col = cmd.createPrimaryKeyColumn(pkeyName);
        col.setColumnType(Types.INTEGER);
        col.setNullAllowed(false);

        cbuff.add(cmd);
        cbuff.execute();
    }

    protected void createView(String viewName, String query) throws Exception {
        CreateView cmd = getSpecification().createCommandCreateView(viewName);
        cmd.setQuery(query);
        cmd.setObjectOwner(getSchema());
        cmd.execute();

        assertFalse(cmd.wasException());
    }

    protected void createSimpleIndex(String tablename,
            String indexname, String colname) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        CreateIndex xcmd = getSpecification().createCommandCreateIndex(tablename);
        xcmd.setIndexName(indexname);

        // *not* unique
        xcmd.setIndexType(new String());

        xcmd.setObjectOwner(getSchema());
        xcmd.specifyColumn(fixIdentifier(colname));

        xcmd.execute();
    }

    /**
     * Adds a basic column.  Non-unique, allows nulls.
     */
    protected void addBasicColumn(String tablename, String colname,
            int type, int size) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        AddColumn cmd = getSpecification().createCommandAddColumn(tablename);
        cmd.setObjectOwner(getSchema());
        TableColumn col = (TableColumn)cmd.createColumn(colname);
        col.setColumnType(type);
        col.setColumnSize(size);
        col.setNullAllowed(true);

        cmd.execute();
        if ( cmd.wasException() ) {
            throw new Exception("Unable to add column");
        }
    }
}
