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

package org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl;

import javax.xml.XMLConstants;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.impl.ImportImpl;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaReferenceImpl<T extends ReferenceableSchemaComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of SchemaReferenceImpl */
    public SchemaReferenceImpl(
            T referenced, 
            Class<T> type, 
            AbstractDocumentComponent parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public SchemaReferenceImpl(Class<T> type, AbstractDocumentComponent parent, String refString){
        super(type, parent, refString);
    }

    public T get() {
        if (getReferenced() == null) {
            String localName = getLocalName();
            String namespace = getEffectiveNamespace();
            T target = null;

            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)) {
                SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
                target = primitiveModel.resolve(namespace, localName, getType());
            } else {
                Types types = getParent().getModel().getDefinitions().getTypes();
                if (types != null) {
                    for (Schema s : types.getSchemas()) {
                        target = s.getModel().resolve(namespace, localName, getType());
                        if (target != null) {
                            break;
                        }
                    }
                }
                if (target == null) {
                    for (Import i : getParent().getModel().getDefinitions().getImports()) {
                        DocumentModel m = null;
                        try {
                            m = ((ImportImpl)i).resolveImportedModel();
                        } catch(CatalogModelException ex) {
                            // checked for null so ignore
                        }
                        if (m instanceof SchemaModel) {
                            target = ((SchemaModel)m).resolve(namespace, localName, getType());
                        }
                        if (target != null) {
                            break;
                        }
                    }
                }
            }
            
            if (target != null) {
                setReferenced(target);
            }
        }
        return getReferenced();
    }

    public WSDLComponentBase getParent() {
        return (WSDLComponentBase) super.getParent();
    }
    
    public String getEffectiveNamespace() {
        if (refString == null) {
            assert getReferenced() != null;
            return getReferenced().getModel().getSchema().getTargetNamespace();
        } else {
            if (getPrefix() == null) {
                return null;
            } else {
                return getParent().lookupNamespaceURI(getPrefix());
            }
        }
    }
}
