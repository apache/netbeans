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
