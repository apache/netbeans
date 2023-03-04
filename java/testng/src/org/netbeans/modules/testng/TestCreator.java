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

package org.netbeans.modules.testng;

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
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.CreateTestParam;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

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
    
    /** Creates a new instance of TestCreator */
    TestCreator(boolean loadDefaults) {
        setup = new TestGeneratorSetup(loadDefaults);
    }
    
    /** Creates a new instance of TestCreator */
    TestCreator(Map<CreateTestParam, Object> params) {
        setup = new TestGeneratorSetup(params);
    }
    
    /**
     */
    public void createEmptyTest(FileObject testFileObj) throws IOException {
        AbstractTestGenerator testGenerator = new TestGenerator(setup);
        doModifications(testFileObj, testGenerator);
    }
    
    /**
     * 
     * @return  list of names of created classes
     */
    public void createSimpleTest(ElementHandle<TypeElement> topClassToTest,
                                 FileObject testFileObj,
                                 boolean isNewTestClass) throws IOException {
        AbstractTestGenerator testGenerator = new TestGenerator(
                                          setup,
                                          Collections.singletonList(topClassToTest),
                                          null,
                                          isNewTestClass);
        doModifications(testFileObj, testGenerator);
    }
    
    /**
     */
    public List<String> createTestSuite(List<String> suiteMembers,
                                        FileObject testFileObj,
                                        boolean isNewTestClass) throws IOException {
        AbstractTestGenerator testGenerator = new TestGenerator(
                                          setup,
                                          null,
                                          suiteMembers,
                                          isNewTestClass);
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
