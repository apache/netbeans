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

import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;

// editor for Rectangle
/** Class implementing all necessary methods for handling Rectangle Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class RectangleCustomEditorOperator extends NbDialogOperator {

    JTextFieldOperator _txtFieldX;
    JTextFieldOperator _txtFieldY;
    JTextFieldOperator _txtFieldWidth;
    JTextFieldOperator _txtFieldHeight;
    
    /** creates new RectangleCustomEditorOperator
     * @param title String title of custom editor */    
    public RectangleCustomEditorOperator(String title) {
        super(title);
    }
    
    /** creates new RectangleCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public RectangleCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** setter for edited rectangle value
     * @param x int x
     * @param y int y
     * @param width int width
     * @param height int height */    
    public void setRectangleValue(String x, String y, String width, String height) {
        txtFieldX().setText(x);
        txtFieldY().setText(y);
        txtFieldWidth().setText(width);
        txtFieldHeight().setText(height);
    }
    
    /** getter for edited X value
     * @return int X */    
    public String getXValue() {
        return txtFieldX().getText();
    }

    /** setter for edited X value
     * @param value int X */    
    public void setXValue(String value) {
        txtFieldX().setText(value);
    }
    
    /** getter for edited Y value
     * @return int Y */    
    public String getYValue() {
        return txtFieldY().getText();
    }

    /** setter for edited Y value
     * @param value int Y */    
    public void setYValue(String value) {
        txtFieldY().setText(value);
    }
    
    /** getter for edited Width value
     * @return int Width */    
    public String getWidthValue() {
        return txtFieldWidth().getText();
    }

    /** setter for edited Width value
     * @param value int Width */    
    public void setWidthValue(String value) {
        txtFieldWidth().setText(value);
    }
    
    /** getter for edited Height value
     * @return int Height */    
    public String getHeightValue() {
        return txtFieldHeight().getText();
    }

    /** setter for edited Height value
     * @param value int Height */    
    public void setHeightValue(String value) {
        txtFieldHeight().setText(value);
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
    
    /** getter for Width JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldWidth() {
        if(_txtFieldWidth==null) {
            _txtFieldWidth = new JTextFieldOperator(this, 2);
        }
        return _txtFieldWidth;
    }
    
    /** getter for Height JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldHeight() {
        if(_txtFieldHeight==null) {
            _txtFieldHeight = new JTextFieldOperator(this, 3);
        }
        return _txtFieldHeight;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtFieldHeight();
        txtFieldWidth();
        txtFieldX();
        txtFieldY();
    }
    
}
