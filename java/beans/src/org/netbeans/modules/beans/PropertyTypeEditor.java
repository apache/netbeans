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

package org.netbeans.modules.beans;

import java.awt.*;
import java.beans.*;
import org.openide.ErrorManager;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.util.NbBundle;

/** Property editor for the property type property
*
* @author Martin Matula
*/
public class PropertyTypeEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    
    /** Default types */
    private final String[] types = new String[] {
                                       "boolean", "char", "byte", "short", "int", // NOI18N
                                       "long", "float", "double", "String" // NOI18N
                                   };

    /** Creates new editor */
    public PropertyTypeEditor () {
    }

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
//    public String getAsText () {
//        Type type = (Type) getValue();
//        return (type == null) ? "" : type.getName(); // NOI18N
//    }

    /**
    * Set the property value by parsing a given String.
    * @param string  The string to be parsed.
    */
//    public void setAsText (String string) throws IllegalArgumentException {
//        String normalizedInput;
//        if (string == null || (normalizedInput = string.trim()).length() == 0) {
//            throw (IllegalArgumentException) ErrorManager.getDefault().annotate(new IllegalArgumentException(string), NbBundle.getMessage(PropertyTypeEditor.class, "MSG_Not_Valid_Type"));
//        }
//        Type oldType = (Type) getValue();
//        Type newType;
//        try {
//            BeanUtils.beginTrans(false);
//            try  finally {
//                BeanUtils.endTrans();
//            }
//            setValue(newType);
//        } catch (JmiException e) {
//            IllegalArgumentException iae = new IllegalArgumentException();
//            iae.initCause(e);
//            throw iae;
//        }
//    }

    /**
    * @param v new value
    */
    public void setValue(Object v) {
//        BeanUtils.beginTrans(false);
//        try {
//            if (!(v instanceof Type) || BeanUtils.isPrimitiveType((Type) v, PrimitiveTypeKindEnum.VOID))
//                throw new IllegalArgumentException();
//        } finally {
//            BeanUtils.endTrans();
//        }
//        super.setValue(v);
    }

    /**
    * @return A fragment of Java code representing an initializer for the
    * current value.
    */
    public String getJavaInitializationString () {
        return getAsText();
    }

    /**
    * @return The tag values for this property.
    */
    public String[] getTags () {
        return types;
    }

    /**
    * @return Returns custom property editor to be showen inside the property
    *         sheet.
    */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
    * @return true if this PropertyEditor provides a enhanced in-place custom
    *              property editor, false otherwise
    */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
    * @return true if this property editor provides tagged values and
    * a custom strings in the choice should be accepted too, false otherwise
    */
    public boolean supportsEditingTaggedValues () {
        return true;
    }
}
