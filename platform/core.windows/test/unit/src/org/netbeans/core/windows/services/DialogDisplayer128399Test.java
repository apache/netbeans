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

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** test issue #128399: Default option (specified in DialogDescriptor) is not implemented in DialogDisplayer
 *
 * @author Jiri Rechtacek
 */
public class DialogDisplayer128399Test extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DialogDisplayer128399Test.class);
    }

    public DialogDisplayer128399Test (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    @Override
    protected int timeOut () {
        return 10000;
    }

    
    public void testDefaultOptionWithStandardOk () {
        JButton testButton = new JButton ("for-test-only");
        DialogDescriptor dd = new DialogDescriptor (
                "Hello", // innerPane
                "title", // title
                false, // isModal
                new Object[] { NotifyDescriptor.CANCEL_OPTION, NotifyDescriptor.OK_OPTION }, // options
                NotifyDescriptor.OK_OPTION, // initialValue
                DialogDescriptor.DEFAULT_ALIGN, // optionsAlign
                HelpCtx.DEFAULT_HELP, // help
                null); // action listener
        dd.setAdditionalOptions (new JButton[] {testButton});
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        //dlg.setVisible (true);
        assertEquals ("OK_OPTION is the default value.", NotifyDescriptor.OK_OPTION, dd.getDefaultValue ());
        assertEquals ("OK_OPTION is the default button on dialog",
                NbBundle.getBundle (NbPresenter.class).getString ("OK_OPTION_CAPTION"),
                testButton.getRootPane ().getDefaultButton ().getText ());
        //dlg.dispose ();
    }
    
    public void testDefaultOptionWithStandardCancel () {
        JButton testButton = new JButton ("for-test-only");
        DialogDescriptor dd = new DialogDescriptor (
                "Hello", // innerPane
                "title", // title
                false, // isModal
                new Object[] { NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION }, // options
                NotifyDescriptor.CANCEL_OPTION, // initialValue
                DialogDescriptor.DEFAULT_ALIGN, // optionsAlign
                HelpCtx.DEFAULT_HELP, // help
                null); // action listener
        dd.setAdditionalOptions (new JButton[] {testButton});
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        //dlg.setVisible (true);
        assertEquals ("CANCEL_OPTION is the default value.", NotifyDescriptor.CANCEL_OPTION, dd.getDefaultValue ());
        assertEquals ("CANCEL_OPTION is the default button on dialog",
                NbBundle.getBundle (NbPresenter.class).getString ("CANCEL_OPTION_CAPTION"),
                testButton.getRootPane ().getDefaultButton ().getText ());
        //dlg.dispose ();
    }
    
    public void testDefaultOptionWithCustomButton () {
        JButton myDefault = new JButton ("MyDefault");
        DialogDescriptor dd = new DialogDescriptor (
                "Hello", // innerPane
                "title", // title
                false, // isModal
                new Object[] {NotifyDescriptor.OK_OPTION, myDefault}, // options
                myDefault, // initialValue
                DialogDescriptor.DEFAULT_ALIGN, // optionsAlign
                HelpCtx.DEFAULT_HELP, // help
                null); // action listener
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        //dlg.setVisible (true);
        assertEquals ("MyDefault is default value.", myDefault, dd.getDefaultValue ());
        assertTrue ("MyDefault is default capable.", myDefault.isDefaultCapable ());
        assertTrue ("MyDefault is default button.", myDefault.isDefaultButton ());
        assertEquals ("MyDefault is the default button on dialog", myDefault, myDefault.getRootPane ().getDefaultButton ());
        //dlg.dispose ();
    }
    
    /** Tests issue #138024: "Yes" no longer marked as default button on Delete dialog
     * 
     */
    @RandomlyFails // NB-Core-Build Unstable: #9939, #9953
    public void testNotifyDescriptorConfirmation () {
        final JTextField testComponent = new JTextField ("for-test-only");
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation (
                testComponent,
                "testNotifyDescriptorConfirmation",
                NotifyDescriptor.YES_NO_OPTION);
        assertEquals ("YES_OPTION is the default value.", NotifyDescriptor.YES_OPTION, nd.getDefaultValue ());
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                assertEquals ("YES_OPTION is the default button on dialog",
                        NbBundle.getBundle (NbPresenter.class).getString ("YES_OPTION_CAPTION"),
                        testComponent.getRootPane ().getDefaultButton ().getText ());
                testComponent.getRootPane ().getDefaultButton ().doClick ();
            }
        }, 1000);
        DialogDisplayer.getDefault ().notify (nd);
    }
    
    //#204066
    public void testDialogDescriptorWithoutOptions () throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        JPanel panel = new JPanel( new FlowLayout() );
        final JTextField text = new JTextField( "text" );
        final JButton button1 = new JButton( "button1" );
        final JButton button2 = new JButton( "button2" );
        panel.add( text );
        panel.add( button1 );
        panel.add( button2 );
        DialogDescriptor dd = new DialogDescriptor(panel, "testDialogDescriptorWithoutOptions", true, new Object[0], null, DialogDescriptor.DEFAULT_ALIGN, null, null);
        assertNull(dd.getDefaultValue());
        final JDialog dlg = ( JDialog ) DialogDisplayer.getDefault().createDialog(dd);
        final boolean[] result = new boolean[1];
        result[0] = false;
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        dlg.getRootPane().setDefaultButton( button2 );
                        button1.requestFocusInWindow();
                        text.requestFocusInWindow();
                    }
                });
                try {
                    Thread.sleep( 1000 );
                    assertEquals( button2, dlg.getRootPane().getDefaultButton() );
                    result[0] = true;
                } catch( InterruptedException ex ) {
                    Exceptions.printStackTrace( ex );
                } finally {
                    SwingUtilities.invokeLater( new Runnable() {

                        @Override
                        public void run() {
                            dlg.setVisible( false );
                        }
                    });
                }
            }
        }, 1000);
        dlg.setVisible( true );
        assertTrue( result[0] );
    }
    
}
