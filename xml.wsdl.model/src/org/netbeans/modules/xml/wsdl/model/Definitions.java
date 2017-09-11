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

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author rico
 * Represents the WSDL definitions section
 */
public interface Definitions extends Nameable<WSDLComponent>, WSDLComponent {
    public static String IMPORT_PROPERTY = "import";
    public static String BINDING_PROPERTY = "binding";
    public static String TYPES_PROPERTY = "types";
    public static String MESSAGE_PROPERTY = "message";
    public static String PORT_TYPE_PROPERTY = "portType";
    public static String SERVICE_PROPERTY = "service";
    public static String TARGET_NAMESPACE_PROPERTY = "targetNamespace";
    
    void addImport(Import importDefinition);
    void removeImport(Import importDefinition);
    Collection<Import> getImports();
    
    void setTypes(Types types);
    Types getTypes();
    
    void addMessage(Message message);
    void removeMessage(Message message);
    Collection<Message> getMessages();
    
    void addPortType(PortType portType);
    void removePortType(PortType portType);
    Collection<PortType> getPortTypes();
    
    void addBinding(Binding binding);
    void removeBinding(Binding binding);
    Collection<Binding> getBindings();
    
    void addService(Service service);
    void removeService(Service service);
    Collection<Service> getServices();
    
    String getTargetNamespace();
    void setTargetNamespace(String uri);
    
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
