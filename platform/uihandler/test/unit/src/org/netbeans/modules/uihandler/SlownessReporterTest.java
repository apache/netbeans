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
package org.netbeans.modules.uihandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;
import static org.junit.Assert.*;
/**
 *
 * @author Jindrich Sedek
 */
public class SlownessReporterTest extends NbTestCase {
    
    private static final String TEST_LOGGER = Installer.UI_LOGGER_NAME + ".test"; // NOI18N
    
    private long now;

    public SlownessReporterTest(String name) {
        super(name);
    }

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDirPath());
        UIHandler.flushImmediatelly();
        clearWorkDir();

        Installer.clearLogs();

        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.restored();

        now = System.currentTimeMillis();
        LogRecord rec = new LogRecord(Level.FINE, "UI_ACTION_EDITOR");
        Object[] params = new Object[]{null, null, null, null, "undo"};
        rec.setMillis(now - SlownessReporter.LATEST_ACTION_LIMIT/2);
        rec.setParameters(params);
        Logger.getLogger(TEST_LOGGER).log(rec);
        LogRecord rec2 = new LogRecord(Level.FINE, "UI_ACTION_EDITOR");
        params = new Object[]{null, null, null, null, "redo"};
        rec2.setMillis(now - SlownessReporter.LATEST_ACTION_LIMIT/5);
        rec2.setParameters(params);
        Logger.getLogger(TEST_LOGGER).log(rec2);
        LogRecord rec3 = new LogRecord(Level.FINE, "SOME OTHER LOG");
        params = new Object[]{null, null, null, null, "redo"};
        rec3.setMillis(now - SlownessReporter.LATEST_ACTION_LIMIT/10);
        rec3.setParameters(params);
        Logger.getLogger(TEST_LOGGER).log(rec3);
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        Installer installer = Installer.findObject(Installer.class, true);
        installer.uninstalled();
    }

    @Test
    public void testGetLatestAction() {
        SlownessReporter reporter = new SlownessReporter();
        String latestAction = reporter.getLatestAction(10L, now);
        assertEquals("redo", latestAction);
    }

    @Test
    public void testIgnoreOldActions() throws InterruptedException {
        SlownessReporter reporter = new SlownessReporter();
        String latestAction = reporter.getLatestAction(10L, now + SlownessReporter.LATEST_ACTION_LIMIT * 2);
        assertNull(latestAction);
    }

    @Test
    public void testGetIdeStartup() {
        SlownessReporter reporter = new SlownessReporter();
        Logger.getLogger(TEST_LOGGER).log(new LogRecord(Level.CONFIG, Installer.IDE_STARTUP));
        String latestAction = reporter.getLatestAction(100L, now);
        assertNotNull(latestAction);
        assertEquals(NbBundle.getMessage(SlownessReporter.class, "IDE_STARTUP"), latestAction);
    }
}

