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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.TestLibrary;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.TestLibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;


/**
 *
 * @author Tomas Zezula
 */
public class LibrariesStorageTest extends NbTestCase {
    
    private FileObject storageFolder;
    LibrariesStorage storage;
    
    public LibrariesStorageTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(
            new TestEntityCatalog(),
            new LibrariesTestUtil.MockLibraryTypeRegistry(),
            new LibrariesTestUtil.MockProjectManager());
        LibrariesTestUtil.registerLibraryTypeProvider(TestLibraryTypeProvider.class);
        this.storageFolder = TestUtil.makeScratchDir(this);
        LibrariesTestUtil.createLibraryDefinition(this.storageFolder,"Library1", null);
        this.storage = new LibrariesStorage (this.storageFolder);
    }

    @RandomlyFails
    /* ergonomics-3373:
     * junit.framework.AssertionFailedError: Event count expected:<1> but was:<2>
	 * at org.netbeans.modules.project.libraries.LibrariesStorageTest.testGetLibraries(LibrariesStorageTest.java:127)
	 * at org.netbeans.junit.NbTestCase.access$200(NbTestCase.java:99)
	 * at org.netbeans.junit.NbTestCase$2.doSomething(NbTestCase.java:405)
	 * at org.netbeans.junit.NbTestCase$1Guard.run(NbTestCase.java:331)
	 * at java.lang.Thread.run(Thread.java:662)
     */
    public void testGetLibraries() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});
        LibrariesTestUtil.createLibraryDefinition(this.storageFolder,"Library2", null);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1", "Library2"});
        TestListener l = new TestListener ();
        this.storage.addPropertyChangeListener(l);
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        LibrariesTestUtil.createLibraryDefinition(this.storageFolder,"Library3", null);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",3,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1", "Library2", "Library3"});
        assertEquals("Event count",1,l.getEventNames().size()); // line 127@ergonomics-3373
        assertEquals("Event names",LibraryProvider.PROP_LIBRARIES,l.getEventNames().get(0));
        assertTrue("Library created called",tlp.wasCreatedCalled());
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
    }

    public void testAddLibrary() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        LibraryImplementation impl = new TestLibrary("Library2");
        this.storage.addLibrary(impl);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1","Library2"});
        assertTrue (tlp.wasCreatedCalled());
    }

    public void testRemoveLibrary() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        this.storage.removeLibrary(libs[0]);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",0,libs.length);
        assertTrue ("Library deleted called",  tlp.wasDeletedCalled());
    }

    public void testUpdateLibrary() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        LibraryImplementation newLib = new TestLibrary ((TestLibrary)libs[0]);
        newLib.setName ("NewLibrary");
        this.storage.updateLibrary(libs[0],newLib);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"NewLibrary"});
        assertTrue ("Library created called",  tlp.wasCreatedCalled());
    }
    
    public void testNamedLibraryImplementation() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);  //NOI18N
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});   //NOI18N
        LibraryImplementation3 lib3 = (LibraryImplementation3)libs[0];
        assertNull(lib3.getDisplayName());
        
        LibraryImplementation3 newLib = new TestLibrary ((TestLibrary)lib3);
        newLib.setDisplayName("FooLib");    //NOI18N
        this.storage.updateLibrary(lib3,newLib);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);  //NOI18N
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});   //NOI18N
        lib3 = (LibraryImplementation3)libs[0];
        assertEquals("FooLib",lib3.getDisplayName());   //NOI18N
    }
    
    public void testLibraryImplementation3() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);  //NOI18N
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});   //NOI18N
        LibraryImplementation3 lib3 = (LibraryImplementation3)libs[0];
        assertTrue(lib3.getProperties().isEmpty());
        
        LibraryImplementation3 newLib = new TestLibrary ((TestLibrary)lib3);
        Map<String,String> props = new HashMap<String, String>();
        props.put("test_prop","test_value");    //NOI18N
        newLib.setProperties(props);
        this.storage.updateLibrary(lib3,newLib);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);  //NOI18N
        LibrariesTestUtil.assertLibEquals(libs, new String[] {"Library1"});   //NOI18N
        lib3 = (LibraryImplementation3)libs[0];
        assertEquals(1, lib3.getProperties().size());
        assertEquals("test_value", lib3.getProperties().get("test_prop"));  //NOI18N
    }

    static class TestListener implements PropertyChangeListener {
        
        private List<String> eventNames = new ArrayList<String>();
        
        public List<String> getEventNames () {
            return this.eventNames;
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            this.eventNames.add (propertyChangeEvent.getPropertyName());
        }
        
        public void reset () {
            this.eventNames.clear();
        }
        
    }
    
    
}
