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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerInterruptedTest extends PersistenceTestCase {

    public PersistentObjectManagerInterruptedTest(String name) {
        super(name);
    }

    public void testInterrupted() throws Exception {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                // first checking that the manager does not (for any reason) initialize temporarily
                ObjectProvider<PersistentObject> provider = new InterruptibleObjectProviderImpl(false);
                PersistentObjectManager<PersistentObject> manager = helper.createPersistentObjectManager(provider);
                manager.getObjects();
                assertFalse(manager.temporary);
                // now checking that the manager initializes temporarily when ObjectProvider.createInitialObjects throws InterruptedException
                provider = new InterruptibleObjectProviderImpl(true);
                manager = helper.createPersistentObjectManager(provider);
                manager.getObjects();
                assertTrue(manager.temporary);
            }
        });
    }
}
