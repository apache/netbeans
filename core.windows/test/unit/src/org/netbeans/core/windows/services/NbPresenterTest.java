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

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.DialogDescriptor;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

/** Tests issue 56534.
 *
 * @author Jiri Rechtacek
 */
public class NbPresenterTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NbPresenterTest.class);
    }

    public NbPresenterTest (String testName) {
        super (testName);
    }

    protected boolean runInEQ () {
        return true;
    }

    public void testDialogsOptionsOnDefaultSystem () {
        System.setProperty ("xtest.looks_as_mac", "false");
        doTestDialogsOptions ();
    }
    
    public void testDialogsOptionsOnMac () {
        System.setProperty ("xtest.looks_as_mac", "true");
        doTestDialogsOptions ();
    }
    
    private void doTestDialogsOptions () {
        boolean modal = false;
        //boolean modal = true;
        JButton erase = new JButton ("Erase all my data");
        JButton rescue = new JButton ("Rescue");
        JButton cancel = new JButton ("Cancel");
        JButton [] options = new JButton [] {erase, rescue, cancel};
        DialogDescriptor dd = new DialogDescriptor (new JLabel ("Something interesting"), "My dialog", modal,
                // options
                options,
                rescue,
                // align
                DialogDescriptor.RIGHT_ALIGN,
                new HelpCtx (NbPresenterTest.class), null);
        

        dd.setClosingOptions (new Object[0]);
                
        NbPresenter presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);
        
        erase.doClick ();
        assertEquals ("Erase was invoked.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        erase.doClick ();
        assertEquals ("Erase was invoked again on same dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        erase.doClick ();
        assertEquals ("Erase was invoked of reused dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        erase.doClick ();
        assertEquals ("Erase was invoked again on reused dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        rescue.doClick ();
        assertEquals ("Rescue was invoked of reused dialog.", rescue.getText (), ((JButton)dd.getValue ()).getText ());
        rescue.doClick ();
        assertEquals ("Rescue was invoked again on reused dialog.", rescue.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();
        
        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        cancel.doClick ();
        assertEquals ("Cancel was invoked of reused dialog.", cancel.getText (), ((JButton)dd.getValue ()).getText ());
        cancel.doClick ();
        assertEquals ("Cancel was invoked again on reused dialog.", cancel.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();
    }
    
    public void testNbPresenterComparator () {
        JButton erase = new JButton ("Erase all my data");
        JButton rescue = new JButton ("Rescue");
        JButton cancel = new JButton ("Cancel");
        JButton [] options = new JButton [] {erase, rescue, cancel};
        DialogDescriptor dd = new DialogDescriptor (new JLabel ("Something interesting"), "My dialog", false,
                // options
                options,
                rescue,
                // align
                DialogDescriptor.RIGHT_ALIGN,
                null, null);
                
        dd.setClosingOptions (null);
                
        NbPresenter presenter = new NbDialog (dd, (JFrame)null);
        assertEquals ("Dialog has Rescue option as default value.", rescue, dd.getDefaultValue ());
        JButton [] backup = (JButton [])options.clone ();
        //showButtonArray (backup);
        Arrays.sort (options, presenter);
        //showButtonArray (options);
        JButton [] onceSorted = (JButton [])options.clone ();
        Arrays.sort (options, presenter);
        //showButtonArray (options);
        JButton [] twiceSorted = (JButton [])options.clone ();
        assertFalse ("Original options not same as sorted option.", Arrays.asList (backup).equals (Arrays.asList (onceSorted)));
        assertEquals ("Sorting of options is invariable.", Arrays.asList (onceSorted), Arrays.asList (twiceSorted));
        presenter.setVisible (true);
        erase.doClick ();
        assertEquals ("Dialog has been close by Erase option", erase, dd.getValue ());

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        options = (JButton [])backup.clone ();
        //showButtonArray (backup);
        Arrays.sort (options, presenter);
        JButton [] onceSorted2 = (JButton [])options.clone ();
        //showButtonArray (onceSorted2);
        Arrays.sort (options, presenter);
        JButton [] twiceSorted2 = (JButton [])options.clone ();
        //showButtonArray (twiceSorted2);
        assertFalse ("Original options not same as sorted option on reused dialog.", Arrays.asList (backup).equals (Arrays.asList (onceSorted2)));
        assertEquals ("Sorting of options is invariable also on reused dialog.", Arrays.asList (onceSorted2), Arrays.asList (twiceSorted2));
        assertEquals ("The options are sorted same on both dialogs.", Arrays.asList (onceSorted), Arrays.asList (twiceSorted2));
        
    }

    public void testIsDefaultOptionPane() {
        NotifyDescriptor descriptor = new NotifyDescriptor( "string message", "test", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        NbPresenter presenter = new NbPresenter( descriptor, (Dialog)null, true );
        assertTrue( Boolean.TRUE.equals(presenter.getRootPane().getClientProperty( "nb.default.option.pane")) );

        descriptor = new NotifyDescriptor( new JLabel("custom component message"), "test", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        presenter = new NbPresenter( descriptor, (Dialog)null, true );
        assertTrue( Boolean.FALSE.equals(presenter.getRootPane().getClientProperty( "nb.default.option.pane")) );
    }
    
    private void showButtonArray (Object [] array) {
        JButton [] arr = (JButton []) array;
        System.out.print("do: ");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i].getText() + ", ");
        }
        System.out.println(".");
    }
    

    public void testNoDefaultClose() {
        DialogDescriptor dd = new DialogDescriptor("Test", "Test dialog");
        NbPresenter dlg = new NbPresenter( dd, (Dialog)null, true );
        assertEquals( "default close operation is DISPOSE", JDialog.DISPOSE_ON_CLOSE, dlg.getDefaultCloseOperation() );

        dd.setNoDefaultClose( true );
        assertEquals( JDialog.DO_NOTHING_ON_CLOSE, dlg.getDefaultCloseOperation() );

        dd.setNoDefaultClose( false );
        assertEquals( JDialog.DISPOSE_ON_CLOSE, dlg.getDefaultCloseOperation() );
}
}
