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

package org.netbeans.beaninfo.editors;

import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** A property editor for Class.
* @author   Jan Jancura
*/
public class ClassEditor extends java.beans.PropertyEditorSupport {

    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *    current value.
     */
    public String getJavaInitializationString() {
        Class<?> clazz = (Class)getValue();
        if (clazz == null) return "null"; // NOI18N
        return "Class.forName (\"" + clazz.getName () + "\")"; // NOI18N
    }

    //----------------------------------------------------------------------

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        Class<?> clazz = (Class)getValue();
        if (clazz == null) return "null"; // NOI18N
        return clazz.getName ();
    }

    /** Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        try {
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            setValue (loader.loadClass (text));
        } catch (ClassNotFoundException e) {
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = MessageFormat.format(
                NbBundle.getMessage(
                    ClassEditor.class, "FMT_EXC_CANT_LOAD_CLASS"), new Object[] {text}); //NOI18N
            UIExceptions.annotateUser(iae, e.getMessage(), msg, e,
                                     new java.util.Date());
            throw iae;
        }
    }
}
