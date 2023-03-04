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
package org.netbeans.modules.web.jsf.api.metamodel;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;


/**
 * An entry point for accessing to collection model information 
 * about JSF configuration inside some module.
 * Such module could be JAR file ( with set of faces-config.xml files
 * and annotations ). Also NB project ( not yet packed into war/jar file )
 * could be also such module.
 * 
 * @author ads
 *
 */
public interface JsfModel {
    
    /**
     * Accessor to list of faces-config models.
     * @return set of models found in module
     */
    List<JSFConfigModel> getModels();
    
    FacesConfig getMainConfig();
    
    List<FacesConfig> getFacesConfigs();
    
    /**
     * Generic accessor to top-level elements .
     * @param <T>
     * @param clazz
     * @return list of all elements in merged model.
     */
    <T extends JsfModelElement> List<T> getElements( Class<T> clazz);
    
    /**
     * Register change listener on model elements.
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Unregister change listener on model elements.
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
