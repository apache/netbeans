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

package org.netbeans.api.project.libraries;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.LibrariesTestUtil;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.Area;
import static org.netbeans.modules.project.libraries.LibrariesTestUtil.mkJar;
import org.netbeans.modules.project.libraries.ui.LibrariesModel;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public final class LibraryManageTest extends NbTestCase {
    public LibraryManageTest(final String name) {
        super(name);
    }


    public void testArealLibraryManagers() throws Exception {
        LibrariesTestUtil.ALP alp = new LibrariesTestUtil.ALP();
        LibrariesTestUtil.WLP lp = new LibrariesTestUtil.WLP();
        MockLookup.setLookup(Lookups.fixed(
            lp,
            alp,
            new LibrariesTestUtil.MockProjectManager(),
            new LibrariesTestUtil.MockLibraryTypeRegistry(),
            new LibrariesModel.LibrariesModelCache()));
        LibrariesTestUtil.registerLibraryTypeProvider(LibrariesTestUtil.TestLibraryTypeProvider.class);
        new LibrariesModel().createArea();
        Area home = new Area("home");
        Area away = new Area("away");
        alp.setOpen(home, away);
        List<String> locations = new ArrayList<String>(); // use list, not set, to confirm size also
        for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
            URL loc = mgr.getLocation();
            locations.add(loc != null ? loc.toString() : "<none>");
        }
        Collections.sort(locations);
        assertEquals("[<none>, http://nowhere.net/away, http://nowhere.net/home, http://nowhere.net/new]", locations.toString());
        alp.setOpen();
        try {
            LibraryManager.forLocation(new URL("http://even-less-anywhere.net/"));
            fail();
        } catch (IllegalArgumentException x) {}
        LibraryManager mgr = LibraryManager.forLocation(home.getLocation());
        assertEquals(home.getLocation(), mgr.getLocation());
        assertEquals("home", mgr.getDisplayName());
        assertEquals(0, mgr.getLibraries().length);
        MockPropertyChangeListener pcl = new MockPropertyChangeListener();
        mgr.addPropertyChangeListener(pcl);
        URL fooJar = mkJar("foo.jar");
        Library lib = mgr.createLibrary(
                LibrariesTestUtil.TestLibraryTypeProvider.TYPE,
                "NewLib",
                Collections.singletonMap("bin", Arrays.asList(fooJar)));
        assertEquals(mgr, lib.getManager());
        pcl.assertEvents(LibraryManager.PROP_LIBRARIES);
        assertEquals(Collections.singletonList(lib), Arrays.asList(mgr.getLibraries()));
        assertEquals(1, alp.libs.get(home).size());
        LibraryImplementation impl = alp.libs.get(home).iterator().next();
        assertEquals("NewLib", impl.getName());
        assertEquals(LibrariesTestUtil.TestLibraryTypeProvider.TYPE, impl.getType());
        assertEquals(Arrays.asList(fooJar), impl.getContent("bin"));
        mgr.removeLibrary(lib);
        pcl.assertEvents(LibraryManager.PROP_LIBRARIES);
        assertEquals(Collections.emptyList(), Arrays.asList(mgr.getLibraries()));
        assertEquals(0, alp.libs.get(home).size());
    }

    public void testCreateLibraryWithPropertiesInAreaLM() throws Exception {
        LibrariesTestUtil.ALP alp = new LibrariesTestUtil.ALP();
        LibrariesTestUtil.WLP lp = new LibrariesTestUtil.WLP();
        MockLookup.setLookup(Lookups.fixed(
            lp,
            alp,
            new LibrariesTestUtil.MockProjectManager(),
            new LibrariesTestUtil.MockLibraryTypeRegistry(),
            new LibrariesModel.LibrariesModelCache()
            ));
        LibrariesTestUtil.registerLibraryTypeProvider(LibrariesTestUtil.TestLibraryTypeProvider.class);
        new LibrariesModel().createArea();
        LibrariesTestUtil.Area space = new LibrariesTestUtil.Area("space");  //NOI18N
        LibraryManager mgr = LibraryManager.forLocation(space.getLocation());
        assertNotNull(mgr);
        assertEquals(space.getLocation(), mgr.getLocation());
        assertEquals("space", mgr.getDisplayName()); //NOI18N
        assertEquals(0, mgr.getLibraries().length);

        final String name = "LibWithProperties";                //NOI18N
        final String displayName = "Library With Properties";   //NOI18N
        final String desc = "A nice library with even nicer properties";   //NOI18N
        final String key = "key";                               //NOI18N
        final String value = "0F07";                      //NOI18N
        URL fooJar = mkJar("foo.jar");
        final List<URL> bin = Arrays.asList(fooJar);
        Library lib = mgr.createLibrary(
            LibrariesTestUtil.TestLibraryTypeProvider.TYPE,
            name,
            displayName,
            desc,
            Collections.singletonMap("bin", bin),           //NOI18N
            Collections.<String,String>singletonMap(key,value));
        lib = mgr.getLibrary(name);
        assertNotNull(lib);
        assertEquals("Name", name, lib.getName());                  //NOI18N
        assertEquals("Display Name", displayName, lib.getDisplayName());    //NOI18N
        assertEquals("Description", desc, lib.getDescription());    //NOI18N
        assertEquals("Content.bin", bin, lib.getContent("bin"));    //NOI18N
        assertEquals("Properties.size", 1, lib.getProperties().size());    //NOI18N
        assertEquals("Properties[key]", value, lib.getProperties().get(key));    //NOI18N
    }
}
