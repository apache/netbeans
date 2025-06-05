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
/*
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.websvc.rest.model.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;

public interface RestServices extends RootInterface {

    /** Property fired when list of services changes. */
    public static final String PROP_SERVICES = "/restservices";

    /** Property fired when list of providers changes. */
    public static final String PROP_PROVIDERS = "providers";

    RestServiceDescription[] getRestServiceDescription();
    
    RestServiceDescription getRestServiceDescription(String name);

    /**
     * Return list of providers, that is classes annotated with @javax.ws.rs.ext.Provider
     */
    Collection<? extends RestProviderDescription> getProviders();

    int sizeRestServiceDescription();
    
    @Override
    void addPropertyChangeListener(PropertyChangeListener pcl);
    
    @Override
    void removePropertyChangeListener(PropertyChangeListener pcl);
    
    void disablePropertyChangeListener();
    
    void enablePropertyChangeListener();
}
