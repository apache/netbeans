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
package org.netbeans.modules.db.explorer;

import java.io.IOException;
import static org.netbeans.modules.db.explorer.DatabaseConnectionConvertor.CONNECTIONS_PATH;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class DatabaseConnectionConvertor2Test extends TestBase {

    public DatabaseConnectionConvertor2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
        Util.clearConnections();
    }

    /**
     * Creating database connection fails if the CONNECTIONS_PATH dir does not
     * exist. Regression test for issue 251674
     */
    public void testCreateAfterConfigDirDelete() throws IOException {
        // Delete CONNECTIONS_PATH
        FileObject fo = FileUtil.getConfigFile(CONNECTIONS_PATH);
        assertNotNull(fo);

        fo.delete();
        fo = FileUtil.getConfigFile(CONNECTIONS_PATH);
        assertNull(fo);

        // Test connection creation
        DatabaseConnection dbconn = new DatabaseConnection("a", "b", "c", "d", "e", (String) null);
        DataObject data = DatabaseConnectionConvertor.create(dbconn);

        assertNotNull(data);
    }
}
