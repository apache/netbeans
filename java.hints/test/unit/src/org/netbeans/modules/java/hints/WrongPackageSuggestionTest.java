/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
