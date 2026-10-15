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
package org.netbeans.modules.java.j2seplatform;

import org.netbeans.modules.java.j2seplatform.libraries.*;
import java.awt.Dialog;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import static junit.framework.TestCase.fail;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class AbstractJ2SEAttacherTestBase extends NbTestCase {

    public AbstractJ2SEAttacherTestBase(String name) {
        super(name);
    }

    private Locale def;
    protected URL classesRootURL;
    
    protected String getBase() throws Exception {
        File dir = getWorkDir();
        if (Utilities.isWindows()) {
            dir = new File(dir.getCanonicalPath());
        }
        return dir.toString();
    }
    
    protected List<Object> additionalServices() {
        ArrayList<Object> obs = new ArrayList<>();
        obs.add(TestLibraryProviderImpl.getDefault());
        obs.add(new TestDisplayer());
        return obs;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        def = Locale.getDefault();
        List<Object> l = additionalServices();
        MockLookup.setLayersAndInstances(l.toArray(new Object[0]));
    }

    @Override
    protected void tearDown() throws Exception {
        Locale.setDefault(def);
        super.tearDown(); 
    }
    
    protected volatile boolean permitUI;
    protected final AtomicBoolean uiPresented = new AtomicBoolean();
    
    class TestDisplayer extends DialogDisplayer {

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            uiPresented.set(true);
            if (permitUI) {
                return NotifyDescriptor.CLOSED_OPTION;
            } else {
                fail("UI should not be presented");
                return null;
            }
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            uiPresented.set(true);
            if (permitUI) {
                return new Dialog(null, false);
            } else {
                fail("UI should not be presented");
                return null;
            }
        }
    }
    
    protected void assertAttacherResults(boolean sources) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        AtomicBoolean b = new AtomicBoolean();
        
        SourceJavadocAttacher.AttachmentListener attachL = new SourceJavadocAttacher.AttachmentListener() {
            @Override
            public void attachmentSucceeded() {
                fail("Library attacher should not auto-attach sources");
            }

            @Override
            public void attachmentFailed() {
                b.set(true);
            }
        };
        
        BiConsumer<URL, SourceJavadocAttacher.AttachmentListener> c = (sources ? SourceJavadocAttacher::attachSources : SourceJavadocAttacher::attachJavadoc);
        c.accept(classesRootURL, attachL);
        Mutex.EVENT.writeAccess(() -> {
            cdl.countDown();
        });
        cdl.await();
        assertTrue("Library sources not attached", b.get());
        assertEquals(permitUI, uiPresented.get());
    }

    /**
     * Library attacher should not assign anything in "yes" mode, the default attacher
     * will do.
     * @throws Exception 
     */
    public void testSourcesAttachedYes() throws Exception {
        Locale.setDefault(new Locale("DA"));
        assertAttacherResults(true);
    }
    
    /**
     * In No mode, no source should be attached.
     * @throws Exception 
     */
    public void testSourceAttachedNo() throws Exception {
        Locale.setDefault(new Locale("NO"));
        assertAttacherResults(true);
    }

    /**
     * Ask mode must enter UI interaction with DialogDisplayer
     * @throws Exception 
     */
    public void testSourceAttachedWithUI() throws Exception {
        permitUI = true;
        assertAttacherResults(true);
    }
    
    public void testJavadocAttachedNo() throws Exception {
        Locale.setDefault(new Locale("NO"));
        assertAttacherResults(false);
    }

    public void testJavadocAttachedWithUI() throws Exception {
        permitUI = true;
        assertAttacherResults(false);
    }
    
    /**
     * Library attacher should not assign anything in "yes" mode, the default attacher
     * will do.
     * @throws Exception 
     */
    public void testJavadocAttachedYes() throws Exception {
        Locale.setDefault(new Locale("DA"));
        assertAttacherResults(false);
    }
    
}
