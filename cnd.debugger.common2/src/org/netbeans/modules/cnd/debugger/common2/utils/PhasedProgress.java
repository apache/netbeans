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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


/**
 * A variation on swings ProgressMonitor which allows for multiple
 * progress bars as well as an active Cancel button.
 * Typical usage pattern:
<pre>
 //
 // start:
 //

 // levelLabels.length doubles as a specification of how many levels there are.
 String levelLabels[] = new String[] {
     "Main work",
     "Level1",
     "Level2"
 }

 String title = "Progress..."

 int cols = 40;		// how many columns wide should the text fields be

 // 'cancelListener' can start out as null and later be assigned via
 // setCancelListener() (or vice-versa)

 PhasedProgress.CancelListener cancelListener;
 cancelListener = new PhasedProgress.CancelListener() {
    public void cancelled() {
	// send an interrupt or whatever
    }
 }

 PhasedProgress phasedProgress = new PhasedProgress(title,
				                    levelLabels,
						    cancelListener,
						    cols);

 phasedProgress.setVisible(true);

 //
 // update as follows:
 // 

	phasedProgress.updateProgress(">" 1, "phase 1", 1, 10)

	    phasedProgress.updateProgress(">" 2, "doing this", 1, 100)
	    phasedProgress.updateProgress("<" 2, null,         1, 100)

	    phasedProgress.updateProgress(">" 2, "doing this", 2, 100)
	    phasedProgress.updateProgress("<" 2, null,         2, 100)

	    ... 100 more ...

	phasedProgress.updateProgress("<" 1, null,      1, 10)


	phasedProgress.updateProgress(">" 1, "phase 2", 2, 10)

	    // count or total of 0 means the time is indeterminate
	    phasedProgress.updateProgress(">" 2, "doing that", 0, 0)
	    ... some time passes ...
	    phasedProgress.updateProgress("<" 2, null,         0, 0)

	phasedProgress.updateProgress("<" 1, null,      2, 10)

	... 10 more ...

 //
 // Finish
 // Unlike ProgressMonitor reaching the end of work will not
 // automatically make the progress dialog go away; you have to bring it
 // down by hand.
 // I believe this is a more sensible approach ... 
 // - In some cases reliable estimates of the amount of work is not available.
 // - Relying on explicit bringdown will allow client to switch between 
 //   determinate and indeterminate w/o worryring about magical bringdowns.
 //
 phasedProgress.setVisible(false);
 phasedProgress.dispose()		// IMPORTANT!
 phasedProgress = null;

</pre>
 */

public class PhasedProgress implements ActionListener {

    /**
     * Interface for notifying of Cancel having been pressed.
     */
    public interface CancelListener {
	public void cancelled();
    }


    /**
     * Panel which contains individual levels' progress bars.
     */

    private class PhasedProgressPanel extends JPanel {
	final Color bgColor =
	    (Color) UIManager.getDefaults().get("Label.background");// NOI18N

	private class PhasePanel extends JPanel {
	    private String label;
	    private JTextArea msgTextArea;
	    private JProgressBar progressBar;

	    public PhasePanel(String label) {
		this.label = label;
		initComponents();
	    }

	    private void initComponents() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc;

		msgTextArea = new JTextArea(label, 1, cols);
		msgTextArea.setEditable(false);
		msgTextArea.setFocusable(true);	// make message Copyable
		msgTextArea.setLineWrap(false);
		msgTextArea.setBackground(bgColor);
		Catalog.setAccessibleName(msgTextArea,
					  "ACSN_ProgressMsg");	// NOI18N
		Catalog.setAccessibleDescription(msgTextArea,
					  "ACSD_ProgressMsg");	// NOI18N

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		add(msgTextArea, gbc);

		progressBar = new JProgressBar();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		add(progressBar, gbc);
	    }

	    public void setMessage(String message, int total) {
		msgTextArea.setText(label + message);
		if (total == 0) {
		    progressBar.setIndeterminate(true);
		} else {
		    progressBar.setIndeterminate(false);
		    progressBar.setMinimum(0);
		    progressBar.setMaximum(total);
		}
	    }

	    public void setProgress(int count) {
		// ignored if indeterminate?
		if (count == progressBar.getMaximum())
		    count = 0;
		progressBar.setValue(count);
	    }
	}

	private final int nLevels;
	private final String[] levelLabels;
	private JTextArea mainTextArea;
	private PhasePanel[] phasePanel;
	private JTextArea cancelTextArea;

	/**
	 * @param levelLabels Array of labels to be used for each level.
	 *                    Level 0 is the top-level message of the 
	 *		      component which doesn't have a progress bar.
	 */
	public PhasedProgressPanel(String[] levelLabels) {
	    this.levelLabels = levelLabels;
	    nLevels = levelLabels.length;
	    phasePanel = new PhasePanel[nLevels];
	    initComponents();
	}

	private void initComponents() {
	    setLayout(new GridBagLayout());
	    GridBagConstraints gbc;

	    // See JLF-II p66
	    // But we deviate because we're embedded in a JOptionPane.
	    final int dialogMargin = 0;     // 11;
	    final int labelSpace = 11;
	    final int titleSpace = 12;      // p74
	    final int bottomMargin = 12;

	    int gridy = 0;
	    mainTextArea = new JTextArea(levelLabels[0], 1, cols);
		mainTextArea.setEditable(false);
		mainTextArea.setFocusable(true);	// make message Copyable
		mainTextArea.setLineWrap(true);
		mainTextArea.setWrapStyleWord(true);
		mainTextArea.setBackground(bgColor);
		Catalog.setAccessibleName(mainTextArea,
					  "ACSN_ProgressMain");	// NOI18N
		Catalog.setAccessibleDescription(mainTextArea,
					  "ACSD_ProgressMain");	// NOI18N

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(dialogMargin, dialogMargin, 12, dialogMargin);
		add(mainTextArea, gbc);

	    for (int lx = 1; lx < nLevels; lx++) {
		phasePanel[lx] = new PhasePanel(levelLabels[lx]);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(dialogMargin, dialogMargin, 12, dialogMargin);
		add(phasePanel[lx], gbc);
	    }

	    cancelTextArea = new JTextArea("", 1, cols);
		cancelTextArea.setEditable(false);
		cancelTextArea.setFocusable(false);
		cancelTextArea.setLineWrap(true);
		cancelTextArea.setWrapStyleWord(true);
		cancelTextArea.setBackground(bgColor);
		cancelTextArea.setForeground(Color.RED);
		Catalog.setAccessibleName(cancelTextArea,
					  "ACSN_ProgressCancel");// NOI18N
		Catalog.setAccessibleDescription(cancelTextArea,
					  "ACSD_ProgressCancel");// NOI18N

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(dialogMargin, dialogMargin, 0, dialogMargin);
		add(cancelTextArea, gbc);
	}

	public void setCancelMsg(String cancelMsg) {
	    cancelTextArea.setText(cancelMsg);
	}

	public void setMessageFor(int level, String message, int total) {
	    if (level >= nLevels)
		return;
	    if (level == 0) 
		mainTextArea.setText(levelLabels[0] + message);
	    else
		phasePanel[level].setMessage(message, total);
	}

	public void setProgressFor(int level, int count) {
	    if (level <= 0 || level >= nLevels)
		return;
	    phasePanel[level].setProgress(count);
	}
    }

    private final Dialog dialog;		// delegate
    private final PhasedProgressPanel ppp;
    private CancelListener cancelListener;
    private final int cols;

    private String cancelMsg;



    private boolean cancelled = false;

    public PhasedProgress(String title,
			  String[] levelLabels,
			  CancelListener cancelListener,
			  int cols) {

	this.cancelListener = cancelListener;
	this.cols = cols;
	ppp = new PhasedProgressPanel(levelLabels);

	boolean modal = false;
	DialogDescriptor dlg = new DialogDescriptor(
	    ppp,
	    title,
	    modal,
	    new Object[] {
		DialogDescriptor.CANCEL_OPTION
	    },
	    DialogDescriptor.CANCEL_OPTION,
	    DialogDescriptor.BOTTOM_ALIGN,
	    null,		// HelpCtx
	    this);		// ActionListener

	dlg.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);

	// null means all options close dialog:
	// 0-sized array means no option closes dialog
	dlg.setClosingOptions(new Object[] {});

	dialog = DialogDisplayer.getDefault().createDialog(dlg);

	// title ultimatey comes from UIManager."ProgressMonitor.progressText";
	Catalog.setAccessibleDescription(dialog,
					 "ACSD_Progress");	// NOI18N

	// Can't make it undecorated because it's already "displayable"
	// The older swing ProgressMonitor was also fully decorated.
	// dialog.setUndecorated(true);

	if (dialog instanceof JDialog) {
	    // NB usually sets it to DISPOSE_ON_CLOSE
	    // Which is why we define our own dispose() to be called.
	    ((JDialog) dialog).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	dialog.addWindowListener(new WindowAdapter() {
            @Override
	    public void windowClosing(WindowEvent e) {
		// Attempt was made to close top-level dialog window
		cancel();
	    }
	} );
    }

    // interface ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == DialogDescriptor.CANCEL_OPTION) {
	    // Cancel button was pressed
	    cancel();
	}
    }


    /**
     * Pass on the cancellation request to the cancel listener if
     * one exists.
     */
    private void passCancelOn() {
	if (cancelListener != null) {
	    if (Log.Progress.debug)
		System.out.printf("PhasedProgress.passCancelOn(): doing it\n"); // NOI18N
	    cancelListener.cancelled();
	} else {
	    if (Log.Progress.debug)
		System.out.printf("PhasedProgress.passCancelOn(): no listnr\n"); // NOI18N
	}
    }

    /**
     * Called when either the Cancel button is pressed or the dialog
     * windows is attempted to be closed.
     */

    private void cancel() {
	if (Log.Progress.debug)
	    System.out.printf("PhasedProgress.cancel()\n"); // NOI18N
	cancelled = true;
	ppp.setCancelMsg(cancelMsg);
	passCancelOn();
    }

    /**
     * Set a cancel acknowledgement message. 
     * It will be rendered in red.
     */
    public void setCancelMsg(String cancelMsg) {
	this.cancelMsg = cancelMsg;
	if (cancelled) {
	    // Change it if we already have one
	    ppp.setCancelMsg(cancelMsg);
	}
    }

    /**
     * Set the message and total work units for the given level.
     */
    public void setMessageFor(int level, String message, int total) {
	ppp.setMessageFor(level, message, total);
	paintImmediately();
    }

    /**
     * Set the progress for the given level.
     */
    public void setProgressFor(int level, int count) {
	ppp.setProgressFor(level, count);
	paintImmediately();
    }


    public void setCancelListener(CancelListener newCancelListener) {
	if (Log.Progress.debug) {
	    System.out.printf("PhasedProgress.setCancelListener(%s)\n", // NOI18N
		newCancelListener);
	}
	boolean setting = false;
	if (cancelListener == null)
	    setting = true;
	cancelListener = newCancelListener;
	if (setting && isCancelled())
	    passCancelOn();
    }

    // after ProgressMonitor
    public boolean isCancelled() {
	return cancelled;
    }

    private void paintImmediately() {
	if (dialog instanceof JDialog) {
	    JDialog jdialog = (JDialog) dialog;
	    JComponent rootPane = jdialog.getRootPane();
	    final Rectangle rect = new Rectangle();
	    rootPane.getBounds(rect);
	    rect.x = 0;
	    rect.y = 0;

	    rootPane.paintImmediately(rect);
	}
    }

    // pseudo-Component
    public void setVisible(boolean v) {
	dialog.setVisible(v);

	if (v) {
	    // If we don't do this the debugger startup execution swamps
	    // the AWT event thread and the dialog icon and titles get painted
	    // _after_ the progress bars themselves! It looks real ugly.
	    paintImmediately();
	}
    }

    // pseudo-Component
    public void dispose() {
	// See setDefaultCloseOperation() above
	dialog.dispose();
    }
}
