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
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;

/**
 * Simply prototype for parsing/indexing time measuring.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsIndexingPerformanceTest extends JellyTestCase {

    private static final char treeSeparator = '|';
    private String projectName = "PhpProject14";

    private static long indexingAllstubs = 0;
    private static long indexingPhpProject14 = 0;

    private static final Logger INDEXING_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final LoggerHandler LOGGING_HANDLER = new LoggerHandler();

    public JsIndexingPerformanceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        INDEXING_LOGGER.addHandler(LOGGING_HANDLER);
        INDEXING_LOGGER.setLevel(Level.INFO);
    }

    @Override
    protected void tearDown() throws Exception {
        INDEXING_LOGGER.removeHandler(LOGGING_HANDLER);
        summariseIndexingTimeResults();
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(JsIndexingPerformanceTest.class).
                clusters(".*").
                enableModules(".*").
                failOnException(Level.INFO).
                failOnMessage(Level.SEVERE).honorAutoloadEager(true);
        conf = conf.addTest("testIndexingTime");
        return NbModuleSuite.create(conf);
    }

    public void testIndexingTime() throws Exception {
        openDataProjects(projectName);
        Thread.sleep(5000);
    }

    public void summariseIndexingTimeResults() {
        System.err.println("**************************************************************************");
        System.err.println("Indexing - allstubs: sources=" + indexingAllstubs + "ms");
        System.err.println("Indexing - phpproject14: sources=" + indexingPhpProject14 + "ms");
        System.err.println("**************************************************************************");
    }

    private static class LoggerHandler extends ConsoleHandler {

        private static final String MSG_INDEXING = "Indexing of:";

        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if (message.startsWith(MSG_INDEXING)) {
                if (message.contains("allstubs.zip")) {
                    JsIndexingPerformanceTest.indexingAllstubs += getLongFromString(message);
                } else if (message.contains("PhpProject14")) {
                    JsIndexingPerformanceTest.indexingPhpProject14 += getLongFromString(message);
                }
            }
        }
    }

    private static Long getLongFromString(String message) {
        int offset = message.indexOf("took:") + 6;
        String substring = message.substring(offset);
        return Long.parseLong(substring.substring(0, substring.indexOf(" ")));
    }

}
