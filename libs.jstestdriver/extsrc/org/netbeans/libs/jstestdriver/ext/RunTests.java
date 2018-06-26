/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.libs.jstestdriver.ext;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.config.DefaultConfiguration;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;
import java.io.File;
import java.util.concurrent.Semaphore;

/**
 *
 */
public class RunTests {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        assert args.length == 5;
        String serverURL = args[0];
        String baseFolder = args[1];
        String configFile = args[2];
        String tests = args[3];
        boolean strict = Boolean.parseBoolean(args[4]);
        new RunTests().runTests(serverURL, baseFolder, configFile, tests, strict);
    }
    
    private Semaphore s;
    
    public void runTests(String serverURL, String baseFolder, String configFile, String tests, boolean strict) {
        JsTestDriver testRunner = new JsTestDriverBuilder()
                .setServer(serverURL)
                .setRunnerMode(RunnerMode.QUIET)
                .addBasePaths(new File(baseFolder))
                .raiseExceptionOnTestFailure(false)
                .addTestListener(new MyListener())
                .setDefaultConfiguration(new ServerConfiguration(strict)).build();
        if ("all".equals(tests)) {
            testRunner.runAllTests(configFile);
        }
    }
    
    static class ServerConfiguration extends DefaultConfiguration {

        private boolean strictMode;

        public ServerConfiguration(boolean strictMode) {
            super(new BasePaths(new File(".")));
            this.strictMode = strictMode;
        }

        @Override
        public DocType getDocType() {
            return strictMode ? DocType.STRICT : DocType.QUIRKS;
        }
    }
    
    class MyListener implements com.google.jstestdriver.hooks.TestListener {

        public MyListener() {
        }

        @Override
        public void onTestComplete(TestResult tr) {
            StringBuilder sb = new StringBuilder();
            sb.append("nb-easel-json:{");
            sb.append("\"testCase\":\""+encode(tr.getTestCaseName())+"\"");
            sb.append(", \"testName\":\""+encode(tr.getTestName())+"\"");
            sb.append(", \"result\":\""+encode(tr.getResult().toString())+"\"");
            sb.append(", \"duration\":"+((long)tr.getTime())+"");
            sb.append(", \"browserName\":\""+encode(tr.getBrowserInfo().getName())+"\"");
            sb.append(", \"browserVersion\":\""+encode(tr.getBrowserInfo().getVersion())+"\"");
            sb.append(", \"browserOS\":\""+encode(tr.getBrowserInfo().getOs())+"\"");
            sb.append(", \"stack\":\""+encode(tr.getStack())+"\"");
            sb.append(", \"message\":\""+("[]".equals(tr.getMessage()) ? "" : encode(tr.getMessage()))+"\"");
            sb.append(", \"log\":\""+encode(tr.getLog())+"\"");
            sb.append("}");
            System.out.println(sb.toString());
        }
        
        private String encode(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("\\", "\\\\").replace("/", "\\/").
                    replace("\"", "\\\"").replace("\t", " ").
                    replace("\n", "\\u000d").replace("\r", " ").
                    replace("\b", " ").replace("\f", " ");
        }

        @Override
        public void onFileLoad(BrowserInfo bi, FileResult fr) {
        }

        @Override
        public void onTestRegistered(BrowserInfo bi, TestCase tc) {
        }

        @Override
        public void finish() {
        }

    }

}
