/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            value = ((Character)ovalue);
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
            setValue(0);
            return;
        }
        
        if (text.length() == 1 && text.charAt(0) != '\\') {
            setValue(text.charAt(0));
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
                    String num = text.substring(2);
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
            setValue(value);
            return;
        }
        
        try {
            setValue(Integer.valueOf(text));
        } catch (NumberFormatException e) {
            setValue(text);
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
        } else if (newValue instanceof Character) {
            char c = (Character) newValue;
            super.setValue((int)c);
            return;
        } else if (newValue instanceof String) {
            String text = (String) newValue;
            if (text.length() >= 1) {
                super.setValue((int)text.charAt(0));
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
