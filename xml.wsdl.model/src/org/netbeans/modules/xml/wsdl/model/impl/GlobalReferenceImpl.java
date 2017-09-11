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

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author Nam Nguyen
 * @author rico
 */
public class GlobalReferenceImpl<T extends ReferenceableWSDLComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of GlobalReferenceImpl */
    //for use by factory, create from scratch
    public GlobalReferenceImpl(
            T referenced, 
            Class<T> type, 
            WSDLComponentBase parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public GlobalReferenceImpl(
            Class<T> type, 
            WSDLComponentBase parent, 
            String refString){
        super(type, parent, refString);
    }
    
    protected Definitions getDefinitions() {
        WSDLComponentBase wparent = WSDLComponentBase.class.cast(getParent());
        return wparent.getModel().getDefinitions();
    }
    
    public T get() {
        WSDLComponentBase wparent = WSDLComponentBase.class.cast(getParent());
        if (super.getReferenced() == null) {
            String localName = getLocalName();
            String namespace = getEffectiveNamespace();
            WSDLModel model = wparent.getWSDLModel();
            T target = null;
            String targetNamespace = model.getDefinitions().getTargetNamespace();
            if ((namespace == null && targetNamespace == null) ||
                (namespace != null && namespace.equals(targetNamespace))) {
                target = new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
            }
            if (target == null) {
                for (Import i : wparent.getWSDLModel().getDefinitions().getImports()) {
                    if (! i.getNamespace().equals(namespace)) {
                        continue;
                    }
                    try {
                        model = i.getImportedWSDLModel();
                    } catch(CatalogModelException ex) {
                        continue;
                    }
                    target = new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
                    if (target != null) {
                        break;
                    }
                }
            }
            setReferenced(target);
        }
        return getReferenced();
    }
    
    public WSDLComponentBase getParent() {
        return (WSDLComponentBase) super.getParent();
    }
    
    public String getEffectiveNamespace() {
        if (getReferenced() != null) {
            return getReferenced().getModel().getDefinitions().getTargetNamespace();
        } else {
            assert refString != null;
            return getParent().lookupNamespaceURI(getPrefix());
        }
    }
}
