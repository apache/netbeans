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

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.netbeans.modules.form.NamedPropertyEditor;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;

/** A property editor for array of Strings.
* @author  Ian Formanek
* @version 0.10, 17 Jun 1998
*/
public class StringArrayEditor implements XMLPropertyEditor,
        StringArrayCustomizable, NamedPropertyEditor {

    // constants for XML persistence
    private static final String XML_STRING_ARRAY = "StringArray"; // NOI18N
    private static final String XML_STRING_ITEM = "StringItem"; // NOI18N
    private static final String ATTR_COUNT = "count"; // NOI18N
    private static final String ATTR_INDEX = "index"; // NOI18N
    private static final String ATTR_VALUE = "value"; // NOI18N

    // private fields
    private String[] strings;
    private PropertyChangeSupport support;

    public StringArrayEditor() {
        support = new PropertyChangeSupport (this);
    }

    @Override
    public Object getValue () {
        return strings;
    }

    @Override
    public void setValue (Object value) {
        strings = (String[]) value;
        support.firePropertyChange ("", null, null); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // StringArrayCustomizable implementation

    /** Used to acquire the current value from the PropertyEditor
    * @return the current value of the property
    */
    @Override
    public String[] getStringArray () {
        return (String[])getValue ();
    }

    /** Used to modify the current value in the PropertyEditor
    * @param value the new value of the property
    */
    @Override
    public void setStringArray (String[] value) {
        setValue (value);
    }

    // end of StringArrayCustomizable implementation

    protected final String getStrings(boolean quoted) {
        if (strings == null) return "null"; // NOI18N

        StringBuilder buf = new StringBuilder ();
        for (int i = 0; i < strings.length; i++) {
            // Handles in-string escapes if quoted
            if (quoted) {
                buf.append("\""); // NOI18N
                char[] chars = strings[i].toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    char c = chars[j];
                    switch (c) {
                    case '\b': buf.append("\\b"); break; // NOI18N
                    case '\t': buf.append("\\t"); break; // NOI18N
                    case '\n': buf.append("\\n"); break; // NOI18N
                    case '\f': buf.append("\\f"); break; // NOI18N
                    case '\r': buf.append("\\r"); break; // NOI18N
                    case '\"': buf.append("\\\""); break; // NOI18N
                    case '\\': buf.append("\\\\"); break; // NOI18N
                    default:
                        if (c >= 0x0020/* && c <= 0x007f*/)
                            buf.append(c);
                        else {
                            buf.append("\\u"); // NOI18N
                            String hex = Integer.toHexString(c);
                            for (int k = 0; k < 4 - hex.length(); k++)
                                buf.append('0');
                            buf.append(hex);
                        }
                    }
                }
                buf.append("\""); // NOI18N
            } else {
                buf.append(strings[i]);
            }
            if (i != strings.length - 1)
                buf.append (", "); // NOI18N
        }
 
        return buf.toString ();
    }

    @Override
    public String getAsText () {
        return getStrings(false);
    }

    @Override
    public void setAsText (String text) {
        if (text.equals("null")) { // NOI18N
            setValue(null);
            return;
        }
        StringTokenizer tok = new StringTokenizer(text, ","); // NOI18N
        java.util.List<String> list = new LinkedList<String>();
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            list.add(s.trim());
        }
        String [] a = list.toArray(new String[0]);
        setValue(a);
    }

    @Override
    public String getJavaInitializationString () {
        if (strings == null) return "null"; // NOI18N
        // [PENDING - wrap strings ???]
        StringBuilder buf = new StringBuilder ("new String[] {"); // NOI18N
        buf.append (getStrings(true));
        buf.append ("}"); // NOI18N
        return buf.toString ();
    }

    @Override
    public String[] getTags () {
        return null;
    }

    @Override
    public boolean isPaintable () {
        return false;
    }

    @Override
    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    @Override
    public Component getCustomEditor () {
        return new StringArrayCustomEditor(this);
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    @Override
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
    @Override
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
    @Override
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
                    if (itemEl.getNodeName().equals(XML_STRING_ITEM)) {
                        String indexStr = itemEl.getAttribute(ATTR_INDEX);
                        String valueStr = itemEl.getAttribute(ATTR_VALUE);
                        if (indexStr != null && valueStr != null) {
                            int index = Integer.parseInt(indexStr);
                            if (index >=0 && index < count){                                                                                                                               
                                stringArray[index] = valueStr;
                            }
                                
                        }
                    }
                }
            }
        }
        else stringArray = new String[0];

        setValue(stringArray);
    }
    
    // NamedPropertyEditor implementation
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(StringArrayEditor.class).getString("CTL_StringArrayEditor_DisplayName"); // NOI18N
    }

}
