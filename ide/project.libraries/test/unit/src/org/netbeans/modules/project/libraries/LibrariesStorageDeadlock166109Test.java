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

package org.netbeans.modules.project.libraries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.libraries.TestEntityCatalog;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.TestLibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.Action;
import org.openide.util.test.MockLookup;
/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LibrariesStorageDeadlock166109Test extends NbTestCase {
    private static final int TIMEOUT = Integer.getInteger("LibrariesTest.timeout", 5000);                 //NOI18N
    static final Logger LOG = Logger.getLogger(LibrariesStorageDeadlock166109Test.class.getName());
    private FileObject storageFolder;

    public LibrariesStorageDeadlock166109Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(
            new TestEntityCatalog(),
            new LibrariesTestUtil.MockProjectManager(),
            new LibraryTypeRegistryImpl());
        storageFolder = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");
        assertNotNull("storageFolder found", storageFolder);
    }

    @RandomlyFails
    public void testDeadlock() throws Exception {
        Library[] arr = LibraryManager.getDefault().getLibraries();
        assertEquals("Empty", 0, arr.length);

        LibrariesTestUtil.createLibraryDefinition(storageFolder,"Library1", null);

        Library[] arr0 = LibraryManager.getDefault().getLibraries();
        assertEquals("Still Empty", 0, arr0.length);
        final CountDownLatch event = new CountDownLatch(1);
        PropertyChangeListener l = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                event.countDown();
            }
        };
        LibraryManager.getDefault().addPropertyChangeListener(l);
        try {
            LibrariesTestUtil.registerLibraryTypeProvider(TestMutexLibraryTypeProvider.class);
            assertTrue(event.await(TIMEOUT, TimeUnit.MILLISECONDS));
        } finally {
            LibraryManager.getDefault().removePropertyChangeListener(l);
        }

        // TBD: There is another problem in the code. When a provider is added,
        // but it is not yet processed, the getLibraries() method uses cache and
        // thus can yield wrong results. To workaround that (and simulate the
        // deadlock) here is direct call to reset the cache.
        // Ideally it shall not be necessary for arr1 to have length 1
        Library[] arr1 = LibraryManager.getDefault().getLibraries();
        assertEquals("One", 1, arr1.length);
    }

    public static final class TestMutexLibraryTypeProvider extends TestLibraryTypeProvider {
        public TestMutexLibraryTypeProvider() {
            LOG.info("TestMutexLibraryTypeProvider created");
        }

        @Override
        public LibraryImplementation createLibrary() {
            assertFalse("No Hold lock", Thread.holdsLock(LibraryManager.getDefault()));
            assertFalse("No mutex", ProjectManager.mutex().isReadAccess());
            assertFalse("No mutex write", ProjectManager.mutex().isWriteAccess());
            try {
                LibrariesTestUtil.registerLibraryTypeProvider(TestLibraryTypeProvider.class);
                Thread.sleep(500);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return ProjectManager.mutex().writeAccess(new Action<LibraryImplementation>() {
                public LibraryImplementation run() {
                    return TestMutexLibraryTypeProvider.super.createLibrary();
                }
            });
        }

    }


}
