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
