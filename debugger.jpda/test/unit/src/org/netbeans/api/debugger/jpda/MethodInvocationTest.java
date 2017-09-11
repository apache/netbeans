/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
