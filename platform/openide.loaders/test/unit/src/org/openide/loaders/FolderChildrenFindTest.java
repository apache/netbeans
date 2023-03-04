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
package org.openide.loaders;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FolderChildrenFindTest extends NbTestCase {
    private Node node;
    int created;

    public FolderChildrenFindTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File wd = new File(getWorkDir(), "wd");
        wd.mkdirs();
        for (int i = 0; i < 1000; i++) {
            new File(wd, "f" + i + ".jt").createNewFile();
        }
        node = DataFolder.findFolder(FileUtil.toFileObject(wd)).getNodeDelegate();
        OperationAdapter ol = new OperationAdapter() {
            @Override
            public void operationPostCreate(OperationEvent ev) {
                created++;
            }
        };
        DataLoaderPool.getDefault().addOperationListener(ol);
    }
    
    public void testFind500() throws Exception {
        Node n = node.getChildren().findChild("f500.jt");
        assertNotNull("node found", n);
        assertEquals("f500.jt", n.getName());
        assertEquals("Only one created data object", 1, created);
    }
}
