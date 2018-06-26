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
package org.netbeans.modules.websvc.api.jaxws.bindings.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponentFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsComponentFactoryImpl 
          implements BindingsComponentFactory{
    
    private BindingsModelImpl model;
    /** Creates a new instance of BindingsComponentFactoryImpl */
    public BindingsComponentFactoryImpl(BindingsModel model) {
        if (model instanceof BindingsModelImpl) {
            this.model = (BindingsModelImpl) model;
        } else {
            throw new IllegalArgumentException("Excpect BindingsModelImpl");
        }
    }

    public BindingsComponent create(Element e, BindingsComponent parent) {
        //TODO implement Visitor to get rid of this humongous if-else block
        QName childQName = new QName(e.getNamespaceURI(), e.getLocalName());
        if(childQName.equals(BindingsQName.BINDINGS.getQName())){
            if(parent instanceof GlobalBindings){
                return new DefinitionsBindingsImpl(model, e);
            }
            else{
                return new GlobalBindingsImpl(model, e);
            }
        }
        if(childQName.equals(BindingsQName.HANDLER_CHAINS.getQName())){
            return new BindingsHandlerChainsImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_CHAIN.getQName())){
            return new BindingsHandlerChainImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER.getQName())){
            return new BindingsHandlerImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_CLASS.getQName())){
            return new BindingsHandlerClassImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_NAME.getQName())){
            return new BindingsHandlerNameImpl(model, e);
        }
        return null;
    }

    public BindingsHandlerClass createHandlerClass() {
        return new BindingsHandlerClassImpl(model);
    }
    public BindingsHandlerName createHandlerName() {
        return new BindingsHandlerNameImpl(model);
    }

    public BindingsHandlerChains createHandlerChains() {
        return new BindingsHandlerChainsImpl(model);
    }

    public BindingsHandlerChain createHandlerChain() {
        return new BindingsHandlerChainImpl(model);
    }

    public GlobalBindings createGlobalBindings() {
        return new GlobalBindingsImpl(model);
    }

    public DefinitionsBindings createDefinitionsBindings() {
        return new DefinitionsBindingsImpl(model);
    }

    public BindingsHandler createHandler() {
        return new BindingsHandlerImpl(model);
    }
    
}
