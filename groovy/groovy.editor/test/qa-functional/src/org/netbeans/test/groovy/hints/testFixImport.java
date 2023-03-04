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
package org.netbeans.test.groovy.hints;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.groovy.GeneralGroovy;

/**
 *
 * @author Vladimir Riha
 */
public class testFixImport extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovyfi_";
    static int name_iterator = 0;

    public testFixImport(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testFixImport.class).addTest(
                "CreateApplication",
                "FixImportHint").enableModules(".*").clusters(".*"));
    }

    public void CreateApplication() {
        startTest();
        createJavaApplication(TEST_BASE_NAME + name_iterator);
        testFixImport.name_iterator++;
        endTest();
    }

    public void FixImportHint() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "AA");
        EditorOperator file = new EditorOperator("AA.groovy");
        file.setCaretPosition("AA ", false);
        type(file, "extends AEADBadTagException");
        waitScanFinished();
        new EventTool().waitNoEvent(1000);
        Object[] anns = getAnnotations(file,0);
        
        assertEquals("More annotations than expected", 1, anns.length);
        String ideal = "Add import for javax.crypto.AEADBadTagException\n"
                + "----\n"
                + "(Alt-Enter shows hints)";
        
        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue(TEST_BASE_NAME, ideal.equals(desc.trim()));
        }

        endTest();
    }
}
