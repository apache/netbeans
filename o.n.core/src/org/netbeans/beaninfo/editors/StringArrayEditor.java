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

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.nodes.Node;
import org.openide.nodes.PropertyEditorRegistration;

/** A property editor for array of Strings.
 * @author  Ian Formanek
 */
@PropertyEditorRegistration(targetType=String[].class)
public class StringArrayEditor implements XMLPropertyEditor, StringArrayCustomizable, ExPropertyEditor {

    // constants for XML persistence
    private static final String XML_STRING_ARRAY = "StringArray"; // NOI18N
    private static final String XML_STRING_ITEM = "StringItem"; // NOI18N
    private static final String ATTR_COUNT = "count"; // NOI18N
    private static final String ATTR_INDEX = "index"; // NOI18N
    private static final String ATTR_VALUE = "value"; // NOI18N

    // private fields
    private String[] strings;
    private PropertyChangeSupport support;
    private boolean editable = true;
    private String separator = ",";

    public StringArrayEditor() {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return strings;
    }

    public void setValue (Object value) {
        strings = (String[]) value;
        support.firePropertyChange("", null, null); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // StringArrayCustomizable implementation

    /** Used to acquire the current value from the PropertyEditor
    * @return the current value of the property
    */
    public String[] getStringArray () {
        return (String[])getValue ();
    }

    /** Used to modify the current value in the PropertyEditor
    * @param value the new value of the property
    */
    public void setStringArray (String[] value) {
        setValue (value);
    }

    // end of StringArrayCustomizable implementation

    protected final String getStrings(boolean quoted) {
        if (strings == null) return "null"; // NOI18N

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            // XXX handles in-string escapes if quoted
            if (quoted) {
                buf.append('"').append(strings[i]).append('"');
            }
            else {
                buf.append(strings[i]);
            }
            if (i != strings.length - 1) {
                buf.append (separator); 
                buf.append (' '); // NOI18N
            }
        }

        return buf.toString ();
    }

    public String getAsText () {
        return getStrings(false);
    }

    public void setAsText (String text) {
        if ("null".equals(text)) { // NOI18N
            setValue(null);
            return;
        }
        StringTokenizer tok = new StringTokenizer(text, separator);
        java.util.List<String> list = new LinkedList<String>();
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            list.add(s.trim());
        }
        String [] a = list.toArray(new String[list.size()]);
        setValue(a);
    }

    public String getJavaInitializationString () {
        if (strings == null) return "null"; // NOI18N
        // [PENDING - wrap strings ???]
        StringBuilder buf = new StringBuilder ("new String[] {"); // NOI18N
        buf.append (getStrings(true));
        buf.append ('}'); // NOI18N
        return buf.toString ();
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        //Don't show custom editor if it's just going to show
        //an empty component
        if (!editable && (strings==null || strings.length==0)) {
            return false;
        } else {
            return true;
        }
    }

    public Component getCustomEditor () {
        if (editable) {
            return new StringArrayCustomEditor(this);
        } else {
            return new JScrollPane(new JList(getStringArray()));
        }
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    // -------------------------------------------
    // XMLPropertyEditor implementation

    /** Called to store current property value into XML subtree.
     * @param doc The XML document to store the XML in - should be used for
     *            creating nodes only
     * @return the XML DOM element representing a subtree of XML from which
               the value should be loaded
     */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element arrayEl = doc.createElement(XML_STRING_ARRAY);
        int count = strings != null ? strings.length : 0;
        arrayEl.setAttribute(ATTR_COUNT, Integer.toString(count));

        for (int i=0; i < count; i++) {
            org.w3c.dom.Element itemEl = doc.createElement(XML_STRING_ITEM);
            itemEl.setAttribute(ATTR_INDEX, Integer.toString(i));
            itemEl.setAttribute(ATTR_VALUE, strings[i]);
            arrayEl.appendChild(itemEl);
        }

        return arrayEl;
    }

    /** Called to load property value from specified XML subtree.
     * If succesfully loaded, the value should be available via getValue().
     * An IOException should be thrown when the value cannot be restored from
     * the specified XML element
     * @param element the XML DOM element representing a subtree of XML from
     *                which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from
                  the specified XML element
     */
    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_STRING_ARRAY.equals(element.getNodeName()))
            throw new java.io.IOException();

        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        String[] stringArray;
        org.w3c.dom.Node countNode = null;
        int count = 0;

        if ((countNode = attributes.getNamedItem(ATTR_COUNT)) != null
                && (count = Integer.parseInt(countNode.getNodeValue())) > 0) {
            stringArray = new String[count];
            org.w3c.dom.NodeList items = element.getChildNodes();
            org.w3c.dom.Element itemEl;

            for (int i = 0; i < items.getLength(); i++) {
                if (items.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    itemEl = (org.w3c.dom.Element)items.item(i);
                    if (XML_STRING_ITEM.equals(itemEl.getNodeName())) {
                        String indexStr = itemEl.getAttribute(ATTR_INDEX);
                        String valueStr = itemEl.getAttribute(ATTR_VALUE);
                        if (indexStr != null && valueStr != null) {
                            int index = Integer.parseInt(indexStr);
                            if (index >=0 && index < count)
                                stringArray[index] = valueStr;
                        }
                    }
                }
            }
        }
        else stringArray = new String[0];

        setValue(stringArray);
    }
    
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor d = env.getFeatureDescriptor();
        readEnv (env.getFeatureDescriptor ());
    }
    
    final void readEnv (FeatureDescriptor d) {
        if (d instanceof Node.Property) {
            editable = ((Node.Property)d).canWrite();
        } else if (d instanceof PropertyDescriptor) {
            editable = ((PropertyDescriptor)d).getWriteMethod() != null;
        } else {
            editable = true;
        }
        
        Object v = d.getValue ("item.separator"); // NOI18N
        if (v instanceof String) {
            separator = (String)v;
        }
    }
}
