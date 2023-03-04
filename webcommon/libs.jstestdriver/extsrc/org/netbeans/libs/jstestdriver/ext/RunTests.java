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
