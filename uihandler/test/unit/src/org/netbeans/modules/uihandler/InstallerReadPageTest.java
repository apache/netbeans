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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import java.util.Locale;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerReadPageTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InstallerReadPageTest.class);
    }

    public InstallerReadPageTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        MemoryURL.initialize();
        Locale.setDefault(new Locale("te", "ST"));
        DD.d = null;
        MockServices.setServices(DD.class);

        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        // setup the listing
        installer.restored();
        Installer.dontWaitForUserInputInTests();
    }

    @Override
    protected void tearDown() throws Exception {
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.doClose();
    }

    public void testURLInCzechEncoding() throws Exception {
        doEncodingTest("iso-8859-2", "<meta http-equiv='Content-Type' content='text/html; charset=iso-8859-2'></meta>");
    }
 
    public void testURLInNoEncoding() throws Exception {
        doEncodingTest("utf-8", "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'></meta>");
    }

    public void testURLInUTF8Encoding() throws Exception {
        doEncodingTest("UTF-8", "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'></meta>");
    }

    @RandomlyFails // NB-Core-Build #7964
    public void testSendLogWithException() throws Exception {
        Logger uiLogger = Logger.getLogger(Installer.UI_LOGGER_NAME);
        LogRecord log1 = new LogRecord(Level.SEVERE, "TESTING MESSAGE");
        LogRecord log2 = new LogRecord(Level.SEVERE, "TESTING MESSAGE");
        LogRecord log3 = new LogRecord(Level.SEVERE, "NO EXCEPTION LOG");
        LogRecord log4 = new LogRecord(Level.INFO, "INFO");
        Throwable t1 = new NullPointerException("TESTING THROWABLE");
        Throwable t2 = new UnknownError("TESTING ERROR");
        log1.setThrown(t1);
        log2.setThrown(t2);
        log4.setThrown(t2);
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.restored();
        uiLogger.log(log1);
        uiLogger.log(log2);
        uiLogger.log(log3);
        UIHandler.waitFlushed();
        if (Installer.getThrown() == null) {
            fail("Exception should be found in the log");
        }
        
        doEncodingTest("UTF-8", "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'></meta>");
    }
    
    private void doEncodingTest(String encoding, String metaTag) throws Exception {
        //String kun = "Žluťoučký kůň";
        String kun = "\u017Dlu\u0165ou\u010Dky k\u016F\u0148";
        String utf8 = 
            "<html><head>" +
            metaTag +
            "</head>" +
            "<body>" +
            "<form action='http://anna.nbextras.org/analytics/upload.jsp' method='post'>" +
            "  <input name='submit' value='" + kun + "' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes(encoding));
        
        MemoryURL.registerURL("memory://kun.html", is);
        
        DialogDisplayer d = DialogDisplayer.getDefault();
        assertTrue(d instanceof DD);
        boolean res = Installer.displaySummary("KUN", true, false,true);
        assertFalse("Close options was pressed", res);
        assertNotNull("DD.d assigned", DD.d);
        
        List<Object> data = Arrays.asList(DD.d.getOptions());
        assertEquals("Two objects: " + data, 2, DD.d.getOptions().length);
        assertEquals("First is jbutton", JButton.class, DD.d.getOptions()[0].getClass());
        JButton b = (JButton)DD.d.getOptions()[0];
        
        assertEquals("It has the right localized text", kun, b.getText());

        assertFalse(EventQueue.isDispatchThread());
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JPanel jp = (JPanel) DD.d.getMessage();
                JScrollPane pane = (JScrollPane) jp.getComponent(0); //pane at idx 0
                Component c = pane.getViewport().getView();
                assertEquals("Dimension is small", new Dimension(350, 50), c.getPreferredSize());
            }
        });
        
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
}
