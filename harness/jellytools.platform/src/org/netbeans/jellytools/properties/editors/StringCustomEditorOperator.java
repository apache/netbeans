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

import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling String Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class StringCustomEditorOperator extends NbDialogOperator {

    private JTextAreaOperator _txtArea;

    /** Creates new StringCustomEditorOperator
     * @param title String title of custom editor */
    public StringCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates new StringCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public StringCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** setter for edited String value
     * @return String */    
    public String getStringValue() {
        return txtArea().getText();
    }
    
    /** getter for edited String value
     * @param text String */    
    public void setStringValue(String text) {
        txtArea().setText(text);
    }
    
    /** getter for JTextFieldOperator with edited text
     * @return JTextFieldOperator */    
    public JTextAreaOperator txtArea() {
        if(_txtArea==null) {
            _txtArea = new JTextAreaOperator(this);
        }
        return _txtArea;
    }
        
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtArea();
    }

}
