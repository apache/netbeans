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

package org.netbeans.api.project.libraries;

import static org.netbeans.modules.project.libraries.LibrariesTestUtil.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

public class LibraryTest extends NbTestCase {

    private WLP lp;

    public LibraryTest (String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        lp = new WLP();
        MockLookup.setLookup(Lookups.singleton(lp));
    }
    
    public void testGetLibraries () throws Exception {        
        LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        LibraryImplementation[] impls = LibraryManagerTest.createTestLibs ();
        lp.set(impls);
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        LibraryManagerTest.assertLibsEquals (libs, impls);
        MockPropertyChangeListener pcl = new MockPropertyChangeListener();
        libs[0].addPropertyChangeListener(pcl);
        impls[0].setName("NewLibrary1");
        pcl.assertEvents(Library.PROP_NAME);
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        
        impls[0].setDescription("NewLibrary1Description");
        pcl.assertEvents(Library.PROP_DESCRIPTION);
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        List<URL> urls = new ArrayList<URL>();
        urls.add (new URL ("file:/lib/libnew1.so"));
        urls.add (new URL ("file:/lib/libnew2.so"));
        impls[0].setContent ("bin",urls);        
        pcl.assertEvents(Library.PROP_CONTENT);
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        urls = new ArrayList<URL>();
        urls.add (new URL ("file:/src/new/src/"));
        impls[0].setContent ("src",urls);        
        pcl.assertEvents(Library.PROP_CONTENT);
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
    }    
    
}
