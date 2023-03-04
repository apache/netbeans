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

package org.netbeans.modules.xml.xam;


import org.openide.util.Lookup;
/**
 * This is the class that encapsulates the physical file for each model.
 * @author girix
 */
public class ModelSource implements Lookup.Provider {
    
    private Lookup lookup;
    private boolean editable;
    /* package access */ Throwable creation;
    
    /**
     * Create a model source object given the lookup context.  If editable is false
     * the model cannot be mutated.  Note that editable is static attribute of the
     * model source, and does not reflect the access attribute of the associated file.
     *
     * @param lookup Lookup object associated with this ModelSource. Lookup minimally 
     * contains a File path of the backing file of the model and a javax.swing.text.Document object.
     * @param editable whether the model is supposed to be mutated.
     */
    public ModelSource(Lookup lookup, boolean editable){
        this.editable = editable;
        this.lookup = lookup;
        this.creation = new Throwable();
    }
    
    /**
     * Returns the lookup object associated with this ModelSource. Lookup minimally 
     * contains a File absolute path or FileObject of the backing file of the model 
     * and javax.swing.text.Document object.  If model is DOM, the lookup should 
     * also contains javax.xml.transform.Source object for use in cases of relative 
     * resolution of resource such as validation.
     */
    public Lookup getLookup(){
        return lookup;
    }
    
    /**
     * States if the backing file can be edited.
     * @return true if the model source file is writable.
     */
    public boolean isEditable(){
        return editable;
    }
}
