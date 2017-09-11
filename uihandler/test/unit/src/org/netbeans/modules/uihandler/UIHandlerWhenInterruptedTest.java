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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.uihandler;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandlerWhenInterruptedTest extends NbTestCase {
    private static Logger UILOG = Logger.getLogger(Installer.UI_LOGGER_NAME + ".actions");

    
    public UIHandlerWhenInterruptedTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        assertNotNull("Installer created", o);
        o.restored();
    }

    protected void tearDown() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        o.uninstalled();
    }

    public void testPublishWhenInterupted() {
        
        for (int i = 0; i < 800; i++) {
            LogRecord rec2 = new LogRecord(Level.FINER, "" + i); // NOI18N
            Thread.currentThread().interrupt();
            UILOG.log(rec2);        
        }

        int cnt = 50;
        while (cnt-- > 0 && Installer.getLogsSize() < 800) {
            // ok, repeat
        }
        List<LogRecord> logs = InstallerTest.getLogs();
        assertEquals("One log: " + logs, 800, logs.size());
        
        for (int i = 1; i < 800; i++) {
            assertEquals("" + i, logs.get(i).getMessage());
        }
        
    }
    
    private static final class MyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
