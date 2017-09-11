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

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPOperationImpl extends SOAPComponentImpl implements SOAPOperation {
    
    /** Creates a new instance of SOAPOperationImpl */
    public SOAPOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPOperationImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.OPERATION.getQName(), model));
    }
    
    public void accept(SOAPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setSoapAction(String soapActionURI) {
        setAttribute(SOAP_ACTION_PROPERTY, SOAPAttribute.SOAP_ACTION, soapActionURI);
    }

    public String getSoapAction() {
        return getAttribute(SOAPAttribute.SOAP_ACTION);
    }
   
    public void setStyle(Style v) {
        setAttribute(STYLE_PROPERTY, SOAPAttribute.STYLE, v);
    }

    public Style getStyle() {
        String s = getAttribute(SOAPAttribute.STYLE);
        if (s == null) {
            WSDLComponent ancestor = getParent() == null? null : getParent().getParent();
            if (ancestor instanceof Binding) {
                Binding b = (Binding) ancestor;
                Collection<SOAPBinding> sbs = b.getExtensibilityElements(SOAPBinding.class);
                if (sbs.size() > 0) {
                    SOAPBinding sb = sbs.iterator().next();
                    Style sbStyle = sb.getStyle();
                    if (sbStyle != null) {
                        return sbStyle;
                    }
                }
            }
            return Style.DOCUMENT;
        }

        return Style.valueOf(s.toUpperCase());

    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingOperation) {
            return true;
        }
        return false;
    }
}
