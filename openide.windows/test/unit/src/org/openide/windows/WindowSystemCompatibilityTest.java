/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.windows;

import junit.framework.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Tests that a window system implementation conforms to the expected
 * behaviour.
 *
 * @author Jaroslav Tulach
 */
public final class WindowSystemCompatibilityTest extends Object {
    /** initialize the lookup for the test */
    public static void init() {
        System.setProperty("org.openide.util.Lookup", WindowSystemCompatibilityTest.class.getName() + "$Lkp");
        
        Object o = Lookup.getDefault();
        if (!(o instanceof Lkp)) {
            Assert.fail("Wrong lookup object: " + o);
        }
    }
    
    private WindowSystemCompatibilityTest(String testName) {
    }

    /** Checks the default implementation.
     */
    public static Test suite() {
        return suite(null);
    }
    
    /** Executes the test for provided window manager.
     */
    public static Test suite(WindowManager wm) {
        init();
        
        Object o = Lookup.getDefault();
        Lkp l = (Lkp)o;
        l.assignWM(wm);
        
        if (wm != null) {
            Assert.assertEquals("Same engine found", wm, WindowManager.getDefault());
        } else {
            o = WindowManager.getDefault();
            Assert.assertNotNull("Engine found", o);
            Assert.assertEquals(DummyWindowManager.class, o.getClass());
        }
        
        TestSuite ts = new TestSuite();
        ts.addTestSuite(WindowManagerHid.class);
        
        return ts;
    }

    /** Default lookup used in the suite.
     */
    public static final class Lkp extends ProxyLookup {
        private InstanceContent ic;
        
        public Lkp() {
            super(new Lookup[0]);
            
            ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            
            setLookups(new Lookup[] {
                al, Lookups.metaInfServices(Lkp.class.getClassLoader())
            });
        }
        
        final void assignWM(WindowManager executionEngine) {
//          ic.setPairs(java.util.Collections.EMPTY_LIST);
            if (executionEngine != null) {
                ic.add(executionEngine);
            }
        }
        
        
    }

}
