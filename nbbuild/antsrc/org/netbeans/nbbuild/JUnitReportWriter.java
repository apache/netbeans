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
                try (OutputStream os = new FileOutputStream(reportFile)) {
                    XMLUtil.write(reportDoc, os);
                }
            } catch (IOException x) {
                throw new BuildException("Could not write " + reportFile + ": " + x, x, task.getLocation());
            }
            task.log(reportFile + ": " + failures + " failures out of " + pseudoTests.size() + " tests");
        }
    }

}
