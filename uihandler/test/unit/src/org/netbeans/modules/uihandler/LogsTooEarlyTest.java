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

import java.awt.Dialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import junit.framework.*;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class LogsTooEarlyTest extends NbTestCase {
    static {
        Logger.getLogger("").addHandler(new Delegating());
    }
    
    private Installer installer;
    
    public LogsTooEarlyTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        UIHandler.flushImmediatelly();
        clearWorkDir();
        
        Installer.clearLogs();
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

    }

    @Override
    protected void tearDown() throws Exception {
        assertNotNull(installer);
        installer.doClose();
    }

    public void testLogsReceivedOnStartup() throws Exception {
        Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything").warning("Ahoj");

        installer.restored();
        
        assertEquals("one logger received", 1, InstallerTest.getLogsSize());
    }
 
    /** This simulates the standard core's handler
     */
    private static final class Delegating extends Handler {

        @Override
        public void publish(LogRecord record) {
            for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
                h.publish(record);
            }
        }

        @Override
        public void flush() {
            for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
                h.flush();
            }
        }

        @Override
        public void close() throws SecurityException {
            for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
                h.close();
            }
        }
        
    }
}
