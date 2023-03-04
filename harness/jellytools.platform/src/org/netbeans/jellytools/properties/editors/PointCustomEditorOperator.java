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

// editor for 2D point
/** Class implementing all necessary methods for handling Point Custom Editor
 * @version 1.0
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PointCustomEditorOperator extends NbDialogOperator {

    JTextFieldOperator _txtFieldX;
    JTextFieldOperator _txtFieldY;

    /** creates new PointCustomEditorOperator
     * @param title String title of custom editor */    
    public PointCustomEditorOperator(String title) {
        super(title);
    }
    
    /** creates new PointCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public PointCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** setter for edited point value
     * @param x int x
     * @param y int y */    
    public void setPointValue(String x, String y) {
        txtFieldX().setText(x);
        txtFieldY().setText(y);
    }
    
    /** getter for edited x value
     * @return int x */    
    public String getXValue() {
        return txtFieldX().getText();
    }

    /** setter for edited x value
     * @param value int x */    
    public void setXValue(String value) {
        txtFieldX().setText(value);
    }
    
    /** getter for edited y value
     * @return int y */    
    public String getYValue() {
        return txtFieldY().getText();
    }

    /** setter for edited y value
     * @param value int y */    
    public void setYValue(String value) {
        txtFieldY().setText(value);
    }
    
    /** getter for X JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldX() {
        if(_txtFieldX==null) {
            _txtFieldX = new JTextFieldOperator(this, 0);
        }
        return _txtFieldX;
    }
    
    /** getter for Y JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldY() {
        if(_txtFieldY==null) {
            _txtFieldY = new JTextFieldOperator(this, 1);
        }
        return _txtFieldY;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtFieldX();
        txtFieldY();
    }
    
}
