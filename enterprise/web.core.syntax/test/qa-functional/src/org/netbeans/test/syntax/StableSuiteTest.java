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
package org.netbeans.test.syntax;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jindrich Sedek
 */
public class StableSuiteTest extends J2eeTestCase {

    public StableSuiteTest(String name) {
        super(name);
    }
    
    public StableSuiteTest() {
        super("StableSuiteTest");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        if (isRegistered(Server.ANY)){
            return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
        }else{
            return NbModuleSuite.create(conf.addTest(J2eeTestCase.class));
        }
    }

    public static final class SuiteCreator extends NbTestSuite {
        FileObject dataDir = FileUtil.createData(new StableSuiteTest().getDataDir());
        FileObject completionTestWebDir = dataDir.getFileObject("CompletionTestProjects/Jsp/web/");
        FileObject completionJSFTestWebDir = dataDir.getFileObject("CompletionTestProjects/JSF/web/");
        FileObject completionJavaEE6TestWebDir = dataDir.getFileObject("CompletionTestProjects/JavaEE6/web/");

        public SuiteCreator() throws IOException {
            addCompletionTest("stableDirectivesBasic.jsp");
            addCompletionTest("stableExpression.jsp");
            addCompletionTest("stableHTML.jsp");
            addCompletionTest("stableHTMLCompletion.html");
            addCompletionTest("stableHTMLEmbedding.html");
            addCompletionTest("stableJSPEmbedding.jsp");
            addCompletionTest("stableJSPElements.jsp");
            addCompletionTest("stableScriptletsJavaBasic.jsp");
            addCompletionTest("stableTaglibCompletion.jsp");
            addCompletionTest("stableXHTML.xhtml");
            addCompletionTest("stableIDClassCC.html");
            addJSFCompletionTest("testJSFObjects.jsp");
            addJSFCompletionTest("testJSFTag.jsp");
            addJavaEE6Test("testInjection.jsp");
            addJavaEE6Test("testInjection.xhtml");
        }
        
        private void addCompletionTest(String fileName) throws IOException{
            String name = fileName.replace('.', '_');
            addTest(new CompletionTest(name, completionTestWebDir.getFileObject(fileName)));
        }
                
        private void addJSFCompletionTest(String fileName) throws IOException{
            String name = fileName.replace('.', '_');
            addTest(new CompletionTest(name, completionJSFTestWebDir.getFileObject(fileName)));
        }

        private void addJavaEE6Test(String fileName) throws IOException{
            String name = fileName.replace('.', '_');
            addTest(new CompletionTest(name, completionJavaEE6TestWebDir.getFileObject(fileName)));
        }
    }
    
}
