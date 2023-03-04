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

package org.netbeans.core.startup;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Dependency;
/** Test AutomaticDependencies (end-to-end).
 * @author Jesse Glick
 */
public class AutomaticDependenciesTest extends NbTestCase {

    public AutomaticDependenciesTest(String name) {
        super(name);
    }

    private AutomaticDependencies ad;
    protected void setUp() throws Exception {
        ad = AutomaticDependencies.parse(new URL[] {
            AutomaticDependenciesTest.class.getResource("data/auto-deps-1.xml"),
            AutomaticDependenciesTest.class.getResource("data/auto-deps-2.xml"),
        });
    }
    
    public void testBasicOperation() throws Exception {
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.3"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "unrelated"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig > 1.0"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig2/1 > 1.5.1"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        AutomaticDependencies.Report rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("Java > 1.3, Nue, nue > 1.0, nue2 > 1.0, orig > 1.0, orig2/1 > 1.5.1, unrelated", normal(_deps));
        assertTrue(rep.isModified());
        assertEquals("message-1, message-2", normalMessages(rep));
    }
    
    public void testExcludes() throws Exception {
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig > 1.0"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("a", _deps);
        assertEquals("orig > 1.0", normal(_deps));
        _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("a.x", _deps);
        assertEquals("Nue, nue > 1.0, orig > 1.0", normal(_deps));
        _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("b", _deps);
        assertEquals("orig > 1.0", normal(_deps));
        _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("b.x", _deps);
        assertEquals("orig > 1.0", normal(_deps));
    }
    
    public void testVersionSensitivity() throws Exception {
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig > 1.1"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig2/2 > 0.1"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("random", _deps);
        assertEquals("orig > 1.1, orig2/2 > 0.1", normal(_deps));
    }
    
    public void testCancellations() throws Exception {
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, "javax.death[AbstractCoffin] > 1.0"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        AutomaticDependencies.Report rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("javax.death/1 > 1.0", normal(_deps));
        assertEquals("message-1", normalMessages(rep));
    }
    
    public void testMerges() throws Exception {
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, "javax.death[AbstractCoffin] > 1.0"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "javax.death/1 > 1.0"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("random", _deps);
        assertEquals("javax.death/1 > 1.0", normal(_deps));
        deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, "javax.death[AbstractCoffin] > 1.0"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "javax.death/1 > 0.3"));
        _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("random", _deps);
        assertEquals("javax.death/1 > 1.0", normal(_deps));
        deps = new HashSet<Dependency>(); // Set<Dependency>
        deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, "[javax.death.AbstractCoffin]"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "javax.death/1 > 0.3"));
        _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("random", _deps);
        assertEquals("javax.death/1 > 1.0", normal(_deps));
        deps = new HashSet<Dependency>(); // Set<Dependency>
        deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, "javax.death[AbstractCoffin] > 1.0"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "javax.death/1 > 1.3"));
        _deps = new HashSet<Dependency>(deps);
        ad.refineDependencies("random", _deps);
        assertEquals("javax.death/1 > 1.3", normal(_deps));
    }
    
    public void testExclusionOfImplDeps() throws Exception {
        // Cf. #46961.
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig = some-orig-version"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig2/1 = some-orig2-version"));
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "orig2/0 = some-ancient-orig2-version"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        AutomaticDependencies.Report rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("orig = some-orig-version, orig2/0 = some-ancient-orig2-version, orig2/1 = some-orig2-version", normal(_deps));
        assertFalse(rep.isModified());
        assertEquals("", normalMessages(rep));
    }

    public void testCanUpgradeMajorVersions() throws Exception {
        // Need to be able to say that anything using module/0 (w/ or w/o spec) should use module/1 instead.
        // Plain dep on old module is upgraded:
        Set<Dependency> deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "stabilized/0"));
        Set<Dependency> _deps = new HashSet<Dependency>(deps);
        AutomaticDependencies.Report rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("stabilized/1 > 1.0", normal(_deps));
        assertTrue(rep.isModified());
        assertEquals("message-1a", normalMessages(rep));
        // Spec dep on old module is upgraded:
        deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "stabilized/0 > 0.5"));
        _deps = new HashSet<Dependency>(deps);
        rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("stabilized/1 > 1.0", normal(_deps));
        assertTrue(rep.isModified());
        assertEquals("message-1a", normalMessages(rep));
        // Impl dep on old module is left alone (will be useless anyway on new version):
        deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "stabilized/0 = whatever"));
        _deps = new HashSet<Dependency>(deps);
        rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("stabilized/0 = whatever", normal(_deps));
        assertFalse(rep.isModified());
        assertEquals("", normalMessages(rep));
        // Plain dep on new module is left alone:
        deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "stabilized/1"));
        _deps = new HashSet<Dependency>(deps);
        rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("stabilized/1", normal(_deps));
        assertFalse(rep.isModified());
        assertEquals("", normalMessages(rep));
        // Spec dep on new module is left alone:
        deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "stabilized/1 > 1.1"));
        _deps = new HashSet<Dependency>(deps);
        rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("stabilized/1 > 1.1", normal(_deps));
        assertFalse(rep.isModified());
        assertEquals("", normalMessages(rep));
        // Impl dep on new module is left alone:
        deps = new HashSet<Dependency>();
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "stabilized/1 = whatever"));
        _deps = new HashSet<Dependency>(deps);
        rep = ad.refineDependenciesAndReport("random", _deps);
        assertEquals("stabilized/1 = whatever", normal(_deps));
        assertFalse(rep.isModified());
        assertEquals("", normalMessages(rep));
    }
    
    private static String normal(Set<Dependency> deps) {
        SortedSet<String> s = new TreeSet<String>(); // Set<String>
        Iterator<Dependency> it = deps.iterator();
        while (it.hasNext()) {
            Dependency d = it.next();
            s.add(dep2String(d));
        }
        StringBuilder b = new StringBuilder();
        Iterator<String> it2 = s.iterator();
        while (it2.hasNext()) {
            b.append(it2.next());
            if (it2.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
    
    private static String normalMessages(AutomaticDependencies.Report rep) {
        SortedSet<String> s = new TreeSet<String>(); // Set<String>
        Iterator it = rep.getMessages().iterator();
        while (it.hasNext()) {
            s.add((String)it.next());
        }
        StringBuilder b = new StringBuilder();
        it = s.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
    
    private static String dep2String(Dependency d) {
        StringBuilder b = new StringBuilder();
        b.append(d.getName());
        switch (d.getComparison()) {
        case Dependency.COMPARE_ANY:
            break;
        case Dependency.COMPARE_IMPL:
            b.append(" = ");
            b.append(d.getVersion());
            break;
        case Dependency.COMPARE_SPEC:
            b.append(" > ");
            b.append(d.getVersion());
            break;
        default:
            throw new IllegalStateException();
        }
        return b.toString();
    }
    
}
