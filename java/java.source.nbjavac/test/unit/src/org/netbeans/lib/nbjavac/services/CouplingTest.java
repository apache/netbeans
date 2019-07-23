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
package org.netbeans.lib.nbjavac.services;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.model.JavacElements;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import junit.framework.TestCase;

/**TODO: finished?
 *
 * @author lahvac
 */
public class CouplingTest extends TestCase {

    public void test200122() throws Exception {
        String code = "package test; public class Test { void t() { new Runnable() { public void run() {} }; } }";
        List<String> fqns = compile(code);

        assertEquals(testCoupling(code, false, fqns), testCoupling(code, true, fqns));
    }
    
    //<editor-fold defaultstate="collapsed" desc=" Test Infrastructure ">
    private static class MyFileObject extends SimpleJavaFileObject {
        private String text;

        public MyFileObject(String text) {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
            this.text = text;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    private File workingDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        workingDir = File.createTempFile("CouplingTest", "");

        workingDir.delete();
        workingDir.mkdirs();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteRecursively(workingDir);
        super.tearDown();
    }

    private List<String> compile(String code) throws Exception {
        final String bootPath = System.getProperty("sun.boot.class.path"); //NOI18N
        final String version = System.getProperty("java.vm.specification.version"); //NOI18N
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));

        final JavacTaskImpl ct = (JavacTaskImpl)tool.getTask(null, std, null, Arrays.asList("-bootclasspath",  bootPath, "-source", version, "-Xjcov", "-XDshouldStopPolicy=GENERATE"), null, Arrays.asList(new MyFileObject(code)));
        Iterable<? extends CompilationUnitTree> cuts = ct.parse();

        ct.analyze();

        final List<String> result = new ArrayList<String>();

        new ErrorAwareTreePathScanner<Void, Void>() {
            @Override public Void visitClass(ClassTree node, Void p) {
                Element el = Trees.instance(ct).getElement(getCurrentPath());

                if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                    result.add(ct.getElements().getBinaryName((TypeElement) el).toString());
                }

                return super.visitClass(node, p);
            }
        }.scan(cuts, null);

        ct.generate();

        return result;
    }

    private Set<String> testCoupling(String code, boolean loadFromClasses, List<String> fqns) throws IOException {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        if (loadFromClasses) {
            std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));
            std.setLocation(StandardLocation.CLASS_PATH, Collections.singleton(workingDir));
        }

        JavacTaskImpl ct = Utilities.createJavac(std);
        ct.enter();
        
        if (loadFromClasses) {
            for (String fqn : fqns) {
                assertNotNull(fqn, ((JavacElements)ct.getElements()).getTypeElementByBinaryName(fqn));
            }
        }

        ct.parse(Utilities.fileObjectFor(code));
        ct.analyze();

        Set<String> classInfo = new HashSet<String>();

        for (String fqn : fqns) {
            ClassSymbol clazz = ((JavacElements)ct.getElements()).getTypeElementByBinaryName(fqn);
            StringBuilder info = new StringBuilder();

            info.append(clazz.flatname.toString()).append(",");
            info.append(Long.toHexString(clazz.flags() & ~(Flags.FROMCLASS | Flags.APT_CLEANED))).append(",");
            info.append(clazz.hasOuterInstance());

            classInfo.add(info.toString());
        }

        return classInfo;
    }

    private void deleteRecursively(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }

        f.delete();
    }
    //</editor-fold>
}
