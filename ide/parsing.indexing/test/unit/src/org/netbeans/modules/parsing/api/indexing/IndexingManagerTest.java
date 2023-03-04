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

package org.netbeans.modules.parsing.api.indexing;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class IndexingManagerTest extends NbTestCase {

    private FileObject root;

    public IndexingManagerTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject workDir = FileUtil.toFileObject(getWorkDir());
        root = FileUtil.createFolder(workDir, "root");  //NOI18N
    }

    public void testRefreshIndexAndWaitCalledWithParserLock() {
        boolean success = false;
        Utilities.acquireParserLock();
        try {
                IndexingManager.getDefault().refreshIndexAndWait(root.toURL(), null);
                success = true;
        } catch (IllegalStateException ise) {
            //pass
        } finally {
            Utilities.releaseParserLock();
        }
        assertFalse(success);
    }

    public void testRefreshIndexAndWaitCalledFromIndexer() {
        final boolean[] success = {false};
        RepositoryUpdater.getDefault().runIndexer(new Runnable() {
            @Override
            public void run() {
                try {
                    IndexingManager.getDefault().refreshIndexAndWait(root.toURL(), null);
                    success[0] = true;
                } catch (IllegalStateException ise) {
                    //pass
                }
            }
        });
        assertFalse(success[0]);
    }

    public void testRefreshIndexAndWait() {
        boolean success = false;
        try {
            IndexingManager.getDefault().refreshIndexAndWait(root.toURL(), null);
            success = true;
        } catch (IllegalStateException ise) {
            //pass
        }
        assertTrue(success);
    }

}
