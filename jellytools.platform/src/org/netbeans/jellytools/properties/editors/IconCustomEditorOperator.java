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
