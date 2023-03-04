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

import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;


/** Checks that libraries are updated as sson as correct library type
 * provider is registered.
 *
 * @author Jaroslav Tulach
 */
public class LibrariesStorageRefreshTest extends NbTestCase {
    
    private FileObject storageFolder;
    LibrariesStorage storage;
    
    public LibrariesStorageRefreshTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(
            new TestEntityCatalog(),
            new LibrariesTestUtil.MockLibraryTypeRegistry(),
            new LibrariesTestUtil.MockProjectManager());
        this.storageFolder = TestUtil.makeScratchDir(this);
        org.netbeans.modules.project.libraries.LibrariesTestUtil.createLibraryDefinition(this.storageFolder,"Library1", null);
        this.storage = new LibrariesStorage (this.storageFolder);
    }

    @RandomlyFails // NB-Core-Build #8253: Libraries count expected:<1> but was:<0>
    public void testGetLibrariesAfterEnablingProvider() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        LibrariesStorageTest.TestListener l = new LibrariesStorageTest.TestListener ();
        this.storage.addPropertyChangeListener(l);
        assertEquals("No libraries found", 0, libs.length);
        LibrariesTestUtil.registerLibraryTypeProvider(LibrariesTestUtil.TestLibraryTypeProvider.class);
        Thread.sleep(1000); //The lookup for path fires with delay, wait for the event
        libs = this.storage.getLibraries();        
        assertEquals("Libraries count",1,libs.length);        
        assertEquals("One change", 1, l.getEventNames().size());
    }
    
    
}
