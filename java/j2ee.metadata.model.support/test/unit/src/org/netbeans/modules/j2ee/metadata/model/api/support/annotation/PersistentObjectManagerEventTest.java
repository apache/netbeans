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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.util.test.MockChangeListener;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerEventTest extends PersistenceTestCase {

    public PersistentObjectManagerEventTest(String name) {
        super(name);
    }

    /**
     * Tests that POM doesn't fire events if it was initialized temporarily. This
     * is important to avoid issues like 119767, where a client getting an event
     * reacts by initializing the model again, which fires an event, which
     * initializes the model, etc.
     */
    public void testNoEventsIfTemporary() throws Exception {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final MockChangeListener listener = new MockChangeListener();
        final ObjectProvider<PersistentObject> provider = new InterruptibleObjectProviderImpl(true);
        final List<PersistentObjectManager<PersistentObject>> manager = new ArrayList<PersistentObjectManager<PersistentObject>>();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                PersistentObjectManager<PersistentObject> newManager = helper.createPersistentObjectManager(provider);
                newManager.addChangeListener(listener);
                newManager.getObjects();
                assertTrue(newManager.temporary);
                manager.add(newManager);
            }
        });
        // just check the manager has was deinitialized by now (as we are out of the java context)
        assertFalse(manager.get(0).initialized);
        // there should have been no events whatsoever
        listener.assertNoEvents();
    }
}
