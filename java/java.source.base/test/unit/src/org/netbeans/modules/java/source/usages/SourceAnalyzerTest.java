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

package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import javax.swing.event.ChangeListener;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class SourceAnalyzerTest extends NbTestCase {

    private FileObject src;

    public SourceAnalyzerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices(CPP.class, SLQ.class);
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        assertNotNull(wd);
        src = FileUtil.createFolder(wd,"src");  //NOI18N
        assertNotNull(src);
        Lookup.getDefault().lookup(CPP.class).root = src;
        Lookup.getDefault().lookup(SLQ.class).root = src;

    }

    public void testMethodReference() throws Exception {
        final FileObject libFile = FileUtil.toFileObject(TestFileUtils.writeFile(
             new File(FileUtil.toFile(src),"Lib.java"), //NOI18N
             "public class Lib {\n" +                   //NOI18N
             "    public static void foo(){}\n" +       //NOI18N
             "}"));                                     //NOI18N
        final FileObject javaFile = FileUtil.toFileObject(TestFileUtils.writeFile(
             new File(FileUtil.toFile(src),"Test.java"),        //NOI18N
             "public class Test {   \n" +                       //NOI18N
             "    public static void main(String[] args) {\n" + //NOI18N
             "        Runnable r = Lib::foo;\n" +               //NOI18N
             "    }\n" +                                        //NOI18N
             "}"));                                             //NOI18N

        final DiagnosticListener<JavaFileObject> diag = new DiagnosticListener<JavaFileObject>() {

            private final Queue<Diagnostic<? extends JavaFileObject>> problems = new ArrayDeque<Diagnostic<? extends JavaFileObject>>();

            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                problems.offer(diagnostic);
            }
        };
        TransactionContext.beginStandardTransaction(src.toURL(), true, ()->false, true);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(
                src,
                null,
                true,
                true,
                false,
                true);
            final JavaFileObject jfo = FileObjects.sourceFileObject(javaFile, src);
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                cpInfo,
                diag,
                SourceLevelQuery.getSourceLevel(src),  //NOI18N
                SourceLevelQuery.Profile.DEFAULT,
                null, null, null, null, Arrays.asList(jfo));
            final Iterable<? extends CompilationUnitTree> trees = jt.parse();
            jt.enter();
            jt.analyze();
            final SourceAnalyzerFactory.SimpleAnalyzer sa = SourceAnalyzerFactory.createSimpleAnalyzer();
            List<Pair<Pair<BinaryName, String>, Object[]>> data = sa.analyseUnit(trees.iterator().next(), jt);
            assertEquals(1, data.size());
            assertTrue(((Collection)data.iterator().next().second()[0]).contains(
                DocumentUtil.encodeUsage("Lib", EnumSet.<ClassIndexImpl.UsageType>of(   //NOI18N
                    ClassIndexImpl.UsageType.METHOD_REFERENCE,
                    ClassIndexImpl.UsageType.TYPE_REFERENCE))));
        } finally {
            TransactionContext.get().rollBack();
        }
    }    

    public void testConstructorReference() throws Exception {
        final FileObject libFile = FileUtil.toFileObject(TestFileUtils.writeFile(
             new File(FileUtil.toFile(src),"Lib.java"), //NOI18N
             "public class Lib {\n" +                   //NOI18N
             "}"));                                     //NOI18N
        final FileObject javaFile = FileUtil.toFileObject(TestFileUtils.writeFile(
             new File(FileUtil.toFile(src),"Test.java"),        //NOI18N
             "public class Test {   \n" +                       //NOI18N
             "    public static void main(String[] args) {\n" + //NOI18N
             "        Runnable r = Lib::new;\n" +               //NOI18N
             "    }\n" +                                        //NOI18N
             "}"));                                             //NOI18N

        final DiagnosticListener<JavaFileObject> diag = new DiagnosticListener<JavaFileObject>() {

            private final Queue<Diagnostic<? extends JavaFileObject>> problems = new ArrayDeque<Diagnostic<? extends JavaFileObject>>();

            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                problems.offer(diagnostic);
            }
        };
        TransactionContext.beginStandardTransaction(src.toURL(), true, ()->false, true);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(
                src,
                null,
                true,
                true,
                false,
                true);
            final JavaFileObject jfo = FileObjects.sourceFileObject(javaFile, src);
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                cpInfo,
                diag,
                SourceLevelQuery.getSourceLevel(src),  //NOI18N
                SourceLevelQuery.Profile.DEFAULT,
                null, null, null, null, Arrays.asList(jfo));
            final Iterable<? extends CompilationUnitTree> trees = jt.parse();
            jt.enter();
            jt.analyze();
            final SourceAnalyzerFactory.SimpleAnalyzer sa = SourceAnalyzerFactory.createSimpleAnalyzer();
            List<Pair<Pair<BinaryName, String>, Object[]>> data = sa.analyseUnit(trees.iterator().next(), jt);
            assertEquals(1, data.size());
            assertTrue(((Collection)data.iterator().next().second()[0]).contains(
                DocumentUtil.encodeUsage("Lib", EnumSet.<ClassIndexImpl.UsageType>of(   //NOI18N
                    ClassIndexImpl.UsageType.METHOD_REFERENCE,
                    ClassIndexImpl.UsageType.TYPE_REFERENCE))));
        } finally {
            TransactionContext.get().rollBack();
        }
    }

    public void testBrokenReference() throws Exception {
        final FileObject javaFile = FileUtil.toFileObject(TestFileUtils.writeFile(
             new File(FileUtil.toFile(src),"Test.java"),        //NOI18N
             "public class Test {   \n" +                       //NOI18N
             "    public static void main(String[] args) {\n" + //NOI18N
             "        Runnable r = Lib::foo;\n" +               //NOI18N
             "    }\n" +                                        //NOI18N
             "}"));                                             //NOI18N

        final DiagnosticListener<JavaFileObject> diag = new DiagnosticListener<JavaFileObject>() {

            private final Queue<Diagnostic<? extends JavaFileObject>> problems = new ArrayDeque<Diagnostic<? extends JavaFileObject>>();

            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                problems.offer(diagnostic);
            }
        };
        TransactionContext.beginStandardTransaction(src.toURL(), true, ()->false, true);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(
                src,
                null,
                true,
                true,
                false,
                true);
            final JavaFileObject jfo = FileObjects.sourceFileObject(javaFile, src);
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                cpInfo,
                diag,
                SourceLevelQuery.getSourceLevel(src),  //NOI18N
                SourceLevelQuery.Profile.DEFAULT,
                null, null, null, null, Arrays.asList(jfo));
            final Iterable<? extends CompilationUnitTree> trees = jt.parse();
            jt.enter();
            jt.analyze();
            final SourceAnalyzerFactory.SimpleAnalyzer sa = SourceAnalyzerFactory.createSimpleAnalyzer();
            List<Pair<Pair<BinaryName, String>, Object[]>> data = sa.analyseUnit(trees.iterator().next(), jt);
            assertEquals(1, data.size());
            assertTrue(((Collection)data.iterator().next().second()[0]).contains(
                DocumentUtil.encodeUsage("Lib", EnumSet.<ClassIndexImpl.UsageType>of(   //NOI18N
                    ClassIndexImpl.UsageType.TYPE_REFERENCE))));
        } finally {
            TransactionContext.get().rollBack();
        }
    }


    public static final class CPP implements ClassPathProvider {

        private volatile FileObject root;

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            final FileObject _root = root;
            if (_root != null && (_root.equals(file) || FileUtil.isParentOf(_root, file))) {
                switch (type) {
                    case ClassPath.SOURCE:
                        return ClassPathSupport.createClassPath(_root);
                    case ClassPath.COMPILE:
                        return ClassPath.EMPTY;
                    case ClassPath.BOOT:
                        return BootClassPathUtil.getBootClassPath();
                }
            }
            return null;
        }
    }

    public static final class SLQ implements SourceLevelQueryImplementation2 {

        private static final Result2 R = new Result2() {

            @Override
            public SourceLevelQuery.Profile getProfile() {
                return SourceLevelQuery.Profile.DEFAULT;
            }
            @Override
            public String getSourceLevel() {
                return "1.8";   //NOI18N
            }
            @Override
            public void addChangeListener(ChangeListener listener) {
            }
            @Override
            public void removeChangeListener(ChangeListener listener) {
            }
        };

        private volatile FileObject root;

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            final FileObject _root = root;
            if (_root != null && (_root.equals(javaFile) || FileUtil.isParentOf(_root, javaFile))) {
                return R;
            }
            return null;
        }



    }
}
