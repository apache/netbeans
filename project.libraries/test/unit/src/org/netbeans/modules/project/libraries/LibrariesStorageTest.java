/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
