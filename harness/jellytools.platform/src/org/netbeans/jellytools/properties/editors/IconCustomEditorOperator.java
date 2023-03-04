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
package org.netbeans.jellytools.properties.editors;

import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling Icon Custom Editor.
 * @author Marian Mirilovic
 * @author Jiri Skrivanek
 */
public class IconCustomEditorOperator extends NbDialogOperator {

    private JRadioButtonOperator _rbExternalImage;
    private JRadioButtonOperator _rbImageWithinProject;
    private JRadioButtonOperator _rbNoImage;
    private JTextFieldOperator _txtFileOrURL;
    private JComboBoxOperator _cbPackage;
    private JComboBoxOperator _cbFile;
    private JButtonOperator _btBrowseClasspath;
    private JButtonOperator _btBrowseLocalDisk;
    private JButtonOperator _btImportToProject;

    /** Creates new IconCustomEditorOperator that can handle it.
     * Throws TimeoutExpiredException when NbDialog not found.
     * @param title title of custom editor 
     */
    public IconCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new IconCustomEditorOperator.
     * @param wrapper JDialogOperator wrapper for custom editor 
     */
    public IconCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog) wrapper.getSource());
    }

    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find "External Image" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbExternalImage() {
        if (_rbExternalImage == null) {
            _rbExternalImage = new JRadioButtonOperator(this, "External Image");

        }
        return _rbExternalImage;
    }

    /** Tries to find "Image Within Project" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbImageWithinProject() {
        if (_rbImageWithinProject == null) {
            _rbImageWithinProject = new JRadioButtonOperator(this, "Image Within Project");
        }
        return _rbImageWithinProject;
    }

    /** Tries to find "No Image" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbNoImage() {
        if (_rbNoImage == null) {
            _rbNoImage = new JRadioButtonOperator(this, "No Image");
        }
        return _rbNoImage;
    }

    /** Tries to find "File or URL" JTextField in this dialog.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtFileOrURL() {
        if (_txtFileOrURL == null) {
            _txtFileOrURL = new JTextFieldOperator(this);
        }
        return _txtFileOrURL;
    }

    /** Finds "Package" combo box.
     * @return JComboBoxOperator instance
     */
    public JComboBoxOperator cbPackage() {
        if (_cbPackage == null) {
            _cbPackage = new JComboBoxOperator(this);
            if (_cbPackage.getSource() == cbFile().getSource()) {
                _cbPackage = new JComboBoxOperator(this, 1);
            }
        }
        return _cbPackage;
    }

    /** Finds "File:" combo box.
     * @return JComboBoxOperator instance
     */
    public JComboBoxOperator cbFile() {
        if (_cbFile == null) {
            _cbFile = new JComboBoxOperator(this, "Select...");
        }
        return _cbFile;
    }

    /** Finds "Browse classpath" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btBrowseClasspath() {
        if (_btBrowseClasspath == null) {
            _btBrowseClasspath = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) this.getSource(), "Browse classpath", true, true));
        }
        return _btBrowseClasspath;
    }

    /** Finds "Browse local disk" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btBrowseLocalDisk() {
        if (_btBrowseLocalDisk == null) {
            _btBrowseLocalDisk = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) this.getSource(), "Browse local disk", true, true));
        }
        return _btBrowseLocalDisk;
    }

    /** Finds "Import to Project..." button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btImportToProject() {
        if (_btImportToProject == null) {
            _btImportToProject = new JButtonOperator(this, "Import to Project...");
        }
        return _btImportToProject;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************
    /** Clicks on "External Image" JRadioButton. */
    public void externalImage() {
        rbExternalImage().push();
    }

    /** Clicks on "Image Within Project" JRadioButton.  */
    public void imageWithinProject() {
        rbImageWithinProject().push();
    }

    /** Clicks on "No Image" JRadioButton. */
    public void noImage() {
        rbNoImage().push();
    }

    /** Gets text from "File or URL" text field.
     * @return text from "File or URL" text field.
     */
    public String getFileOrURL() {
        return txtFileOrURL().getText();
    }

    /** Sets text in "File or URL" text field.
     * @param text text to be written to "File or URL" text field
     */
    public void setFileOrURL(String text) {
        txtFileOrURL().setText(text);
    }

    /** Types text in "File or URL" text field.
     * @param text text to be written to "File or URL" text field
     */
    public void typeFileOrURL(String text) {
        txtFileOrURL().typeText(text);
    }

    /** Pushes "Browse classpath" button.
     * @return NbDialogOperator instance
     */
    public NbDialogOperator browseClasspath() {
        btBrowseClasspath().pushNoBlock();
        return new NbDialogOperator("Select Image File");
    }

    /** Pushes "Browse local disk" button.
     * @return NbDialogOperator instance
     */
    public NbDialogOperator browseLocalDisk() {
        btBrowseLocalDisk().pushNoBlock();
        return new NbDialogOperator("Select Image File");
    }

    /** Pushes "Import to Project..." button.
     * @return WizardOperator instance
     */
    public WizardOperator importToProject() {
        btImportToProject().pushNoBlock();
        return new WizardOperator("");
    }
}
