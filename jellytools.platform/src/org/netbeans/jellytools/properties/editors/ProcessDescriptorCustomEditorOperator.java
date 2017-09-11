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

package org.netbeans.jellytools.properties.editors;

/*
 * ProcessDescriptorCustomEditorOperator.java
 *
 * Created on 6/12/02 5:00 PM
 */

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling Process Descriptor Custom
 * Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class ProcessDescriptorCustomEditorOperator extends NbDialogOperator {

    /** Creates new ProcessDescriptorCustomEditorOperator
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public ProcessDescriptorCustomEditorOperator(String title) {
        super(title);
    }

    /** creates new ProcessDescriptorCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ProcessDescriptorCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JTextAreaOperator _txtArgumentKey;
    private JButtonOperator _btSelectProcessExecutable;
    private JTextFieldOperator _txtProcess;
    private JTextAreaOperator _txtArguments;

    /** Tries to find null JTextArea in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtArgumentKey() {
        if (_txtArgumentKey==null) {
            _txtArgumentKey = new JTextAreaOperator( this, 1 );
        }
        return _txtArgumentKey;
    }

    /** Tries to find Browse... JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btSelectProcessExecutable() {
        if (_btSelectProcessExecutable==null) {
             _btSelectProcessExecutable = new JButtonOperator(this, 
                        Bundle.getStringTrimmed("org.netbeans.core.execution.beaninfo.editors.Bundle",
                                                "CTL_NbProcessDescriptorCustomEditor.jButton1.text"));
        }
        return _btSelectProcessExecutable;
    }

    /** Tries to find null JTextField in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProcess() {
        if (_txtProcess==null) {
            _txtProcess = new JTextFieldOperator( this, 0 );
        }
        return _txtProcess;
    }

    /** Tries to find null JTextArea in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtArguments() {
        if (_txtArguments==null) {
            _txtArguments = new JTextAreaOperator( this, 0 );
        }
        return _txtArguments;
    }

    /** getter for Argument Key text
     * @return String text of Argument Key */    
    public String getArgumentKey() {
        return txtArgumentKey().getText();
    }

    /** clicks on ... JButton
     * @throws TimeoutExpiredException when JButton not found
     * @return FileCustomEditorOperator */
    public FileCustomEditorOperator selectProcessExecutable() {
        btSelectProcessExecutable().pushNoBlock();
        return new FileCustomEditorOperator(Bundle.getString("org.openide.actions.Bundle", "Open"));
    }

    /** getter of edited process text
     * @return String process text */    
    public String getProcess() {
        return txtProcess().getText();
    }

    /** tries to find and set text of txtProcess
     * @param text String text
     */
    public void setProcess( String text ) {
        txtProcess().setText(text);
    }

    /** getter of edited arguments text
     * @return String argumentstext */    
    public String getArguments() {
        return txtArguments().getText();
    }

    /** tries to find and set text of txtArguments
     * @param text String text
     */
    public void setArguments( String text ) {
        txtArguments().setText(text);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtArgumentKey();
        txtArguments();
        txtProcess();
        btSelectProcessExecutable();
    }
}

