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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.LibrariesTestUtil;
import static org.netbeans.modules.project.libraries.LibrariesTestUtil.*;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.ALP;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.Area;
import org.netbeans.modules.project.libraries.LibrariesTestUtil.WLP;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

public class LibraryManagerTest extends NbTestCase {

    private WLP lp;

    private static final String LIBRARY_TYPE = "j2se";  //NOI18N
    private static final String[] VOLUME_TYPES = new String[] {
        "bin",
        "src",
        "doc"
    };

    public LibraryManagerTest (String testName) {
        super (testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        lp = new WLP();
        MockLookup.setLookup(Lookups.fixed(
            lp,
            new LibrariesTestUtil.MockLibraryTypeRegistry(),
            new LibrariesTestUtil.MockProjectManager()));
        LibrariesTestUtil.registerLibraryTypeProvider(TestLibraryTypeProvider.class);
    }

    public void testGetLibraries () throws Exception {
        LibraryManager lm = LibraryManager.getDefault();
        MockPropertyChangeListener pcl = new MockPropertyChangeListener();
        lm.addPropertyChangeListener(pcl);
        Library[] libs = lm.getLibraries();
        assertEquals ("Libraries count", 0, libs.length);
        LibraryImplementation[] impls = createTestLibs ();
        lp.set(impls);
        libs = lm.getLibraries();
        pcl.assertEvents(LibraryManager.PROP_LIBRARIES);
        assertEquals ("Libraries count", 2, libs.length);
        assertLibsEquals (libs, impls);
        lp.set();
        pcl.assertEvents(LibraryManager.PROP_LIBRARIES);
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 0, libs.length);
    }

    public void testGetLibrary () throws Exception {
        LibraryImplementation[] impls = createTestLibs ();
        lp.set(impls);
        LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        assertLibsEquals (libs, impls);
        Library lib = lm.getLibrary("Library1");
        assertNotNull ("Existing library", lib);
        assertLibsEquals(new Library[] {lib}, new LibraryImplementation[] {impls[0]});
        lib = lm.getLibrary("Library2");
        assertNotNull ("Existing library", lib);
        assertLibsEquals(new Library[] {lib}, new LibraryImplementation[] {impls[1]});
        lib = lm.getLibrary("Library3");
        assertNull ("Nonexisting library", lib);
    }

    @SuppressWarnings("deprecation")
    public void testAddRemoveLibrary () throws Exception {
        final LibraryImplementation[] impls = createTestLibs();
        lp.set(impls);
        final LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        assertLibsEquals (libs, impls);
        final LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(LIBRARY_TYPE);
        assertNotNull (provider);
        final LibraryImplementation newLibImplementation = provider.createLibrary();
        newLibImplementation.setName("NewLib");
        final Library newLibrary = LibraryFactory.createLibrary(newLibImplementation);
        lm.addLibrary(newLibrary);
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 3, libs.length);
        List<LibraryImplementation> newLibs = new ArrayList<LibraryImplementation>(Arrays.asList(impls));
        newLibs.add (newLibImplementation);
        assertLibsEquals(libs, newLibs.toArray(new LibraryImplementation[0]));
        lm.removeLibrary(newLibrary);
        libs = lm.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        assertLibsEquals (libs, impls);
    }

    public void testCreateRemoveLibrary() throws Exception {
        LibraryManager mgr = LibraryManager.getDefault();
        URL fooJar = mkJar("foo.jar");
        Library lib = mgr.createLibrary(LIBRARY_TYPE, "NewLib", Collections.singletonMap("bin", Arrays.asList(fooJar)));
        assertEquals(mgr, lib.getManager());
        assertEquals(Collections.singletonList(lib), Arrays.asList(mgr.getLibraries()));
        assertEquals(1, lp.libs.size());
        assertLibsEquals(new Library[] {lib}, lp.libs.toArray(new LibraryImplementation[1]));
        mgr.removeLibrary(lib);
        assertEquals(Collections.emptyList(), Arrays.asList(mgr.getLibraries()));
        assertEquals(0, lp.libs.size());
    }

    public void testArealLibraryManagers() throws Exception {
        ALP alp = new ALP();
        MockLookup.setLookup(Lookups.fixed(
             lp,
             alp,
             new LibrariesTestUtil.MockProjectManager(),
             new LibrariesTestUtil.MockLibraryTypeRegistry(),
             new LibrariesTestUtil.MockLibraryStorageAreaCache()));
        LibrariesTestUtil.registerLibraryTypeProvider(TestLibraryTypeProvider.class);
        final Area newArea = alp.createArea();
        Lookup.getDefault().lookup(LibrariesTestUtil.MockLibraryStorageAreaCache.class).addToCache(newArea.getLocation());
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
        Library lib = mgr.createLibrary(LIBRARY_TYPE, "NewLib", Collections.singletonMap("bin", Arrays.asList(fooJar)));
        assertEquals(mgr, lib.getManager());
        pcl.assertEvents(LibraryManager.PROP_LIBRARIES);
        assertEquals(Collections.singletonList(lib), Arrays.asList(mgr.getLibraries()));
        assertEquals(1, alp.libs.get(home).size());
        LibraryImplementation impl = alp.libs.get(home).iterator().next();
        assertEquals("NewLib", impl.getName());
        assertEquals(LIBRARY_TYPE, impl.getType());
        assertEquals(Arrays.asList(fooJar), impl.getContent("bin"));
        mgr.removeLibrary(lib);
        pcl.assertEvents(LibraryManager.PROP_LIBRARIES);
        assertEquals(Collections.emptyList(), Arrays.asList(mgr.getLibraries()));
        assertEquals(0, alp.libs.get(home).size());
    }

    public void testCreateLibraryWithPropertiesInGlobalLM() throws Exception {
        final String name = "LibWithProperties";                //NOI18N
        final String displayName = "Library With Properties";   //NOI18N
        final String desc = "A nice library with even nicer properties";   //NOI18N
        final String key = "key";                               //NOI18N
        final String value = "EA0000FFFF";                      //NOI18N
        LibraryManager mgr = LibraryManager.getDefault();
        URL fooJar = mkJar("foo.jar");                          //NOI18N
        final List<URL> bin = Arrays.asList(fooJar);
        Library lib = mgr.createLibrary(
                LIBRARY_TYPE,
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

    public void testCreateLibraryWithPropertiesInAreaLM() throws Exception {
        ALP alp = new ALP();
        MockLookup.setLookup(Lookups.fixed(
            lp,
            alp,
            new LibrariesTestUtil.MockProjectManager(),
            new LibrariesTestUtil.MockLibraryTypeRegistry(),
            new LibrariesTestUtil.MockLibraryStorageAreaCache()
            ));
        LibrariesTestUtil.registerLibraryTypeProvider(TestLibraryTypeProvider.class);
        final Area newArea = alp.createArea();
        Lookup.getDefault().lookup(LibrariesTestUtil.MockLibraryStorageAreaCache.class).addToCache(newArea.getLocation());
        Area space = new Area("space");  //NOI18N
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
            LIBRARY_TYPE,
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

    static LibraryImplementation[] createTestLibs () throws MalformedURLException {
        LibraryImplementation[] impls = {
            LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES),
            LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES),
        };
        impls[0].setName ("Library1");
        impls[1].setName ("Library2");
        impls[0].setDescription("Library1 description");
        impls[1].setDescription("Library2 description");
        impls[0].setContent("bin", Collections.singletonList( new URL("file:/lib/libest1.so")));
        impls[1].setContent("bin", Collections.singletonList(new URL("file:/lib/libest2.so")));
        return impls;
    }

    static void assertLibsEquals (Library[] libs, LibraryImplementation[] impls) {
        assertEquals ("libs equals (size)", impls.length, libs.length);
        for (int i=0; i< libs.length; i++) {
            assertEquals ("libs equals (name)", impls[i].getName(), libs[i].getName());
            assertEquals ("libs equals (description)", impls[i].getDescription(), libs[i].getDescription());
            List lc = libs[i].getContent("bin");
            List ic = impls[i].getContent("bin");
            assertEquals ("libs equals (content bin)", ic, lc);
            lc = libs[i].getContent("src");
            ic = impls[i].getContent("src");
            assertEquals ("libs equals (content src)", ic, lc);
            lc = libs[i].getContent("doc");
            ic = impls[i].getContent("doc");
            assertEquals ("libs equals (content doc)", ic, lc);
        }
    }

    public static class TestLibraryTypeProvider implements LibraryTypeProvider {


        public String getDisplayName() {
            return LIBRARY_TYPE;
        }

        public String getLibraryType() {
            return LIBRARY_TYPE;
        }

        public String[] getSupportedVolumeTypes() {
            return VOLUME_TYPES;
        }

        public LibraryImplementation createLibrary() {
            assert !ProjectManager.mutex().isReadAccess();
            assert !ProjectManager.mutex().isWriteAccess();
            return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
        }

        public void libraryDeleted(LibraryImplementation library) {
        }

        public void libraryCreated(LibraryImplementation library) {
        }

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public org.openide.util.Lookup getLookup() {
            return null;
        }
    }

    public static void resetCache() {
        LibraryManager.getDefault().resetCache();
    }
}
