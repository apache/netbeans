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

package org.netbeans.modules.j2ee.persistence.action;

import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import java.io.File;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for <code>ApplicationManagedResourceTransactionInjectableIeInEJB</code>, 
 * obscure name due to #122544.
 * 
 * @author Erno Mononen
 */
public class AppMgdResTxInjEJBTest extends EntityManagerGenerationTestSupport {
    
    public AppMgdResTxInjEJBTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File javaxAnnotation = new File(getWorkDir(), "javax" + File.separator + "annotation");
        javaxAnnotation.mkdirs();
        TestUtilities.copyStringToFile(
                new File(javaxAnnotation, "PostConstruct.java"), 
                "package javax.annotation; public @interface PostConstruct{}");
        TestUtilities.copyStringToFile(
                new File(javaxAnnotation, "PreDestroy.java"), 
                "package javax.annotation; public @interface PreDestroy{}");
    }

    /**
     * need additional investigation, commented for now
     * @throws Exception
     */
//    public void testGenerate() throws Exception{
//
//        File testFile = new File(getWorkDir(), "Test.java");
//        TestUtilities.copyStringToFile(testFile,
//                "package org.netbeans.test;\n\n" +
//                "import java.util.*;\n\n" +
//                "public class Test {\n" +
//                "}"
//                );
//        GenerationOptions options = new GenerationOptions();
//        options.setMethodName("create");
//        options.setOperation(GenerationOptions.Operation.PERSIST);
//        options.setParameterName("object");
//        options.setParameterType("Object");
//        options.setQueryAttribute("");
//        options.setReturnType("Object");
//
//        FileObject result = generate(FileUtil.toFileObject(testFile), options);
//        assertFile(result);
//    }
    
    public void testGenerateWithExistingEM() throws Exception{
        
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n" +
                "import javax.persistence.EntityManager;\n\n" +
                "public class Test {\n\n" +
                "    private EntityManager myEm;\n" + 
                "}"
                );
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("create");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setQueryAttribute("");
        options.setReturnType("Object");
        
        FileObject result = generate(FileUtil.toFileObject(testFile), options);
        
        assertFile(getGoldenFile("testGenWithExistingEM.pass"), FileUtil.toFile(result));
    }

    protected Class<? extends EntityManagerGenerationStrategy> getStrategyClass() {
        return ApplicationManagedResourceTransactionInjectableInEJB.class;
    }
}

