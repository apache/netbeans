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

package org.netbeans.modules.junit.ant.ui;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import jdk.internal.net.http.common.Log;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.java.testrunner.JavaRegexpPatterns;
import org.netbeans.modules.java.testrunner.JavaRegexpUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.junit.api.JUnitTestSuite;
import org.netbeans.modules.junit.ui.api.JUnitTestMethodNode;
import org.netbeans.modules.java.testrunner.ui.api.NodeOpener;
import org.netbeans.modules.java.testrunner.ui.api.UIJavaUtils;
import org.netbeans.modules.junit.ui.api.JUnitCallstackFrameNode;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
@NodeOpener.Registration(projectType = CommonUtils.ANT_PROJECT_TYPE, testingFramework = CommonUtils.JUNIT_TF)
public final class AntJUnitNodeOpener extends NodeOpener {

    static final Action[] NO_ACTIONS = new Action[0];

    public void openTestsuite(TestsuiteNode node) {
        TestSuite suite = node.getSuite();
        if (suite instanceof JUnitTestSuite) {
            final FileObject fo = ((JUnitTestSuite)suite).getSuiteFO();
            if (fo != null){
                final long[] line = new long[]{0};
                JavaSource javaSource = JavaSource.forFileObject(fo);
                if (javaSource != null) {
                    try {
                        javaSource.runUserActionTask(new Task<CompilationController>() {
                                public void run(CompilationController compilationController) throws Exception {
                                    compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                    Trees trees = compilationController.getTrees();
                                    CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                    List<?extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                                    for (Tree tree : typeDecls) {
                                        Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                        if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo.getName())){
                                            long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, tree);
                                            line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                            break;
                                        }
                                    }
                                }
                            }, true);

                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
                UIJavaUtils.openFile(fo, (int)line[0]);
            }
        }
    }

    public void openTestMethod(final TestMethodNode node) {
        if(!(node instanceof JUnitTestMethodNode)) {
            return;
        }
        final FileObject fo = ((JUnitTestMethodNode)node).getTestcase().getClassFileObject();
        if (fo != null){
	    final FileObject[] fo2open = new FileObject[]{fo};
            final long[] line = new long[]{0};
            JavaSource javaSource = JavaSource.forFileObject(fo2open[0]);
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                            public void run(CompilationController compilationController) throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                Trees trees = compilationController.getTrees();
                                CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                List<?extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                                for (Tree tree : typeDecls) {
                                    Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                    if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo2open[0].getName())){
                                        List<? extends ExecutableElement> methodElements = ElementFilter.methodsIn(element.getEnclosedElements());
                                        for(Element child: methodElements){
                                            String name = node.getTestcase().getName(); // package.name.method.name
                                            if (child.getSimpleName().contentEquals(name.substring(name.lastIndexOf(".") + 1))) {
                                                long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, trees.getTree(child));
                                                line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                                break;
                                            }
                                        }
					// method not found in this FO, so try to find where this method belongs
					if (line[0] == 0) {
					    UIJavaUtils.searchAllMethods(node, fo2open, line, compilationController, element);
					}
                                        break;
                                    }
                                }
                            }
                        }, true);

                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            UIJavaUtils.openFile(fo2open[0], (int)line[0]);
        }
    }

    public void openCallstackFrame(Node node, String frameInfo) {
        if(frameInfo.isEmpty()) { // user probably clicked on a failed test method node, find failing line within the testMethod using the stacktrace
            if (!(node instanceof JUnitTestMethodNode)) {
                return;
            }
        } else { // user probably clicked on a stacktrace node
            if (!(node instanceof JUnitCallstackFrameNode)) {
                return;
            }
        }
        // #213935 - copied from org.netbeans.modules.maven.junit.nodes.AntJUnitNodeOpener
        JUnitTestMethodNode methodNode = (JUnitTestMethodNode)UIJavaUtils.getTestMethodNode(node);
        FileLocator locator = methodNode.getTestcase().getSession().getFileLocator();
        if (locator == null) {
            return;
        }
        // Method node might belong to an inner class
        FileObject testfo = methodNode.getTestcase().getClassFileObject(true);
        String fqMethodName = methodNode.getTestcase().getClassName() + '.' + methodNode.getTestcase().getName();
	if(testfo == null) {
	    return;
	}
        final int[] lineNumStorage = new int[1];
        FileObject file = UIJavaUtils.getFile(frameInfo, lineNumStorage, locator);
        //lineNumStorage -1 means no regexp for stacktrace was matched.
        if ((file == null) && (methodNode.getTestcase().getTrouble() != null) && lineNumStorage[0] == -1) {
            String[] st = methodNode.getTestcase().getTrouble().getStackTrace();
            if ((st != null) && (st.length > 0)) {
                int index = 0;//st.length - 1;
                //Jump to the first line matching the fully qualified test method name.
                // and ignore the infrastructure stack lines in the process
                while (index < st.length) {
                    if (st[index].contains(fqMethodName)) {
                        file = UIJavaUtils.getFile(st[index], lineNumStorage, locator);
                        break;
                    }
                    index++;
                }
                // if not found, return top line of stack trace.
                if (index == st.length) {
                    index=0;
                    for(index=0; index < st.length; index++) {
                        String trimmed=JavaRegexpUtils.specialTrim(st[index]);
                        if (trimmed.startsWith(JavaRegexpUtils.CALLSTACK_LINE_PREFIX_CATCH) ||
                               trimmed.startsWith(JavaRegexpUtils.CALLSTACK_LINE_PREFIX )){
                            file = UIJavaUtils.getFile(st[index], lineNumStorage, locator);
                            break;
                        }
                    }
                }
                // if that fails, return the test file object.
                if (file == null) {

                    file = testfo;
                }
            }
        }
        UIJavaUtils.openFile(file, lineNumStorage[0]);
    }

}
