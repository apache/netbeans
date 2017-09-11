/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.metrics.hints;

import org.netbeans.modules.java.metrics.hints.MethodMetrics;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests basic method metrics
 * 
 * @author sdedic
 */
public class MethodMetricsTest extends NbTestCase {

    public MethodMetricsTest(String name) {
        super(name);
    }
    
    private String code(String fileName) throws IOException {
        File f = getDataDir();
        FileObject dd = FileUtil.toFileObject(f);
        
        FileObject file = dd.getFileObject("hints/metrics/" + fileName);
        return file.asText();
    }
    /**
     * Checks the 'method too complex' hint with the default
     * configuration.
     */
    public void testMethodTooComplexDefault() throws Exception {
        HintTest.create().input("test/MethodTooComplex.java", code("MethodTooComplex.java")).
        run(MethodMetrics.class).
        assertContainsWarnings("15:15-15:16:verifier:The method 'm' is too complex; cyclomatic complexity: 14");
    }

    /**
     * Checks that simple methods pass the check
     */
    public void testMethodNotTooComplex() throws Exception {
        HintTest.create().input("test/MethodNotTooComplex.java", code("MethodNotTooComplex.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".methodTooComplex").
        assertWarnings();
    }
    
    /**
     * Checks that threshold for complexity in preferences is read & used.
     */
    public void testMethodTooComplexCustom() throws Exception {
        HintTest.create().input("test/MethodNotTooComplex.java", code("MethodNotTooComplex.java")).
        preference(MethodMetrics.OPTION_COMPLEXITY_TRESHOLD, 5).
        run(MethodMetrics.class).
        assertContainsWarnings("15:15-15:16:verifier:The method 'm' is too complex; cyclomatic complexity: 10");
    }
    
    /**
     * Runs the hints through code, which triggers all branches of TreeScanner.
     * This is just a sanity check that a hint does not throw exceptions on some
     * construction; but will not catch all errors, obviously.
     */
    public void testAllVisitorBranches() throws Exception {
        HintTest.create().input("test/FullBranch.java", code("FullBranch.java")).
        run(MethodMetrics.class);
    }
    
    public void testMethodTooDeep() throws Exception {
        HintTest.create().input("test/MethodTooDeep.java", code("MethodTooDeep.java")).
        run(MethodMetrics.class).
        assertContainsWarnings("15:15-15:16:verifier:Method 'm' contains too deep statement structure: 9");
    }
    
    public void testMethodNotTooDeep() throws Exception {
        HintTest.create().input("test/MethodNotTooDeep.java", code("MethodNotTooDeep.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooDeepNesting").
        assertWarnings();
    }
    
    public void testMethodTooDeepCustom() throws Exception {
        HintTest.create().input("test/MethodNotTooDeep.java", code("MethodNotTooDeep.java")).
        preference(MethodMetrics.OPTION_NESTING_LIMIT, 3).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooDeepNesting").
        assertWarnings("15:15-15:16:verifier:Method 'm' contains too deep statement structure: 6");
    }
    
    public void testTooManyLinesAndStatements() throws Exception {
        HintTest.create().input("test/TooManyLinesOrCommands.java", code("TooManyLinesOrCommands.java")).
        run(MethodMetrics.class).
        assertContainsWarnings(
                "95:15-95:32:verifier:Method 'tooManyStatements' is too long: 31 statements",
                "15:15-15:27:verifier:Method 'tooManyLines' is too long: 62 lines"
        );
    }
    
    public void testNotTooManyLinesAndStatements() throws Exception {
        HintTest.create().input("test/NotTooManyLinesOrCommands.java", code("NotTooManyLinesOrCommands.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooLong").
        assertWarnings();
    }

    public void testTooManyLinesAndStatementsCustom() throws Exception {
        HintTest.create().input("test/NotTooManyLinesOrCommands.java", code("NotTooManyLinesOrCommands.java")).
        preference(MethodMetrics.OPTION_LINES_LIMIT, 40).
        preference(MethodMetrics.OPTION_STATEMENTS_LIMIT, 20).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooLong").
        assertWarnings(
                "15:15-15:27:verifier:Method 'tooManyLines' is too long: 59 lines",
                "77:15-77:32:verifier:Method 'tooManyStatements' is too long: 30 statements");
    }
    
    
    public void testTooManyExceptions() throws Exception {
        HintTest.create().input("test/MethodLimits.java", code("MethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyExceptions").
        assertWarnings(
            "15:27-15:47:verifier:Method 'methodWithExceptions' declares too many exceptions: 4", 
            "22:48-22:69:verifier:Method 'methodWithExceptions2' declares too many exceptions: 4");
    }
    
    public void testTooManyParameters() throws Exception {
        HintTest.create().input("test/MethodLimits.java", code("MethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyParameters").
        assertWarnings(
            "30:27-30:47:verifier:Method 'methodWithParameters' takes too many parameters: 12",
            "42:9-42:30:verifier:Method 'methodWithParameters2' takes too many parameters: 12");
    }
    
    public void testTooManyNegations() throws Exception {
        HintTest.create().input("test/MethodLimits.java", code("MethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleNegations").
        assertWarnings(
            "52:27-52:46:verifier:Method 'methodWithNegations' contains too many negations: 4",
            "63:8-63:27:verifier:Method 'methodWithNegations' contains too many negations: 4");
    }
    
    public void testTooManyLoops() throws Exception {
        HintTest.create().input("test/MethodLimits.java", code("MethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleLoops").
        assertWarnings(
            "96:27-96:42:verifier:Method 'methodWithLoops' contains 4 loops", 
            "111:8-111:24:verifier:Method 'methodWithLoops2' contains 4 loops");
    }
    
    public void testTooNotManyExceptions() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyExceptions").
        assertWarnings();
    }
    
    public void testTooNotManyParameters() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyParameters").
        assertWarnings();
    }
    
    public void testTooNotManyNegations() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleNegations").
        assertWarnings();
    }
    
    public void testTooNotManyLoops() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleLoops").
        assertWarnings();
    }
    
    public void testTooCustomManyExceptions() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        preference(MethodMetrics.OPTION_EXCEPTIONS_LIMIT, 2).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyExceptions").
        assertWarnings("15:27-15:47:verifier:Method 'methodWithExceptions' declares too many exceptions: 3",
        "21:48-21:69:verifier:Method 'methodWithExceptions2' declares too many exceptions: 3");
    }
    
    public void testTooCustomManyParameters() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        preference(MethodMetrics.OPTION_METHOD_PARAMETERS_LIMIT, 4).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyParameters").
        assertWarnings("28:27-28:47:verifier:Method 'methodWithParameters' takes too many parameters: 8",
            "38:9-38:30:verifier:Method 'methodWithParameters2' takes too many parameters: 8");
    }
    
    public void testTooCustomManyNegations() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        preference(MethodMetrics.OPTION_NEGATIONS_LIMIT, 2).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleNegations").
        assertWarnings("46:27-46:46:verifier:Method 'methodWithNegations' contains too many negations: 3",
            "57:8-57:27:verifier:Method 'methodWithNegations' contains too many negations: 3");
    }
    
    public void testTooManyNegationsWithAsserts() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        preference(MethodMetrics.OPTION_NEGATIONS_IGNORE_ASSERT, false).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleNegations").
        assertWarnings("83:8-83:27:verifier:Method 'negationsWithAssert' contains too many negations: 4");
    }

    public void testTooManyNegationsWithEquals() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        preference(MethodMetrics.OPTION_NEGATIONS_IGNORE_EQUALS, false).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleNegations").
        assertWarnings("70:19-70:25:verifier:Method 'equals' contains too many negations: 4");
    }

    public void testTooCustomManyLoops() throws Exception {
        HintTest.create().input("test/NoMethodLimits.java", code("NoMethodLimits.java")).
        preference(MethodMetrics.OPTION_LOOPS_LIMIT, 2).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleLoops").
        assertWarnings("91:27-91:42:verifier:Method 'methodWithLoops' contains 3 loops",
            "103:8-103:24:verifier:Method 'methodWithLoops2' contains 3 loops");
    }
    
    public void testMethodReturns() throws Exception {
        HintTest.create().input("test/MethodReturn.java", code("MethodReturn.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleReturnPoints").
        assertWarnings("13:19-13:31:verifier:Method 'methodReturn' has multiple return points: 4");
    }

    public void testMethodNotReturns() throws Exception {
        HintTest.create().input("test/MethodNoReturn.java", code("MethodNoReturn.java")).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleReturnPoints").
        assertWarnings();
    }

    public void testMethodCustomReturns() throws Exception {
        HintTest.create().input("test/MethodNoReturn.java", code("MethodNoReturn.java")).
        preference(MethodMetrics.OPTION_RETURN_LIMIT, 1).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleReturnPoints").
        assertWarnings("13:19-13:31:verifier:Method 'methodReturn' has multiple return points: 2");
    }
    
    public void testMethodIgnoreEquals() throws Exception {
        HintTest.create().input("test/MethodNoReturn.java", code("MethodNoReturn.java")).
        preference(MethodMetrics.OPTION_RETURN_IGNORE_EQUALS, false).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleReturnPoints").
        assertWarnings("38:19-38:25:verifier:Method 'equals' has multiple return points: 4");
    }

    public void testMethodIgnoreGuards() throws Exception {
        HintTest.create().input("test/MethodNoReturn.java", code("MethodNoReturn.java")).
        preference(MethodMetrics.OPTION_RETURN_IGNORE_GUARDS, false).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".multipleReturnPoints").
        assertWarnings("25:19-25:39:verifier:Method 'methodGuardedReturns' has multiple return points: 4");
    }
    
    public void testMethodCoupled() throws Exception {
        HintTest.create().
                input("test/MethodCoupled.java", code("MethodCoupled.java"), false).
                input("test/CoupledEnum.java", code("CoupledEnum.java")).
                input("test/CoupledException.java", code("CoupledException.java")).
        classpath(FileUtil.getArchiveRoot(junit.framework.TestCase.class.getProtectionDomain().getCodeSource().getLocation())).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyDependencies").
        assertWarnings("23:20-23:33:verifier:Method 'coupledMethod' is too coupled. References 18 types");
        
    }

    public void testMethodNotCoupled() throws Exception {
        HintTest.create().
                input("test/MethodNotCoupled.java", code("MethodNotCoupled.java"), false).
                input("test/CoupledEnum.java", code("CoupledEnum.java")).
                input("test/CoupledException.java", code("CoupledException.java")).
        classpath(FileUtil.getArchiveRoot(junit.framework.TestCase.class.getProtectionDomain().getCodeSource().getLocation())).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyDependencies").
        assertWarnings("");
    }

    public void testMethodCustomCoupled() throws Exception {
        HintTest.create().
                input("test/MethodNotCoupled.java", code("MethodNotCoupled.java"), false).
                input("test/CoupledEnum.java", code("CoupledEnum.java")).
                input("test/CoupledException.java", code("CoupledException.java")).
        classpath(FileUtil.getArchiveRoot(junit.framework.TestCase.class.getProtectionDomain().getCodeSource().getLocation())).
        preference(MethodMetrics.OPTION_COUPLING_LIMIT, 13).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyDependencies").
        assertWarnings("23:20-23:33:verifier:Method 'coupledMethod' is too coupled. References 15 types");
    }

    public void testMethodCoupledWithJavaPlatform() throws Exception {
        HintTest.create().
                input("test/MethodNotCoupled.java", code("MethodNotCoupled.java"), false).
                input("test/CoupledEnum.java", code("CoupledEnum.java")).
                input("test/CoupledException.java", code("CoupledException.java")).
        classpath(FileUtil.getArchiveRoot(junit.framework.TestCase.class.getProtectionDomain().getCodeSource().getLocation())).
        preference(MethodMetrics.OPTION_COUPLING_IGNORE_JAVA, false).
        run(MethodMetrics.class, MethodMetrics.class.getName() + ".tooManyDependencies").
        assertWarnings("23:20-23:33:verifier:Method 'coupledMethod' is too coupled. References 20 types");
    }
}
