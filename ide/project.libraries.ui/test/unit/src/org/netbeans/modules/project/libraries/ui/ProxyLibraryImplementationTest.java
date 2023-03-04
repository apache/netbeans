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

package org.netbeans.modules.project.libraries.ui;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.LibrariesStorage;
import org.netbeans.modules.project.libraries.LibrariesStorageAccessor;
import org.netbeans.modules.project.libraries.LibrariesTestUtil;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.TestLibraryTypeProvider;
import org.netbeans.modules.project.libraries.TestEntityCatalog;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;


/**
 *
 * @author Tomas Zezula
 */
public class ProxyLibraryImplementationTest extends NbTestCase {

    private FileObject storageFolder;
    LibrariesStorage storage;

    public ProxyLibraryImplementationTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(
            new TestEntityCatalog(),
            new LibrariesTestUtil.MockProjectManager(),
            new LibrariesTestUtil.MockLibraryTypeRegistry());
        LibrariesTestUtil.registerLibraryTypeProvider(TestLibraryTypeProvider.class);
        this.storageFolder = TestUtil.makeScratchDir(this);
        LibrariesTestUtil.createLibraryDefinition(this.storageFolder,"Library1", null);
        this.storage = LibrariesStorageAccessor.createLibrariesStorage(this.storageFolder);
    }

    public void testGetDisplayNameLibraries() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});
        LibrariesTestUtil.createLibraryDefinition(this.storageFolder,"Library2", "MyName");
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        LibraryImplementation impl = libs[0].getName().equals("Library2") ? libs[0] : libs[1];

        assertEquals("MyName", LibrariesSupport.getLocalizedName(impl));

        LibrariesModel model = new LibrariesModel();
        ProxyLibraryImplementation proxy = ProxyLibraryImplementation.createProxy(impl, model);

        assertEquals("MyName", LibrariesSupport.getLocalizedName(proxy));
    }

}
