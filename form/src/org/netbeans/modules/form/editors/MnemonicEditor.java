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

package org.netbeans.modules.form.editors;

import java.beans.*;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.openide.util.NbBundle;

/**
 * Editor for mnemonic property
 * @author  Josef Kozak
 */
public class MnemonicEditor extends PropertyEditorSupport implements NamedPropertyEditor {

    /**
     * Converts the char to String by either leaving
     * the single char or by creating unicode escape.
     */
    @Override
    public String getAsText () {
        Object ovalue = getValue();
        char value = (char)0;
        
        if (java.lang.Character.class.isInstance(ovalue))
            value = ((Character)ovalue).charValue();
        else if (java.lang.Integer.class.isInstance(ovalue))
            value = (char)(((Integer)ovalue).intValue());
        
        if (value == 0) return "";
        
        final StringBuffer buf = new StringBuffer(6);
        switch (value) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (value >= 0x0020 && value <= 0x007f)
                    buf.append(value);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(value);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
        }         
        return buf.toString() ;
    }
    /**
     * Set the property value by parsing given String.
     * @param text  The string to be parsed.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.length() < 1) {
            setValue(new Integer(0));
            return;
        }
        
        if (text.length() == 1 && text.charAt(0) != '\\') {
            setValue(new Character(text.charAt(0)));
            return;
        }                
                
        if (text.charAt(0) == '\\') {
            // backslash means unicode escape sequence
            char value = 0;
            char ch = text.length() >=2 ? text.charAt(1) : '\\';
            switch (ch) {
                case 'b': value = '\b'; break;
                case 't': value = '\t'; break;
                case 'n': value = '\n'; break;
                case 'f': value = '\f'; break;
                case 'r': value = '\r'; break;
                case '\\': value = '\\' ; break;
                case 'u' :
                    String num = text.substring(2,text.length());
                    if (num.length () > 4) {
                        // ignore longer strings
                        return;
                    }
                    try {
                        int intValue = Integer.parseInt(num,16);
                        value = (char) intValue;
                        break;
                    } catch (NumberFormatException nfe) {
                        // ignore non parsable strings
                        return;
                    }
                default:
                        // ignore non-chars after backslash
                        return;
                        
            }
            setValue(new Character(value));
            return;
        }
        
        try {
            setValue(new Integer(text));
            return;            
        } catch (NumberFormatException e) {
            setValue(text);
            return;
        }
        
    }
    
    /**
     * Accepts Integer, Character and String values. If the argument is
     * a String the first character is taken as the new value.
     * @param newValue new value
     */
    @Override
    public void setValue(Object newValue) throws IllegalArgumentException {
        if  (newValue instanceof Integer) {
            super.setValue(newValue);
            return;
        }
        if  (newValue instanceof Character) {
            super.setValue(new Integer((int)(((Character)newValue).charValue())));
            return;
        }
        if (newValue instanceof String) {
            String text = (String ) newValue;
            if (text.length() >= 1) {
                super.setValue(new Integer((int)text.charAt(0)));
                return;
            }
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */
    @Override
    public String getJavaInitializationString() {
	return "'" + getAsText() + "'"; // NOI18N
    }

    // NamedPropertyEditor implementation
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString("CTL_MnemonicsEditor_DisplayName"); // NOI18N
    }

}
