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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Factory class to create an AXI model.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelFactory extends AbstractModelFactory<AXIModel> {
            
    /**
     * Creates a new instance of AXIModelFactory
     */
    private AXIModelFactory() {
    }
    
    /**
     * Return the single factory instance.
     */
    public static AXIModelFactory getDefault() {
        return instance;
    }
    
    /**
     * Convenient method to get the AXI model.
     */
    public AXIModel getModel(SchemaModel schemaModel) {
        FileObject file = (FileObject)schemaModel.getModelSource().
                getLookup().lookup(FileObject.class);
        Lookup lookup = null;
        if(file == null) {
            Object[] objectsToLookup = {schemaModel};
            lookup = Lookups.fixed(objectsToLookup);
        } else {
            Object[] objectsToLookup = {schemaModel, file};
            lookup = Lookups.fixed(objectsToLookup);
        }
        ModelSource source = new ModelSource(lookup, true);
        assert(source != null);
        return getModel(source);
    }    
    
    /**
     * Get model from given model source.  Model source should at very least 
     * provide lookup for SchemaModel
     */
    protected AXIModel getModel(ModelSource modelSource) {
        Lookup lookup = modelSource.getLookup();
        assert lookup.lookup(SchemaModel.class) != null;
        return super.getModel(modelSource);
    }
    
    /**
     * For AXI, SchemaModel is the key.
     */
    protected Object getKey(ModelSource modelSource) {
        return modelSource.getLookup().lookup(SchemaModel.class);
    }
    
    /**
     * Creates the AXI model here.
     */
    protected AXIModel createModel(ModelSource modelSource) {
        return new AXIModelImpl(modelSource);
    }
    
    /////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    /////////////////////////////////////////////////////////////////////
    /**
     * Singleton instance of the factory class.
     */
    private static AXIModelFactory instance = new AXIModelFactory();
}
