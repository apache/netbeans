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

/*
 * FontCustomEditorOperator.java
 *
 * Created on 6/13/02 4:29 PM
 */

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Font Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class FontCustomEditorOperator extends NbDialogOperator {

    /** Creates new FontCustomEditorOperator
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public FontCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates a new instance of FontCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public FontCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JListOperator _lstFontName;
    private JListOperator _lstFontSize;
    private JTextFieldOperator _txtFontName;
    private JTextFieldOperator _txtFontSize;
    private JTextFieldOperator _txtFontStyle;
    private JListOperator _lstFontStyle;
    /** String constant for plain font style */    
    public static final String STYLE_PLAIN = Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "CTL_Plain"); 
    /** String constant for bold font style */    
    public static final String STYLE_BOLD = Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "CTL_Bold"); 
    /** String constant for italic font style */    
    public static final String STYLE_ITALIC = Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "CTL_Italic"); 
    /** String constant for bold italic font style */    
    public static final String STYLE_BOLDITALIC = Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "CTL_BoldItalic"); 


    /** Tries to find null JList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstFontName() {
        if (_lstFontName==null) {
            _lstFontName = new JListOperator( this, 0 );
        }
        return _lstFontName;
    }

    /** Tries to find null JList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstFontSize() {
        if (_lstFontSize==null) {
            _lstFontSize = new JListOperator( this, 2 );
        }
        return _lstFontSize;
    }

    /** Tries to find null JTextField in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFontName() {
        if (_txtFontName==null) {
            _txtFontName = new JTextFieldOperator( this, 0 );
        }
        return _txtFontName;
    }

    /** Tries to find null JTextField in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFontSize() {
        if (_txtFontSize==null) {
            _txtFontSize = new JTextFieldOperator( this, 2 );
        }
        return _txtFontSize;
    }

    /** Tries to find null JTextField in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFontStyle() {
        if (_txtFontStyle==null) {
            _txtFontStyle = new JTextFieldOperator( this, 1 );
        }
        return _txtFontStyle;
    }

    /** Tries to find null JList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstFontStyle() {
        if (_lstFontStyle==null) {
            _lstFontStyle = new JListOperator( this, 1 );
        }
        return _lstFontStyle;
    }

    /** returns edited font name
     * @return String font name */    
    public String getFontName() {
        return txtFontName().getText();
    }
    
    /** returns edited font style
     * @return String font style */    
    public String getFontStyle() {
        return txtFontStyle().getText();
    }

    /** returns edited font size
     * @return String font size */    
    public String getFontSize() {
        return txtFontSize().getText();
    }

    /** tries to find and set text of txtFontName
     * @param name String font name */
    public void setFontName( String name ) {
        lstFontName().selectItem(name);
    }

    /** tries to find and set text of txtFontSize
     * @param size String font size */
    public void setFontSize( String size ) {
        txtFontSize().setText("");
        txtFontSize().typeText(size);
    }

    /** tries to find and set text of txtFontStyle
     * @param style String font style */
    public void setFontStyle( String style ) {
        lstFontStyle().selectItem(style);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtFontName();
        txtFontSize();
        txtFontStyle();
        lstFontName();
        lstFontSize();
        lstFontStyle();
    }

}

