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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author schmidtm
 */
import java.util.Map;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class FieldCCTest extends GroovyTestBase {

    String TEST_BASE = "testfiles/completion/field/";

    public FieldCCTest(String testName) {
        super(testName);
        Logger.getLogger(CompletionHandler.class.getName()).setLevel(Level.FINEST);
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = super.createClassPathsForTest();
        map.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {
            FileUtil.toFileObject(getDataFile("/testfiles/completion/field")) }));
        return map;
    }

    public void testFields1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields1.groovy", "\"User $nom^\"", true);
    }

    public void testFields2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields1.groovy", "println \"Hi: $ad^\"", true);
    }

}
