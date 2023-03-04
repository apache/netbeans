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

package org.netbeans.modules.db.api.metadata;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.test.DBTestBase;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author David
 */
public class DBConnMetadataModelManagerTest extends DDLTestBase {
    private static final Action<Metadata> CHECK_TABLE_EXISTS_ACTION = new Action<Metadata>() {
        public void run(Metadata md) {
            assertNotNull(getTestTable(md));
        }
    };

    private static Table getTestTable(Metadata md) {
        return md.getDefaultSchema().getTable(DBTestBase.getTestTableName());
    }

    public DBConnMetadataModelManagerTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of get method, of class DBConnMetadataModelManager.
     */
    @Test
    public void testGet() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(true);
        createTestTable();

        MetadataModel model = DBConnMetadataModelManager.get(dbconn);
        assertNotNull(model);
        
        model.runReadAction(CHECK_TABLE_EXISTS_ACTION);
    }
}
