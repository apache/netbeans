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

import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.LibraryManagerTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.libraries.TestEntityCatalog;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.TestLibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LibrariesStorageDeadlock167218Test extends NbTestCase {
    static final Logger LOG = Logger.getLogger(LibrariesStorageDeadlock167218Test.class.getName());
    private FileObject storageFolder;

    public LibrariesStorageDeadlock167218Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(
            new TestEntityCatalog(),
            new LibraryTypeRegistryImpl(),
            new LibrariesTestUtil.MockProjectManager());
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

        class Block implements Mutex.ExceptionAction<Void> {

            public Void run() throws Exception {
                LibrariesTestUtil.registerLibraryTypeProvider(TestMutexLibraryTypeProvider.class);

                Thread.sleep(100);

                LibraryManagerTest.resetCache();
                Library[] arr1 = LibraryManager.getDefault().getLibraries();
//                assertEquals("Nothing, as we are under mutex", 0, arr1.length); Was wrong caller under PM.mutex didn't see changes
                assertEquals("See changes even under mutex", 1, arr1.length);

                return null;
            }
        }
        Block b = new Block();
        ProjectManager.mutex().writeAccess(b);
        Library[] arr1 = LibraryManager.getDefault().getLibraries();
        assertEquals("Real value, as we are outside of mutex and there is no risk of deadlocks", 1, arr1.length);
    }

    public static final class TestMutexLibraryTypeProvider extends TestLibraryTypeProvider {
        public TestMutexLibraryTypeProvider() {
            LOG.info("TestMutexLibraryTypeProvider created");
        }

        @Override
        public LibraryImplementation createLibrary() {
            assertFalse("No Hold lock", Thread.holdsLock(LibraryManager.getDefault()));
//            assertFalse("No mutex", ProjectManager.mutex().isReadAccess());   Libraries refreshed synchronously by caller - makes no sence
//            assertFalse("No mutex write", ProjectManager.mutex().isWriteAccess());  Libraries refreshed synchronously by caller - makes no sence
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
