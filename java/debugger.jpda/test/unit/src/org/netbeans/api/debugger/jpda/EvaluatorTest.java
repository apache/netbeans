/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests evaluation of various expressions.
 * Automatically parses the expressions from the test methods and compares
 * their evaluations with test methods calls.
 *
 * @author Martin Entlicher
 */
public class EvaluatorTest extends NbTestCase {
    
    private static final String METHOD_TO_TEST = null;//"testMember7";

    private JPDASupport     support;
    private URL             source;


    public EvaluatorTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(EvaluatorTest.class);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        //PreferredCCParser is using SourceUtils.isScanInProgress() to modify behavior; ensure indexing is not running.
        FileObject prjRoot = FileUtil.toFileObject(new File(System.getProperty("test.dir.src")));
        assertNotNull(prjRoot);
        Project prj = FileOwnerQuery.getOwner(prjRoot);
        assertNotNull(prj);
        Project annotationsPrj = FileOwnerQuery.getOwner(prj.getProjectDirectory().getParent().getParent().getFileObject("platform/api.annotations.common"));
        assertNotNull(annotationsPrj);
        OpenProjects.getDefault().open(new Project[] {annotationsPrj}, false);
        JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY)).runWhenScanFinished(p -> {}, true).get();
        System.setProperty("debugger.evaluator2", "true");
        JPDASupport.removeAllBreakpoints ();
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src")+
                                  "org/netbeans/api/debugger/jpda/testapps/EvaluatorApp.java");
        LineBreakpoint lb = bp.getLineBreakpoints().get(0);
        source = new URL(lb.getURL());
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            new String[] {"--add-opens=java.desktop/java.beans=ALL-UNNAMED"},
            "org.netbeans.api.debugger.jpda.testapps.EvaluatorApp",
            new String[0],
            new File[0]
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }

    public void testStaticEvaluation () throws Exception {
        try {
            List<Method> methods = getMethods(true);
            AssertionFailedError te = null;
            AssertionFailedError ex = null;
            for (Method m : methods) {
                try {
                    checkEval (m);
                } catch (AssertionFailedError e) {
                    if (te == null) {
                        te = ex = e;
                    } else {
                        ex.initCause(e);
                        ex = e;
                    }
                }
            }
            if (te != null) {
                throw te;
            }
            //checkEvalFails ("this");
            checkEvalFails ("NoSuchClass.class");
        } finally {
            support.doFinish ();
        }
    }

    public void testInstanceEvaluation() throws Exception {
        runInstanceEvaluation(1);
    }
    
    public void testSuperInstanceEvaluation() throws Exception {
        runInstanceEvaluation(2);
    }
    
    private void runInstanceEvaluation(int bpNo) throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src")+
                                      "org/netbeans/api/debugger/jpda/testapps/EvaluatorApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(bpNo);
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support.doContinue();
            support.waitState (JPDADebugger.STATE_STOPPED);
            
            List<Method> methods = getMethods(false);
            AssertionFailedError te = null;
            AssertionFailedError ex = null;
            for (Method m : methods) {
                try {
                    checkEval (m);
                } catch (AssertionFailedError e) {
                    if (te == null) {
                        te = ex = e;
                    } else {
                        ex.initCause(e);
                        ex = e;
                    }
                }
            }
            if (te != null) {
                throw te;
            }
        } finally {
            support.doFinish ();
        }
    }

    private void checkEvalFails (String expression) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            fail (
                "Evaluation of expression was unexpectedly successful: " + 
                expression + " = " + var.getValue ()
            );
        } catch (InvalidExpressionException e) {
            // its ok
            return;
        }
    }
    
    private void checkEval(Method m) {
        if (METHOD_TO_TEST != null && !METHOD_TO_TEST.equals(m.getName())) {
            return ;
        }
        String expression = null;
        try {
            expression = m.getName()+"()";
            Variable eMethod = null;
            String eMethodExc = null;
            try {
                eMethod = support.getDebugger ().evaluate (expression);
            } catch (InvalidExpressionException iexe) {
                if (iexe.hasApplicationTarget()) {
                    eMethodExc = iexe.getTargetException().getClass().getName();
                    eMethodExc = getTargetExceptionDescription(iexe.getTargetException());
                } else {
                    throw iexe;
                }
            }
            String undo = m.getUndo();
            if (undo != null) {
                expression = undo+"()";
                support.getDebugger ().evaluate (expression);
            }
            expression = m.getExpression();
            Variable eVal = null;
            String eValExc = null;
            try {
                eVal = support.getDebugger ().evaluate (expression);
            } catch (InvalidExpressionException iexe) {
                if (iexe.hasApplicationTarget()) {
                    eValExc = iexe.getTargetException().getClass().getName();
                    eValExc = getTargetExceptionDescription(iexe.getTargetException());
                } else {
                    throw iexe;
                }
            }
            if (undo != null) {
                expression = undo+"()";
                support.getDebugger ().evaluate (expression);
            }
            
            if (eMethodExc != null) {
                assertEquals(
                    "Evaluation of expression '" + m.getExpression()+"' of method '"+m.getName()+"()' produced different exceptions:",
                    eMethodExc,
                    eValExc
                );
            } else {
                /*System.err.println("  eMethod = "+eMethod);
                System.err.println("  eVal = "+eVal);
                System.err.println("   equals = "+eMethod.equals(eVal));*/
                Value eMethodJDIValue = ((JDIVariable) eMethod).getJDIValue();
                Value eValJDIValue = ((JDIVariable) eVal).getJDIValue();
                /*System.err.println("  eMethod JDI Value = "+eMethodJDIValue);
                System.err.println("  eVal JDI Value = "+eValJDIValue);
                System.err.println("   equals = "+eMethodJDIValue.equals(eValJDIValue));*/
                assertEquals (
                    "Evaluation of expression '" + m.getExpression()+"' of method '"+m.getName()+"()' produced a wrong type of result:",
                    eMethod.getType(), 
                    eVal.getType()
                );
                assertEquals (
                    "Evaluation of expression '" + m.getExpression()+"' of method '"+m.getName()+"()' produced a wrong value:",
                    new JDIValue(eMethodJDIValue),
                    new JDIValue(eValJDIValue)
                );
            }
            System.err.println("  Method "+m.getName()+"() evaluated successfully.");
        } catch (InvalidExpressionException e) {
            e.printStackTrace();
            fail (
                "Evaluation of expression '"+expression+"' was unsuccessful: " + e
            );
        }
    }
    
    private String getTargetExceptionDescription(Throwable t) {
        java.io.StringWriter s = new java.io.StringWriter();
        java.io.PrintWriter p = new java.io.PrintWriter(s);
        t.printStackTrace(p);
        p.close();
        String str = s.toString();
        int end = str.indexOf('\n');
        if (end > 0) {
            str = str.substring(0, end);
        }
        int npe = str.indexOf("java.lang.NullPointerException");
        if (npe >= 0) {
            //NullPointerExceptions may have enhanced message, not produced by the interpreted debugger evaluator, strip such a message, if any:
            str = str.substring(0, npe + "java.lang.NullPointerException".length());
        }
        return str;
    }

    private List<Method> getMethods(boolean staticMethods) throws Exception{
        List<Method> methods = new ArrayList<Method>();
        BufferedReader r = new BufferedReader(new InputStreamReader(source.openStream()));
        try {
            Method m = null;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("*")) continue;
                if (m != null) {
                    int rt = line.indexOf("return ");
                    if (rt < 0) {
                        continue;
                    }
                    String expression;
                    try {
                        expression = line.substring(rt+7, line.lastIndexOf(';'));
                    } catch (RuntimeException rex) {
                        System.err.println("line = '"+line+"', rt = "+rt+", lastIndexOf(';') = "+line.lastIndexOf(';'));
                        rex.printStackTrace();
                        throw rex;
                    }
                    expression = expression.trim();
                    m.setExpression(expression);
                    methods.add(m);
                    m = null;
                    continue;
                }
                if (line.indexOf(" test") < 0 || line.indexOf("()") < 0) {
                    continue;
                }
                if (staticMethods != line.indexOf("static") >= 0) {
                    continue;
                }
                String name = line.substring(line.indexOf("test"), line.indexOf("()"));
                if (name.endsWith("_undo")) {
                    String origName = name.substring(0, name.length() - "_undo".length());
                    for (Method om : methods) {
                        if (om.getName().equals(origName)) {
                            om.setUndo(name);
                            break;
                        }
                    }
                } else {
                    m = new Method(name);
                }
            }
        } finally {
            r.close();
        }
        return methods;
    }
    
    private static class Method {
        
        private String name;
        private String undoName;
        private String expression;
        
        public Method(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setUndo(String undoName) {
            this.undoName = undoName;
        }
        
        public String getUndo() {
            return undoName;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
        
        public String getExpression() {
            return expression;
        }
    }
    
    private static class JDIValue {
        
        private Value value;
        
        public JDIValue(Value value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JDIValue)) return false;
            Value v = ((JDIValue) obj).value;
            if (value == null) return v == null;
            if (value instanceof StringReference) {
                if (!(v instanceof StringReference)) return false;
                return ((StringReference) value).value().equals(((StringReference) v).value());
            }
            if (value instanceof ArrayReference) {
                if (!(v instanceof ArrayReference)) return false;
                ArrayReference a1 = (ArrayReference) value;
                ArrayReference a2 = (ArrayReference) v;
                if (!a1.type().equals(a2.type())) return false;
                if (a1.length() != a2.length()) return false;
                int n = a1.length();
                for (int i = 0; i < n; i++) {
                    if (!new JDIValue(a1.getValue(i)).equals(new JDIValue(a2.getValue(i)))) {
                        return false;
                    }
                }
                return true;
            }
            return value.equals(v);
        }

        @Override
        public int hashCode() {
            if (value == null) return 0;
            else return value.hashCode();
        }

        @Override
        public String toString() {
            return ""+value;
        }
        
    }
}
