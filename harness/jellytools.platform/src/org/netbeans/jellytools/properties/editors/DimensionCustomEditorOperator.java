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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;

// editor for Dimension
/** Class implementing all necessary methods for handling Dimension Custom Editor */
public class DimensionCustomEditorOperator extends NbDialogOperator {

    JTextFieldOperator _txtFieldWidth;
    JTextFieldOperator _txtFieldHeight;

    /** Creates a new instance of DimensionCustomEditorOperator
     * @param title String title of custom editor */    
    public DimensionCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates a new instance of DimensionCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public DimensionCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** sets dimension value
     * @param width int width
     * @param height int height */    
    public void setDimensionValue(String width, String height) {
        txtFieldWidth().setText(width);
        txtFieldHeight().setText(height);
    }
    
    /** returns width value
     * @return ind width */    
    public String getWidthValue() {
        return txtFieldWidth().getText();
    }

    /** sets width value
     * @param value int width */    
    public void setWidthValue(String value) {
        txtFieldWidth().setText(value);
    }
    
    /** returns height value
     * @return int height */    
    public String getHeightValue() {
        return txtFieldHeight().getText();
    }

    /** sets height value
     * @param value int height */    
    public void setHeightValue(String value) {
        txtFieldHeight().setText(value);
    }
    
    /** getter for height text field operator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldWidth() {
        if(_txtFieldWidth==null) {
            _txtFieldWidth = new JTextFieldOperator(this, 0);
        }
        return _txtFieldWidth;
    }
    
    /** getter for width text field operator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldHeight() {
        if(_txtFieldHeight==null) {
            _txtFieldHeight = new JTextFieldOperator(this, 1);
        }
        return _txtFieldHeight;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtFieldHeight();
        txtFieldWidth();
    }
    
}
