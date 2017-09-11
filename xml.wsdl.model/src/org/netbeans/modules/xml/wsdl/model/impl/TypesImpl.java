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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.WSDLSchemaImpl;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Nam Nguyen
 */
public class TypesImpl extends WSDLComponentBase implements Types {
    private boolean registeredSchemaQNameAttributes;
    
    /** Creates a new instance of TypesImpl */
    public TypesImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public TypesImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.TYPES.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public Collection<Schema> getSchemas(){
        //get list of WSDLSchemas
        List<Schema> schemas = new ArrayList<Schema>();
        List<WSDLSchema> wsdlSchemas = getExtensibilityElements(WSDLSchema.class);
        
        for(WSDLSchema wsdlSchema : wsdlSchemas){
            schemas.add(wsdlSchema.getSchemaModel().getSchema());
            if (! registeredSchemaQNameAttributes) {
                AbstractDocumentModel schemaModel = (AbstractDocumentModel) wsdlSchema.getSchemaModel();
                getModel().getAccess().addQNameValuedAttributes(schemaModel.getQNameValuedAttributes());
                registeredSchemaQNameAttributes = true;
            }
        }
        return schemas;
    }

    @Override
    protected <N extends Node> void updateReference(Element peer, List<N> pathToRoot) {
        super.updateReference(peer, pathToRoot);
        int iPeer = pathToRoot.indexOf(peer);
        assert iPeer > -1 : "Provided peer is outside context path";
        if (iPeer > 0) {
            for (WSDLSchema wschema : getExtensibilityElements(WSDLSchema.class)) {
                Schema schema = wschema.getSchemaModel().getSchema();
                if (schema.referencesSameNode(pathToRoot.get(iPeer-1))) {
                    ((WSDLSchemaImpl)wschema).updateReference(schema.getPeer());
                    break;
                }
            }
        }
    }

    public List<EmbeddableRoot> getAdoptedChildren() {
        return new ArrayList<EmbeddableRoot>(getSchemas());
    }
}
