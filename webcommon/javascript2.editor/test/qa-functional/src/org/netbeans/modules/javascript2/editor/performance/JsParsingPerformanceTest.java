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
package org.netbeans.modules.javascript2.editor.performance;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.editor.parser.JsParser;
import org.openide.filesystems.FileObject;

/**
 * Simply prototype for parsing/indexing time measuring.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsParsingPerformanceTest extends JellyTestCase {

    private static final char treeSeparator = '|';
    private String projectName = "PhpProject14";

    private static long parsingReal2k = 0;
    private static long parsingReal500 = 0;
    private static long parsingSyntheticDoc10k = 0;
    private static long parsingReal2kComment = 0;
    private static long parsingReal500Comment = 0;
    private static long parsingSyntheticDoc10kComment = 0;

    private static final Logger PARSING_LOGGER = Logger.getLogger(JsParser.class.getName());
    private static final LoggerHandler LOGGING_HANDLER = new LoggerHandler();

    public JsParsingPerformanceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        PARSING_LOGGER.addHandler(LOGGING_HANDLER);
        PARSING_LOGGER.setLevel(Level.FINE);
    }

    @Override
    protected void tearDown() throws Exception {
        PARSING_LOGGER.removeHandler(LOGGING_HANDLER);
        summariseParsingTimeResults();
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(JsParsingPerformanceTest.class).
                clusters(".*").
                enableModules(".*").
                failOnException(Level.INFO).
                failOnMessage(Level.SEVERE);
        conf = conf.addTest("testParsingTime").honorAutoloadEager(true);
        return NbModuleSuite.create(conf);
    }

    public void testParsingTime() throws Exception {
        openDataProjects(projectName);
        Thread.sleep(5000);
    }

    public void summariseParsingTimeResults() {
        System.err.println("**************************************************************************");
        System.err.println("Parsing - real2k: all=" + parsingReal2k + "ms");
        System.err.println("Parsing - real2k: code=" + (parsingReal2k - parsingReal2kComment) + "ms");
        System.err.println("Parsing - real2k: comments=" + parsingReal2kComment + "ms");
        System.err.println("Parsing - realDoc500: all=" + parsingReal500 + "ms");
        System.err.println("Parsing - realDoc500: code=" + (parsingReal500 - parsingReal500Comment) + "ms");
        System.err.println("Parsing - realDoc500: comments=" + parsingReal500Comment + "ms");
        System.err.println("Parsing - syntheticDoc10k: all=" + parsingSyntheticDoc10k + "ms");
        System.err.println("Parsing - syntheticDoc10k: code=" + (parsingSyntheticDoc10k - parsingSyntheticDoc10kComment) + "ms");
        System.err.println("Parsing - syntheticDoc10k: comments=" + parsingSyntheticDoc10kComment + "ms");
        System.err.println("**************************************************************************");
    }

    private static class LoggerHandler extends ConsoleHandler {

        private static final String MSG_PARSING_ALL = "Parsing took:";
        private static final String MSG_PARSING_DOC = "Parsing of comments took:";

        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if (message != null && message.startsWith(MSG_PARSING_ALL)) {
                message = ((String)record.getParameters()[1]);
                if (message.contains("real2k")) {
                    JsParsingPerformanceTest.parsingReal2k += getLongFromLogRecord(record);
                } else if (message.contains("real500")) {
                    JsParsingPerformanceTest.parsingReal500 += getLongFromLogRecord(record);
                } else if (message.contains("syntheticDoc10k")) {
                    JsParsingPerformanceTest.parsingSyntheticDoc10k += getLongFromLogRecord(record);
                }
            } else if (message != null && message.startsWith(MSG_PARSING_DOC)) {
                message = ((String)record.getParameters()[1]);
                if (message.contains("real2k")) {
                    JsParsingPerformanceTest.parsingReal2kComment += getLongFromLogRecord(record);
                } else if (message.contains("real500")) {
                    JsParsingPerformanceTest.parsingReal500Comment += getLongFromLogRecord(record);
                } else if (message.contains("syntheticDoc10k")) {
                    JsParsingPerformanceTest.parsingSyntheticDoc10kComment += getLongFromLogRecord(record);
                }
            }
        }
    }

    private static Long getLongFromLogRecord(LogRecord record) {
        return (Long) record.getParameters()[0];
    }
    
}
