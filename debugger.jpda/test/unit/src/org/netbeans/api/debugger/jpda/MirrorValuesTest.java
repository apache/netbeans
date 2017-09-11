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
import com.sun.jdi.IntegerValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.awt.Color;
import java.awt.Point;
import java.beans.FeatureDescriptor;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.openide.util.Exceptions;

/**
 * Tests {@link Variable#createMirrorObject()} and {@link MutableVariable#setFromMirrorObject(java.lang.Object)}.
 * 
 * @author Martin Entlicher
 */
public class MirrorValuesTest extends NbTestCase {
    
    private static final String CLASS_NAME =
        "org.netbeans.api.debugger.jpda.testapps.MirrorValuesApp";
    
    private static String[] names =
        { "boo", "b", "c", "s", "i", "l", "f", "d",
          "iarr",
          "darr",
          "str", "integer", "date", "color",
          "point", "file", "url", "url2" };
    private static Object[] mirrors =
        { true, (byte) 5, 'c', (short) 512, 10000, Long.MAX_VALUE, 12.12f, 1e150,
          new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
          new double[][] { { 0.1, 0.2, 0.3 }, { 1.1, 1.2, 1.3 }, { 2.1, 2.2, 2.3 } },
          "A String", Integer.MIN_VALUE, new Date(1000000000l), Color.RED,
          new Point(10, 10), new File("/tmp/Foo.txt"), createURL("http://netbeans.org"),
          null/*pristine URL*/ };
    private static Object[] newMirrors =
        { false, (byte) 255, 'Z', (short) -1024, -1, 123456789101112l, 2e-2f, -3e250,
          new int[] { 9, 7, 5, 3, 1 },
          new double[][] { { 1e100, 2e200 }, { 1.1e100, 2.1e200 }, { 1.2e100, 2.2e200 }, { 1.3e100, 2.3e200 } },
          "An alternate sTRING", -1048576, new Date(3333333333l), Color.GREEN,
          new Point(-100, -100), new File("/tmp/Test.java"), createURL("http://debugger.netbeans.org"),
          null };
        
    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    public MirrorValuesTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(MirrorValuesTest.class);
    }
    
    public void testMirrors() throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/MirrorValuesApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            LineBreakpoint lb2 = bp.getLineBreakpoints().get(1);
            dm.addBreakpoint (lb2);

            support = JPDASupport.attach (CLASS_NAME);

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit
            
            CallStackFrame sf = support.getDebugger ().getCurrentCallStackFrame ();
            LocalVariable [] vars = sf.getLocalVariables ();
            Map<String, Variable> variablesByName = getVariablesByName(vars);
            Map<String, Object> mirrorsByName = getMirrorsByName(mirrors);
            
            Variable v;
            Object m;
            
            // Test of createMirrorObject():
            for (String name : mirrorsByName.keySet()) {
                v = variablesByName.get(name);
                System.err.println("Creating a mirror of "+name);
                m = v.createMirrorObject();
                Object mm = mirrorsByName.get(name);
                if (mm == null) {
                    assertNull(name, m);
                    continue;
                }
                assertNotNull(name, m);
                m.toString(); // Test that the mirror can process toString()
                if (mm.getClass().isArray()) {
                    assertTrue(name+" is array", m.getClass().isArray());
                    assertTrue(name+" array "+arrayToString(mm)+
                               " does not equal to "+arrayToString(m),
                               compareArrays(mm, m));
                } else {
                    assertEquals(name, mm, m);
                }
            }
            
            mirrorsByName = getMirrorsByName(newMirrors);
            for (String name : mirrorsByName.keySet()) {
                v = variablesByName.get(name);
                m = mirrorsByName.get(name);
                System.err.println("Setting to "+name+" value = "+m);
                try {
                    ((MutableVariable) v).setFromMirrorObject(m);
                } catch (Exception ex) {
                    System.err.println("Exception "+ex.getLocalizedMessage()+" when setting from "+m);
                    throw ex;
                }
            }
            
            // Test of objects referenced in a circle
            
            v = variablesByName.get("selfReferencedList");
            m = v.createMirrorObject();
            assertTrue("mirror is "+m, m instanceof LinkedList);
            assertTrue("mirror is "+m, ((List) m).get(0) == m);
            
            v = variablesByName.get("event");
            m = v.createMirrorObject();
            assertTrue("mirror is "+((m == null) ? null : m.getClass()), m instanceof EventObject);
            Object source = ((EventObject) m).getSource();
            assertTrue("mirror's source is "+((source == null) ? null : source.getClass()),
                       source instanceof FeatureDescriptor);
            assertTrue(((FeatureDescriptor) ((EventObject) m).getSource()).getValue("event") == m);
            
            /*
            // Test of setFromMirrorObject():
            v = variablesByName.get("boo");
            assertEquals("boo", "true", v.getValue());
            ((MutableVariable) v).setFromMirrorObject(Boolean.FALSE);
            assertEquals("boo", "false", v.getValue());
            
            v = variablesByName.get("i");
            assertEquals("i", "10000", v.getValue());
            ((MutableVariable) v).setFromMirrorObject(12345);
            assertEquals("i", "12345", v.getValue());
            
            v = variablesByName.get("color");
            assertEquals("color", "java.awt.Color[r=255,g=0,b=0]", ((ObjectVariable) v).getToStringValue());
            ((MutableVariable) v).setFromMirrorObject(Color.GREEN);
            assertEquals("color", "java.awt.Color[r=0,g=255,b=0]", ((ObjectVariable) v).getToStringValue());
            */
            
            support.doContinue();
            support.waitState (JPDADebugger.STATE_STOPPED);  // the next breakpoint hit
            sf = support.getDebugger ().getCurrentCallStackFrame ();
            vars = sf.getLocalVariables ();
            variablesByName = getVariablesByName(vars);
            Variable newValues = variablesByName.get("newValues");
            boolean success = Boolean.TRUE.equals(newValues.createMirrorObject());
            if (!success) {
                // Find the difference for report:
                for (String name : mirrorsByName.keySet()) {
                    v = variablesByName.get(name);
                    m = v.createMirrorObject();
                    assertNotNull("Wrong new value: "+name, m);
                    Object mm = mirrorsByName.get(name);
                    if (mm.getClass().isArray()) {
                        assertTrue("Wrong new value: "+name+" is array", m.getClass().isArray());
                        assertTrue("Wrong new value: "+name+" array "+arrayToString(mm)+
                                   " does not equal to "+arrayToString(m),
                                   compareArrays(mm, m));
                    } else {
                        assertEquals("Wrong new value: "+name, mm, m);
                    }
                }
            }
            assertTrue("The new values were set successfully", success);

        } finally {
            support.doFinish ();
        }
        
    }
    
    public void testTargetMirrors() throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/MirrorValuesApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            
            support = JPDASupport.attach (CLASS_NAME);

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit
            
            JPDADebugger debugger = support.getDebugger();
            
            Variable mirrorVar = debugger.createMirrorVar("Test");
            Value v = ((JDIVariable) mirrorVar).getJDIValue();
            assertTrue("Value "+v+" should be a String", v instanceof StringReference);
            assertEquals("Test", ((StringReference) v).value());
            
            Point p = new Point(-1, 1);
            mirrorVar = debugger.createMirrorVar(p);
            Object mp = mirrorVar.createMirrorObject();
            assertTrue("Correct point was created: "+mp, p.equals(mp));
            
            mirrorVar = debugger.createMirrorVar(1);
            v = ((JDIVariable) mirrorVar).getJDIValue();
            assertTrue("Value "+v+" should be an Integer object.",
                       (v.type() instanceof ClassType) && Integer.class.getName().equals(((ClassType) v.type()).name()));
            
            mirrorVar = debugger.createMirrorVar(1, true);
            v = ((JDIVariable) mirrorVar).getJDIValue();
            assertTrue("Value "+v+" should be an int.", v instanceof IntegerValue);
            assertEquals(((IntegerValue) v).value(), 1);
        } finally {
            support.doFinish ();
        }
    }
    
    private static boolean compareArrays(Object arr1, Object arr2) {
        if (arr1 instanceof Object[]) {
            return Arrays.deepEquals((Object[]) arr1, (Object[]) arr2);
        } else if (arr1 instanceof int[]) {
            return Arrays.equals((int[]) arr1, (int[]) arr2);
        } else {
            throw new IllegalStateException(arr1+", "+arr2);
        }
    }
    
    private static String arrayToString(Object a) {
        if (a instanceof Object[]) {
            return Arrays.deepToString((Object[]) a);
        } else if (a instanceof int[]) {
            return Arrays.toString((int[]) a);
        } else {
            throw new IllegalStateException(a.toString());
        }
    }
    
    private static Map<String, Variable> getVariablesByName(LocalVariable[] vars) {
        Map<String, Variable> map = new LinkedHashMap<String, Variable>();
        for (LocalVariable lv : vars) {
            assertTrue("Not mutable", lv instanceof MutableVariable);
            map.put(lv.getName(), lv);
        }
        return map;
    }
    
    private static Map<String, Object> getMirrorsByName(Object[] mirrors) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        assertEquals(names.length, mirrors.length);
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], mirrors[i]);
        }
        return map;
    }
    
    private static URL createURL(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            url = null;
        }
        return url;
    }
}
