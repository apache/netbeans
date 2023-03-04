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
public class ViewNodeTest extends DDLTestBase {

    public ViewNodeTest(String testName) {
        super(testName);
    }

    public void testClipboardCopy() throws Exception {
        String tablename = "testtable";
        String pkName = "id";
        createBasicTable(tablename, pkName);

        String viewname = "testview";
        createView(viewname, "select id from testtable");

        ViewNode viewNode = getViewNode(viewname);
        assertNotNull(viewNode);
        assertTrue(viewNode.canCopy());

        Transferable transferable = (Transferable)viewNode.clipboardCopy();
        Set mimeTypes = new HashSet();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            mimeTypes.add(flavors[i].getMimeType());
        }

        assertTrue(mimeTypes.contains("application/x-java-netbeans-dbexplorer-view; class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$View"));
        assertTrue(mimeTypes.contains("application/x-java-openide-nodednd; mask=1; class=org.openide.nodes.Node"));

        dropView(viewname);
        dropTable(tablename);
    }
}
