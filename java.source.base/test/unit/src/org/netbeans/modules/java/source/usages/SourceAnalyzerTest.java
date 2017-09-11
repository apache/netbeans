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

package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.File;
import java.util.ArrayDeque;
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
        TransactionContext.beginStandardTransaction(src.toURL(), true, false, true);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(
                src,
                null,
                true,
                true,
                false,
                true);
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                cpInfo,
                diag,
                SourceLevelQuery.getSourceLevel(src),  //NOI18N
                SourceLevelQuery.Profile.DEFAULT,
                null, null, null, null, null);
            final JavaFileObject jfo = FileObjects.sourceFileObject(javaFile, src);
            final Iterable<? extends CompilationUnitTree> trees = jt.parse(jfo);
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
        TransactionContext.beginStandardTransaction(src.toURL(), true, false, true);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(
                src,
                null,
                true,
                true,
                false,
                true);
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                cpInfo,
                diag,
                SourceLevelQuery.getSourceLevel(src),  //NOI18N
                SourceLevelQuery.Profile.DEFAULT,
                null, null, null, null, null);
            final JavaFileObject jfo = FileObjects.sourceFileObject(javaFile, src);
            final Iterable<? extends CompilationUnitTree> trees = jt.parse(jfo);
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
        TransactionContext.beginStandardTransaction(src.toURL(), true, false, true);
        try {
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(
                src,
                null,
                true,
                true,
                false,
                true);
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                cpInfo,
                diag,
                SourceLevelQuery.getSourceLevel(src),  //NOI18N
                SourceLevelQuery.Profile.DEFAULT,
                null, null, null, null, null);
            final JavaFileObject jfo = FileObjects.sourceFileObject(javaFile, src);
            final Iterable<? extends CompilationUnitTree> trees = jt.parse(jfo);
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
                        return ClassPathSupport.createClassPath(System.getProperty("sun.boot.class.path")); //NOI18N
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
