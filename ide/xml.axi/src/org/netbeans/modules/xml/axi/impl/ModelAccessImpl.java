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
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelAccess;

/**
 * ModelAccess implementation.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ModelAccessImpl extends ModelAccess {
    
    /**
     * Creates a new instance of ModelAccessImpl
     */
    public ModelAccessImpl(AXIModel model) {
        this.model = (AXIModelImpl)model;
    }
        
    public void addUndoableEditListener(UndoableEditListener listener) {
        model.addUndoableEditListener(listener);
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
        model.removeUndoableEditListener(listener);
    }
    
    public void prepareForUndoRedo() {        
    }
    
    public void finishUndoRedo() {
    }
    
    private SchemaModel getSchemaModel() {
	return model.getSchemaModel();
    }
    
    public Model.State sync() throws IOException {
        //build the ref cache and listens to referenced AXIModels
        model.buildReferenceableCache();
        
        //run the validator
        if(!model.validate()) {
            setAutoSync(true);
            return Model.State.NOT_WELL_FORMED;
        }
        
        //initialize the AXIDocument for the first time.
        //and sets auto-sync to true.
        if(!model.isAXIDocumentInitialized()) {
            model.initializeAXIDocument();
            setAutoSync(true);
            return Model.State.VALID;
        }
        
        if(!model.doSync()) {
            return Model.State.NOT_SYNCED;
        }
        
        //if everythings goes well, return a valid state.
	return Model.State.VALID;
    }   
        
    public void flush() {
        try {
            SchemaGeneratorFactory sgf = SchemaGeneratorFactory.getDefault();			
            sgf.updateSchema(model.getSchemaModel(), model.getSchemaDesignPattern());
        } catch (Exception ex) {
            throw new IllegalStateException("Exception during flush: ",ex); //NOI18N
        } finally {
            model.getPropertyChangeListener().clearEvents();
        }
    }
    
    /**
     * Returns length in milliseconds since last edit if the model source buffer 
     * is dirty, or 0 if the model source is not dirty.
     */
    private long dirtyTimeMillis = 0;
    public long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }
    
    public void setDirty() {
        dirtyTimeMillis = System.currentTimeMillis();
    }
    
    public void unsetDirty() {
        dirtyTimeMillis = 0;
    }
    
    private AXIModelImpl model;        
}
