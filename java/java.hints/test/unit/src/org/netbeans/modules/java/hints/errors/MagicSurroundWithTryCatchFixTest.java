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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;

import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class MagicSurroundWithTryCatchFixTest extends ErrorHintsTestBase {
    
    public MagicSurroundWithTryCatchFixTest(String testName) {
        super(testName);
    }

    public void test104085() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {try {}finally{System.out.println(\"\"); new java.io.FileInputStream(\"\");}}}",
                       150 - 43,
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test() {try {}finally{try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } }}}");
    }

    public void testLogStatementLogger() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() { System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test() { try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } }}");
    }

    // For pre Java 9 there is no System.Logger.
    public void testLogStatementSystemLogger_Java8() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        boolean orig = ErrorFixesFakeHint.isUseSystemLogger(prefs);

        try {
            ErrorFixesFakeHint.setUseSystemLogger(prefs, true);
            performFixTest("test/Test.java",
                       "package test; public class Test {public void test() { System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test() { try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } }}");
        } finally {
            ErrorFixesFakeHint.setUseSystemLogger(prefs, orig);
        }
    }

    public void testLogStatementUseExistingLogger() throws Exception {
            performFixTest("test/Test.java",
                       "package test; import java.util.logging.Logger; public class Test {private static final Logger LOG; public void test() { System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                       "FixImpl",
                       "package test;import java.io.FileNotFoundException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {private static final Logger LOG; public void test() { try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { LOG.log(Level.SEVERE, null, ex); } }}");
    }

    public void testLogStatementSystemLogger() throws Exception {
        sourceLevel = "9";
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        boolean orig = ErrorFixesFakeHint.isUseSystemLogger(prefs);

        try {
            ErrorFixesFakeHint.setUseSystemLogger(prefs, true);
            performFixTest("test/Test.java",
                       "package test; public class Test {public void test() { System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; public class Test {public void test() { try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { System.getLogger(Test.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex); } }}");
        } finally {
            ErrorFixesFakeHint.setUseSystemLogger(prefs, orig);
        }
    }

    public void testLogStatementSystemLoggerUseExistingLogger() throws Exception {
        sourceLevel = "9";
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        boolean orig = ErrorFixesFakeHint.isUseSystemLogger(prefs);

        try {
            ErrorFixesFakeHint.setUseSystemLogger(prefs, true);
            performFixTest("test/Test.java",
                       "package test; public class Test {private static final System.Logger LOG; public void test() { System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; public class Test {private static final System.Logger LOG; public void test() { try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { LOG.log(System.Logger.Level.ERROR, (String) null, ex); } }}");
        } finally {
            ErrorFixesFakeHint.setUseSystemLogger(prefs, orig);
        }
    }

    public void testLogStatementExceptions() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; import org.openide.util.Exceptions; public class Test {public void test() {try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { Exceptions.printStackTrace(ex); } }}");
    }

    public void testLogPrint() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        boolean orig = ErrorFixesFakeHint.isUseLogger(prefs);

        try {
            ErrorFixesFakeHint.setUseLogger(prefs, false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public void test() {System.out.println(\"\"); |new java.io.FileInputStream(\"\");}}",
                    "FixImpl",
                    "package test; import java.io.FileNotFoundException; public class Test {public void test() {try { System.out.println(\"\"); new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { ex.printStackTrace(); } }}");
        } finally {
            ErrorFixesFakeHint.setUseLogger(prefs, orig);
        }
    }

    public void test117085a() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(Exception ex) {System.out.println(\"\"); |x();} private void x() throws Exception {}}",
                       "FixImpl",
                       "package test; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test(Exception ex) {try { System.out.println(\"\"); x(); } catch (Exception ex1) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex1); } } private void x() throws Exception {}}");
    }

    public void test117085b() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(Exception ex) {System.out.println(\"\"); |x();} private void x() throws Exception {}}",
                       "FixImpl",
                       "package test; import org.openide.util.Exceptions; public class Test {public void test(Exception ex) {try { System.out.println(\"\"); x(); } catch (Exception ex1) { Exceptions.printStackTrace(ex1); } } private void x() throws Exception {}}");
    }

    public void test117085c() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        boolean orig = ErrorFixesFakeHint.isUseLogger(prefs);

        try {
            ErrorFixesFakeHint.setUseLogger(prefs, false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public void test(Exception ex) {System.out.println(\"\"); |x();} private void x() throws Exception {}}",
                    "FixImpl",
                    "package test; public class Test {public void test(Exception ex) {try { System.out.println(\"\"); x(); } catch (Exception ex1) { ex1.printStackTrace(); } } private void x() throws Exception {}}");
        } finally {
            ErrorFixesFakeHint.setUseLogger(prefs, orig);
        }
    }

    public void test117085d() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(Exception ex, Exception ex1) {System.out.println(\"\"); |x();} private void x() throws Exception {}}",
                       "FixImpl",
                       "package test; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test(Exception ex, Exception ex1) {try { System.out.println(\"\"); x(); } catch (Exception ex2) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex2); } } private void x() throws Exception {}}");
    }

    public void test117085e() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(Exception ex) {while (true) {Exception ex1; if (1 != 1) {System.out.println(\"\"); |x();}}} private void x() throws Exception {}}",
                       "FixImpl",
                       "package test; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test(Exception ex) {while (true) {Exception ex1; if (1 != 1) {try { System.out.println(\"\"); x(); } catch (Exception ex2) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex2); } }}} private void x() throws Exception {}}");
    }

//    public void test86483() throws Exception {
//        performFixTest("test/Test.java",
//                       "package test; import java.io.IOException; import java.io.FileNotFoundException; import org.openide.util.Exceptions; public class Test {public void test(int a) {try {if (a == 1) |throw new IOException(); } catch (FileNotFoundException e) { Exceptions.printStacktrace(e); } } }",
//                       "FixImpl",
//                       "package test; import java.io.IOException; import java.io.FileNotFoundException; import org.openide.util.Exceptions; public class Test {public void test(int a) {try {if (a == 1) throw new IOException(); } catch (FileNotFoundException e) { Exceptions.printStacktrace(e); } catch (IOException e) { Exceptions.printStacktrace(e); } } }");
//    }
    
    public void test143965() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { {\n int h; thr|ow new Exception();} }",
                       "FixImpl",
                       "package test; import java.util.logging.Level; import java.util.logging.Logger; public class Test { { try { int h; throw new Exception(); } catch (Exception ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } } }");
    }

    public void testComments171262() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {\n /*zbytek*/\nSystem.out.println(\"\"); /*obycej*/\n/*bystry*/\n|new java.io.FileInputStream(\"\"); /*bylina*/\n}}",
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test() { try { /*zbytek*/ System.out.println(\"\"); /*obycej*/ /*bystry*/ new java.io.FileInputStream(\"\"); /*bylina*/ } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } }}");
    }

    public void test204165a() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { static {\n int h; thr|ow new Exception();} }",
                       "FixImpl",
                       "package test; import java.util.logging.Level; import java.util.logging.Logger; public class Test { static { try { int h; throw new Exception(); } catch (Exception ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } } }");
    }

    public void test204165b() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.io.*; public class Test { static {\n int h; BufferedReader br = new BufferedReader(n|ew FileReader(\"\"));\n } }",
                       "FixImpl",
                       "package test; import java.io.*;import java.util.logging.Level; import java.util.logging.Logger; public class Test { static { BufferedReader br = null; try { int h; br = new BufferedReader(new FileReader(\"\")); } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } finally { try { br.close(); } catch (IOException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } } } }");
    }

    public void test200382() throws Exception {
        UncaughtException.allowMagicSurround = true;
        try {
        performFixTest("test/Test.java",
                       "package test; import java.io.*; import java.net.*; public class Test { public void getTestCase(URL url) { try { File tc = new File(url.toURI()); BufferedReader br = new BufferedReader(ne|w FileReader(tc)); } catch (URISyntaxException ex) { } } }",
                       "FixImpl",
                       "package test; import java.io.*; import java.net.*;import java.util.logging.Level; import java.util.logging.Logger; public class Test { public void getTestCase(URL url) { BufferedReader br = null; try { File tc = new File(url.toURI()); br = new BufferedReader(new FileReader(tc)); } catch (URISyntaxException ex) { } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } finally { try { br.close(); } catch (IOException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } } } }");
        } finally {
            UncaughtException.allowMagicSurround = false;
        }
    }
    
    public void test207480() throws Exception {
        prepareTest("test/Test.java",
                    "package test; import java.io.*; public class Test { public void getTestCase(URL url) throws FileNotFoundException { try(Reader r = new FileReader(\"\")) { } } }");
        
        int pos = positionForErrors();
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        List<Fix> fixes = computeFixes(info, pos, path);
        
        for (Fix e : fixes) {
            if (e instanceof JavaFixImpl && ((JavaFixImpl) e).jf instanceof MagicSurroundWithTryCatchFix) {
                fail ("Should not provide the MagicSurroundWithTryCatchFix!");
            }
        }
    }

    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new UncaughtException().run(info, null, pos, path, null);
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
        return new UncaughtException().getCodes();
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof JavaFixImpl && ((JavaFixImpl) f).jf instanceof MagicSurroundWithTryCatchFix) {
            return "FixImpl";
        }
        
        return super.toDebugString(info, f);
    }

    @Override
    protected FileObject[] getExtraClassPathElements() {
        if ("testLogStatementExceptions".equals(getName()) || "test117085b".equals(getName()) || "test86483".equals(getName())) {
            FileObject ooutils = URLMapper.findFileObject(Exceptions.class.getProtectionDomain().getCodeSource().getLocation());
            
            assertNotNull(ooutils);
            assertTrue(FileUtil.isArchiveFile(ooutils));
            
            return new FileObject[] {FileUtil.getArchiveRoot(ooutils)};
        }
        
        return super.getExtraClassPathElements();
    }

}
