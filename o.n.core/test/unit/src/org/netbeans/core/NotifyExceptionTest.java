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

package org.netbeans.core;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.junit.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import org.openide.windows.WindowManager;

/**
 * Test NotifyExcPanel class.
 * 
 * @author Stanislav Aubrecht
 */
public class NotifyExceptionTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NotifyExceptionTest.class);
    }

    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    public NotifyExceptionTest(String name) {
        super(name);
    }

    private static void waitEQ() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        // initialize the logging
        new TopLogging();

        DD.lastDescriptor = null;
        DD.toReturn = null;

        System.getProperties().remove("netbeans.exception.alert.min.level");
        System.getProperties().remove("netbeans.exception.report.min.level");

        NotifyExcPanel.cleanInstance();
    }
    
    /**
     * A simple test to ensure that error dialog window is not created modal
     * until the MainWindow is visible.
     */
    public void testNoModalErrorDialog() throws Exception {
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog modalDialog = new HiddenDialog( mainWindow, true );
        DD.toReturn = modalDialog;

        Logger.global.log(Level.WARNING, "Something is wrong", new NullPointerException("npe"));
        waitEQ();
        assertNotNull("Really returned", DD.lastDescriptor);
        assertEquals("It is DialogDescriptor", DialogDescriptor.class, DD.lastDescriptor.getClass());
        DialogDescriptor dd = (DialogDescriptor)DD.lastDescriptor;
        assertFalse( "The request is for non-modal dialog", dd.isModal());
        assertFalse("Main window is not visible", mainWindow.isVisible());
    }

    public void testExceptionWillGetTheLevelFromAnnoatation() throws Exception {
        NullPointerException npe = new NullPointerException("npe");
        ErrorManager.getDefault().annotate(npe, ErrorManager.WARNING, null, null, null, null);

        DD.toReturn = new HiddenDialog();
        Exceptions.printStackTrace(npe);

        waitEQ();
        assertNotNull("We are going to display a warning", DD.lastDescriptor);

    }

    public void testDirectlyLoggingAnExceptionWithALocalizedMessageAndTheRightLevelShowsItInADialog() throws Exception {
        NullPointerException npe = new NullPointerException("npe");

        LogRecord rec = new LogRecord(OwnLevel.UI, "MSG_KEY");
        rec.setThrown(npe);
        ResourceBundle b = ResourceBundle.getBundle("org/netbeans/core/NotifyExceptionBundle");
        rec.setResourceBundle(b);
        DD.toReturn = new HiddenDialog();
        Logger.global.log(rec);
        waitEQ();
        assertNotNull("We are going to display a warning", DD.lastDescriptor);
        assertTrue("We want message: " + DD.lastDescriptor, DD.lastDescriptor instanceof NotifyDescriptor.Message);
        NotifyDescriptor.Message msg = (NotifyDescriptor.Message)DD.lastDescriptor;
        assertEquals("Info msg", NotifyDescriptor.INFORMATION_MESSAGE, msg.getMessageType());
        assertEquals("Msg is localized", b.getString("MSG_KEY"), msg.getMessage());
    }

    public void testYesDialogShown() throws Exception {
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog modalDialog = new HiddenDialog( mainWindow, true );
        DD.toReturn = modalDialog;

        Logger l = Logger.getLogger(getName());
        l.setLevel(Level.ALL);
        System.setProperty("netbeans.exception.report.min.level", "200");
        l.log(Level.CONFIG, "Something is wrong", new NullPointerException("npe"));
        waitEQ();
        assertNotNull("Really returned", DD.lastDescriptor);
        assertEquals("It is DialogDescriptor", DialogDescriptor.class, DD.lastDescriptor.getClass());
        DialogDescriptor dd = (DialogDescriptor)DD.lastDescriptor;
        assertFalse( "The request is for non-modal dialog", dd.isModal());
        assertFalse("Main window is not visible", mainWindow.isVisible());
    }
    public void testNoDialogShownJustFlashing() throws Exception {
        NotifyExcPanel.ExceptionFlasher.flash = null;

        Logger l = Logger.getLogger(getName());
        l.setLevel(Level.ALL);
        System.setProperty("netbeans.exception.alert.min.level", "200");
        l.log(Level.CONFIG, "Something is wrong", new NullPointerException("npe"));
        waitEQ();
        assertNull("Really returned", DD.lastDescriptor);

        assertNotNull("Notification displayed", NotifyExcPanel.ExceptionFlasher.flash);
    }

    private static final class OwnLevel extends Level {
        public static final Level UI = new OwnLevel("UI", 1973);

        private OwnLevel(String n, int i) {
            super(n, i);
        }
    }

    private static final class DD extends DialogDisplayer {
        public static NotifyDescriptor lastDescriptor;
        public static Object toReturn;

        public Object notify(NotifyDescriptor descriptor) {
            Object t = toReturn;
            toReturn = null;
            assertNotNull("There is something to return", t);
            lastDescriptor = descriptor;
            return t;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            return (Dialog)notify(descriptor);
        }
    } // end of DD

    public static final class Lkp extends AbstractLookup {
        public static InstanceContent IC;

        public Lkp() {
            this(new InstanceContent());
        }
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new DD());
            ic.add(new NbErrorManager());
        }
    }

    private static final class HiddenDialog extends JDialog {
        private boolean v;

        public HiddenDialog() {
        }

        public HiddenDialog(Frame p, boolean b) {
            super(p, b);
        }

        @Override
        public void setVisible(boolean b) {
            v = b;
        }
        @Override
        public boolean isVisible() {
            return v;
        }
    }
}
