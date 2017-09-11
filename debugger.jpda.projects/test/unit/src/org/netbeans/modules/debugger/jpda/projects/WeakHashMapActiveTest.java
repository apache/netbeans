/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.projects;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Martin Entlicher
 */
public class WeakHashMapActiveTest extends NbTestCase {
    
    public WeakHashMapActiveTest(String name) {
        super(name);
    }
    
    public void testRelease() throws Exception {
        Object k1 = new Object();
        Object k2 = new Object();
        Object v1 = new Object();
        Object v2 = new Object();
        Reference rv1 = new WeakReference(v1);
        Reference rv2 = new WeakReference(v2);
        WeakHashMapActive whma = new WeakHashMapActive();
        whma.put(k1, v1);
        whma.put(k2, v2);
        assertEquals("Size", 2, whma.size());
        assertEquals(v1, whma.get(k1));
        assertEquals(v2, whma.get(k2));
        Reference rk1 = new WeakReference(k1);
        Reference rk2 = new WeakReference(k2);
        k1 = k2 = null;
        System.gc();
        int[] arr = new int[10000000];
        arr[1000000] = 10;
        arr = null;
        System.gc();
        assertGC("WeakHashMapActive's key is not released from memory", rk1);
        assertGC("WeakHashMapActive's key is not released from memory", rk2);
        assertEquals("Size", 0, whma.size());
        v1 = v2 = null;
        System.gc();
        arr = new int[20000000];
        arr[1000000] = 10;
        arr = null;
        System.gc();
        assertGC("WeakHashMapActive's value is not released from memory", rv1);
        assertGC("WeakHashMapActive's value is not released from memory", rv2);
    }
}
