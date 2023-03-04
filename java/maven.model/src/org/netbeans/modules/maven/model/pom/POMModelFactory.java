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
package org.netbeans.modules.maven.model.pom;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.maven.model.pom.impl.POMModelImpl;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
public class POMModelFactory extends AbstractModelFactory<POMModel> {
    
    private static final POMModelFactory modelFactory = new POMModelFactory();
    
    public static POMModelFactory getDefault(){
        return modelFactory;
    }
    
    /** Creates a new instance of POMModelFactory */
    private POMModelFactory() {
    }

    /**
     * Gets domain model from given model source. Model source should 
     * provide lookup for:
     * 1. FileObject of the model source
     * 2. DataObject represent the model
     * 3. Swing Document buffer for in-memory text of the model source
     */
    @Override
    public POMModel getModel(ModelSource source) {
        if (source == null) return null;
        Lookup lookup = source.getLookup();
        if (lookup.lookup(Document.class) == null) {
            Logger.getLogger(POMModelFactory.class.getName()).log(Level.WARNING, "no Document instance found in {0}", lookup);
        }
        return super.getModel(source);
    }
    
    @Override
    protected POMModel createModel(ModelSource source) {
        return new POMModelImpl(source);
    }
}
