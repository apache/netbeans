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

package org.netbeans.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.netbeans.upgrade.AutoUpgrade;
import org.netbeans.upgrade.systemoptions.Importer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Provides utility methods
 *
 * @author Marek Slama
 */

public class Util {

    private static OptionsImportingTask task;
    private static JProgressBar progressBar;
    private static File sourceFolder;

    /** Creates a new instance of Utilities */
    private Util() {
    }

    /** Tries to set default L&F according to platform.
     * Uses:
     *   Metal L&F on Linux and Solaris
     *   Windows L&F on Windows
     *   Aqua L&F on Mac OS X
     *   System L&F on other OS
     */
    public static void setDefaultLookAndFeel () {
        String uiClassName;
        if (Utilities.isWindows()) {
            uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; //NOI18N
        } else if (Utilities.isMac()) {
            uiClassName = "apple.laf.AquaLookAndFeel"; //NOI18N
        } else if (Utilities.isUnix()) {
            uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel"; //NOI18N
        } else {
            uiClassName = UIManager.getSystemLookAndFeelClassName();
        }
        if (uiClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
            //Desired L&F is already set
            return;
        }
        try {
            UIManager.setLookAndFeel(uiClassName);
        } catch (Exception ex) {
            System.err.println("Cannot set L&F " + uiClassName); //NOI18N
            System.err.println("Exception:" + ex.getMessage()); //NOI18N
            ex.printStackTrace();
        }
    }

    /** #154031 - set NetBeans icons for license dialog.
     */
    public static void initIcons(JDialog dialog) {
            List<Image> images = new ArrayList<Image>();
            images.add(ImageUtilities.loadImage("org/netbeans/core/startup/frame.gif", true));  //NOI18N
            images.add(ImageUtilities.loadImage("org/netbeans/core/startup/frame32.gif", true));  //NOI18N
            images.add(ImageUtilities.loadImage("org/netbeans/core/startup/frame48.gif", true));  //NOI18N
            dialog.setIconImages(images);
    }

    public static JDialog createJOptionProgressDialog(JOptionPane p, String title, File source, JProgressBar bar) {
	progressBar = bar;
	sourceFolder = source;

	Object[] options = p.getOptions();
	JButton bYES = ((JButton) options[0]);
	JButton bNO = ((JButton) options[1]);	
	OptionsListener listener = new OptionsListener(p, bYES, bNO);
	bYES.addActionListener(listener);
	bNO.addActionListener(listener);
	
	return createJOptionDialog(p, title);
    }

    /** #154030 - Creates JDialog around JOptionPane. The body is copied from JOptionPane.createDialog
     * because we need APPLICATION_MODAL type of dialog on JDK6.
     */
    public static JDialog createJOptionDialog(final JOptionPane pane, String title) {
        final JDialog dialog = new JDialog(null, title, Dialog.ModalityType.APPLICATION_MODAL);
	if(sourceFolder != null) {
	    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
        Util.initIcons(dialog);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        WindowAdapter adapter = new WindowAdapter() {

            private boolean gotFocus = false;

            @Override
            public void windowClosing(WindowEvent we) {
		// Options importing might still be in progress, so do nothing
		// and let the OptionsImportingTask close the dialog when it is done.
		if(sourceFolder != null) {
		    return;
		}
                pane.setValue(null);
            }

            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    pane.selectInitialValue();
                    gotFocus = true;
                }
            }
        };
        dialog.addWindowListener(adapter);
        dialog.addWindowFocusListener(adapter);
        dialog.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent ce) {
                // reset value to ensure closing works properly
                pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });
        pane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                // Let the defaultCloseOperation handle the closing
                // if the user closed the window without selecting a button
                // (newValue = null in that case).  If the user chose the "Yes"
		// button then do the import. Otherwise, close the dialog.
                if (dialog.isVisible() && event.getSource() == pane &&
                        (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) &&
                        event.getNewValue() != null &&
                        event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
		    if (new Integer(JOptionPane.YES_OPTION).equals(pane.getValue())) {
                        // IOException from CopyFiles constructor created this error dialog
                        // most probably because netbeans.import could not be located,
                        // so discard the error dialog, stop the importing task and return
                        if(pane.getMessageType() == JOptionPane.ERROR_MESSAGE) {
                            dialog.setVisible(false);
                            task.done();
                            return;
                        }
			((JButton) pane.getOptions()[0]).setEnabled(false);
			((JButton) pane.getOptions()[1]).setEnabled(false);
			progressBar.setVisible(true);
			task = new OptionsImportingTask(dialog);
			task.execute();
		    } else {			
			dialog.setVisible(false);
		    }
                }
            }
        });
        return dialog;
    }
    
    private static class OptionsListener implements ActionListener {

	private JOptionPane pane;
	private JButton bYES;
	private JButton bNO;

	OptionsListener(JOptionPane pane, JButton bYES, JButton bNO) {
	    this.pane = pane;
	    this.bYES = bYES;
	    this.bNO = bNO;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == bYES) {
		pane.setValue(JOptionPane.YES_OPTION);
	    } else if (e.getSource() == bNO) {
		pane.setValue(JOptionPane.NO_OPTION);
	    }
	}
    }

    private static class OptionsImportingTask extends SwingWorker<Void, Void> {
	JDialog dialog;

	public OptionsImportingTask(JDialog d) {
	    dialog = d;
	}

	@Override
	public Void doInBackground() throws Exception {
	    progressBar.setString(NbBundle.getMessage (AutoUpgrade.class, "MSG_ImportingSettings"));
            AutoUpgrade.doCopyToUserDir(sourceFolder);
            //migrates SystemOptions, converts them as a Preferences
	    progressBar.setString(NbBundle.getMessage (AutoUpgrade.class, "MSG_MigratingSystemOptions"));
            Importer.doImport();
	    return null;
	}

	@Override
	public void done() {
	    sourceFolder = null;
	    dialog.setVisible(false);
	}
    }

}

