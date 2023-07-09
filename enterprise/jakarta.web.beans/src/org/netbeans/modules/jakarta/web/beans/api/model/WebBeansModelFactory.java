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
package org.netbeans.modules.jakarta.web.beans.api.model;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation;


/**
 * @author ads
 *
 */
public final class WebBeansModelFactory {

    private WebBeansModelFactory(){
    }
    
    public static synchronized MetadataModel<WebBeansModel> getMetaModel( ModelUnit unit ){
        WeakReference<MetadataModel<WebBeansModel>> reference = MODELS.get( unit );
        MetadataModel<WebBeansModel> metadataModel = null;
        if ( reference != null ){
            metadataModel = reference.get();
        }
        if (  metadataModel == null ){
            metadataModel = createMetaModel(unit);
            if ( reference == null ){
                reference = new WeakReference<MetadataModel<WebBeansModel>>( metadataModel);
            }
            MODELS.put( unit,  reference );
        }
        return metadataModel;
    }
    
    public static MetadataModel<WebBeansModel> createMetaModel( ModelUnit unit ){
        return MetadataModelFactory.createMetadataModel( 
                WebBeansModelImplementation.createMetaModel(unit ));
    }
    
    private static HashMap<ModelUnit, WeakReference<MetadataModel<WebBeansModel>>>
        MODELS = new HashMap<ModelUnit, WeakReference<MetadataModel<WebBeansModel>>>();
}
