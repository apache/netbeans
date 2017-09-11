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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.core.osgi;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;

public class DependencyQueueTest extends NbTestCase {

    public DependencyQueueTest(String n) {
        super(n);
    }

    public void testOfferNoDeps() {
        DependencyQueue<String,String> q = new DependencyQueue<String,String>();
        assertEquals(list("1"), q.offer("1", set(), set(), set()));
    }
    
    public void testOfferApiAndImpl() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list("api"), q.offer("api", set("api"), set(), set()));
        assertEquals(list("impl"), q.offer("impl", set(), set("api"), set()));
    }
    
    public void testOfferImplRequiresAPI() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("impl", set(), set("api"), set()));
        assertEquals(list("api", "impl"), q.offer("api", set("api"), set(), set()));
    }
    
    public void testAPIThatNeedsItsImpl() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("api", set("api"), set(), set("impl")));
        assertEquals(list(), q.offer("client", set(), set("api"), set()));
        assertEquals(list("api", "impl", "client"), q.offer("impl", set("impl"), set("api"), set()));
    }
    public void testSimpleChain3requires2requires1() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("3", set("3"), set("2"), set()));
        assertEquals(list(), q.offer("2", set("2"), set("1"), set()));
        assertEquals(list("1", "2", "3"), q.offer("1", set("1"), set(), set()));
    }
    
    public void test1requires2needs3requires1() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("1", set("1"), set("2"), set()));
        assertEquals(list(), q.offer("2", set("2"), set(), set("3")));
        assertEquals(list("2", "1", "3"), q.offer("3", set("3"), set("1"), set()));
    }
    
    public void testAffectedByOrderOnJDK8_1requires2and0_2needs3_3requires1_Oisthere() {
        DependencyQueue<String, String> q = new DependencyQueue<String,String>();
        assertEquals(list(), q.offer("1", set("1"), set("2", "0"), set()));
        assertEquals(list(), q.offer("2", set("2"), set(), set("3")));
        assertEquals(list(), q.offer("3", set("3"), set("1"), set()));
        assertEquals(list("0", "2", "1", "3"), q.offer("0", set("0"), set(), set()));
    }

    public void testRetract() {
        DependencyQueue<String,String> q = new DependencyQueue<String,String>();
        q.offer("1", set(), set(), set());
        assertEquals(list("1"), q.retract("1"));
        q = new DependencyQueue<String,String>();
        q.offer("api", set("api"), set(), set());
        q.offer("impl", set(), set("api"), set());
        assertEquals(list("impl"), q.retract("impl"));
        assertEquals(list("api"), q.retract("api"));
        q = new DependencyQueue<String,String>();
        q.offer("api", set("api"), set(), set());
        q.offer("impl", set(), set("api"), set());
        assertEquals(list("impl", "api"), q.retract("api"));
        assertEquals(list(), q.retract("impl"));
    }

    // XXX testGC

    private static List<String> list(String... items) {
        return Arrays.asList(items);
    }

    private static Set<String> set(String... items) {
        return new TreeSet<String>(list(items));
    }

}
