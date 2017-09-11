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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/** Tests implementation #148730: Add helper class to simplify dealing with error/warning/info messages in dialogs
 *
 * @author Jiri Rechtacek
 */
public class NotificationLineSupportTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NotificationLineSupportTest.class);
    }

    private JButton closeButton = new JButton ("Close action");
    private JButton [] options = new JButton [] {closeButton};
    private static String NOTIFICATION_LABEL_NAME = "FixedHeightLabel";

    public NotificationLineSupportTest() {
        super("NotificationLineSupportTest");
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testAppendNotificationLine () {
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options,
                closeButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertNull ("No NotificationLineSupport created.", dd.getNotificationLineSupport ());
        NotificationLineSupport supp = dd.createNotificationLineSupport ();
        assertNotNull ("NotificationLineSupport is created.", dd.getNotificationLineSupport ());

        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);

        JLabel notificationLabel = findNotificationLabel (d);
        assertNotNull (notificationLabel);

        assertNotNull ("NotificationLineSupport not null", supp);
        testSetInformationMessage (supp, "Hello");
        assertEquals ("Hello", notificationLabel.getText ());
        testSetWarningMessage (supp, "Hello");
        assertEquals ("Hello", notificationLabel.getText ());
        testSetErrorMessage (supp, "Hello");
        assertEquals ("Hello", notificationLabel.getText ());
        testEmpty (supp);
        assertEquals (null, notificationLabel.getText ());
        closeButton.doClick ();
    }

    public void testSetMessageBeforeCreateDialog () {
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options,
                closeButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertNull ("No NotificationLineSupport created.", dd.getNotificationLineSupport ());
        NotificationLineSupport supp = dd.createNotificationLineSupport ();
        assertNotNull ("NotificationLineSupport is created.", dd.getNotificationLineSupport ());

        assertNotNull ("NotificationLineSupport not null", supp);
        testSetInformationMessage (supp, "Hello");

        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);

        JLabel notificationLabel = findNotificationLabel (d);
        assertNotNull (notificationLabel);

        assertEquals ("Hello", notificationLabel.getText ());
        closeButton.doClick ();
    }

    public void testSetMessageAfterCreateDialog () {
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options,
                closeButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertNull ("No NotificationLineSupport created.", dd.getNotificationLineSupport ());
        NotificationLineSupport supp = dd.createNotificationLineSupport ();
        assertNotNull ("NotificationLineSupport is created.", dd.getNotificationLineSupport ());

        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);

        assertNotNull ("NotificationLineSupport not null", supp);
        testSetInformationMessage (supp, "Hello");

        JLabel notificationLabel = findNotificationLabel (d);
        assertNotNull (notificationLabel);

        assertEquals ("Hello", notificationLabel.getText ());
        closeButton.doClick ();
    }

    private void testSetInformationMessage (NotificationLineSupport supp, String msg) {
        supp.setInformationMessage (msg);
    }

    private void testSetWarningMessage (NotificationLineSupport supp, String msg) {
        supp.setWarningMessage (msg);
    }

    private void testSetErrorMessage (NotificationLineSupport supp, String msg) {
        supp.setErrorMessage (msg);
    }

    private void testEmpty (NotificationLineSupport supp) {
        supp.clearMessages ();
    }

    public void testNonAppendNotificationLine () {
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options,
                closeButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertNull ("No NotificationLineSupport created.", dd.getNotificationLineSupport ());

        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);
        try {
            // !! It's package-private
            dd.setInformationMessage ("Hello");
            fail ();
        } catch (IllegalStateException x) {
            // must be throw because no NotificationLineSupport created
        }
        closeButton.doClick ();
    }

    private static JLabel findNotificationLabel (Container container) {
        for (Component component : container.getComponents ()) {
            if (component.getClass ().getName ().indexOf (NOTIFICATION_LABEL_NAME) != -1) {
                return (JLabel) component;
            }
            if (component instanceof JRootPane) {
                JRootPane rp = (JRootPane) component;
                return findNotificationLabel (rp.getContentPane ());
            }
            if (component instanceof JPanel) {
                JPanel p = (JPanel) component;
                return findNotificationLabel (p);
            }
        }
        return null;
    }
}
