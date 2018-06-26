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
package org.netbeans.modules.php.project.ui.testrunner;

import java.util.Collections;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

public class TestCaseImpl implements TestCase {

    private final TestSuiteImpl testSuite;
    private final Testcase testCase;


    TestCaseImpl(TestSuiteImpl testSuite, Testcase testCase) {
        assert testSuite != null;
        assert testCase != null;
        this.testSuite = testSuite;
        this.testCase = testCase;
    }

    @Override
    public void setClassName(String className) {
        Parameters.notWhitespace("className", className); // NOI18N
        testSuite.checkFrozen();
        testCase.setClassName(className);
    }

    @Override
    public void setLocation(Locations.Line location) {
        Parameters.notNull("location", location); // NOI18N
        testSuite.checkFrozen();
        testCase.setLocation(FileUtil.toFile(location.getFile()).getAbsolutePath());
    }

    @Override
    public void setTime(long time) {
        testSuite.checkFrozen();
        testCase.setTimeMillis(time);
        testSuite.updateReport(time, false);
    }

    @Override
    public void setStatus(Status status) {
        Parameters.notNull("status", status); // NOI18N
        testSuite.checkFrozen();
        testCase.setStatus(map(status));
    }

    @Override
    public void setFailureInfo(String message, String[] stackTrace, boolean error, Diff diff) {
        Parameters.notNull("message", message); // NOI18N
        Parameters.notNull("stackTrace", stackTrace); // NOI18N
        Parameters.notNull("diff", diff); // NOI18N
        testSuite.checkFrozen();
        Trouble trouble = new Trouble(error);
        trouble.setStackTrace(createStackTrace(message, stackTrace));
        if (diff.isValid()) {
            Trouble.ComparisonFailure failure = new Trouble.ComparisonFailure(diff.getExpected(), diff.getActual());
            trouble.setComparisonFailure(failure);
        }
        testCase.setTrouble(trouble);
        Manager manager = testSuite.getTestSession().getManager();
        TestSession session = testSuite.getTestSession().getTestSession();
        manager.displayOutput(session, getClassName() + "::"  + testCase.getName() + "()", error); // NOI18N
        manager.displayOutput(session, message, error);
        testCase.addOutputLines(Collections.singletonList("<u>" + testCase.getName() + ":</u>")); // NOI18N
        for (String s : stackTrace) {
            manager.displayOutput(session, s, error);
            testCase.addOutputLines(Collections.singletonList(s.replace("<", "&lt;"))); // NOI18N
        }
        manager.displayOutput(session, "", false); // NOI18N
    }

    private String[] createStackTrace(String message, String[] stackTrace) {
        String[] tmp = new String[stackTrace.length + 1];
        tmp[0] = message;
        System.arraycopy(stackTrace, 0, tmp, 1, stackTrace.length);
        return tmp;
    }

    private String getClassName() {
        String className = testCase.getClassName();
        if (className != null) {
            return className;
        }
        className = testSuite.getTestSuite().getName();
        assert className != null;
        return className;
    }

    //~ Mappers

    private org.netbeans.modules.gsf.testrunner.api.Status map(TestCase.Status status) {
        return org.netbeans.modules.gsf.testrunner.api.Status.valueOf(status.name());
    }

}
