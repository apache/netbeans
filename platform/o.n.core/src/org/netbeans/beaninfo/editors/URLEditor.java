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

import java.beans.PropertyEditorSupport;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.util.NbBundle;

/** A property editor for java.net.URL class.
*
* @author   Ian Formanek
*/
public class URLEditor extends PropertyEditorSupport implements org.openide.explorer.propertysheet.editors.XMLPropertyEditor  {

    /** sets new value */
    @Override
    public void setAsText(String s) {
        if ("null".equals(s)) { // NOI18N
            setValue(null);
            return;
        }

        try {
            URL url = new URL (s);
            setValue(url);
        } catch (MalformedURLException e) {
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = MessageFormat.format(
                NbBundle.getMessage(
                    URLEditor.class, "FMT_EXC_BAD_URL"), new Object[] {s}); //NOI18N
             UIExceptions.annotateUser(iae, e.getMessage(), msg, e,
                                      new java.util.Date());
            throw iae;
        }
    }

    /** @return the current value as String */
    @Override
    public String getAsText() {
        URL url = (URL)getValue();
        return url != null ? url.toString() : "null"; // NOI18N
    }

    @Override
    public String getJavaInitializationString () {
        URL url = (URL) getValue ();
        return "new java.net.URL(\""+url.toString ()+"\")"; // NOI18N
    }

    @Override
    public boolean supportsCustomEditor () {
        return false;
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_URL = "Url"; // NOI18N

    public static final String ATTR_VALUE = "value"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception java.io.IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_URL.equals (element.getNodeName ())) {
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
        org.w3c.dom.Element el = doc.createElement (XML_URL);
        el.setAttribute (ATTR_VALUE, getAsText ());
        return el;
    }
}
