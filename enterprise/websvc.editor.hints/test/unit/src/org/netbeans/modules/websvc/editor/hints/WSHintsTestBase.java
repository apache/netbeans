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
