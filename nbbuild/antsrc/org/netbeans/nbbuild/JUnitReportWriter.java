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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility permitting Ant tasks to write out JUnit-format reports rather than aborting the build.
 */
public class JUnitReportWriter {

    private JUnitReportWriter() {}

    /**
     * Possibly write out a report.
     * @param task the Ant task doing the work
     * @param suiteName name for the reported test (defaults to class name of {@code task}
     * @param reportFile an XML file to create with the report; if null, and there were some failures,
     *                   throw a {@link BuildException} instead
     * @param pseudoTests the results of the "tests", as a map from test name (e.g. <samp>testSomething</samp>)
     *        to either null (success) or a (possibly multiline) failure message;
     *        use of a {@link java.util.LinkedHashMap} to preserve order is recommended
     * @throws BuildException in case <code>reportFile</code> was null
     *                        and <code>pseudoTests</code> contained some non-null values
     */
    public static void writeReport(Task task, String suiteName, File reportFile, Map<String,String> pseudoTests) throws BuildException {
        if (reportFile == null) {
            StringBuilder errors = new StringBuilder();
            for (Map.Entry<String,String> entry : pseudoTests.entrySet()) {
                String msg = entry.getValue();
                if (msg != null) {
                    errors.append("\n" + entry.getKey() + ": " + msg);
                }
            }
            if (errors.length() > 0) {
                throw new BuildException("Some tests failed:" + errors, task.getLocation());
            } else {
                task.log("All tests passed");
            }
        } else {
            Document reportDoc = XMLUtil.createDocument("testsuite");
            Element testsuite = reportDoc.getDocumentElement();
            int failures = 0;
            testsuite.setAttribute("errors", "0");
            testsuite.setAttribute("time", "0.0");
            String name = suiteName != null ? suiteName : task.getClass().getName();
            testsuite.setAttribute("name", name); // http://www.nabble.com/difference-in-junit-publisher-and-ant-junitreport-tf4308604.html#a12265700
            for (Map.Entry<String,String> entry : pseudoTests.entrySet()) {
                Element testcase = reportDoc.createElement("testcase");
                testsuite.appendChild(testcase);
                testcase.setAttribute("classname", name);
                testcase.setAttribute("name", entry.getKey());
                testcase.setAttribute("time", "0.0");
                String msg = entry.getValue();
                if (msg != null) {
                    failures++;
                    Element failure = reportDoc.createElement("failure");
                    testcase.appendChild(failure);
                    failure.setAttribute("type", "junit.framework.AssertionFailedError");
                    failure.setAttribute("message", msg.replaceFirst("(?s)\n.*", ""));
                    failure.appendChild(reportDoc.createTextNode(msg));
                }
            }
            testsuite.setAttribute("failures", Integer.toString(failures));
            testsuite.setAttribute("tests", Integer.toString(pseudoTests.size()));
            try {
                OutputStream os = new FileOutputStream(reportFile);
                try {
                    XMLUtil.write(reportDoc, os);
                } finally {
                    os.close();
                }
            } catch (IOException x) {
                throw new BuildException("Could not write " + reportFile + ": " + x, x, task.getLocation());
            }
            task.log(reportFile + ": " + failures + " failures out of " + pseudoTests.size() + " tests");
        }
    }

}
