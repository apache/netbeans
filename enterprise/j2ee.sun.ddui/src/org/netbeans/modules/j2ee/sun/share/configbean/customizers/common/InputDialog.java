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
/*
 * InputDialog.java
 *
 * Created on October 4, 2003, 7:34 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;


/* A modal dialog object with Ok, Cancel, Help buttons and an optional required
 * field notation.
 *
 * This object supports inline error message reporting.
 *
 * @author  Rajeshwar Patil
 * Enhancements by Peter Williams
 * @version %I%, %G%
 */
public abstract class InputDialog extends JDialog implements HelpCtx.Provider {

    /** Represents clicking on the Cancel button or closing the dialog 
     */
    public static final int CANCEL_OPTION = 0;

    /** Represents clicking on the OK button 
     */
    public static final int OK_OPTION = 1;

    /** Represents clicking on the HELP button 
     */
    public static final int HELP_OPTION = 2;

    private final ResourceBundle bundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); //NOI18N


    private int chosenOption;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton helpButton;
    private JPanel messagePanel;
    private List errorList;
    private List warningList;

    /** Creates a new instance of modal InputDialog
     *  @param panel the panel from this dialog is opened
     *  @param title title for the dialog
     */
    public InputDialog(JPanel panel, String title) {
        this(panel, title, false, false);
    }

    /** Creates a new instance of modal InputDialog
     *  @param panel the panel from this dialog is opened
     *  @param title title for the dialog
     *  @param showRequiredNote set this if you want a '* denotes required field'
     *   message in the lower left hand corner.
     */
    public InputDialog(JPanel panel, String title, boolean showRequiredNote) {
        this(panel, title, showRequiredNote, false);
    }

    /** Creates a new instance of modal InputDialog
     *  @param panel the panel from this dialog is opened
     *  @param title title for the dialog
     *  @param showRequiredNote set this if you want a '* denotes required field'
     *   message in the lower left hand corner.
     *  @param resizeMsg set this to true if you want the error message panel to
     *   resize vertically if the user expands the dialog.  Useful for dialogs that
     *   can have multiple warnings.
     */
    public InputDialog(JPanel panel, String title, boolean showRequiredNote, boolean resizeMsg) {
        super(getFrame(panel), title, true);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                chosenOption = CANCEL_OPTION;
            }
        });

        Object buttonPanelConstraints;

        if(!resizeMsg) {
            // default to borderlayout...
            getContentPane().setLayout(new BorderLayout());
            buttonPanelConstraints = BorderLayout.SOUTH;
        } else {
            // for resizing button panels, we need gridbag.
            getContentPane().setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.SOUTH;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            buttonPanelConstraints = constraints;
        }

        // Create button panel -- using gridbaglayout now due to possible 
        // message label on left hand side (in addition to buttons on right
        // hand side) and error text area above the buttons.
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        messagePanel = new JPanel();
        messagePanel.setLayout(new GridBagLayout());
        messagePanel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ErrorTextArea")); // NOI18N
        messagePanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ErrorTextArea")); // NOI18N

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(6,12,5,11);
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(messagePanel, gridBagConstraints);

        if(showRequiredNote) {
            JLabel requiredNote = new JLabel();
            requiredNote.setText(bundle.getString("LBL_RequiredMessage")); // NOI18N
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new Insets(6,12,11,5);
            gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
            buttonPanel.add(requiredNote, gridBagConstraints);
        }

        okButton = new JButton(bundle.getString("LBL_OK")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(6,6,11,5);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                chosenOption = OK_OPTION;
                actionOk();
            }
        });
        okButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_OK"));	// NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_OK"));	// NOI18N
        buttonPanel.add(okButton, gridBagConstraints);

        JButton cancelButton = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(6,0,11,5);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                chosenOption = CANCEL_OPTION;
                actionCancel();
            }
        });
        cancelButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Cancel"));	// NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Cancel"));	// NOI18N
        buttonPanel.add(cancelButton, gridBagConstraints);

		helpButton = new JButton();
        Mnemonics.setLocalizedText(helpButton, bundle.getString("LBL_Help")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new Insets(6,0,11,11);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        helpButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                chosenOption = HELP_OPTION;
                actionHelp();
            }
        });
        helpButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Help"));	// NOI18N
        helpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Help"));	// NOI18N
        buttonPanel.add(helpButton, gridBagConstraints);

        getContentPane().add(buttonPanel, buttonPanelConstraints);
        getRootPane().setDefaultButton(okButton);
    }


    /** Displays the dialog.
     *  @return identifies whether the OK button or Cancel button was selected
     */        
    public int display() {
        this.setVisible(true);
        return chosenOption;
    }

    protected void actionOk() {
        super.dispose();
    }

    protected void actionCancel() {
        super.dispose();
    }

    protected void actionHelp() {
        Utils.invokeHelp(getHelpId());
    }

    protected abstract String getHelpId();

    /** Seeks for the nearest Frame containg this component.
     */
    public static Frame getFrame(Component component) {
        while (!(component instanceof Frame)){
            component = component.getParent();
        }
        return ((Frame) component);
    }


    /** Enables to place this dialog in the middle of the given panel.
     *  @param panel where the dialog should be placed
     */
    protected void setLocationInside(JPanel panel) {
        java.awt.Rectangle rect = this.getBounds();
        int width = rect.width;
        int height = rect.height;
        java.awt.Rectangle panelRect = panel.getBounds();
        if (width > panelRect.width || height > panelRect.height) {
            setLocationRelativeTo(panel);
        } else {
            java.awt.Point location = panel.getLocationOnScreen();
            setLocation(location.x + (panelRect.width - width) / 2, 
                location.y + (panelRect.height - height) / 2);
        }
    }

    /** Simple enable/disable mechanism for the ok button - this should be improved
     *  to allow specification of which button, or a range of buttons, where the
     *  buttons are specified by enums (using typesafe enum pattern).
     */
    protected void setOkEnabled(boolean flag) {
        okButton.setEnabled(flag);
    }

    /** Shows the errors at the top of dialog panel.
     *  Set focus to the focused component.
     */    
    public void showErrors() {
        boolean hasErrors = false;

        // clear existing errors first.
        messagePanel.removeAll();

        if(warningList != null && warningList.size() > 0) {
            for(Iterator iter = warningList.iterator(); iter.hasNext();) {
                String message = iter.next().toString();

                // Add warning message
                JLabel label = new JLabel();
                label.setIcon(ImageUtilities.loadIcon("org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/warningIcon.png"));
                label.setText("<html>" + message + "</html>"); // NOI18N
                label.getAccessibleContext().setAccessibleName(bundle.getString("ASCN_WarningMessage")); // NOI18N
                label.getAccessibleContext().setAccessibleDescription(message);
                label.setForeground(Util.getWarningForegroundColor());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                messagePanel.add(label, constraints);
            }
        }

        if(errorList != null && errorList.size() > 0) {
            hasErrors = true;
            for(Iterator iter = errorList.iterator(); iter.hasNext();) {
                String message = iter.next().toString();

                // Add error message
                JLabel label = new JLabel();
                label.setIcon(ImageUtilities.loadIcon("org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/errorIcon.png"));
                label.setText("<html>" + message + "</html>"); // NOI18N
                label.getAccessibleContext().setAccessibleName(bundle.getString("ASCN_ErrorMessage")); // NOI18N
                label.getAccessibleContext().setAccessibleDescription(message);
                label.setForeground(Util.getErrorForegroundColor());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                messagePanel.add(label, constraints);
            }
        }

        // Layout the dialog again to account for new or removed messages.  validate()
        // is not used here because the dialog may need to be resized to look nice.
        pack();

        // Display errors and warnings (if any) and disable/enable the OkButton as appropriate
        setOkEnabled(!hasErrors);
    }

    protected void setErrors(Collection errors, Collection warnings) {
        warningList = new ArrayList(warnings);
        errorList = new ArrayList(errors);
        showErrors();
    }

    /** Sets the existing error list to the collection of errors passed in.
     *  @param errors Collection of error messages.
     */
    protected void setErrors(Collection errors) {
        setErrors(errors, Collections.EMPTY_LIST);
    }

    /** Adds an warning string to the warning list.
     *  @param warning error message
     */
    public void addWarning(String warning) {
        if(warningList == null) {
            warningList = new ArrayList();
        }

        warningList.add(warning);
        showErrors();
    }

    /** Adds a collection of warnings to the warning list.
     *  @param warnings Collection of warning messages.
     */
    protected void addWarnings(Collection warnings) {
        if(warningList == null) {
            warningList = new ArrayList(warnings);
        } else {
            warningList.addAll(warnings);
        }

        showErrors();
    }

    /** Adds an error string to the error list.
     *  @param error error message
     */
    public void addError(String error) {
        if(errorList == null) {
            errorList = new ArrayList();
        }

        errorList.add(error);
        showErrors();
    }

    /** Adds a collection of errors to the error list.
     *  @param errors Collection of error messages.
     */
    protected void addErrors(Collection errors) {
        if(errorList == null) {
            errorList = new ArrayList(errors);
        } else {
            errorList.addAll(errors);
        }

        showErrors();
    }


    /** Clears out all error messages.
     */
    protected void clearErrors() {
        warningList = null;
        errorList = null;
        showErrors();
    }


    /** Test if the error list is filled or not. 
     *  @return true if there are errors, false if not.
     */
    public boolean hasErrors() {
        boolean result = false;

        if(errorList != null && errorList.size() > 0) {
            result = true;
        }

        return result;
    }

    /** ----------------------------------------------------------------------- 
     *  Implementation of HelpCtx.Provider interface
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getHelpId());
    }	


    protected void setButtonPanelPreferredSize(Dimension dimension){
        buttonPanel.setMinimumSize(dimension);
        buttonPanel.setPreferredSize(dimension);
    }
}
