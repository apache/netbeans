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
