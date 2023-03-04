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
package org.netbeans.modules.db.explorer.node;

import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.SetDefaultDatabase;
import org.netbeans.lib.ddl.impl.SetDefaultSchema;
import org.netbeans.lib.ddl.impl.Specification;

/**
 * This class factors out interaction with the DDL package.  This allows
 * us to unit test this interaction
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public class DDLHelper {
    public static void deleteTable(Specification spec, 
            String schema, String tablename) throws Exception {
        AbstractCommand cmd = spec.createCommandDropTable(tablename);
        cmd.setObjectOwner(schema);
        cmd.execute();
    }

    public static void setDefaultDatabase(Specification spec, String dbname) throws CommandNotSupportedException, DDLException {
        SetDefaultDatabase cmd = spec.createSetDefaultDatabase(dbname);
        cmd.execute();
    }

    public static void setDefaultSchema(Specification spec, String schemaName) throws CommandNotSupportedException, DDLException {
        SetDefaultSchema cmd = spec.createSetDefaultSchema(schemaName);
        cmd.execute();
    }

    public static void deleteIndex(Specification spec,
            String schema, String tablename, String indexname) throws CommandNotSupportedException, DDLException
    {
        DropIndex cmd = spec.createCommandDropIndex(indexname);
        cmd.setTableName(tablename);
        cmd.setObjectOwner(schema);
        cmd.execute();        
    }
    
    public static void deleteView(Specification spec,
            String schema, String viewname) throws CommandNotSupportedException, DDLException {
        AbstractCommand cmd = spec.createCommandDropView(viewname);
        cmd.setObjectOwner(schema);
        cmd.execute();        
    }
}
