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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.schema.model.impl.resolver.ChamelionResolver;
import org.netbeans.modules.xml.schema.model.impl.resolver.ImportResolver;
import org.netbeans.modules.xml.schema.model.impl.resolver.IncludeResolver;

/**
 *
 * @author Vidhya Narayanan
 */
public class SchemaModelImpl extends AbstractDocumentModel<SchemaComponent> implements SchemaModel {

    private SchemaImpl mSchema;
    private SchemaComponentFactory csef;
    private RefCacheSupport mRefCacheSupport;
    private GlobalComponentsIndexSupport mGlobalIndexSupport;
    
    public SchemaModelImpl(ModelSource modelSource) {
        super(modelSource);
        //
        csef = new SchemaComponentFactoryImpl(this);
        //getAccess().setAutoSync(true);
        mRefCacheSupport = new RefCacheSupport(this);
        mGlobalIndexSupport = new GlobalComponentsIndexSupport(this);
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public RefCacheSupport getRefCacheSupport() {
        return mRefCacheSupport;
    }
    
    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public GlobalComponentsIndexSupport getGlobalComponentsIndexSupport() {
        return mGlobalIndexSupport;
    }

    /**
     *
     *
     * @return the schema represented by this model. The returned schema
     * instance will be valid and well formed, thus attempting to update
     * from a document which is not well formed will not result in any changes
     * to the schema model.
     */
    @Override
    public SchemaImpl getSchema() {
        return (SchemaImpl)getRootComponent();
    }
    
    /**
     *
     *
     * @return common schema element factory valid for this instance
     */
    @Override
    public SchemaComponentFactory getFactory() {
        return csef;
    }

    @Override
    public SchemaComponent createRootComponent(org.w3c.dom.Element root) {
        SchemaImpl newSchema = (SchemaImpl)csef.create(root, null);
        if (newSchema != null) {
            mSchema = newSchema;
        } else {
            return null;
        }
        return getSchema();
    }

    @Override
    public SchemaComponent getRootComponent() {
        return mSchema;
    }

    @Override
    public <T extends NamedReferenceable>
            T resolve(String namespace, String localName, Class<T> type) 
    {
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace) &&
            XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(getSchema().getTargetNamespace())) {
            return resolveImpl(namespace, localName, type);
        }
        
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)) {
            SchemaModel sm = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            return sm.findByNameAndType(localName, type);
        }
        
        return resolveImpl(namespace, localName, type);
    }

    protected <T extends NamedReferenceable> T resolveImpl(
            String namespace, String localName, Class<T> type) {
        //
        if (getState() != State.VALID) {
            return null;
        }
        //
        Schema mySchema = getSchema();
        if (mySchema == null) {
            return null;
        }
        //
        String targetNamespace = mySchema.getTargetNamespace();
        //
        if (Util.equal(targetNamespace, namespace)) { // Both can be null
            return IncludeResolver.resolve(this, namespace, localName, type);
        } else {
            if (targetNamespace == null) {
                return ChamelionResolver.resolve(this, namespace, localName, type);
            } else {
                return ImportResolver.resole(this, namespace, localName, type);
            }
        }
    }

    /**
     * Resolves reference to another model with the help of references' cache.
     * Calling ref.resolveReferencedModel() does time consuming resolve.
     * So it is recommended to use this method whenever it possible.
     *
     * @param ref
     * @return
     */
    public SchemaModelImpl resolve(SchemaModelReference ref) {
        if (mRefCacheSupport == null) {
            try {
                return (SchemaModelImpl) ref.resolveReferencedModel();
            } catch (CatalogModelException ex) {
                // Do nothing here. Exception means that the reference can't be resolved
                return null;
            }
        } else {
            return mRefCacheSupport.optimizedResolve(ref);
        }
    }

    public Collection<SchemaModelReference> getSchemaModelReferences() {
        Collection<SchemaModelReference> refs = new ArrayList<SchemaModelReference>();
        Schema schema = getSchema();
        if (schema != null) {
            refs.addAll(schema.getRedefines());
            refs.addAll(schema.getIncludes());
            refs.addAll(schema.getImports());
        }
        return refs;
    }
    
    public Collection<SchemaModelReference> getNotImportRefrences() {
        Collection<SchemaModelReference> refs = new ArrayList<SchemaModelReference>();
        Schema schema = getSchema();
        if (schema != null) {
            refs.addAll(schema.getRedefines());
            refs.addAll(schema.getIncludes());
        }
        return refs;
    }

    @Override
    public <T extends NamedReferenceable> T findByNameAndType(String localName, Class<T> type) {
        return mGlobalIndexSupport.findByNameAndType(localName, type);
    }

    @Override
    public Set<Schema> findSchemas(String namespace) {
        Set<Schema> result = new HashSet<Schema>();
        
        // build-in XSD schema is always visible
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)){
            SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            result.add(primitiveModel.getSchema());
            return result;
        } 
        
        return _findSchemas(namespace, result, null);
    }
    
    protected enum ReferenceType { IMPORT, INCLUDE, REDEFINE }
    
    Set<Schema> _findSchemas(String namespace, Set<Schema> result, ReferenceType refType) {
        SchemaImpl schema = getSchema();
        // schema could be null, if last sync throwed exception
        if (schema == null) {
            return result;
        }
        
        String targetNamespace = schema.getTargetNamespace();
        if (targetNamespace != null && targetNamespace.equals(namespace) ||
            targetNamespace == null && namespace == null) {
            result.add(schema);
        }
        
        if (refType != ReferenceType.IMPORT) {
            checkIncludeSchemas(namespace, result);
            checkRedefineSchemas(namespace, result);
            checkImportedSchemas(namespace, result);
        }
        
        return result;    
    }
    
    private void checkIncludeSchemas(String namespace, Set<Schema> result) {
        Collection<Include> includes = getSchema().getIncludes();
        for (Include include : includes) {
            try {
                SchemaModel model = include.resolveReferencedModel();
                if (model.getState() == Model.State.NOT_WELL_FORMED) {
                    continue;            
                }
                
                if (! result.contains(model.getSchema())) {
                    result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.INCLUDE));
                }
            } catch (CatalogModelException ex) {
                // ignore this exception to proceed with search
            }
        }
    }
    
    private void checkRedefineSchemas(String namespace, Set<Schema> result) {
        Collection<Redefine> redefines = getSchema().getRedefines();
        for (Redefine redefine : redefines) {
	       try {
		   SchemaModel model = redefine.resolveReferencedModel();
           if (model.getState() == Model.State.NOT_WELL_FORMED)
                continue;
            
		   if (! result.contains(model.getSchema())) {
		       result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.REDEFINE));
		   }
	       } catch (CatalogModelException ex) {
		   // ignore this exception to proceed with search
	       }
	   }
    }
    
    private void checkImportedSchemas(String namespace, Set<Schema> result) {
        Collection<Import> imports = getSchema().getImports();
        for (Import imp : imports) {
		try {
		    SchemaModel model = imp.resolveReferencedModel();
            if (model.getState() == Model.State.NOT_WELL_FORMED)
                continue;
            
		   if (! result.contains(model.getSchema())) {
		       result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.IMPORT));
		   }
		} catch (CatalogModelException ex) {
		    // ignore this exception to proceed with search
		}
	    }
    }
    
    /**
	 * This api returns the effective namespace for a given component. 
	 * If given component has a targetNamespace different than the 
	 * this schema, that namespace is returned. The special case is that if
	 * the targetNamespace of the component is null, there is no target
	 * namespace defined, then the import statements for this file are 
	 * examined to determine if this component is directly or indirectly 
	 * imported. If the component is imported, then null if returned 
	 * otherwise the component is assumed to be included or redefined and
	 * the namespace of this schema is returned. 
     */
    @Override
    public String getEffectiveNamespace(SchemaComponent component) {
	SchemaModel componentModel = component.getModel();
	Schema schema = getSchema();
        Schema componentSchema = componentModel.getSchema();
        String tns = schema.getTargetNamespace();
        String componentTNS = componentSchema.getTargetNamespace();
	if (this == componentModel) {
	    return tns;
        } else if (componentTNS == null && tns != null) {
            // only include/redefine model can assum host model targetNamespace
            // so check if is from imported to just return null
	    Collection<Import> imports = schema.getImports();
	    for (Import imp: imports) {
		SchemaModel m = null;
		try {
		    m = imp.resolveReferencedModel();
		} catch (CatalogModelException ex) {
		    // the import cannot be resolved 
		}
		if(componentModel.equals(m)) {
		    return null;
		}
                if (m == null || m.getState() == Model.State.NOT_WELL_FORMED) {
                    continue;
                }
                String importedTNS = m.getSchema().getTargetNamespace();
                if (importedTNS == null) continue;
                Set<Schema> visibleSchemas = findSchemas(importedTNS);
                for (Schema visible : visibleSchemas) {
                    if (componentModel.equals(visible.getModel()))  {
                        return null;
                    }
                }
	    }
            return tns;
    	} else {
            return componentTNS;
        }
    }

    @Override
    public SchemaComponent createComponent(SchemaComponent parent, org.w3c.dom.Element element) {
       return csef.create(element, parent);
    }

    @Override
    protected ComponentUpdater<SchemaComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    @Override
    public Set<QName> getQNames() {
        return SchemaElements.allQNames();
    }
    
    @Override
    public SyncUnit prepareSyncUnit(ChangeInfo changes, SyncUnit unit) {
        unit = super.prepareSyncUnit(changes, unit);
        if (unit != null) {
            return new SyncUnitReviewVisitor().review(unit);
        }
        return null;
    }
    
    private final AtomicBoolean atomicCalled = new AtomicBoolean(false);
    
    @Override
    public DocumentModelAccess getAccess() {
        DocumentModelAccess acc = super.getAccess();
        if (atomicCalled.getAndSet(true) == false) {
            super.getAccess().setAutoSync(true);  // default autosync true
        }
        return acc;
    }
    
    @Override
    public Map<QName,List<QName>> getQNameValuedAttributes() {
        return SchemaAttributes.getQNameValuedAttributes();
    }

    @Override
    public boolean isEmbedded() {
        return false;
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
