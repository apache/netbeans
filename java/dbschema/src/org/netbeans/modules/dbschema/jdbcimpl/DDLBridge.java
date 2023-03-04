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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;


import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.util.Exceptions;

public class DDLBridge extends Object {

    private DriverSpecification drvSpec;

    /** Creates new DDLBridge */
    public DDLBridge(Connection con, String schema, DatabaseMetaData dmd) {
        try {
            SpecificationFactory fac = new SpecificationFactory();
            drvSpec = fac.createDriverSpecification(dmd.getDriverName().trim());
            drvSpec.setMetaData(dmd);
            drvSpec.setSchema(schema);
            
            //workaround for issue #4825200 - it seems there is a timing/thread problem with PointBase driver on Windows
            if (/*Utilities.isWindows() && */dmd.getDatabaseProductName().trim().equals("PointBase")) //NOI18N
                Thread.sleep(60);

            drvSpec.setCatalog(con.getCatalog());
        } catch (Exception exc) {
            Exceptions.printStackTrace(exc);
        }
    }

    public DriverSpecification getDriverSpecification() {
        return drvSpec;
    }
    
}
