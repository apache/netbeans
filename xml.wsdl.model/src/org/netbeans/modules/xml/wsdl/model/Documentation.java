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

package org.netbeans.modules.xml.wsdl.model;

import java.io.IOException;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 * @author rico
 * @author Nam Nguyen
 * Represents the documentation element in a WSDL document
 */
public interface Documentation extends WSDLComponent{
    public static final String CONTENT_PROPERTY = "content";
    
    /**
     * @return text representation of the documentation element content.
     */
    String getTextContent();
    
    /**
     * Set the documentation element content to a text node with the given
     * string value.
     */
    void setTextContent(String content);
    
    /**
     * @return XML fragment text of documentation element content.
     */
    String getContentFragment();
    
    /**
     * Sets the XML fragment text of documentation element content.
     * The XML fragment will be parsed and the resulting nodes will
     * replace the current children of this documentation element.
     * @param text XML fragment text.
     * @exception IOException if the fragment text is not well-form.
     */
    void setContentFragment(String text) throws IOException;
    
    /**
     * @return a mutable clone of the documentation element itself.
     */
    Element getDocumentationElement();
    
    /**
     * Sets the documentation element to the given element.
     */
    void setDocumentationElement(Element documentationElement);
    
    /**
     * Returns string value of the attribute from different namespace.
     * If given QName has prefix, it will be ignored.
     * @param attr non-null QName represents the attribute name.
     * @return attribute value
     */
    String getAnyAttribute(QName attr);

    /**
     * Set string value of the attribute identified by given QName.
     * This will fire property change event using attribute local name.
     * @param attr non-null QName represents the attribute name.
     * @param value string value for the attribute.
     */
    void setAnyAttribute(QName attr, String value);
}
