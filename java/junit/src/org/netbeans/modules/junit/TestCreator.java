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

package org.netbeans.modules.junit;

import org.netbeans.modules.junit.api.JUnitVersion;
import org.netbeans.modules.junit.api.JUnitTestUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
//import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.CreateTestParam;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
public final class TestCreator implements TestabilityJudge {
    
    /**
     * bitmap combining modifiers PUBLIC, PROTECTED and PRIVATE
     *
     * @see  java.lang.reflect.Modifier
     */
    static final Set<Modifier> ACCESS_MODIFIERS
            = EnumSet.of(Modifier.PUBLIC,
                         Modifier.PROTECTED,
                         Modifier.PRIVATE);
    
    /** */
    private final TestGeneratorSetup setup;
    /** */
    private final JUnitVersion junitVersion;
    
    /** Creates a new instance of TestCreator */
    TestCreator(boolean loadDefaults,
                JUnitVersion junitVersion) {
        setup = new TestGeneratorSetup(loadDefaults);
        this.junitVersion = junitVersion;
    }
    
    /** Creates a new instance of TestCreator */
    TestCreator(Map<CreateTestParam, Object> params,
                JUnitVersion junitVersion) {
        setup = new TestGeneratorSetup(params);
        this.junitVersion = junitVersion;
    }
    
    /**
     */
    public void createEmptyTest(FileObject testFileObj) throws IOException {
        AbstractTestGenerator testGenerator;
        switch (junitVersion) {
            case JUNIT3:
                testGenerator = new JUnit3TestGenerator(
                        setup,
                        JUnitTestUtil.getSourceLevel(testFileObj));
                break;
            case JUNIT4:
                testGenerator = new JUnit4TestGenerator(setup);
                break;
            case JUNIT5:
                testGenerator = new JUnit5TestGenerator(setup);
                break;
            default:
                throw new IllegalStateException("junit version not set");//NOI18N
        }
        doModifications(testFileObj, testGenerator);
    }
    
    /**
     * 
     * @return  list of names of created classes
     */
    public void createSimpleTest(ElementHandle<TypeElement> topClassToTest,
                                 FileObject testFileObj,
                                 boolean isNewTestClass) throws IOException {
        AbstractTestGenerator testGenerator;
        switch (junitVersion) {
            case JUNIT3:
                testGenerator = new JUnit3TestGenerator(
                      setup,
                      Collections.singletonList(topClassToTest),
                      null,
                      isNewTestClass,
                      JUnitTestUtil.getSourceLevel(testFileObj));
                break;
            case JUNIT4:
                testGenerator = new JUnit4TestGenerator(
                                          setup,
                                          Collections.singletonList(topClassToTest),
                                          null,
                                          isNewTestClass);
                break;
            case JUNIT5:
                testGenerator = new JUnit5TestGenerator(
                                          setup,
                                          Collections.singletonList(topClassToTest),
                                          null,
                                          isNewTestClass);
                break;
            default:
                throw new IllegalStateException("junit version not set");//NOI18N
        }
        doModifications(testFileObj, testGenerator);
    }
    
    /**
     */
    public List<String> createTestSuite(List<String> suiteMembers,
                                        FileObject testFileObj,
                                        boolean isNewTestClass) throws IOException {
        AbstractTestGenerator testGenerator;
        switch (junitVersion) {
            case JUNIT3:
                testGenerator = new JUnit3TestGenerator(
                      setup,
                      null,
                      suiteMembers,
                      isNewTestClass,
                      JUnitTestUtil.getSourceLevel(testFileObj));
                break;
            case JUNIT4:
                testGenerator = new JUnit4TestGenerator(
                                          setup,
                                          null,
                                          suiteMembers,
                                          isNewTestClass);
                break;
            case JUNIT5:
                testGenerator = new JUnit5TestGenerator(
                                          setup,
                                          null,
                                          suiteMembers,
                                          isNewTestClass);
                String message = NbBundle.getMessage(getClass(), "MSG_using_junit5_for_test_suites");
                JUnitTestUtil.notifyUser(message,  NotifyDescriptor.INFORMATION_MESSAGE);
                break;

            default:
                throw new IllegalStateException("junit version not set");//NOI18N
        }
        doModifications(testFileObj, testGenerator);
        
        return testGenerator.getProcessedClassNames();
    }

    private void doModifications(final FileObject testFileObj,
                                 final AbstractTestGenerator testGenerator)
                                                            throws IOException {
        final JavaSource javaSource = JavaSource.forFileObject(testFileObj);
        javaSource.runUserActionTask(
                new Task<CompilationController>() {
                    public void run(CompilationController parameter) throws Exception {
                        ModificationResult result
                                = javaSource.runModificationTask(testGenerator);
                        result.commit();
                    }
                },
            true);

    }
    
    public TestabilityResult isClassTestable(CompilationInfo compInfo,
                                             TypeElement classElem, long skipTestabilityResultMask) {
        return setup.isClassTestable(compInfo, classElem, skipTestabilityResultMask);
    }

    public boolean isMethodTestable(ExecutableElement method) {
        return setup.isMethodTestable(method);
    }
   
}
