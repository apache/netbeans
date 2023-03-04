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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.util.List;
import java.util.Properties;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;

/**
 *
 * @author Martin
 */
public class MethodInvocationTest extends NbTestCase {
    
    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    
    public MethodInvocationTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(MethodInvocationTest.class);
    }
    
    public void testTargetMirrors() throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/MirrorValuesApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            
            support = JPDASupport.attach ("org.netbeans.api.debugger.jpda.testapps.MirrorValuesApp");

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit
            
            JPDADebugger debugger = support.getDebugger();
            
            List<JPDAClassType> systemClasses = debugger.getClassesByName("java.lang.System");
            assertEquals(systemClasses.size(), 1);
            JPDAClassType systemClass = systemClasses.get(0);
            Properties properties = System.getProperties();
            Variable propertiesVar = systemClass.invokeMethod("getProperties", "()Ljava/util/Properties;", new Variable[]{});
            Value pv = ((JDIVariable) propertiesVar).getJDIValue();
            assertTrue("Properties "+pv, (pv instanceof ObjectReference) &&
                                         Properties.class.getName().equals(((ClassType) pv.type()).name()));
            String userHomeProperty = properties.getProperty("user.home");
            Variable propVar = ((ObjectVariable) propertiesVar).invokeMethod("getProperty",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    new Variable[] { debugger.createMirrorVar("user.home") });
            Value p = ((JDIVariable) propVar).getJDIValue();
            assertTrue(p instanceof StringReference);
            assertEquals(userHomeProperty, ((StringReference) p).value());
        } finally {
            support.doFinish ();
        }
    }
}
