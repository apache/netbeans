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

import java.util.Locale;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class ImportImpl extends WSDLComponentBase implements Import {
    
    /** Creates a new instance of ImportImpl */
    public ImportImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public ImportImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.IMPORT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setNamespace(String namespaceURI) {
        setAttribute(NAMESPACE_URI_PROPERTY, WSDLAttribute.NAMESPACE_URI, namespaceURI);
    }

    public void setLocation(String locationURI) {
        setAttribute(LOCATION_PROPERTY, WSDLAttribute.LOCATION, locationURI);
    }

    public String getNamespace() {
        return getAttribute(WSDLAttribute.NAMESPACE_URI);
    }

    public String getLocation() {
        return getAttribute(WSDLAttribute.LOCATION);
    }

    public WSDLModel getImportedWSDLModel() throws CatalogModelException {
        DocumentModel m = resolveImportedModel();
        if (m instanceof WSDLModel) {
            return (WSDLModel) m;
        } else {
            String msg = NbBundle.getMessage(ImportImpl.class, "MSG_CANNOT_LOAD_WSDL", getLocation());
            throw new CatalogModelException(msg);
        }
    }
    
    public WSDLModel resolveToWSDLModel() throws CatalogModelException {
        DocumentModel m = resolveImportedModel();
        if (m instanceof WSDLModel) {
            return (WSDLModel) m;
        } else {
            return null;
        }
    }
    
    public SchemaModel resolveToSchemaModel() throws CatalogModelException {
        DocumentModel m = resolveImportedModel();
        if (m instanceof SchemaModel) {
            return (SchemaModel) m;
        } else {
            return null;
        }
    }
    
    public DocumentModel resolveImportedModel() throws CatalogModelException {
        ModelSource ms = resolveModel(getLocation());
        
        String location = getLocation().toLowerCase(Locale.US);
        if (location.endsWith(".wsdl")) { //NOI18N
            return loadAsWSDL(ms);
        } else if (location.endsWith(".xsd")) { //NOI18N
            return loadAsSchema(ms);
        } else {
            DocumentModel m = loadAsWSDL(ms);
            if (m == null) {
                m = loadAsSchema(ms);
            }
            return m;
        }
    }
    
    private WSDLModel loadAsWSDL(ModelSource ms) {
        WSDLModel m = WSDLModelFactory.getDefault().getModel(ms);
        if (m != null && m.getState() == DocumentModel.State.NOT_WELL_FORMED) {
            return null;
        }
        return m;
    }
    
    private SchemaModel loadAsSchema(ModelSource ms) {
        SchemaModel m = SchemaModelFactory.getDefault().getModel(ms);
        if (m != null && m.getState() == DocumentModel.State.NOT_WELL_FORMED) {
            return null;
        }
        return m;
    }
}
