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
package org.netbeans.test.java.editor.formatting;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.formatting.operators.SpacesOperator;
import org.netbeans.test.java.editor.lib.EditorTestCase;

/**
 *
 * @author jprox
 */
public class FormattingOptionsTest extends EditorTestCase {

    public String curPackage;

    public String curTest;

    public FormattingOptionsTest(String testMethodName) {
        super(testMethodName);
        curTest = testMethodName;
        curPackage = getClass().getPackage().getName();
    }

    public static Test suite() {
        return NbModuleSuite                             
                .createConfiguration(GeneralFormattingOptionsTest.class)                
//                .addTest(GeneralFormattingOptionsTest.class)
//                .addTest(JavaTabsAndIndentsTest.class)
//                .addTest(AlignmentTest.class)
//                .addTest(BracesTest.class)
//                .addTest(WrappingTest.class)
//                .addTest(BlankLinesTest.class)
//                .addTest(SpacesTest.class)                
                .enableModules(".*")
                .clusters(".*")
                .suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("*** Starting " + this.getName() + " ***");
        openProject("Formatting");
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("*** Finished " + this.getName() + " ***");
        super.tearDown();
    }

    
    protected void formatFileAndCompare(String packageName, final String fileName) {
        openSourceFile(packageName, fileName);
        EditorOperator editor = new EditorOperator(fileName);
        MainWindowOperator.getDefault().menuBar().pushMenu("Source|Format", "|");
        new EventTool().waitNoEvent(250);
        getRef().print(editor.getText());
        try {
            compareReferenceFiles();
        } finally {
            editor.closeDiscard();
        }
    }

}
