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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.debugger.jpda;

import java.util.List;
import junit.framework.Test;

import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.junit.NbTestCase;

/**
 * The test of heap walking functionality - retrieval of classes, instances and back references.
 * 
 * @author Martin Entlicher
 */
public class HeapWalkingTest extends NbTestCase {
    
    private JPDASupport     support;
    
    /** Creates a new instance of HeapWalkingTest */
    public HeapWalkingTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(HeapWalkingTest.class);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
        LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(System.getProperty ("test.dir.src")+
                             "org/netbeans/api/debugger/jpda/testapps/HeapWalkApp.java"),
                62
            );
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.HeapWalkApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }
    
    public void testClasses () throws Exception {
        List<JPDAClassType> allClasses = support.getDebugger().getAllClasses();
        boolean foundHeapWalkApp = false;
        boolean foundMultiInstanceClass = false;
        System.out.println("All Classes size = "+allClasses.size());
        for (JPDAClassType type : allClasses) {
            //System.out.println("Have class: '"+type.getName()+"'");
            if (type.getName().equals("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp")) {
                foundHeapWalkApp = true;
            }
            if (type.getName().equals("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass")) {
                foundMultiInstanceClass = true;
            }
        }
        assertTrue("The class HeapWalkApp was not found!", foundHeapWalkApp);
        assertTrue("The class MultiInstanceClass was not found!", foundMultiInstanceClass);
        
        List<JPDAClassType> hClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp");
        assertEquals("HeapWalkApp classes bad number: ", 1, hClasses.size());
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        assertEquals("HeapWalkApp classes bad number: ", 1, mClasses.size());
    }

    public void testInstances () throws Exception {
        if (!support.getDebugger().canGetInstanceInfo()) {
            System.out.println("Can not retrieve instance information! Test is skipped.");
            return ; // Nothing to test
        }
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        JPDAClassType mClass = mClasses.get(0);
        assertEquals("Bad instance count: ", mClass.getInstanceCount(), 10);
        List<ObjectVariable> instances = mClass.getInstances(0);
        assertEquals("Bad number of instances: ", instances.size(), 10);
        long[] mClassesInstanceCounts = support.getDebugger().getInstanceCounts(mClasses);
        assertEquals("Bad number of instances: ", mClassesInstanceCounts[0], 10L);
        for (ObjectVariable instance : instances) {
            assertEquals("The class type differs: ", instance.getClassType(), mClass);
        }
    }
    
    public void testBackReferences() throws Exception {
        if (!support.getDebugger().canGetInstanceInfo()) {
            System.out.println("Can not retrieve instance information! Test is skipped.");
            return ; // Nothing to test
        }
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        JPDAClassType mClass = mClasses.get(0);
        List<ObjectVariable> instances = mClass.getInstances(0);
        List<ObjectVariable> referrers = instances.get(0).getReferringObjects(0);
        assertEquals("Bad number of referrers: ", referrers.size(), 1);
        
        List<JPDAClassType> hClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp");
        ObjectVariable hInstance = hClasses.get(0).getInstances(0).get(0);
        ObjectVariable var = referrers.get(0);
        while (var.getUniqueID() != hInstance.getUniqueID()) {
            var = var.getReferringObjects(0).get(0);
            assertNotNull("Object "+hInstance+" not found as a referrer to "+referrers.get(0), var);
        }
    }

}
