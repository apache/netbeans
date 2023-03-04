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


package org.netbeans.test.java.editor.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.diff.LineDiff;

/**
 *
 * @author  mroskanin
 */
public class JavaEditorTestCase extends EditorTestCase {

    public static final String PROJECT_NAME = "java_editor_test"; //NOI18N;

    public JavaEditorTestCase(String testMethodName) {
        super(testMethodName);
    }

    protected String getDefaultProjectName() {
        return PROJECT_NAME;
    }

    /**
     * Compare goldenfile with content of editor
     * @param oper
     * @throws IOException
     */
    public void compareGoldenFile(EditorOperator oper) throws IOException {        
        ref(oper.getText());        
        compareGoldenFile();
    }
    
    public void checkContentOfEditorRegexp(EditorOperator editor, String regExp) {
        Pattern p = Pattern.compile(regExp, Pattern.DOTALL | Pattern.MULTILINE);
        String code = editor.getText();        
        Matcher matcher = p.matcher(code);
        boolean match = matcher.matches();
        if(!match) {
            System.out.println(regExp);
            System.out.println("-------------------");
            System.out.println(code);
            
        }
        assertTrue("Editor does not contain "+regExp,match);
    }
    
    /**
     * Compare goldenfile with ref file, which was created during the test
     * @throws IOException
     */
    public void compareGoldenFile() throws IOException {
	File fGolden = getGoldenFile();
	String refFileName = getName()+".ref";
	String diffFileName = getName()+".diff";
	File fRef = new File(getWorkDir(),refFileName);
	LineDiff diff = new LineDiff(false);
	File fDiff = new File(getWorkDir(),diffFileName);
        if(diff.diff(fGolden, fRef, fDiff)) fail("Golden files differ");
    }
}
