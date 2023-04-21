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

import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.modules.jakarta.web.beans.impl.model.BeansModelImpl;


/**
 * @author ads
 *
 */
public final class BeansModelFactory {
    
    private BeansModelFactory(){
    }

    public static BeansModel createModel( ModelUnit unit ){
        return new BeansModelImpl(unit);
    }
    
    public static synchronized BeansModel getModel( ModelUnit unit ){
        BeansModel model = MODELS.get( unit );
        if ( model == null ){
            model = createModel( unit );
            MODELS.put(unit, model);
        }
        return model;
    }
    
    private static final Map<ModelUnit, BeansModel> MODELS = 
        new WeakHashMap<ModelUnit, BeansModel>();
}
