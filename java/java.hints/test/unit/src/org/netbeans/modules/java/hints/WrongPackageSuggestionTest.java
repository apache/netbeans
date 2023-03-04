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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.WrongPackageSuggestion.CorrectPackageDeclarationFix;
import org.netbeans.modules.java.hints.WrongPackageSuggestion.MoveToCorrectPlace;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;

/**
 *
 * @author Jan Lahoda
 */
public class WrongPackageSuggestionTest extends TreeRuleTestBase {
    
    public WrongPackageSuggestionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }
    
    public void testEvaluate1() throws Exception {
        performAnalysisTest("test/Test.java", "pac|kage other; public class Test{}", "0:8-0:13:error:Incorrect Package");
    }
    
    public void testEvaluate2() throws Exception {
        performAnalysisTest("Test.java", "pac|kage other; public class Test{}", "0:8-0:13:error:Incorrect Package");
    }
    
    public void testEvaluate3() throws Exception {
        performAnalysisTest("test/Test.java", "|public class Test{}", "0:0-0:1:error:Incorrect Package");
    }
    
    public void testEvaluate4() throws Exception {
        performAnalysisTest("test/Test.java", "pac|kage test; public class Test{}");
    }
    
    public void testEvaluate5() throws Exception {
        performAnalysisTest("Test.java", "|public class Test{}");
    }
    
    public void testEvaluate121562() throws Exception {
        performAnalysisTest("test/Test.java", "", 0);
    }
    
    public void testAdjustPackageClause1() throws Exception {
        performFixTest("test/Test.java", "pac|kage other; public class Test{}", "0:8-0:13:error:Incorrect Package", "CorrectFix", "package test; public class Test{}");
    }
    
    public void testAdjustPackageClause2() throws Exception {
        performFixTest("Test.java", "pac|kage other; public class Test{}", "0:8-0:13:error:Incorrect Package", "CorrectFix", " public class Test{}");
    }
    
    public void testAdjustPackageClause3() throws Exception {
        performFixTest("test/Test.java", "|public class Test{}", "0:0-0:1:error:Incorrect Package", "CorrectFix", "package test; public class Test{}");
    }
    
    public void testMoveToCorrectPackage1() throws Exception {
        performFixTest("test/Test.java", "pac|kage other; public class Test{}", "0:8-0:13:error:Incorrect Package", "MoveFix", "other/Test.java", "package other; public class Test{}");
    }
    
    public void testMoveToCorrectPackage2() throws Exception {
        performFixTest("Test.java", "pac|kage other; public class Test{}", "0:8-0:13:error:Incorrect Package", "MoveFix", "other/Test.java", "package other; public class Test{}");
    }
    
    public void testMoveToCorrectPackage3() throws Exception {
        performFixTest("test/Test.java", "|public class Test{}", "0:0-0:1:error:Incorrect Package", "MoveFix", "Test.java", "public class Test{}");
    }
    
    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        WrongPackageSuggestion s = new WrongPackageSuggestion();
        
        if (!s.getTreeKinds().contains(path.getLeaf().getKind())) {
            return null;
        }
        return s.run(info, path);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof CorrectPackageDeclarationFix) {
            return "CorrectFix";
        }
        
        if (f instanceof MoveToCorrectPlace) {
            return "MoveFix";
        }
        
        throw new UnsupportedOperationException();
    }
    
}
