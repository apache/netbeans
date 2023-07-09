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

package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.TestBaseForTestProject;
import org.netbeans.modules.web.jsf.editor.index.JsfCustomIndexer;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Function;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;

/**
 *
 * @author marekfukala
 */
public class FaceletsLibrarySupportTest extends TestBaseForTestProject {

    public FaceletsLibrarySupportTest(String name) {
        super(name);
    }

    public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new FaceletsLibrarySupportTest("testModifyCompositeComponentLibrary"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        JsfCustomIndexer.LOGGER.setLevel(Level.FINE);

        Handler h = new ConsoleHandler();
        h.setLevel(Level.FINE);
        JsfCustomIndexer.LOGGER.addHandler(h);
        super.setUp();
    }

    public void testCompositeComponentLibraryWithoutDescriptor() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String ezCompLibraryNS = LibraryUtils.getCompositeLibraryURL("ezcomp", instance.isJsf22Plus());

        Library ezcompLib = instance.getLibrary(ezCompLibraryNS);
        assertNotNull(String.format("Library %s not found!", ezCompLibraryNS), ezcompLib);

        assertTrue(ezcompLib instanceof CompositeComponentLibrary);
        CompositeComponentLibrary cclib = (CompositeComponentLibrary)ezcompLib;

        assertNotNull(cclib.getLibraryDescriptor());
        assertEquals("ezcomp", ezcompLib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, ezcompLib.getType());

        assertEquals(ezCompLibraryNS, ezcompLib.getDefaultNamespace());
        assertEquals(ezCompLibraryNS, ezcompLib.getNamespace());
        Tag t = cclib.getLibraryDescriptor().getTags().get("test");
        assertNotNull(t);

        assertEquals("test", t.getName());
        Attribute a = t.getAttribute("testAttr");
        assertNotNull(a);
        assertEquals("testAttr", a.getName());

        //we should be able to get same info via components
        LibraryComponent comp = ezcompLib.getComponent("test");
        assertEquals("test", comp.getName());
        Tag t2 = comp.getTag();
        assertNotNull(t2);

        Attribute a2 = t2.getAttribute("testAttr");
        assertNotNull(a2);
        assertEquals("testAttr", a2.getName());

    }

    public void testCompositeComponentLibraryWithDescriptor() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String ezCompLibraryNS = "http://ezcomp.com/jsflib";

        Library ezcompLib = instance.getLibrary(ezCompLibraryNS);
        assertNotNull(String.format("Library %s not found!", ezCompLibraryNS), ezcompLib);

        assertTrue(ezcompLib instanceof CompositeComponentLibrary);
        CompositeComponentLibrary cclib = (CompositeComponentLibrary)ezcompLib;

        assertNotNull(cclib.getLibraryDescriptor());
        assertEquals("ezcomp2", ezcompLib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, ezcompLib.getType());

        String ezCompLibraryDefaultNS = LibraryUtils.getCompositeLibraryURL("ezcomp2", instance.isJsf22Plus());
        assertEquals(ezCompLibraryDefaultNS, ezcompLib.getDefaultNamespace());
        assertEquals(ezCompLibraryNS, ezcompLib.getNamespace());
        Tag t = cclib.getLibraryDescriptor().getTags().get("test");
        assertNotNull(t);

        assertEquals("test", t.getName());
        Attribute a = t.getAttribute("testAttr");
        assertNotNull(a);
        assertEquals("testAttr", a.getName());

    }

    public void testClassBaseLibraryWithinCurrentProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://mysite.org/classTaglib";

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertTrue(lib instanceof FaceletsLibrary);
        FaceletsLibrary cblib = (FaceletsLibrary)lib;

        assertNotNull(cblib.getLibraryDescriptor());
        assertEquals("moc", lib.getDefaultPrefix());
        assertSame(LibraryType.CLASS, lib.getType());

        assertEquals(libNs, lib.getNamespace());
        Tag t = cblib.getLibraryDescriptor().getTags().get("mytag");
        assertNotNull(t);

        assertEquals("mytag", t.getName());
        assertNotNull(t.getDescription());

        Attribute a = t.getAttribute("myattr");
        assertNotNull(a);
        assertEquals("myattr", a.getName());
        assertNotNull(a.getDescription());

        //we should be able to get same info via components
        LibraryComponent comp = lib.getComponent("mytag");
        assertEquals("mytag", comp.getName());
        Tag t2 = comp.getTag();
        assertNotNull(t2);

        Attribute a2 = t2.getAttribute("myattr");
        assertNotNull(a2);
        assertEquals("myattr", a2.getName());

    }

    public void testClassBaseLibraryFromLibraryProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://mysite.org/classTaglibIJL";

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertTrue(lib instanceof FaceletsLibrary);
        FaceletsLibrary cblib = (FaceletsLibrary)lib;

        assertNotNull(cblib.getLibraryDescriptor());
        assertEquals("moc", lib.getDefaultPrefix());
        assertSame(LibraryType.CLASS, lib.getType());

        assertEquals(libNs, lib.getNamespace());
        Tag t = cblib.getLibraryDescriptor().getTags().get("mytag");
        assertNotNull(t);

        assertEquals("mytag", t.getName());
        assertNotNull(t.getDescription());

        Attribute a = t.getAttribute("myattr");
        assertNotNull(a);
        assertEquals("myattr", a.getName());
        assertNotNull(a.getDescription());

    }

    public void testCompositeComponentLibraryWithoutDescriptorFromLibraryProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

//        debugLibraries(instance);

        String libNs = LibraryUtils.getCompositeLibraryURL("cclib", instance.isJsf22Plus());

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertTrue(lib instanceof CompositeComponentLibrary);
        CompositeComponentLibrary cclib = (CompositeComponentLibrary)lib;

        assertNotNull(cclib.getLibraryDescriptor());
        assertEquals("cclib", lib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, lib.getType());

        assertEquals(libNs, lib.getDefaultNamespace());
        assertEquals(libNs, lib.getNamespace());
        Tag t = cclib.getLibraryDescriptor().getTags().get("cc");
        assertNotNull(t);

        assertEquals("cc", t.getName());
        Attribute a = t.getAttribute("ccattr");
        assertNotNull(a);
        assertEquals("ccattr", a.getName());

    }

    public void testCompositeComponentLibraryWithDescriptorFromLibraryProject() {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://mysite.org/cclib2";

        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertTrue(lib instanceof CompositeComponentLibrary);
        CompositeComponentLibrary cclib = (CompositeComponentLibrary)lib;

        assertNotNull(cclib.getLibraryDescriptor());
        assertEquals("cclib2", lib.getDefaultPrefix());
        assertSame(LibraryType.COMPOSITE, lib.getType());

        String ezCompLibraryDefaultNS = LibraryUtils.getCompositeLibraryURL("cclib2", instance.isJsf22Plus());
        assertEquals(ezCompLibraryDefaultNS, lib.getDefaultNamespace());
        assertEquals(libNs, lib.getNamespace());
        Tag t = cclib.getLibraryDescriptor().getTags().get("cc2");
        assertNotNull(t);

        assertEquals("cc2", t.getName());
        Attribute a = t.getAttribute("ccattr2");
        assertNotNull(a);
        assertEquals("ccattr2", a.getName());

    }

    public void testLibraryDescriptorWithFunctions() throws Exception {
        JsfSupportImpl instance = getJsfSupportImpl();

        String libNs = "http://java.sun.com/jsp/jstl/functions";
        Library lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        libNs = "http://xmlns.jcp.org/jsp/jstl/functions";
        lib = instance.getLibrary(libNs);
        assertNotNull(String.format("Library %s not found!", libNs), lib);

        assertTrue(lib instanceof FaceletsLibrary);
        FaceletsLibrary flib = (FaceletsLibrary) lib;
        assertNotNull(flib.getFaceletsLibraryDescriptor());
        LibraryDescriptor descriptor = flib.getFaceletsLibraryDescriptor();
        assertTrue(descriptor instanceof FaceletsLibraryDescriptor);
        FaceletsLibraryDescriptor faceletsDescriptor = (FaceletsLibraryDescriptor) descriptor;

        assertTrue(!faceletsDescriptor.getFunctions().isEmpty());
        Map<String, Function> functions = faceletsDescriptor.getFunctions();
        Function function = functions.get("length");
        assertNotNull(function);

        assertEquals(function.getName(), "length");
        assertEquals(function.getSignature(), "int length(java.lang.Object)");
        assertEquals(function.getDescription(), "Returns the number of items in a collection, or the number of characters in a string.");
    }


    //TODO fix the test - it seems to fail on a real bug!
//    public void testModifyCompositeComponentLibrary() throws IOException {
//        //verifies if the composite library model updates if one of the composite
//        //components changes.
//
//        JsfSupportImpl instance = getJsfSupportImpl();
//
//        String ezCompLibraryNS = LibraryUtils.getCompositeLibraryURL("ezcomp");
//
//        Library ezcompLib = instance.getLibrary(ezCompLibraryNS);
//        assertNotNull(String.format("Library %s not found!", ezCompLibraryNS), ezcompLib);
//
//        assertTrue(ezcompLib instanceof CompositeComponentLibrary);
//        CompositeComponentLibrary cclib = (CompositeComponentLibrary)ezcompLib;
//
//        LibraryComponent t = ezcompLib.getComponent("test");
//        assertNotNull(t);
//
//        assertEquals("test", t.getName());
//
//        //now rename the test.xhtml file and check if the library is updated
//        FileObject testFo = getTestFile("testWebProject/web/resources/ezcomp/test.xhtml");
//        assertNotNull(testFo);
//
//        FileLock lock = testFo.lock();
//        try {
//            testFo.rename(lock, "renamed", "xhtml");
//        } finally {
//            lock.releaseLock();
//        }
//
//        testFo.getParent().refresh();
//        refreshIndexAndWait();
//
//        //there shouldn't be such tag
//        LibraryComponent lc2 = cclib.getComponent("test");
//        assertNull(lc2);
//
//        Tag t2 = cclib.getLibraryDescriptor().getTags().get("test");
//        assertNull(t2);
//
//        //but the renamed one
//        LibraryComponent lc3 = cclib.getComponent("renamed");
//        assertNotNull(lc3); //this seems to randomly fail!
//
//        Tag t3 = cclib.getLibraryDescriptor().getTags().get("renamed");
//        assertNotNull(t3);
//
//
//    }


    private void debugLibraries(JsfSupport jsfs) {
        System.out.println("Found libraries:");
        for(Library lib : jsfs.getLibraries().values()) {
            System.out.println(lib.getNamespace());
        }
        System.out.println("-------------------");
    }


}