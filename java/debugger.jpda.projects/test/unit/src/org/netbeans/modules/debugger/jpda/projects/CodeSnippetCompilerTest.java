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
package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.StatementTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.ByteArrayInputStream;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Method;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class CodeSnippetCompilerTest extends NbTestCase {

    public CodeSnippetCompilerTest(String name) {
        super(name);
    }

    private FileObject wd;
    private FileObject root;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {
            new ClassPathProvider() {
                @Override
                public ClassPath findClassPath(FileObject file, String type) {
                    if (type == ClassPath.SOURCE) {
                        return ClassPathSupport.createClassPath(root);
                    }
                    return null;
                }
            },
            new SourceLevelQueryImplementation() {
                @Override
                public String getSourceLevel(FileObject javaFile) {
                    return "8";
                }
            }
        });
        wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        root = FileUtil.createFolder(wd, "src");    //NOI18N
    }

    private static FileObject createFile(
            final FileObject root,
            final String path,
            final String content) throws Exception {
        FileObject file = FileUtil.createData(root, path);
        TestUtilities.copyStringToFile(file, content);
        return file;
    }

    public void testInferResultType() throws Exception {
        String code = "package test;\n class A { public void test(java.util.List<String> l) { } }";
        int pos = code.indexOf("{", code.indexOf("{") + 1);
        FileObject java = createFile(root, "test/A.java", code);    //NOI18N
        JavaSource.forFileObject(java).runUserActionTask((CompilationController cc) -> {
            cc.toPhase(Phase.RESOLVED);
            TreePath posPath = cc.getTreeUtilities().pathFor(pos);
            for (String watch : new String[] {
                "l.stream().map(s -> s.length()).collect(java.util.stream.Collectors.toList());",
                "l.stream().map(s -> s.length()).collect(java.util.stream.Collectors.toList())",
                "l.stream().map(s -> s.length()).collect(java.util.stream.Collectors.toList()); \t\n",
            }) {
                StatementTree tree = cc.getTreeUtilities().parseStatement(
                    watch,
                    new SourcePositions[1]
                );
                cc.getTreeUtilities().attributeTree(tree, cc.getTrees().getScope(posPath));
                TreePath tp = new TreePath(posPath, tree);
                ClassToInvoke cti = CodeSnippetCompiler.compileToClass(cc, watch, 0, cc.getJavaSource(), java, -1, tp, tree, false);

                ClassFile cf = new ClassFile(new ByteArrayInputStream(cti.bytecode));

                for (Method m : cf.getMethods()) {
                    if (m.getName().equals("invoke")) {
                        assertEquals("(Ljava/util/List;)Ljava/util/List;", m.getDescriptor());
                    }
                }
            }
        }, true);

    }

}
