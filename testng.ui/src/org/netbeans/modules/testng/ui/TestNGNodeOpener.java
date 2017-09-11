/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.testng.ui;

import org.netbeans.modules.testng.api.TestNGTestSuite;
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
import static javax.lang.model.util.ElementFilter.methodsIn;
import javax.swing.Action;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.java.testrunner.JavaRegexpUtils;
import org.netbeans.modules.java.testrunner.ui.api.NodeOpener;
import org.netbeans.modules.java.testrunner.ui.api.UIJavaUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
@NodeOpener.Registration(projectType = "", testingFramework = CommonUtils.TESTNG_TF)
public final class TestNGNodeOpener extends NodeOpener {

    private static final Logger LOGGER = Logger.getLogger(TestNGNodeOpener.class.getName());
    static final Action[] NO_ACTIONS = new Action[0];

    public void openTestsuite(TestsuiteNode node) {
        TestSuite suite = node.getSuite();
        if ((suite != null) && (suite instanceof TestSuite)) {
            final FileObject fo = ((TestNGTestSuite) suite).getSuiteFO();
            if (fo != null) {
                int[] location = XmlSuiteHandler.getSuiteLocation(fo, suite.getName());
                UIJavaUtils.openFile(fo, location[0], location[1]);
            }
        }
    }

    public void openTestMethod(final TestMethodNode node) {
        if(!(node instanceof TestNGMethodNode)) {
            return;
        }
        final FileObject fo = ((TestNGMethodNode) node).getTestcase().getClassFileObject();
        if (fo != null) {
            final long[] line = new long[]{0};
            JavaSource javaSource = JavaSource.forFileObject(fo);
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {

                        public void run(CompilationController compilationController) throws Exception {
                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                            Trees trees = compilationController.getTrees();
                            CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                            List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                            for (Tree tree : typeDecls) {
                                Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo.getName())) {
                                    List<? extends ExecutableElement> methodElements = methodsIn(element.getEnclosedElements());
                                    for (Element child : methodElements) {
                                        if (child.getSimpleName().contentEquals(((TestNGMethodNode) node).getTestcase().getTestName())) {
                                            long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, trees.getTree(child));
                                            line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }, true);

                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            UIJavaUtils.openFile(fo, (int) line[0]);
        }
    }

    public void openCallstackFrame(Node node, String frameInfo) {
        if(!(node instanceof TestNGMethodNode)) {
            return;
        }
        TestNGMethodNode methodNode = (TestNGMethodNode) UIJavaUtils.getTestMethodNode(node);
        FileLocator locator = methodNode.getTestcase().getSession().getFileLocator();
        if (locator == null) {
            return;
        }
        final int[] lineNumStorage = new int[1];
        FileObject file = UIJavaUtils.getFile(frameInfo, lineNumStorage, locator);
        if ((file == null) && (methodNode.getTestcase().getTrouble() != null)) {
            String[] st = methodNode.getTestcase().getTrouble().getStackTrace();
            if ((st != null) && (st.length > 0)) {
                file = UIJavaUtils.getFile(st[st.length - 1], lineNumStorage, locator);
            }
        }
        UIJavaUtils.openFile(file, lineNumStorage[0]);
    }
}
