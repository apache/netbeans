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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.TestEntityCatalog;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class LibrariesStorageDeadlock172779Test extends NbTestCase {
    static final Logger LOG = Logger.getLogger(LibrariesStorageDeadlock166109Test.class.getName());
    private FileObject storageFolder;
    private static CountDownLatch cond;

    public LibrariesStorageDeadlock172779Test(String name) {
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
        LibrariesTestUtil.registerLibraryTypeProvider(TestMutexLibraryTypeProvider.class);
        org.netbeans.modules.project.libraries.LibrariesTestUtil.createLibraryDefinition(storageFolder, "Library1", null);
    }

    public void testDeadlock() throws Exception {        
        cond = new CountDownLatch(1);
        final ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    es.submit(new Runnable() {
                        public void run() {
                            //Trigger init_storage
                            LibraryManager.getDefault().getLibraries();
                        }
                    });
                    try {
                        cond.await();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    //Deadlock
                    LibraryManager.getDefault().getLibraries();
                }
            });                        
        } finally {
            es.shutdown();
        }
    }

    public static final class TestMutexLibraryTypeProvider implements LibraryTypeProvider {

        private static final String LIBRARY_TYPE = "Test";  //NOI18N
        private static final String[] VOLUME_TYPES = new String[] {"bin","src","doc"};  //NOI18N

        public TestMutexLibraryTypeProvider() {
            LOG.info("TestMutexLibraryTypeProvider created");
        }

        public String getLibraryType() {
            return LIBRARY_TYPE;
        }

        public String getDisplayName() {
            return "Test Provider"; //NOI18N
        }

        public void libraryCreated(LibraryImplementation library) {
            cond.countDown();
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    //Deadlock
                }
            });            
        }

        public String[] getSupportedVolumeTypes() {
            return VOLUME_TYPES;
        }

        public LibraryImplementation createLibrary() {
            return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
        }

        public void libraryDeleted(LibraryImplementation library) {
        }      

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public org.openide.util.Lookup getLookup() {
            return Lookup.EMPTY;
        }    

    }

}
