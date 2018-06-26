/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.client.cmdline;

import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;

/**
 *
 * @author Martin Entlicher
 */
public class V8ECMAScript6Test extends AbstractTestBase {
    
    private static final String NODE_ARG_DBG = "--debug-brk";   // NOI18N
    private static final String TEST_STABLE_FILE = "es6/TestECMAScript6_V8Stable.js"; // NOI18N
    private static final String TEST_ARROWS_FILE = "es6/TestECMAScript6_Arrows.js"; // NOI18N
    private static final String TEST_CLASSES_FILE = "es6/TestECMAScript6_Classes.js"; // NOI18N
    private static final String NODE_ARG_ARROWS = "--harmony_arrow_functions";   // NOI18N
    private static final String NODE_ARG_CLASSES = "--harmony_classes"; // NOI18N
    
    private static final int LINE_BRKP_VAR_TYPES = 47;//71;
    
    public V8ECMAScript6Test() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        // To block standard in:
        System.setIn(new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ex) {
                    throw new IOException(ex.getLocalizedMessage());
                }
                return -1;
            }
        });
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
    }
    
    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(2000); // To recover
        if (nodeProcess != null) {
            nodeProcess.destroy();
        }
    }
    
    @Test
    public void testStable() throws IOException, InterruptedException {
        startUp(V8DebugTest.class.getResourceAsStream(TEST_STABLE_FILE), TEST_STABLE_FILE, NODE_ARG_DBG);
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        
//        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_VAR_TYPES);
//        lastResponse = responseHandler.getLastResponse();
//        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
//        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 2, testFilePath, LINE_BRKP_VAR_TYPES-1, -1, 0);
        // Get unknown values when stopped on breakpoint. Doing next instead...
        V8Debug.TestAccess.doCommand(v8dbg, "next 15");
        responseHandler.getLastResponse();
        
        // Start testing:
        checkVarTypes();
        
        V8Debug.TestAccess.doCommand(v8dbg, "next 4");
        responseHandler.getLastResponse();
        
        checkLocalVar("n", 10l, false);
        checkLocalVar("ts", "The n = 10.", false);
        checkLocalVar("ts2", "Multi\n" +
                             "line\n" +
                             "String", false);
        
        //checkFrame(0, LINE_BEGIN-1, "var glob_n = 100;");
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
    }
    
    private void checkVarTypes() throws IOException, InterruptedException {
        checkLocalVar("set", new ES6TypesCheck("set", "#<Set>"), false);
        checkLocalVar("map", new ES6TypesCheck("map", "#<Map>"), false);
        checkLocalVar("symbol", new ES6TypesCheck("symbol", "Symbol(symbolKey)"), false);
        checkLocalVar("promise", new ES6TypesCheck("promise", "#<Promise>"), false);
        
        checkEval("set", new ES6TypesCheck("set", "#<Set>"));
        checkEval("weakSet", new ES6TypesCheck("set", "#<WeakSet>"));
        checkEval("weakMap", new ES6TypesCheck("map", "#<WeakMap>"));
        
        checkEval("iter", new ObjectCheck("Array Iterator", null, null, "#<Object>"));
        //checkEval("iter", new ES6TypesCheck("iterator", "#<Iterator>"));
        checkEval("it1.value", new ObjectCheck("Array", null, null, "#<Array>"));
        
        checkEval("gen", new FunctionCheck("",
                                           "gen",
                                           "function* () {\n    var pre = 0, cur = 1;\n    for (;;) {\n      var temp = pre;\n      pre = cur;\n      cur += temp;\n      yield cur;\n    }\n  }",
                                           testFilePath, 76,
                                           2699, 57, 19,
                                           null, null));
    }
    
    @Test
    public void testClasses() throws IOException, InterruptedException {
        startUp(V8DebugTest.class.getResourceAsStream(TEST_CLASSES_FILE),
                TEST_CLASSES_FILE,
                new String[] { NODE_ARG_DBG, NODE_ARG_CLASSES });
        
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        
        V8Debug.TestAccess.doCommand(v8dbg, "next 5");
        responseHandler.getLastResponse();
        
        checkLocalVar("p", new ObjectCheck("Object",
                                           new String[] { "x", "y" },
                                           new Object[] { 15l, 25l },
                                           "#<Point>"), false);
    }
    
    @Test
    public void testArrows() throws IOException, InterruptedException {
        startUp(V8DebugTest.class.getResourceAsStream(TEST_ARROWS_FILE),
                TEST_ARROWS_FILE,
                new String[] { NODE_ARG_DBG, NODE_ARG_ARROWS });
        
        
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        
        V8Debug.TestAccess.doCommand(v8dbg, "next 4");
        responseHandler.getLastResponse();
        
//        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+50);
//        lastResponse = responseHandler.getLastResponse();
//        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
//        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 2, testFilePath, 50-1, -1, 0);

        checkLocalVar("arrow", new FunctionCheck("", "f", "f => f + 1",
                                                 testFilePath, 76,
                                                 2192, 42, 12,
                                                 null, null), false);
        checkLocalVar("cat", "abc", false);

        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+53);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 2, testFilePath, 53-1, null, 4);
        
        checkLocalVar("cat", "abc", false);
        
        V8Debug.TestAccess.doCommand(v8dbg, "c");
        lastResponse = responseHandler.getLastResponse();
        checkLocalVar("elem", "a", true);
        V8Debug.TestAccess.doCommand(v8dbg, "next");
        responseHandler.getLastResponse();
        checkEval("cat", "bc");
    }
    
    
}
