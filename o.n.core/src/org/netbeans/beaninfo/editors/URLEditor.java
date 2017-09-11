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
    public String getAsText() {
        URL url = (URL)getValue();
        return url != null ? url.toString() : "null"; // NOI18N
    }

    public String getJavaInitializationString () {
        URL url = (URL) getValue ();
        return "new java.net.URL(\""+url.toString ()+"\")"; // NOI18N
    }

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
    * @exception IOException thrown when the value cannot be restored from the specified XML element
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
