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

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 * Base interface of all WSDL components
 */
public interface WSDLComponent extends DocumentComponent<WSDLComponent> {
    public static final String DOCUMENTATION_PROPERTY = "documentation"; //NOI18N
    public static final String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement";

    /**
     * @return WSDL model.
     */
    WSDLModel getModel();
    
    void accept(WSDLVisitor visitor);
    
    void setDocumentation(Documentation doc);
    Documentation getDocumentation();
    
    /**
     * Creates a global reference to the given target WSDL component.
     * @param target the target WSDLComponent
     * @param type actual type of the target
     * @return the global reference.
     */
    <T extends ReferenceableWSDLComponent> NamedComponentReference<T> createReferenceTo(T target, Class<T> type);
    
    /**
     * Creates a GlobalReference to a Schema component
     * @param target The schema component that is being referenced.
     * @param type Class object of the schema component
     */
    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> 
            createSchemaReference(T target, Class<T> type);

    void addExtensibilityElement(ExtensibilityElement ee);
    void removeExtensibilityElement(ExtensibilityElement ee);
    List<ExtensibilityElement> getExtensibilityElements();
    
    <T extends ExtensibilityElement> List<T> getExtensibilityElements(Class<T> type);

    /**
     * Returns map of attribute names and string values.
     */
    Map<QName,String> getAttributeMap();
    
}
