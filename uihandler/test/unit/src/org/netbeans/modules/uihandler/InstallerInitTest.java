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
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerInitTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InstallerInitTest.class);
    }

    private Installer installer;
    
    static {
        MemoryURL.initialize();
    }
    
    public InstallerInitTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        UIHandler.flushImmediatelly();
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        Installer.dontWaitForUserInputInTests();
        DD.d = null;
        MockServices.setServices(DD.class);

        // setup the listing
        installer.restored();

        assertNull("No dialog as there are no records", DD.d);
        
        Locale.setDefault(new Locale("in", "IT"));
        
    }

    @Override
    protected void tearDown() throws Exception {
        assertNotNull(installer);
        installer.doClose();
    }

    public void testGenerateEnoughLogsExit() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
        r.setLoggerName(Installer.UI_LOGGER_NAME + ".anything");

        String utf8 = 
            "<html><head>" +
            "</head>" +
            "<body>" +
            "<form action='http://anna.nbextras.org/analytics/upload.jsp' method='post'>" +
            "  <input name='submit' value='&amp;Fill Survey' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes("utf-8"));
        
        MemoryURL.registerURL("memory://start.html", is);
        
        for (int i = 0; i < 1500; i++) {
            Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything").log(r);
        }
        assertEquals("full buffer", 1000, InstallerTest.getLogsSize());
        
        assertNull("No dialogs so far", DD.d);
        
        installer.doClose();
        waitForGestures();
        
        assertNull("No dialogs at close", DD.d);

        Preferences prefs = NbPreferences.forModule(Installer.class);
        prefs.putInt("count", UIHandler.MAX_LOGS );
        
        installer.restored();
        
        waitForGestures();

//        assertNotNull("A dialog shown at begining", DD.d);
    }

    public void testDontSubmitTwiceIssue128306(){
        final AtomicBoolean submittingTwiceStopped = new AtomicBoolean(false);
        final Installer.SubmitInteractive interactive = new Installer.SubmitInteractive("hallo", true);
        final ActionEvent evt = new ActionEvent("submit", 1, "submit");
        Installer.LOG.setLevel(Level.FINEST);
        Installer.LOG.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                if ("posting upload UIGESTURES".equals(record.getMessage())){
                    interactive.actionPerformed(evt);
                }
                if ("ALREADY SUBMITTING".equals(record.getMessage())){
                    submittingTwiceStopped.set(true);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        interactive.createDialog();
        interactive.actionPerformed(evt);
        Installer.RP_SUBMIT.post(new Runnable() {

            public void run() {
                // block RP executor
                synchronized(this){
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        Installer.RP_SUBMIT.post(new Runnable() {

            public void run() {
                // just wait for processing
            }
        }).waitFinished();
        assertTrue(submittingTwiceStopped.get());
    }

    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor d;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            return NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            
            return new DialogImpl(d, new Frame());
        }

        private static class DialogImpl extends Dialog 
        implements PropertyChangeListener {
            NotifyDescriptor d;
            
            private DialogImpl(NotifyDescriptor d, Frame owner) {
                super(owner);
                this.d = d;
            }

            @java.lang.Override
            public synchronized void setVisible(boolean b) {
                assertFalse(isModal());
            }

            public synchronized void propertyChange(PropertyChangeEvent evt) {
                if (d != null && d.getOptions().length == 2) {
                    d.setValue(NotifyDescriptor.CLOSED_OPTION);
                    d = null;
                    notifyAll();
                }
            }
        }
        
    }

    private void waitForGestures() throws InterruptedException, InvocationTargetException {
        class RunnableImpl implements Runnable {
            
            private RunnableImpl() {
            }
            
            public void run() {
            }
        }
        SwingUtilities.invokeAndWait(new RunnableImpl());
        Installer.RP.post(new RunnableImpl()).waitFinished();
    }

}
