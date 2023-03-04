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

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class DomainCompletionProviderTest extends GroovyTestBase {

    String TEST_BASE = "projects/completion/grails-app/domain/";


    public DomainCompletionProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        indexFile("projects/completion/grails-app/domain/TestDomain.groovy");
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = super.createClassPathsForTest();

        map.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {
            FileUtil.toFileObject(getDataFile("/projects/completion/grails-app/domain")) }));
        return map;
    }

    public void testDomainMethods1() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findBy^", true);
    }

    public void testDomainMethods2() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findByAg^", true);
    }

    public void testDomainMethods3() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findByAge^", true);
    }

    public void testDomainMethods4() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findByAgeAnd^", true);
    }

    public void testDomainMethods5() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findByAgeAndNa^", true);
    }

    public void testDomainMethods6() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findByAgeAndName^", true);
    }

    public void testDomainMethods7() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.findR^", false);
    }

    public void testDomainMethods8() throws Exception {
        checkCompletion(TEST_BASE + "TestDomain.groovy", "        TestDomain.find^", true);
    }
}
