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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
