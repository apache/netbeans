/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author rico
 * @author Nam Nguyen
 */
public class WSDLModelImpl extends WSDLModel {
    private Definitions definitions;
    private WSDLComponentFactory wcf;
    
    public WSDLModelImpl(ModelSource source) {
        super(source);
        wcf = new WSDLComponentFactoryImpl(this);
    }
    
    @Override
    public WSDLComponent createRootComponent(Element root) {
        DefinitionsImpl newDefinitions = null;
        QName q = root == null ? null : AbstractDocumentComponent.getQName(root);
        if (root != null && WSDLQNames.DEFINITIONS.getQName().equals(q)) {
            newDefinitions = new DefinitionsImpl(this, root);
            setDefinitions(newDefinitions);
        } else {
            return null;
        }
        
        return getDefinitions();
    }

    @Override
    public WSDLComponent getRootComponent() {
        return definitions;
    }

    @Override
    public WSDLComponent createComponent(WSDLComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    @Override
    protected ComponentUpdater<WSDLComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<WSDLComponent>();
    }

    @Override
    public WSDLComponentFactory getFactory() {
        return wcf;
    }
    
    public void setDefinitions(Definitions def){
        assert (def instanceof DefinitionsImpl) ;
        definitions = DefinitionsImpl.class.cast(def);
    }

    @Override
    public Definitions getDefinitions(){
        return definitions;
    }

    ElementFactoryRegistry getElementRegistry() {
        return ElementFactoryRegistry.getDefault();
    }
    
    public List<WSDLModel> getImportedWSDLModels() {
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        Collection<Import> imports = getDefinitions().getImports();
        for (Import i:imports) {
            try {
                WSDLModel m = i.getImportedWSDLModel();
                if (m != null) {
                    ret.add(m);
                }
            } catch(Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedWSDLModels", e);
            }
        }
        return ret;
    }

    public List<SchemaModel> getImportedSchemaModels() {
        List<SchemaModel> ret = new ArrayList<SchemaModel>();
        Collection<Import> imports = getDefinitions().getImports();
        for (Import i:imports) {
            try {
                SchemaModel m = ((ImportImpl)i).resolveToSchemaModel();
                if (m != null) {
                    ret.add(m);
                }
            } catch(Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedSchemaModels", e); //NOI18N
            }
        }
        return ret;
    }

    public List<SchemaModel> getEmbeddedSchemaModels() {
        List<SchemaModel> ret = new ArrayList<SchemaModel>();
        Types types = getDefinitions().getTypes();
        List<WSDLSchema> embeddedSchemas = Collections.emptyList();
        if (types != null) {
            embeddedSchemas = types.getExtensibilityElements(WSDLSchema.class);
        }
        for (WSDLSchema wschema : embeddedSchemas) {
            ret.add(wschema.getSchemaModel());
        }
        return ret;
    }

    @Override
    public List<WSDLModel> findWSDLModel(String namespace) {
        if (namespace == null) {
            return Collections.emptyList();
        }
        
        List<WSDLModel> models = getImportedWSDLModels();
        models.add(0, this);

        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        for (WSDLModel m : models) {
            String targetNamespace = m.getDefinitions().getTargetNamespace();
            if (namespace.equals(targetNamespace)) {
                ret.add(m);
            }
        }
        return ret;
    }

    @Override
    public List<Schema> findSchemas(String namespace) {
        List<Schema> ret = new ArrayList<Schema>();
        for (SchemaModel sm : getEmbeddedSchemaModels()) {
            try {
                ret.addAll(sm.findSchemas(namespace));
            } catch(Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "findSchemas", ex);
            }
        }
        SchemaModel sm = findSchemaModelFromImports(namespace);
        if (sm != null) {
            ret.add(sm.getSchema());
        }
        return ret;
    }
    
    private SchemaModel findSchemaModelFromImports(String namespace) {
        if (namespace == null) {
            return null;
        }
        
        List<SchemaModel> models = getImportedSchemaModels();
        for (SchemaModel m : models) {
            String targetNamespace = m.getSchema().getTargetNamespace();
            if (namespace.equals(targetNamespace)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type) {
        return type.cast(new FindReferencedVisitor(getDefinitions()).find(name, type));
    }

    @Override
    public <T extends ReferenceableWSDLComponent> T findComponentByName(QName name, Class<T> type) {
        String namespace = name.getNamespaceURI();
        if (namespace == null) {
            return findComponentByName(name.getLocalPart(), type);
        } else {
            for (WSDLModel targetModel : findWSDLModel(namespace)) {
                T found = targetModel.findComponentByName(name.getLocalPart(), type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    @Override
    public Set<QName> getQNames() {
        return getElementRegistry().getKnownQNames();
    }

    @Override
    public Set<String> getElementNames() {
        return getElementRegistry().getKnownElementNames();
    }
    
    @Override
    public ChangeInfo prepareChangeInfo(List<? extends Node> pathToRoot,
            List<? extends Node> nsContextPathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot, nsContextPathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        if (! (parentComponent.getModel() instanceof WSDLModel)) 
        {
            getElementRegistry().addEmbeddedModelQNames((AbstractDocumentModel)parentComponent.getModel());
            // Run preparation again because after registration a new model,
            // the set of QNames can be changed.
            //
            // TODO: Optimization: addEmbeddedModelQNames() has to return a flag which indicates if
            // a new QNames was added. It's not necessary to do preparation again if the set
            // isn't changed and parentComponent can be speficied to get rid of repeated call of findComponent()
            change = super.prepareChangeInfo(pathToRoot, nsContextPathToRoot);
        } else if (isDomainElement(parentComponent.getPeer()) && 
                ! change.isDomainElement() && change.getChangedElement() != null) 
        {
            // TODO: Sort out with the use-case of this code. 
            if (change.getOtherNonDomainElementNodes() == null ||
                change.getOtherNonDomainElementNodes().isEmpty()) 
            {
                // case add or remove generic extensibility element
                change.setDomainElement(true);
                change.setParentComponent(null);
            } else if (! (parentComponent instanceof Documentation)) {
                List<Element> rootToChanged = new ArrayList<Element>(change.getRootToParentPath());
                rootToChanged.add(change.getChangedElement());
                DocumentComponent changedComponent = findComponent(rootToChanged);
                if (changedComponent != null && 
                    changedComponent.getClass().isAssignableFrom(GenericExtensibilityElement.class)) {
                    // case generic extensibility element changed
                    change.markNonDomainChildAsChanged();
                    change.setParentComponent(null);
                }
            }
        } else {
            change.setParentComponent(parentComponent);
        }
        return change;
    }

    @Override
    public SyncUnit prepareSyncUnit(ChangeInfo changes, SyncUnit unit) {
        unit = super.prepareSyncUnit(changes, unit);
        if (unit != null) {
            return new SyncReviewVisitor().review(unit);
        }
        return null;
    }

    @Override
    public AbstractDocumentComponent findComponent(
            AbstractDocumentComponent current,
            List<org.w3c.dom.Element> pathFromRoot, 
            int iCurrent) {
        
        if (current instanceof ExtensibilityElement.EmbeddedModel) {
            ExtensibilityElement.EmbeddedModel emb = (ExtensibilityElement.EmbeddedModel) current;
            AbstractDocumentModel axm = (AbstractDocumentModel) emb.getEmbeddedModel();
            AbstractDocumentComponent embedded = (AbstractDocumentComponent) axm.getRootComponent();
            return axm.findComponent(embedded, pathFromRoot, iCurrent);
        } else {
            return super.findComponent(current, pathFromRoot, iCurrent);
        }
    }

    @Override
    public Map<QName, List<QName>> getQNameValuedAttributes() {
        return WSDLAttribute.getQNameValuedAttributes();
    }

    @Override
    public String toString() {
        ModelSource source = getModelSource();
        if (source != null) {
            Lookup lookup = source.getLookup();
            if (lookup != null) {
                FileObject fileObject = lookup.lookup(FileObject.class);
                if (fileObject != null) {
                    return fileObject.getNameExt();
                }
            }
        }
        return super.toString();
    }

}
