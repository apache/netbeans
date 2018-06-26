/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.editor.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.TreeUtilities;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.RulesEngine;

/**
 *
 * @author Ajit
 */
public abstract class WSHintsTestBase extends NbTestCase {

    private ProblemContext context;
    private FileObject dataDir;
    private WSHintsTestRuleEngine ruleEngine;
    private CancellableTask<CompilationController> testTask;

    public WSHintsTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // workaround for JavaSource class
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        dataDir = FileUtil.toFileObject(FileUtil.normalizeFile(getDataDir()));
        context = new ProblemContext();
        ruleEngine = new WSHintsTestRuleEngine();
        testTask = new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ruleEngine.getProblemsFound().clear();
                for (Tree tree : workingCopy.getCompilationUnit().getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                        TreePath path = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), tree);
                        TypeElement javaClass = (TypeElement) workingCopy.getTrees().getElement(path);

                        context.setJavaClass(javaClass);
                        context.setCompilationInfo(workingCopy);

                        javaClass.accept(ruleEngine, context);
                    }
                }
            }
        };
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected final void testRule(Rule<? extends Element> instance, String testFile) throws IOException {
        System.out.println("Executing " + getName());
        System.out.println("Checking rule " + instance.getClass().getSimpleName());
        context.setFileObject(dataDir.getFileObject(testFile));
        JavaSource testSource = JavaSource.forFileObject(context.getFileObject());
        testSource.runUserActionTask(testTask, true);
        assertFalse("Expected non empty hints list.", ruleEngine.getProblemsFound().isEmpty());
        for(ErrorDescription ed:ruleEngine.getProblemsFound()) {
            for(Fix fix:ed.getFixes().getFixes()) {
                try {
                    fix.implement();
                } catch (Exception ex) {
                    log(ex.getLocalizedMessage());
                }
            }
        }
        testSource.runUserActionTask(testTask, true);
        assertTrue("Expected empty hints list.", ruleEngine.getProblemsFound().isEmpty());
    }

    protected final WSHintsTestRuleEngine getRulesEngine() {
        return ruleEngine;
    }

    public static final class WSHintsTestRuleEngine extends RulesEngine {

        private Collection<Rule<TypeElement>> classRules = new ArrayList<Rule<TypeElement>>();
        private Collection<Rule<ExecutableElement>> operationRules = new ArrayList<Rule<ExecutableElement>>();
        private Collection<Rule<VariableElement>> parameterRules = new ArrayList<Rule<VariableElement>>();

        public Collection<Rule<TypeElement>> getClassRules() {
            return classRules;
        }

        public Collection<Rule<ExecutableElement>> getOperationRules() {
            return operationRules;
        }

        public Collection<Rule<VariableElement>> getParameterRules() {
            return parameterRules;
        }
    }

}
