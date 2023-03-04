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
/*
 * BoolEditor.java
 *
 * Created on February 28, 2003, 1:13 PM
 */

package org.netbeans.beaninfo.editors;
import java.beans.*;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.nodes.PropertyEditorRegistration;
import org.openide.util.NbBundle;
/** Replacement editor for boolean primitive values which supports
 *  internationalization and alternate string values that
 *  can be supplied to the property editor via adding an array
 *  returning an array of two Strings (false then true) from
 *  <code>env.getFeatureDescriptor().getValue()</code>.  These
 *  string values will then be used for getAsText, setAsText, and getTags.
 *  These strings should be correctly internationalized if supplied
 *  by a module.  String value matching in setAsText is non-case-sensitive
 *  ("TRUE" and "tRue" are equivalent).
 *
 * @author  Tim Boudreau
 */
@PropertyEditorRegistration(targetType=Boolean.class)
public class BoolEditor extends ExPropertyEditorSupport {
    String[] stringValues = null;
    /** Creates a new instance of BoolEditor */
    public BoolEditor() {
    }
    
    protected void attachEnvImpl(org.openide.explorer.propertysheet.PropertyEnv env) {
        stringValues = (String[]) env.getFeatureDescriptor().getValue(
        "stringValues"); //NOI18N
    }
    
    /** Throws an EnvException if the stringValues key is not 2 items in length.   */
    protected void validateEnv(org.openide.explorer.propertysheet.PropertyEnv env) {
        if (stringValues != null) {
            if (stringValues.length != 2) {
                throw new EnvException(
                "String value hint for boolean editor must contain exactly 2 "
                + "items.  The supplied value contains " + stringValues.length +
                " items: " + arrToStr(stringValues));
            }
        }
    }
    
    private String getStringRep(boolean val) {
        if (stringValues != null) {
            return stringValues [val ? 0 : 1];
        }
        String result;
        if (val) {
            result = NbBundle.getMessage(BoolEditor.class, "TRUE"); //NOI18N
        } else {
            result = NbBundle.getMessage(BoolEditor.class, "FALSE"); //NOI18N
        }
        return result;
    }
    
    /** Returns Boolean.TRUE, Boolean.FALSE or null in the case of an
     *  unrecognized string. */
    private Boolean stringVal(String val) {
        String valToTest = val.trim().toUpperCase();
        String test = getStringRep(true).toUpperCase();
        if (test.equals(valToTest)) return Boolean.TRUE;
        test = getStringRep(false).toUpperCase();
        if (test.equals(valToTest)) return Boolean.FALSE;
        return null;
    }
    
    public String getJavaInitializationString() {
        Boolean val = (Boolean) getValue();
        if (val == null) return "null"; //NOI18N
        return Boolean.TRUE.equals(getValue()) ? "true" : "false"; //NOI18N
    }
    
    public String[] getTags() {
        return new String[] {
            getStringRep(true), getStringRep(false)
        };
    }
    
    public String getAsText() {
        Boolean val = (Boolean) getValue();
        if (val == null) return NbBundle.getMessage(BoolEditor.class, "NULL");
        return getStringRep(Boolean.TRUE.equals(getValue()));
    }
    
    public void setAsText(String txt) {
        Boolean val = stringVal(txt);
        boolean newVal = val == null ? false : val.booleanValue();
        setValue(newVal ? Boolean.TRUE : Boolean.FALSE);
    }
}
