/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

