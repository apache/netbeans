/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.modules.php.spi.testing.run.TestSuite;

public final class TestRunner {

    private static final Logger LOGGER = Logger.getLogger(TestRunner.class.getName());

    private final PhpModule phpModule;


    public TestRunner(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    public void runTests(TestRunInfo runInfo, TestSession testSession) throws TestRunException {
        Codecept codecept = Codecept.getForPhpModule(phpModule, true);
        if (codecept == null) {
            throw new TestRunException();
        }
        Integer result = codecept.runTests(phpModule, runInfo);
        if (result == null) {
            throw new TestRunException();
        }
        TestSessionVo sessionVo = createTestSession(Codecept.XML_LOG);
        if (sessionVo != null) {
            map(sessionVo, testSession);
        }
    }

    @CheckForNull
    private TestSessionVo createTestSession(File xmlLog) throws TestRunException {
        Reader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(xmlLog), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ex) {
            processCodeceptionError(ex);
            return null;
        }
        TestSessionVo session = new TestSessionVo();
        boolean parsed = CodeceptionLogParser.parse(reader, session);
        if (!parsed) {
            processCodeceptionError(null);
            return null;
        }
        return session;
    }

    private void processCodeceptionError(Exception cause) throws TestRunException {
        LOGGER.info(String.format("File %s not found or cannot be parsed. If there are no errors in Codeception output (verify in Output window), "
                + "please report an issue (http://www.netbeans.org/issues/).", Codecept.XML_LOG));
        throw new TestRunException(cause);
    }

    //~ Mappers

    private void map(TestSessionVo sessionVo, TestSession testSession) {
        testSession.setOutputLineHandler(sessionVo.getOutputLineHandler());
        String initMessage = sessionVo.getInitMessage();
        if (initMessage != null) {
            testSession.printMessage(initMessage, false);
            testSession.printMessage("", false); // NOI18N
        }
        for (TestSuiteVo suiteVo : sessionVo.getTestSuites()) {
            TestSuite testSuite = testSession.addTestSuite(suiteVo.getName(), suiteVo.getLocation());
            for (TestCaseVo caseVo : suiteVo.getTestCases()) {
                TestCase testCase = testSuite.addTestCase(caseVo.getName(), caseVo.getType());
                String className = caseVo.getClassName();
                if (className != null) {
                    testCase.setClassName(className);
                }
                Locations.Line location = caseVo.getLocation();
                if (location != null) {
                    testCase.setLocation(location);
                }
                testCase.setStatus(caseVo.getStatus());
                boolean error = caseVo.isError();
                if (error
                        || caseVo.isFailure()) {
                    String[] stackTrace = caseVo.getStackTrace();
                    String[] tmp;
                    if (stackTrace.length == 1) {
                        tmp = new String[0];
                    } else {
                        tmp = new String[stackTrace.length - 1];
                        System.arraycopy(stackTrace, 1, tmp, 0, stackTrace.length - 1);
                    }
                    testCase.setFailureInfo(stackTrace[0], tmp, error, caseVo.getDiff());
                }
                testCase.setTime(caseVo.getTime());
            }
            testSuite.finish(suiteVo.getTime());
        }
        String finishMessage = sessionVo.getFinishMessage();
        if (finishMessage != null) {
            testSession.printMessage(finishMessage, false);
        }
    }

}
