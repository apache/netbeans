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

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

public class ValidationMethodEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    static String VALIDATION_TYPE = "validation"; //NOI18N
    static String TRANX_SUPPORT_TYPE = "TransactionSupport"; //NOI18N
    
    public static String editorType = VALIDATION_TYPE;
    public static String curr_Sel;
    public String[] choices = {
                 "auto-commit", // NOI18N
                 "meta-data", // NOI18N
                 "table" // NOI18N
                 };
    
    public String[] choicesTranx = {
                 "", // NOI18N
                 "XATransaction", // NOI18N
                 "LocalTransaction", // NOI18N
                 "NoTransaction"
                 };

   public ValidationMethodEditor() {
	curr_Sel = null;
   }
   
   public ValidationMethodEditor(String type) {
	curr_Sel = null;
        editorType = type;
   }

    public String getAsText () {
	return curr_Sel;
    }

    public void setAsText (String string) throws IllegalArgumentException {
        if (! editorType.equals(TRANX_SUPPORT_TYPE)) {
            if ((string == null) || (string.equals(""))) { // NOI18N
                throw new IllegalArgumentException();
            } else {
                curr_Sel = string;
            }
        } else {
            curr_Sel = string;
        }
        this.firePropertyChange();
    }

   public void setValue (Object val) {
       if (! (val instanceof String)) {
      	    throw new IllegalArgumentException ();
	}
        curr_Sel = (String) val;
    }

    public Object getValue () {
        return curr_Sel;
    }

    public String getJavaInitializationString () {
	return getAsText ();
    }

    public String[] getTags () {
        if(editorType.equals(TRANX_SUPPORT_TYPE)){
           return choicesTranx; 
        } else {
           return choices;
        }    
    }

   public Component getInPlaceCustomEditor () {
        return null;
    }

   
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    public boolean supportsEditingTaggedValues () {
        return false;
    }

 }


