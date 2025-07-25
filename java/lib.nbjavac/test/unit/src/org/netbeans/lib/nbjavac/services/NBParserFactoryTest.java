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
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.junit.NbTestCase;

import org.openide.util.Pair;

public class NBParserFactoryTest extends NbTestCase {

    public NBParserFactoryTest(String testName) {
        super(testName);
    }

    public void testUnnamedClassStartsAt0() throws Exception {
        String code = "void main() {}";
        Pair<JavacTask, CompilationUnitTree> parsed = compile(code);

        ClassTree ct = (ClassTree) parsed.second().getTypeDecls().get(0);
        SourcePositions sp = Trees.instance(parsed.first()).getSourcePositions();

        assertEquals(0, sp.getStartPosition(parsed.second(), ct));
        assertEquals(-1, sp.getEndPosition(parsed.second(), ct));
    }

    public void testImplicitClassPositions() throws Exception {
        String code = """
                      import java.util.*;

                      //prefix

                      void main() {
                      }

                      //suffix
                      """;
        Pair<JavacTask, CompilationUnitTree> parsed = compile(code);

        ClassTree ct = (ClassTree) parsed.second().getTypeDecls().get(0);
        SourcePositions sp = Trees.instance(parsed.first()).getSourcePositions();

        assertEquals(19, sp.getStartPosition(parsed.second(), ct));
        assertEquals(-1, sp.getEndPosition(parsed.second(), ct));
        assertEquals(0, sp.getStartPosition(parsed.second(), parsed.second()));
        assertEquals(code.length(), sp.getEndPosition(parsed.second(), parsed.second()));
    }

    public void testErrorRecoveryCompactSourceFilePackage() throws Exception {
        String code = """
                      package test;

                      void main() {
                      }
                      """;
        Pair<JavacTask, CompilationUnitTree> parsed = compile(code);

        if (parsed.second().getPackage() == null) {
            throw new AssertionError("no package");
        }
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
        workingDir = getWorkDir();
    }

    private Pair<JavacTask, CompilationUnitTree> compile(String code) throws Exception {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));

        Context context = new Context();
        NBParserFactory.preRegister(context);
        NBTreeMaker.preRegister(context);
        final JavacTaskImpl ct = (JavacTaskImpl) ((JavacTool)tool).getTask(null, std, null, Arrays.asList("-source", "21"), null, Arrays.asList(new MyFileObject(code)), context);

        CompilationUnitTree cut = ct.parse().iterator().next();

        ct.analyze();

        return Pair.<JavacTask, CompilationUnitTree>of(ct, cut);
    }
    //</editor-fold>
}
