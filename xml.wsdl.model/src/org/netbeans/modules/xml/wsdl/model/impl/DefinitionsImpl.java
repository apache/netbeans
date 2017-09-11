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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class DefinitionsImpl extends NamedImpl implements Definitions {
            
    /** Creates a new instance of DefinitionsImpl */
    public DefinitionsImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public DefinitionsImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.DEFINITIONS.getQName(), model));
    }

    public void addBinding(Binding binding) {
        addAfter(BINDING_PROPERTY, binding, TypeCollection.FOR_BINDING.types());
    }

    public void removeBinding(Binding binding) {
        removeChild(BINDING_PROPERTY, binding);
    }

    public void addService(Service service) {
        addAfter(SERVICE_PROPERTY, service, TypeCollection.FOR_SERVICE.types());
    }

    public void removeService(Service service) {
        removeChild(SERVICE_PROPERTY, service);
    }

    public void addImport(Import importDefinition) {
        addAfter(IMPORT_PROPERTY, importDefinition, TypeCollection.FOR_IMPORT.types());
    }

    public void removeImport(Import importDefinition) {
        removeChild(IMPORT_PROPERTY, importDefinition);
    }

    public void addPortType(PortType portType) {
        addAfter(PORT_TYPE_PROPERTY, portType, TypeCollection.FOR_PORTTYPE.types());
    }

    public void removePortType(PortType portType) {
        removeChild(PORT_TYPE_PROPERTY, portType);
    }

    public static final String TNS = "tns"; //NOI18N
    
    public void setTargetNamespace(String uri) {
        String currentTargetNamespace = getTargetNamespace();
        setAttribute(TARGET_NAMESPACE_PROPERTY, WSDLAttribute.TARGET_NAMESPACE, uri);
        ensureValueNamespaceDeclared(uri, currentTargetNamespace, TNS);
    }

    public void setTypes(Types types) {
        setChild(Types.class, TYPES_PROPERTY, types, TypeCollection.FOR_TYPES.types());
    }

    public void addMessage(Message message) {
        addAfter(MESSAGE_PROPERTY, message, TypeCollection.FOR_MESSAGE.types());
    }

    public void removeMessage(Message message) {
        removeChild(MESSAGE_PROPERTY, message);
    }

    public Collection<Service> getServices() {
        return getChildren(Service.class);
    }

    public Collection<PortType> getPortTypes() {
        return getChildren(PortType.class);
    }

    public Collection<Message> getMessages() {
        return getChildren(Message.class);
    }

    public Collection<Import> getImports() {
        return getChildren(Import.class);
    }

    public Collection<Binding> getBindings() {
        return getChildren(Binding.class);
    }

    public String getTargetNamespace() {
        return getAttribute(WSDLAttribute.TARGET_NAMESPACE);
    }

    public Types getTypes() {
        return getChild(Types.class);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
