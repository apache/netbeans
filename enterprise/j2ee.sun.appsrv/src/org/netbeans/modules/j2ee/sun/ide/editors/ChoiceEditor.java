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
package org.netbeans.modules.j2ee.sun.ide.editors;


import java.awt.Component;
import java.beans.PropertyEditorSupport;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

public abstract class ChoiceEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {

    public String curr_Sel;

    public ChoiceEditor() {
        curr_Sel = null;
    }

    @Override
    public String getAsText() {
        return curr_Sel;
    }
    
    @Override
    public void setAsText(String string) throws IllegalArgumentException {
        if((string==null)||(string.equals(""))) // NOI18N
            throw new IllegalArgumentException();
        else
            curr_Sel = string;
        this.firePropertyChange();
    }
    
    @Override
    public void setValue(Object val) {
        if (val == null) {
            String str = NbBundle.getMessage(BooleanEditor.class, "TXT_Null_Value");     //NOI18N
            curr_Sel = str;
        }
        else {
            if (! (val instanceof String)) {
                throw new IllegalArgumentException();
            }
            curr_Sel = (String) val;
        }
        super.setValue(curr_Sel);
    }
    
    @Override
    public Object getValue() {
        return curr_Sel;
    }
    
    @Override
    public String getJavaInitializationString() {
        return getAsText();
    }
    
    @Override
    public abstract String[] getTags();
    
    public Component getInPlaceCustomEditor() {
        return null;
    }
    
    
    public boolean hasInPlaceCustomEditor() {
        return false;
    }
    
    public boolean supportsEditingTaggedValues() {
        return false;
    }
    
}


