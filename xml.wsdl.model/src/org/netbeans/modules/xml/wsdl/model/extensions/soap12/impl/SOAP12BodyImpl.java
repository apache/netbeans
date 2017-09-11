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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 * @author Sujit Biswas
 *
 */
public class SOAP12BodyImpl extends SOAP12MessageBaseImpl implements SOAP12Body {
    
    /** Creates a new instance of SOAPBodyImpl */
    public SOAP12BodyImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAP12BodyImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAP12QName.BODY.getQName(), model));
    }
    
    public void accept(SOAP12Component.Visitor visitor) {
        visitor.visit(this);
    }

    public List<String> getParts() {
        String s = getAttribute(SOAP12Attribute.PARTS);
        return s == null ? null : Util.parse(s);
    }
    
    public List<Reference<Part>> getPartRefs() {
        String s = getAttribute(SOAP12Attribute.PARTS);
        return s == null ? null : parseParts(s);
    }
    
    public void addPart(String part) {
        String parts = getAttribute(SOAP12Attribute.PARTS);
        parts = parts == null ? part : parts.trim() + " " + part; //NOI18N
        setAttribute(PARTS_PROPERTY, SOAP12Attribute.PARTS, parts);
    }
    
    public void addPartRef(Reference<Part> partRef) {
        addPart(partRef.getRefString());
    }
    
    public void addPart(int index, String part) {
        List<String> parts = getParts();
        if (parts != null) {
            parts.add(index, part);
        } else {
            parts = Collections.singletonList(part);
        }
        setAttribute(PARTS_PROPERTY, SOAP12Attribute.PARTS, Util.toString(parts));;
    }
    
    public void addPartRef(int index, Reference<Part> partRef) {
        addPart(index, partRef.getRefString());
    }
    
    public void removePart(String part) {
        Collection<String> parts = getParts();
        if (parts != null && parts.remove(part)) {
            setAttribute(PARTS_PROPERTY, SOAP12Attribute.PARTS, Util.toString(parts));
        }
    }

    public void removePartRef(Reference<Part> partRef) {
        removePart(partRef.getRefString());
    }

    public void setParts(List<String> parts) {
        setAttribute(PARTS_PROPERTY, SOAP12Attribute.PARTS, Util.toString(parts));
    }

    public void setPartRefs(List<Reference<Part>> parts) {
        String value = null;
        if (parts != null && ! parts.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Reference<Part> ref : parts) {
                sb.append(ref.getRefString());
            }
            value = sb.toString();
        }
        setAttribute(PARTS_PROPERTY, SOAP12Attribute.PARTS, value);
    }
    
    private List<Reference<Part>> parseParts(String s) {
        List<Reference<Part>> ret = new ArrayList<Reference<Part>>();
        for (String part : Util.parse(s)) {
            ret.add(new PartReference(this, part));
        }
        return ret;
    }


    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingInput || target instanceof BindingOutput) {
            return true;
        }
        return false;
    }
}
