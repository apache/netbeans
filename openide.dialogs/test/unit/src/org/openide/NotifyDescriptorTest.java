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
package org.openide;

import java.awt.GraphicsEnvironment;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.*;

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
}
