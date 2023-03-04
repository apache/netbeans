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
package org.netbeans.modules.xml.axi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.WeakListeners;


/**
 * Represents an AXI model for a schema.
 * It keeps a map of AXI elements against schema global elements.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelImpl extends AXIModel {
            
    /**
     * Creates a new instance AXIModelImpl.
     */
    public AXIModelImpl(ModelSource modelSource) {
        super(modelSource);
        
        //create and add listener to listen to this model changes
        this.propertyListener = new AXIModelListener();
        addPropertyChangeListener(propertyListener);
        
        //create and add listener to listen to schema model changes
        this.schemaModelListener = new SchemaModelListener(this);
        this.getSchemaModel().addPropertyChangeListener(schemaModelListener);
    }
    
    /**
     * Returns true if the AXIDocument has been initialized, false otherwise.
     */
    boolean isAXIDocumentInitialized() {
        return isAXIDocumentInitialized;
    }
    
    /**
     * Initializes AXIDocument.
     */
    void initializeAXIDocument() {
        AXIDocument doc = getRoot();
        Schema schema = (Schema)doc.getPeer();
        if(schema == null) {
            doc.setPeer(getSchemaModel().getSchema());
        }
        Util.updateAXIDocument(doc);
        isAXIDocumentInitialized = true;
				
        //initialize schema design pattern
        SchemaGenerator.Pattern dp = null;
        if(dp != null)
            setSchemaDesignPattern(dp);
        else
            setSchemaDesignPattern(SchemaGenerator.DEFAULT_DESIGN_PATTERN);
    }

    /**
     * Retunrs true if the specified schema component and this AXI model
     * belong to the same schema model. False otherwise.
     */
    public boolean fromSameSchemaModel(SchemaComponent schemaComponent) {
        return (getSchemaModel() == schemaComponent.getModel());
    }
        
    /**
     * Returns the global AXI component from other AXI model.
     */
    public AXIComponent lookupFromOtherModel(SchemaComponent schemaComponent) {
        if(!schemaComponent.isInDocumentModel())
            return null;
        AXIModelFactory factory = AXIModelFactory.getDefault();
        AXIModelImpl model = (AXIModelImpl)factory.getModel(schemaComponent.getModel());
        return model!=null?model.lookup(schemaComponent):null;
    }    
    
    /**
     * Returns the global AXI component against the specified global schema component.
     */
    public AXIComponent lookup(SchemaComponent schemaComponent) {
        return ((AXIDocumentImpl)getRoot()).findChild(schemaComponent);
    }
        
    /**
     * Check if sync is really required or not. True for the very first time.
     * Returns false, if user started a design pattern transformation or if the
     * model was mutated inside a transaction. Else, true if the listeners have
     * accumulated events.
     */
    protected boolean needsSync() {
        if(isForceSync)
            return true;
        if(!isAXIDocumentInitialized())
            return true;
        
        if(designPatternMode || isIntransaction()) {
            return false;
        }
        
        if(axiModelListener != null)
           return axiModelListener.needsSync() || schemaModelListener.needsSync();
        
        return schemaModelListener.needsSync();
    }
        
    void disableAutoSync() {
        designPatternMode = true;
        super.getAccess().setAutoSync(false);
    }
    
    void enableAutoSync() {
        designPatternMode = false;
        super.getAccess().setAutoSync(true);
    }
    
    /**
     * Sync started.
     */
    protected void syncStarted() {
        try {
            getSchemaModel().sync();
        } catch(IOException ex) {
            setState(Model.State.NOT_SYNCED);
        }
    }
    
    /**
     * Finished sync. Clear the event lists.
     */
    protected void syncCompleted() {
        schemaModelListener.syncCompleted();
        if(axiModelListener != null)
            axiModelListener.syncCompleted();
        propertyListener.clearEvents();
    }
    
    @Override
    public void sync() {
        //fix for issue 155428.
        //lock schemaModel (below) when there is a need.
        if(!needsSync())
            return;
        try {
            synchronized(getSchemaModel()) {
                super.sync();
            }
        } catch(IOException ex) {
            setState(Model.State.NOT_SYNCED);
        } finally {
            setForceSync(false);
        }
    }

    /**
     * This is where the actual sync starts.
     * Returns true if success, false if failed.
     */
    public synchronized boolean doSync() {
        //finally sync itself.
        AXIModelUpdater updater = new AXIModelUpdater(this);
        return updater.doSync();        
    }
    
    public SchemaGenerator.Pattern getSchemaDesignPattern() {
        return schemaDesignPattern;
    }
	
    public void setSchemaDesignPattern(SchemaGenerator.Pattern schemaDesignPattern) {
        this.schemaDesignPattern = schemaDesignPattern;
    }
	
    /**
     * Returns the PCL who listens to this model changes.
     */
    public AXIModelListener getPropertyChangeListener() {
        return propertyListener;
    }
    
    /**
     * Runs the validator to see if the schema is valid or not.
     * Returns true for a valid schema, false otherwise.
     */
    public boolean validate() {
        Model.State state = getSchemaModel().getState();
        if(state != SchemaModel.State.VALID) {
            return false;
        }
        
        return true;
    }    
    
    public List<AXIModel> getReferencedModels() {
        List<AXIModel> models = Collections.emptyList();
        Schema schema = getSchemaModel().getSchema();
        if(schema == null)
            return models;
        Collection<SchemaModelReference> refs = schema.getSchemaReferences();
        if(refs == null || refs.size() == 0) {
            return models;
        }
        models = new ArrayList<AXIModel>();
        Iterator<SchemaModelReference> iter = refs.iterator();
        while(iter.hasNext()) {
            try {
                SchemaModelReference ref = iter.next();
                Schema s = ref.resolveReferencedModel().getSchema();
                AXIModel m = AXIModelFactory.getDefault().
                        getModel(ref.resolveReferencedModel());
                models.add(m);
            } catch (CatalogModelException ex) {
                //will not be added to the list
            }
        }

        return Collections.unmodifiableList(models);
    }
            
    /**
     * Builds referenceable cache and listens to appropriate
     * referenced models. This gets updated in each sync.
     */
    void buildReferenceableCache() {
        listenerMap.clear();
        Schema schema = getSchemaModel().getSchema();
        if(schema == null)
            return;
        buildCache(schema);
        Collection<SchemaModelReference> refs = schema.getSchemaReferences();
        if(refs == null || refs.size() == 0)
            return;
        Iterator<SchemaModelReference> iter = refs.iterator();
        while(iter.hasNext()) {
            try {
                SchemaModelReference ref = iter.next();
                Schema s = ref.resolveReferencedModel().getSchema();
                buildCache(s);
                if(getRoot().canVisitChildren()) {
                    AXIModel m = AXIModelFactory.getDefault().
                            getModel(ref.resolveReferencedModel());
                    //listen to other model, only if 
                    //the doc was expanded earlier.
                    listenToReferencedModel(m);
                }
            } catch (Exception ex) {
                continue;
            }
        }
    }

    public void listenToReferencedModel(AXIModel model) {
        if(listenerMap.get(model) == null) {
            ComponentListener listener = (ComponentListener)WeakListeners.
                    create(ComponentListener.class, axiModelListener, model);
            model.addComponentListener(listener);
            listenerMap.put(model, listener);
        }
    }
       
    /**
     * Updates the cache of all referenceable for a given schema file.
     * Note: the key in the map is namespace:name.
     */
    private void buildCache(Schema schema) {
        for(SchemaComponent child : schema.getChildren()) {
            if(child instanceof NamedReferenceable) {
                NamedReferenceable ref = (NamedReferenceable)child;
                mapReferenceable.put(schema.getTargetNamespace() + ":" + ref.getName(), ref);
            }
        }
    }
    
    /**
     * Fetches an item from the cache of all referenceable.
     * Note: the key in the map is namespace:name.
     */
    public SchemaComponent getReferenceableSchemaComponent(NamedComponentReference ncr) {
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        Class<? extends NamedReferenceable> elementType = ncr.getType();
        NamedReferenceable ref = mapReferenceable.get(name);
        if(ref != null && elementType.isAssignableFrom(ref.getClass())) {
            return (SchemaComponent)ref;
        }
        
        return (SchemaComponent)ncr.get();
    }
        
    public String toString() {
        if(getRoot() == null)
            return null;
        
        return getRoot().getTargetNamespace();
    }

    public void setForceSync(boolean b) {
        isForceSync = b;
    }
    
    public AXIComponent findChild(SchemaComponent child) {
        AXIDocumentImpl doc = (AXIDocumentImpl)getRoot();
        return doc.findChild(child);
    }
    

    /**
     * PCL to be used by code generator.
     */
    private AXIModelListener propertyListener;
	
    /*
     * Schema model listener.
     */
    private SchemaModelListener schemaModelListener; 

    /*
     * AXI Model listener to listen to other AXI models.
     */
    private OtherAXIModelListener axiModelListener = new OtherAXIModelListener(this);
    private WeakHashMap<AXIModel, ComponentListener> listenerMap = 
            new WeakHashMap<AXIModel, ComponentListener>();
        
    /*
     * True, when design patten transformation is being carried out.
     */
    private boolean designPatternMode = false;
        
    /*
     * Flag to indicate if the AXIDocument was initialized or no.
     */
    private boolean isAXIDocumentInitialized = false;

    private SchemaGenerator.Pattern schemaDesignPattern;
    private HashMap<String, NamedReferenceable> mapReferenceable =
            new HashMap<String, NamedReferenceable>();

    private boolean isForceSync = false;
}
