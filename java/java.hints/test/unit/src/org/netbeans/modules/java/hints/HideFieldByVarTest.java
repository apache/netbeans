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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jaroslav Tulach
 */
public class HideFieldByVarTest extends TreeRuleTestBase {
    
    public HideFieldByVarTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        SourceUtilsTestUtil.setLookup(new Object[0], getClass().getClassLoader());
    }
    

    public void testDoesNotHideItself() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int va";
        String after = "lue = -1;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testLocaVarAgainsInstanceVar() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "  private int compute() {" +
            "    int va";
        String after = "lue = -1;" +
            "    return 10;" +
            "  }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:82-0:87:verifier:Local variable hides a field"
        );
    }

    public void testLocaVarInStaticMethod() throws Exception {
        String text = "package test; class Test {" +
            "  protected  int value;" +
            "  private static int compute() {" +
            "    int value = -1;" +
            "    return 10;" +
            "  }" +
            "}";
        
        for (int i = 0; i < text.length(); i++) {
            workDirIndex = i;
            performAnalysisTest("test/Test.java", "// index: " + i + "\n" + text, i);
        }
    }

    public void testLocaVarAgainsInhVar() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "}" +
            "class Test2 extends Test {" +
            "  private int compute() {" +
            "    int va";
        String after = "lue = -1;" +
            "    return 10;" +
            "  }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:109-0:114:verifier:Local variable hides a field"
        );
    }

    public void testParamIsOkAgainstInhVar() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "}" +
            "class Test2 extends Test {" +
            "  private void compute(int val";
        String after =         "ue) {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new HideFieldByVar().run(info, path);
    }
    
    private final String sourceLevel = "1.5";

    private int workDirIndex = -1;

    @Override
    public String getWorkDirPath() {
        String basePath = super.getWorkDirPath();
        if (workDirIndex != (-1)) {
            basePath += File.separator + workDirIndex;
        }
        return basePath;
    }
    
}
