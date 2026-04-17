/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.axi;

import java.util.List;
import org.netbeans.modules.xml.axi.impl.AXIDocumentImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelBuilderQuery;
import org.netbeans.modules.xml.axi.impl.ModelAccessImpl;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.ModelAccess;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Component;
import org.openide.filesystems.FileObject;

/**
 * Represents an AXI model for a schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIModel extends AbstractModel<AXIComponent> {

    /**
     * Creates a new instance AXIModel.
     */
    public AXIModel(ModelSource modelSource) {
        super(modelSource);
        this.factory = new AXIComponentFactory(this);
        this.root = new AXIDocumentImpl(this, getSchemaModel().getSchema());
        this.modelAccess = new ModelAccessImpl(this);
    }
    
    /**
     * Returns other AXIModels this model refers to.
     */
    public abstract List<AXIModel> getReferencedModels();
            
    /**
     * Returns the schema design pattern property.
     */	
    public abstract SchemaGenerator.Pattern getSchemaDesignPattern();
	
    /**
     * Sets the schema design pattern property.
     */	
    public abstract void setSchemaDesignPattern(SchemaGenerator.Pattern p);
    
    /**
     * Returns the corresponding SchemaModel.
     * @return Returns the corresponding SchemaModel.
     */
    public SchemaModel getSchemaModel() {
        return (SchemaModel)getModelSource().getLookup().
                lookup(SchemaModel.class);
    }
        
    /**
     * Returns the root of the AXI model.
     */
    public AXIDocument getRoot() {
        return root;
    }
    
    /**
     * Returns the component factory.
     */
    public AXIComponentFactory getComponentFactory() {
        return factory;
    }
    
    /**
     * Returns true if the underlying document is read-only, false otherwise.
     */
    public boolean isReadOnly() {
        ModelSource ms = getModelSource();
        assert(ms != null);
        if (ms.isEditable()) {
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            assert(fo != null);
            return !fo.canWrite();
        }
        return true;
    }
    
    /**
     * Returns true if there exists a corresponding visible AXIComponent.
     */
    public boolean canView(SchemaComponent component) {
        AXIModelBuilderQuery factory = new AXIModelBuilderQuery((AXIModelImpl)this);
        return factory.canView(component);
    }

    // XAM methods
    public ModelAccess getAccess() {
        return modelAccess;
    }
    
    public void addChildComponent(Component parent, Component child, int index) {
        AXIComponent axiParent = (AXIComponent)parent;
        AXIComponent axiChild = (AXIComponent)child;
        axiParent.addChildAtIndex(axiChild, index);
    }

    public void removeChildComponent(Component child) {
        AXIComponent axiChild = (AXIComponent)child;        
        AXIComponent axiParent = axiChild.getParent();
        axiParent.removeChild(axiChild);
    }
    
    // member variables
    /**
     * Keeps a component factory.
     */
    private AXIComponentFactory factory;
    
    /**
     * ModelAccess
     */
    private ModelAccess modelAccess;
    
    /**
     * Root of the AXI tree.
     */
    private AXIDocument root;
}
