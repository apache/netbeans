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

package org.netbeans.modules.java.editor.rename;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenameActionTest extends NbTestCase {
    
    public InstantRenameActionTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
    }
    
    public void testSimpleRename() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{private int xx; public void run() {xx = 1;}}", 68, wasResolved);
        
        validateChangePoints(changePoints, 103 - 59, 105 - 59, 126 - 59, 128 - 59);
    }
    
    private void checkInnerclasses1(int caret) throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{public void run() {Test1 t = new Test1();} private static class Test1{public Test1(){}}", caret, wasResolved);
        
        validateChangePoints(changePoints, 110 - 59, 115 - 59, 124 - 59, 129 - 59, 155 - 59, 160 - 59, 168 - 59, 173 - 59);
    }
    
    public void testRenameInnerclass1() throws Exception {
        checkInnerclasses1(113 - 59);
    }
    
    public void testRenameInnerclass2() throws Exception {
        checkInnerclasses1(127 - 59);
    }
    
    public void testRenameInnerclass3() throws Exception {
        checkInnerclasses1(158 - 59);
    }
    
    public void testRenameInnerclass4() throws Exception {
        checkInnerclasses1(171 - 59);
    }
    
    public void testNonPrivateClassWithPrivateConstructor() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{public void run() {Test1 t = new Test1();} static class Test1{private Test1(){}}", 112 - 59, wasResolved);
        
        assertNull(changePoints);
        assertTrue(wasResolved[0]);
    }
    
    public void testBrokenSource89736() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{private void run(int hj test) {} }", 116 - 59, wasResolved);
        
        assertNull(changePoints);
        assertFalse(wasResolved[0]);
    }
    
    public void testLocalClassAreRenamable1() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{public void run() {class PPP {PPP() {} PPP p = new PPP();}}}", 117 - 59, wasResolved);
        
        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 116 - 59, 119 - 59, 121 - 59, 124 - 59, 130 - 59, 133 - 59, 142 - 59, 145 - 59);
    }
    
    public void testLocalClassAreRenamable2() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{public     Test() {class PPP {PPP() {} PPP p = new PPP();}}}", 117 - 59, wasResolved);
        
        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 116 - 59, 119 - 59, 121 - 59, 124 - 59, 130 - 59, 133 - 59, 142 - 59, 145 - 59);
    }
    
    public void testLocalClassAreRenamable3() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{                  {class PPP {PPP() {} PPP p = new PPP();}}}", 117 - 59, wasResolved);
        
        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 116 - 59, 119 - 59, 121 - 59, 124 - 59, 130 - 59, 133 - 59, 142 - 59, 145 - 59);
    }
    
    public void testLocalClassAreRenamable4() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test{           static {class PPP {PPP() {} PPP p = new PPP();}}}", 117 - 59, wasResolved);
        
        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 116 - 59, 119 - 59, 121 - 59, 124 - 59, 130 - 59, 133 - 59, 142 - 59, 145 - 59);
    }

    public void testIsInaccessibleOutsideOuterClassForStaticFieldOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public static int field;}}", 81, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 79, 84);
    }

    public void testIsInaccessibleOutsideOuterClassForStaticFieldOfNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { static class PPP { public static int field;}}", 73, wasResolved);

        assertNull(changePoints);
        assertTrue(wasResolved[0]);
    }

    public void testIsInaccessibleOutsideOuterClassForFieldOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public int field;}}", 73, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 72, 77);
    }

    public void testIsInaccessibleOutsideOuterClassForFieldOfPrivateFinalNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static final class PPP { public int field;}}", 80, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 78, 83);
    }

    public void testIsInaccessibleOutsideOuterClassForFieldOfPrivateFinalNestedClassWithExtends() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static final class PPP extends Test { public int field;}}", 93, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 91, 96);
    }

    public void testIsInaccessibleOutsideOuterClassForStaticMethodOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public static void method() {}}}", 81, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 80, 86);
    }

    public void testIsInaccessibleOutsideOuterClassForStaticMethodOfNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { static class PPP { public static void method() {}}}", 73, wasResolved);

        assertNull(changePoints);
        assertTrue(wasResolved[0]);
    }

    public void testIsInaccessibleOutsideOuterClassForMethodOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public void method() {}}}", 74, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 73, 79);
    }

    public void testIsInaccessibleOutsideOuterClassForMethodOfPrivateFinalNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static final class PPP { public void method() {}}}", 80, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 79, 85);
    }

    public void testIsInaccessibleOutsideOuterClassForMethodOfPrivateFinalNestedClassWithExtends() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static final class PPP extends Test { public void method() {}}}", 93, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 92, 98);
    }

    public void testIsInaccessibleOutsideOuterClassForOverridenMethodOfPrivateFinalNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { public void method() {} private static final class PPP extends Test { public void method() {}}}", 117, wasResolved);

        assertNull(changePoints);
        assertTrue(wasResolved[0]);
    }

    public void testIsInaccessibleOutsideOuterClassForEnumOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public enum En {E1;}}}", 75, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 73, 75);
    }

    public void testIsInaccessibleOutsideOuterClassForEnumConstantOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public enum En {E1;}}}", 79, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 77, 79);
    }

    public void testIsInaccessibleOutsideOuterClassForInterfaceOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public interface In {int method();}}}", 80, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 78, 80);
    }

    public void testIsInaccessibleOutsideOuterClassForInterfaceMethodOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public interface In {int method();}}}", 88, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 86, 92);
    }

    public void testIsInaccessibleOutsideOuterClassForInterfaceMethodOfPrivateNestedClassImplementedWithinOutermostClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public interface In {int method();}} public static class Impl implements PPP.In {public int method() {return 0;}}}", 88, wasResolved);

        assertNull(changePoints);
        assertTrue(wasResolved[0]);
    }

    public void testIsInaccessibleOutsideOuterClassForAnnTypeOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public @interface In {int method();}}}", 81, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 79, 81);
    }

    public void testIsInaccessibleOutsideOuterClassForAnnTypeMethodOfPrivateNestedClass() throws Exception {
        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private static class PPP { public @interface In {int method();}}}", 89, wasResolved);

        assertNotNull(changePoints);
        assertTrue(wasResolved[0]);
        validateChangePoints(changePoints, 87, 93);
    }
    
    public void testNoInstanceRenameForRecordComponents() throws Exception {
        sourceLevel = "17";

        boolean[] wasResolved = new boolean[1];
        Collection<Token> changePoints = performTest("package test; public class Test { private record Rec(String component) { } }", 120 - 55, wasResolved);

        assertNull(changePoints);
        assertTrue(wasResolved[0]);
    }

    private void validateChangePoints(Collection<Token> changePoints, int... origs) {
        Set<Pair> awaited = new HashSet<Pair>();
        
        for (int cntr = 0; cntr < origs.length; cntr += 2) {
            awaited.add(new Pair(origs[cntr], origs[cntr + 1]));
        }
        
        Set<Pair> got = new HashSet<Pair>();
        
        for (Token<JavaTokenId> h : changePoints) {
            got.add(new Pair(h.offset(null), h.offset(null) + h.length()));
        }
        
        assertEquals(awaited, got);
    }
    
    private static class Pair {
        private int a;
        private int b;
        
        public Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }
        
        @Override
        public int hashCode() {
            return a ^ b;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Pair) {
                Pair p = (Pair) o;
                
                return a == p.a && b == p.b;
            }
            
            return false;
        }
        
        @Override
        public String toString() {
            return "(" + a + "," + b + ")";
        }
    }
    
    private FileObject source;
    private String sourceLevel;
    
    private Collection<Token> performTest(String sourceCode, final int offset, boolean[] wasResolved) throws Exception {
        FileObject root = makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        
        source = sourceDir.createFolder("test").createData("Test.java");
        
        writeIntoFile(source, sourceCode);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(sourceDir, sourceLevel);
        }

        DataObject od = DataObject.find(source);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        
        return InstantRenamePerformer.computeChangePoints(SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(source), Phase.RESOLVED), offset, wasResolved);
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
}
