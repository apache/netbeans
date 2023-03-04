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

package org.openide.modules;

import java.util.Iterator;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

/** Test parsing of dependency information and loading of static version information.
 * Ensures that malformed manifests are rejected correctly.
 * @author Jesse Glick
 */
public class DependencyTest extends NbTestCase {

    public DependencyTest(String name) {
        super(name);
    }

    public void testParseModule() throws Exception {
        Set singleton = Dependency.create(Dependency.TYPE_MODULE, "org.foo.bar/1 > 1.1");
        assertTrue(singleton.size() == 1);
        Dependency d = (Dependency)singleton.iterator().next();
        //System.err.println("dependency: " + d);
        assertTrue(d.getType() == Dependency.TYPE_MODULE);
        assertEquals("org.foo.bar/1", d.getName());
        assertTrue(d.getComparison() == Dependency.COMPARE_SPEC);
        assertEquals("1.1", d.getVersion());
    }
    
    public void testParsePackage() throws Exception {
        Set singleton = Dependency.create(Dependency.TYPE_PACKAGE, "javax.help[HelpSet] = 1.1.2  ");
        assertTrue(singleton.size() == 1);
        Dependency d = (Dependency)singleton.iterator().next();
        assertTrue(d.getType() == Dependency.TYPE_PACKAGE);
        assertEquals("javax.help[HelpSet]", d.getName());
        assertTrue(d.getComparison() == Dependency.COMPARE_IMPL);
        assertEquals("1.1.2", d.getVersion());
    }
    
    public void testParseJavaPlatform() throws Exception {
        Set singleton = Dependency.create(Dependency.TYPE_JAVA, "Java > 1.3");
        assertTrue(singleton.size() == 1);
        Dependency d = (Dependency)singleton.iterator().next();
        assertTrue(d.getType() == Dependency.TYPE_JAVA);
        assertEquals(Dependency.JAVA_NAME, d.getName());
        assertTrue(d.getComparison() == Dependency.COMPARE_SPEC);
        assertEquals("1.3", d.getVersion());
    }
    
    public void testParseRequires() throws Exception {
        Set singleton = Dependency.create(Dependency.TYPE_REQUIRES, "some.thing.here");
        assertTrue(singleton.size() == 1);
        Dependency d = (Dependency)singleton.iterator().next();
        assertTrue(d.getType() == Dependency.TYPE_REQUIRES);
        assertEquals("some.thing.here", d.getName());
        assertTrue(d.getComparison() == Dependency.COMPARE_ANY);
        assertEquals(null, d.getVersion());
        Dependency.create(Dependency.TYPE_REQUIRES, "org.foo.Thing$Inner");
    }
    
    public void testParseMultiple() throws Exception {
        Set multiple = Dependency.create(Dependency.TYPE_MODULE, " org.foo/1 > 0.1, org.bar  ");
        assertTrue(multiple.size() == 2);
        Iterator it = multiple.iterator();
        Dependency d1 = (Dependency)it.next();
        Dependency d2 = (Dependency)it.next();
        if (d1.getName().equals("org.bar")) {
            // Swap so they are in order.
            Dependency tmp = d1;
            d1 = d2;
            d2 = tmp;
        }
        assertTrue(d1.getType() == Dependency.TYPE_MODULE);
        assertEquals("org.foo/1", d1.getName());
        assertTrue(d1.getComparison() == Dependency.COMPARE_SPEC);
        assertEquals("0.1", d1.getVersion());
        assertTrue(d2.getType() == Dependency.TYPE_MODULE);
        assertEquals("org.bar", d2.getName());
        assertTrue(d2.getComparison() == Dependency.COMPARE_ANY);
        assertNull(d2.getVersion());
        assertTrue(Dependency.create(Dependency.TYPE_PACKAGE, null).isEmpty());
    }
    
    public void testParseCodename() throws Exception {
        Dependency.create(Dependency.TYPE_MODULE, "org.foo.thing");
    }
    
    public void testParseRangedRelVers() throws Exception {
        Dependency d1 = Dependency.create(Dependency.TYPE_MODULE, "org.foo/1-2 > 1.1").iterator().next();
        Dependency d2 = Dependency.create(Dependency.TYPE_MODULE, "org.foo/2-4").iterator().next();
        assertEquals("org.foo/1-2", d1.getName());
        assertEquals(Dependency.COMPARE_SPEC, d1.getComparison());
        assertEquals("1.1", d1.getVersion());
        assertEquals("org.foo/2-4", d2.getName());
        assertEquals(Dependency.COMPARE_ANY, d2.getComparison());
    }
    
    public void testAllowJavaIdentifiers() throws Exception {
        Set<Dependency> single = Dependency.create(Dependency.TYPE_MODULE, "acme.j2ee.webapp.import");
        assertEquals("One item created: " + single, 1, single.size());
    }

    private void misparse(int type, String s) {
        try {
            Dependency.create(type, s);
            assertTrue("parsing should have failed for " + s, false);
        } catch (IllegalArgumentException iae) {
            // Expected, OK.
        }
    }
    
    public void testMisparseNothing() throws Exception {
        misparse(Dependency.TYPE_MODULE, "");
        misparse(Dependency.TYPE_MODULE, "  ");
    }
    
    public void testMisparseWrongType() throws Exception {
        misparse(Dependency.TYPE_JAVA, "org.foo/1 > 0.9");
        Dependency.create(Dependency.TYPE_JAVA, "VM = whatever");
        misparse(Dependency.TYPE_JAVA, "VM");
    }
    
    public void testMisparseBadCodename() throws Exception {
        misparse(Dependency.TYPE_MODULE, "org.foo/-1");
        misparse(Dependency.TYPE_MODULE, "org-foo/1");
        misparse(Dependency.TYPE_MODULE, "org.foo./1");
    }
    
    public void testMisparseBadRangedRelVers() throws Exception {
        misparse(Dependency.TYPE_MODULE, "org.foo/-1-5");
        misparse(Dependency.TYPE_MODULE, "org.foo/3-3");
        misparse(Dependency.TYPE_MODULE, "org.foo/3-");
        misparse(Dependency.TYPE_MODULE, "org.foo/-3");
        misparse(Dependency.TYPE_MODULE, "org.foo/3,4,5");
        misparse(Dependency.TYPE_MODULE, "org.foo/1-2 = build99");
        misparse(Dependency.TYPE_MODULE, "org.foo/[123] > 1");
    }
    
    public void testMisparseBadRelease() throws Exception {
        misparse(Dependency.TYPE_MODULE, "org.foo/new");
    }
    
    public void testMisparseMissingVersion() throws Exception {
        misparse(Dependency.TYPE_MODULE, "org.foo >");
    }
    
    public void testMisparseBadSpec() throws Exception {
        // Cf. #5573.
        misparse(Dependency.TYPE_MODULE, "org.foo > 1.2beta");
    }
    
    public void testMisparseMalformedPackage() throws Exception {
        misparse(Dependency.TYPE_PACKAGE, "org.foo/1");
        misparse(Dependency.TYPE_PACKAGE, "foo[something");
        misparse(Dependency.TYPE_PACKAGE, "org.foo[bar.baz]");
    }
    
    public void testMisparseGreaterThanEquals() throws Exception {
        misparse(Dependency.TYPE_MODULE, "org.foo/1 >= 1.1");
    }
    
    public void testMisparseScriptingError() throws Exception {
        // Scripting module did this. You cannot declare a numeric dependency
        // on a single class--there is no Package object to even compare it to.
        misparse(Dependency.TYPE_PACKAGE, "[org.python.util.PythonInterpreter] > 1.0");
    }
    
    public void testMisparseRequiresWithVersion() throws Exception {
        misparse(Dependency.TYPE_REQUIRES, "some.thing > 1.0");
        misparse(Dependency.TYPE_REQUIRES, "some.thing = whatever-impl-version");
    }
    
    public void testMisparseRequiresBadCodeName() throws Exception {
        misparse(Dependency.TYPE_REQUIRES, "some-thing");
    }
    
    public void testMisparseDuplicates() throws Exception {
        misparse(Dependency.TYPE_MODULE, "org.foo/1 > 1.0, org.foo/2 > 2.0");
        // Was actually used in tomcatint/tomcat40/manifest.mf:
        Dependency.create(Dependency.TYPE_PACKAGE, "[org.apache.jasper.Constants], [org.apache.jasper.Options]");
        Dependency.create(Dependency.TYPE_PACKAGE, "org.apache.jasper[Constants], [org.apache.jasper.Options]");
        misparse(Dependency.TYPE_PACKAGE, "org.apache.jasper[Constants], org.apache.jasper[Options]");
        misparse(Dependency.TYPE_PACKAGE, "org.apache.jasper > 1.1, org.apache.jasper[Options]");
        misparse(Dependency.TYPE_REQUIRES, "foo.bar, foo.bar");
        misparse(Dependency.TYPE_JAVA, "Java > 1.4.0, Java = 1.4.0_01");
    }
    
    public void testAllowNumbers() throws Exception {
        Dependency.create(Dependency.TYPE_MODULE, "acme.2.webapp.importing");
        Dependency.create(Dependency.TYPE_MODULE, "acme.2xyz.webapp.importing");
        Dependency.create(Dependency.TYPE_MODULE, "org.apache.servicemix.specs.jsr303_api_1.0.0");
    }
    
    public void testConstants() throws Exception {
        assertEquals("Java", Dependency.JAVA_NAME);
        assertNotNull(Dependency.JAVA_SPEC);
        assertNotNull(Dependency.JAVA_IMPL);
        assertEquals("VM", Dependency.VM_NAME);
        assertNotNull(Dependency.VM_SPEC);
        assertNotNull(Dependency.VM_IMPL);
    }
    
}
