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
package org.netbeans.modules.maven.junit.ui;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.junit.ui.api.JUnitTestMethodNode;
import org.netbeans.modules.java.testrunner.ui.api.NodeOpener;
import org.netbeans.modules.java.testrunner.ui.api.UIJavaUtils;
import org.netbeans.modules.junit.api.JUnitTestcase;
import org.netbeans.modules.junit.ui.api.JUnitCallstackFrameNode;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import static java.util.Arrays.asList;

/**
 *
 * @author Marian Petras
 */
@NodeOpener.Registration(projectType = CommonUtils.MAVEN_PROJECT_TYPE, testingFramework = CommonUtils.JUNIT_TF)
public final class MavenJUnitNodeOpener extends NodeOpener {

    private static final Logger LOG = Logger.getLogger(MavenJUnitNodeOpener.class.getName());

    @Override
    public void openTestsuite(TestsuiteNode node) {
        Children childrens = node.getChildren();
        if (childrens != null) {
            Node child = childrens.getNodeAt(0);
            if (child instanceof MavenJUnitTestMethodNode junitMethodNode) {
                final FileObject fo = junitMethodNode.getTestcaseFileObject();
                final MethodInfo mi = MethodInfo.fromTestCase(junitMethodNode.getTestcase());
                if (fo != null) {
                    final long[] line = new long[]{0};
                    JavaSource javaSource = JavaSource.forFileObject(fo);
                    if (javaSource != null) {
                        try {
                            javaSource.runUserActionTask(compilationController -> {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                Trees trees = compilationController.getTrees();
                                CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                                for (Tree tree : typeDecls) {
                                    Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                    if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(mi.topLevelClass())) {
                                        element = resolveNestedClass(mi.nestedClasses(), element);
                                        long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, trees.getTree(element));
                                        line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                        break;
                                    }
                                }
                            }, true);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        }
                    }
                    UIJavaUtils.openFile(fo, (int) line[0]);
                }
            }
        }
    }

    @Override
    public void openTestMethod(final TestMethodNode node) {
        if (!(node instanceof MavenJUnitTestMethodNode)) {
            return;
        }
        MavenJUnitTestMethodNode mtn = (MavenJUnitTestMethodNode) node;
        final FileObject fo = mtn.getTestcaseFileObject();
        final MethodInfo mi = MethodInfo.fromTestCase(mtn.getTestcase());
        if (fo != null) {
            final FileObject[] fo2open = new FileObject[]{fo};
            final long[] line = new long[]{0};
            JavaSource javaSource = JavaSource.forFileObject(fo2open[0]);
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(compilationController -> {
                        compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                        Trees trees = compilationController.getTrees();
                        CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                        List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                        for (Tree tree : typeDecls) {
                            Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                            if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(mi.topLevelClass())) {
                                element = resolveNestedClass(mi.nestedClasses(), element);
                                List<? extends ExecutableElement> methodElements = ElementFilter.methodsIn(element.getEnclosedElements());
                                for (Element child : methodElements) {
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
                    }, true);

                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            UIJavaUtils.openFile(fo2open[0], (int) line[0]);
        }
    }

    @Override
    public void openCallstackFrame(Node node, @NonNull String frameInfo) {
        if(frameInfo.isEmpty()) { // user probably clicked on a failed test method node, find failing line within the testMethod using the stacktrace
            if (!(node instanceof JUnitTestMethodNode)) {
                return;
            }
        } else { // user probably clicked on a stacktrace node
            if (!(node instanceof JUnitCallstackFrameNode)) {
                return;
            }
        }
        JUnitTestMethodNode methodNode = (JUnitTestMethodNode) UIJavaUtils.getTestMethodNode(node);
        if (!(methodNode instanceof MavenJUnitTestMethodNode)) {
            return;
        }
        FileLocator locator = methodNode.getTestcase().getSession().getFileLocator();
        if (locator == null) {
            return;
        }
        FileObject testfo = ((MavenJUnitTestMethodNode) methodNode).getTestcaseFileObject();
        if (testfo == null) {
            //#221053 more logging
            StringBuilder stack = new StringBuilder();
            if (methodNode.getTestcase().getTrouble() != null) {
                String[] st = methodNode.getTestcase().getTrouble().getStackTrace();
                if (st != null) {
                    stack.append("\n");
                    for (String s : st) {
                        stack.append(s).append("\n");
                    }
                } else {
                    stack.append("<none>");
                }
            } else {
                stack.append("<none>");
            }
            LOG.log(Level.INFO, "#221053: unknown location: {0} classname:{1}, stacktrace:", new Object[]{methodNode.getTestcase().getLocation(), methodNode.getTestcase().getClassName(), stack});
        }
        final int[] lineNumStorage = new int[1];
        FileObject file = UIJavaUtils.getFile(frameInfo, lineNumStorage, locator);
        //lineNumStorage -1 means no regexp for stacktrace was matched.
        if (testfo != null && file == null && methodNode.getTestcase().getTrouble() != null && lineNumStorage[0] == -1) {
                //213935 we could not recognize the stack trace line and map it to known file
            //if it's a failure text, grab the testcase's own line from the stack.
                // 213935 we need to find the testcase linenumber to jump to.
                // and ignore the infrastructure stack lines in the process
                for(int index = 0; !testfo.equals(file) && index < st.length; index++) {
                    if (st[index].contains(fqMethodName)) {
                        file = UIJavaUtils.getFile(st[index], lineNumStorage, locator);
                        break;
                    }
                }
                }
            }
        }
        // Is this a @Test(expected = *Exception.class) test method that failed?
        if (file == null && lineNumStorage[0] == -1 && node instanceof MavenJUnitTestMethodNode) {
            openTestMethod((MavenJUnitTestMethodNode) node);
        }
        UIJavaUtils.openFile(file, lineNumStorage[0]);
    }

    private Element resolveNestedClass(List<String> nestedClasses, Element e) {
        if(nestedClasses.isEmpty()) {
            return e;
        } else {
            String simpleName = nestedClasses.get(0);
            for(Element childElement: e.getEnclosedElements()) {
                if(childElement.getSimpleName().contentEquals(simpleName)) {
                    return resolveNestedClass(nestedClasses.subList(1, nestedClasses.size()), childElement);
                }
            }
            return e;
        }
    }

    private record MethodInfo (String packageName, String topLevelClass, List<String> nestedClasses, String method) {
        public static MethodInfo fromTestCase(JUnitTestcase testcase) {
            String className = testcase.getClassName();
            String[] nestedClasses = className.split("\\$");
            String packageName = null;
            int lastDotInTopLevelClass = nestedClasses[0].lastIndexOf(".");
            if (lastDotInTopLevelClass >= 0) {
                packageName = nestedClasses[0].substring(0, lastDotInTopLevelClass);
                nestedClasses[0] = nestedClasses[0].substring(lastDotInTopLevelClass + 1);
            }
            String method = null;
            if(testcase.getName().startsWith(className)) {
                method = testcase.getName().substring(className.length() + 1);
            }
            return new MethodInfo(packageName, nestedClasses[0], asList(nestedClasses).subList(1, nestedClasses.length), method);
        }
    }
}
