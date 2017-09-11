/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.api.project.libraries;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
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
