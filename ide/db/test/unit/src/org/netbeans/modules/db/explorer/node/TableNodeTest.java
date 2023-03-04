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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author Rob Englander
 */
public class TableNodeTest extends DDLTestBase {

    public TableNodeTest(String testName) {
        super(testName);
    }

    public void testClipboardCopy() throws Exception {
        String tablename = "testtable";
        String pkName = "id";
        createBasicTable(tablename, pkName);

        TableNode tableNode = getTableNode(tablename);
        assertNotNull(tableNode);
        assertTrue(tableNode.canCopy());

        Transferable transferable = (Transferable)tableNode.clipboardCopy();
        Set mimeTypes = new HashSet();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            mimeTypes.add(flavors[i].getMimeType());
        }
        assertTrue(mimeTypes.contains("application/x-java-netbeans-dbexplorer-table; class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table"));
        assertTrue(mimeTypes.contains("application/x-java-openide-nodednd; mask=1; class=org.openide.nodes.Node"));

        dropTable(tablename);
    }
}
