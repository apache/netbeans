/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.util.lookup;

import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * Contains tests of class {@code SingletonLookup}.
 *
 * @author  Marian Petras
 */
public class SingletonLookupTest extends NbTestCase {
    
    public SingletonLookupTest(String testName) {
        super(testName);
    }

    public void testBasics() {
        Object orig = new Object();
        Lookup p1 = new SingletonLookup(orig);
        Object obj = p1.lookup(Object.class);
        assertTrue(obj == orig);
        assertNull(p1.lookup(String.class)); 
        assertTrue(orig == p1.lookup(Object.class)); // 2nd time, still the same?
        //
        Lookup p2 = new SingletonLookup("test");
        assertNotNull(p2.lookup(Object.class));
        assertNotNull(p2.lookup(String.class));
        assertNotNull(p2.lookup(java.io.Serializable.class));
    }

    public void testId() {
        Object orig = new Object();
        Lookup l = new SingletonLookup(orig, "id");
        doTest(l, orig);
    }
    public void testDefaultId() {
        Object orig = "id";
        Lookup l = new SingletonLookup(orig);
        doTest(l, orig);
    }

    private void doTest(Lookup l, Object orig) {
        Collection allInstances;


        allInstances = l.lookup(new Lookup.Template<Object>(Object.class, null, null)).allInstances();
        assertNotNull(allInstances);
        assertFalse(allInstances.isEmpty());
        assertEquals(1, allInstances.size());
        assertTrue(allInstances.iterator().next() == orig);

        allInstances = l.lookup(new Lookup.Template<Object>(Object.class, "id", null)).allInstances();
        assertNotNull(allInstances);
        assertFalse(allInstances.isEmpty());
        assertEquals(1, allInstances.size());
        assertTrue(allInstances.iterator().next() == orig);

        allInstances = l.lookup(new Lookup.Template<Object>(Object.class, "not", null)).allInstances();
        assertNotNull(allInstances);
        assertTrue(allInstances.isEmpty());

        if (!(orig instanceof String)) {
            allInstances = l.lookup(new Lookup.Template<String>(String.class, null, null)).allInstances();
            assertNotNull(allInstances);
            assertTrue(allInstances.isEmpty());

            allInstances = l.lookup(new Lookup.Template<String>(String.class, "id", null)).allInstances();
            assertNotNull(allInstances);
            assertTrue(allInstances.isEmpty());

            allInstances = l.lookup(new Lookup.Template<String>(String.class, "not", null)).allInstances();
            assertNotNull(allInstances);
            assertTrue(allInstances.isEmpty());
        }
        
        allInstances = l.lookup(new Lookup.Template<Number>(Number.class, null, null)).allInstances();
        assertNotNull(allInstances);
        assertTrue(allInstances.isEmpty());

        allInstances = l.lookup(new Lookup.Template<Number>(Number.class, "id", null)).allInstances();
        assertNotNull(allInstances);
        assertTrue(allInstances.isEmpty());

        allInstances = l.lookup(new Lookup.Template<Number>(Number.class, "not", null)).allInstances();
        assertNotNull(allInstances);
        assertTrue(allInstances.isEmpty());
    }

    public void testSize() {
        final Object obj = new Object();
        assertSize("The singleton lookup instance should be small",
                   24,
                   new SingletonLookup(obj));
    }

}
