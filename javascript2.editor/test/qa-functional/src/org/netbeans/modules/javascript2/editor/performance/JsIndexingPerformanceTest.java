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
