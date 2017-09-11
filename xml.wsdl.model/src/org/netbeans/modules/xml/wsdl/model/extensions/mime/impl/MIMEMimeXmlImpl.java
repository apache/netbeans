/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.mime.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMimeXml;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEQName;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author jyang
 */
public class MIMEMimeXmlImpl extends MIMEComponentImpl implements MIMEMimeXml {

    public MIMEMimeXmlImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public MIMEMimeXmlImpl(WSDLModel model) {
        this(model, createPrefixedElement(MIMEQName.MIME_XML.getQName(), model));
    }

    /**
     * Set the part for this MIME content.
     *
     * @param part the desired part
     */
    public void setPart(String part) {
        setAttribute(PART_PROPERTY, MIMEAttribute.PART, part);
    }

    /**
     * Get the part for this MIME content.
     */
    public String getPart() {
        return getAttribute(MIMEAttribute.PART);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingInput || target instanceof BindingOutput) {
            return true;
        }
        return false;
    }

    public void accept(MIMEComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public Reference<Part> getPartRef() {
        String v = getPart();
        return v == null ? null : new PartReference(this, v);
    }

    public void setTypeRef(Reference<Part> partRef) {
        String v = partRef == null ? null : partRef.getRefString();
        setAttribute(PART_PROPERTY, MIMEAttribute.PART, v);
    }
}
    
    

