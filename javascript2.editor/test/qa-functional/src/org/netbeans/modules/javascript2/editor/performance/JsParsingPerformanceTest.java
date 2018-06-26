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
