/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2009-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.api;

import java.io.File;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author answer
 */
//suite/test/class
public class TestNGTestSuite extends TestSuite {

    private final TestSession session;
    private FileObject suiteFO = null;
    private long elapsedTime = 0;
    private int expectedTestCases;
    private FileObject cfgFO;

    public TestNGTestSuite(String tcClassName, TestSession testSession) {
        super(tcClassName);
        this.session = testSession;
    }

    public TestNGTestSuite(String name, TestSession session, int expectedTCases, String configFile) {
        super(name);
        this.session = session;
        expectedTestCases = expectedTCases;
        cfgFO = configFile.equals("null") ? null : FileUtil.toFileObject(FileUtil.normalizeFile(new File(configFile)));
    }

    public FileObject getSuiteFO() {
        return cfgFO;
//        if (suiteFO == null) {
//            FileLocator locator = session.getFileLocator();
//            if (locator != null) {
//                suiteFO = locator.find(getName().replace('.', '/') + ".java"); //NOI18N
//            }
//        }
//        return suiteFO;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void finish(int run, int fail, int skip, int confFail, int confSkip) {
        //not needed?
        //TODO: update tcases with proper status
    }

    public TestNGTestcase getTestCase(String testCase, String parameters) {
        for (Testcase tc: getTestcases()) {
            if (tc.getName().equals(parameters != null ? testCase + "(" + parameters + ")" : testCase)) {
                return (TestNGTestcase) tc;
            }
        }
        return null;
    }

}
