/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.*;
import org.openide.NotifyDescriptor.InputLine;

/** Testing issue 56878.
 * @author  Jiri Rechtacek
 */
public class NotifyDescriptorTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NotifyDescriptorTest.class);
    }

    public NotifyDescriptorTest (String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testDefaultValue () {
        JButton defaultButton = new JButton ("Default");
        JButton customButton = new JButton ("Custom action");
        JButton [] options = new JButton [] {defaultButton, customButton};
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options, defaultButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertEquals ("Test descriptor has defaultButton as defaultValue", defaultButton, dd.getValue ());
        dd.setClosingOptions (null);
        
        DialogDisplayer.getDefault().createDialog(dd);
        customButton.doClick ();
        
        assertEquals ("Test dialog closed by CustomButton", customButton, dd.getValue ());
        assertEquals ("Test dialog has the same default value as before", defaultButton, dd.getDefaultValue ());
    }

    /** Tests that clearMessages() really clears all previously set messages. */
    public void testNotificationClearMessages() {
        DialogDescriptor dd = new DialogDescriptor("Test", "Test dialog");
        NotificationLineSupport nls = dd.createNotificationLineSupport();

        String expected = "INFO";
        nls.setInformationMessage(expected);
        assertEquals("setInformationMessage doesn't work.", expected, nls.getInformationMessage());
        nls.clearMessages();
        assertNull("clearMessages doesn't work.", nls.getInformationMessage());

        expected = "WARNING";
        nls.setWarningMessage(expected);
        assertEquals("setWarningMessage doesn't work.", expected, nls.getWarningMessage());
        nls.clearMessages();
        assertNull("clearMessages doesn't work.", nls.getWarningMessage());

        expected = "ERROR";
        nls.setErrorMessage(expected);
        assertEquals("setErrorMessage doesn't work.", expected, nls.getErrorMessage());
        nls.clearMessages();
        assertNull("clearMessages doesn't work.", nls.getErrorMessage());
    }

    public void testNoDefaultClose() {
        DialogDescriptor dd = new DialogDescriptor("Test", "Test dialog");
        JDialog dlg = ( JDialog ) DialogDisplayer.getDefault().createDialog( dd );
        assertEquals( "default close operation is DISPOSE", JDialog.DISPOSE_ON_CLOSE, dlg.getDefaultCloseOperation() );

        dd.setNoDefaultClose( true );
        assertEquals( JDialog.DO_NOTHING_ON_CLOSE, dlg.getDefaultCloseOperation() );

        dd.setNoDefaultClose( false );
        assertEquals( JDialog.DISPOSE_ON_CLOSE, dlg.getDefaultCloseOperation() );
    }

    public void testInputLineInputEvents() throws BadLocationException {
        JTextField input = new JTextField();
        InputLine il = new InputLine("test", "test") {
            @Override
            JTextField createTextField() {
                return input;
            }
        };
        List<PropertyChangeEvent> events = new ArrayList<>();

        il.addPropertyChangeListener(evt -> events.add(evt));

        il.setInputText("new text 1");
        input.getDocument().insertString(0, "a", null);
        input.getDocument().remove(0, 1);

        assertEquals(events.toString(), 0, events.size());

        il.setInputTextEventEnabled(true);
        il.setInputText("new text 2");

        assertEquals(events.toString(), 1, events.size());

        events.clear();

        input.getDocument().insertString(0, "a", null);

        assertEquals(events.toString(), 1, events.size());

        events.clear();
        input.getDocument().remove(0, 1);

        assertEquals(events.toString(), 1, events.size());

        events.clear();
    }
}
