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

import java.awt.*;
import java.beans.*;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.*;

public class Int0Editor extends PropertyEditorSupport implements EnhancedPropertyEditor {

    public String prev = "null"; // NOI18N
    private String curValue;
    private String errorMessage;

    public Int0Editor() {
	curValue = null;
	
    }

    public String getAsText () {
        if (curValue==null || curValue.equals("")) {// NOI18N
            curValue = prev;
        }
        if (errorMessage != null) {
           // String title = NbBundle.getMessage(Int0Editor.class, "TTL_Input_Error");
            errorMessage = null;
        }
        return curValue;
    }

    public String checkValid(String string) {
        if (EditorUtils.isValidInt0(string))
            return null;  //no error message
        else 
            return NbBundle.getMessage(Int0Editor.class, "MSG_RangeForInt0");      
    }
    
    public void setAsText (String string) throws IllegalArgumentException {
        prev = curValue;
        if((string==null)||(string.equals(""))) {// NOI18N
           return;
        }

        errorMessage = checkValid(string);
        if (errorMessage == null) {
            prev = curValue;
            curValue = string;
            firePropertyChange();
        }
        else 
            curValue = prev;
    }
    
    public void setValue (Object v) {
        if(!(v.equals(""))){ // NOI18N
           prev = (String)v;
        }   
        curValue = (String)v;
    
    }

    public Object getValue () {
       return curValue;
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


  
      
  
