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

package org.netbeans.beaninfo.editors;

import java.util.StringTokenizer;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;

/** Support for property editors for several integers.
* for example:  Point - [2,4], Insets [2,3,4,4],...
*
* @author   Petr Hamernik
* @version  0.14, Jul 20, 1998
*/
abstract class ArrayOfIntSupport extends java.beans.PropertyEditorSupport
implements XMLPropertyEditor, ExPropertyEditor  {
    private static final String VALUE_FORMAT = org.openide.util.NbBundle.getBundle(
                ArrayOfIntSupport.class).getString("EXC_BadFormatValue");

    /** Length of the array of the integers */
    private int count;

    /** Class Name of the edited property. It is used in getJavaInitializationString
    * method.
    */
    private String className;

    /** associated env, accessible by package private subclasses */
    PropertyEnv env;


    /** constructs new property editor.
    * @param className Name of the class which is this editor for. (e.g. "java.awt.Point")
    * @param count Length of the array of int
    */
    public ArrayOfIntSupport(String className, int count) {
        this.className = className;
        this.count = count;
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }


    /** This method is intended for use when generating Java code to set
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
        int[] val = getValues();
        StringBuffer buf = new StringBuffer("new "); // NOI18N

        buf.append(className);
        buf.append("("); // NOI18N
        addArray(buf, val);
        buf.append(")"); // NOI18N
        return buf.toString();
    }

    /** Abstract method for translating the value from getValue() method to array of int. */
    abstract int[] getValues();

    /** Abstract method for translating the array of int to value
    * which is set to method setValue(XXX)
    */
    abstract void setValues(int[] val);

    //----------------------------------------------------------------------

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        if (getValue() == null)
            return "null"; // NOI18N
        
        int[] val = getValues();

        if (val == null)
            return null;
        else {
            StringBuffer buf = new StringBuffer("["); // NOI18N
            addArray(buf, val);
            buf.append("]"); // NOI18N
            return buf.toString();
        }
    }

    /** Add array of integers to the StringBuffer. Numbers are separated by ", " string */  // NOI18N
    private void addArray(StringBuffer buf, int[] arr) {
        for (int i = 0; i < count; i++) {
            if (arr == null)
                buf.append("0"); // NOI18N
            else
                buf.append(arr[i]);

            if (i < count - 1)
                buf.append(", "); // NOI18N
        }
    }

    /** Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText(String text) throws IllegalArgumentException {
        if ("null".equals(text) || "".equals(text)) { // NOI18N
            setValue(null);
            return;
        }
        int[] newVal = new int[count];
        int nextNumber = 0;

        StringTokenizer tuk = new StringTokenizer(text, "[] ,;", false); // NOI18N
        while (tuk.hasMoreTokens()) {
            String token = tuk.nextToken();
            if (nextNumber >= count)
                badFormat(null);

            try {
                newVal[nextNumber] = new Integer(token).intValue();
                nextNumber++;
            }
            catch (NumberFormatException e) {
                badFormat(e);
            }
        }

        // if less numbers are entered, copy the last entered number into the rest
        if (nextNumber != count) {
            if (nextNumber > 0) {
                int copyValue = newVal [nextNumber - 1];
                for (int i = nextNumber; i < count; i++)
                    newVal [i] = copyValue;
            }
        }
        setValues(newVal);
    }

    /** Always throws the new exception */
    private void badFormat(Exception e) throws IllegalArgumentException {
        String msg = new MessageFormat(VALUE_FORMAT).format(new Object[] 
            { className , getHintFormat() } );
        IllegalArgumentException iae = new IllegalArgumentException(msg);
        UIExceptions.annotateUser(iae, e == null ? ""
                                                : e.getMessage(), msg, e,
                                 new java.util.Date()); //NOI18N
        throw iae;                                          
    }

    /** @return the format info for the user. Can be rewritten in subclasses. */
    String getHintFormat() {
        StringBuilder buf = new StringBuilder("["); // NOI18N
        for (int i = 0; i < count; i++) {
            buf.append("<n"); // NOI18N
            buf.append(i);
            buf.append(">"); // NOI18N

            if (i < count - 1)
                buf.append(", "); // NOI18N
        }
        buf.append("]"); // NOI18N

        return buf.toString();
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String ATTR_VALUE = "value"; // NOI18N

    /** Provides name of XML tag to use for XML persistence of the property value */
    protected abstract String getXMLValueTag ();

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!getXMLValueTag ().equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String value = attributes.getNamedItem (ATTR_VALUE).getNodeValue ();
            setAsText (value);
        } catch (Exception e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (getXMLValueTag ());
        el.setAttribute (ATTR_VALUE, getAsText ());
        return el;
    }


}
