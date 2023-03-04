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

package org.netbeans.modules.web.jsf.spi.components;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Provides all {@link JsfComponentImplementation}s in given {@link JsfComponentProvider}.
 * JSF Support searches in registered {@link JsfComponentProvider}s for all 
 * {@link JsfComponentImplementation}s to offer them as JSF component libraries.
 * 
 * <p>
 * Every JSF suite module should contain only one {@link JsfComponentProvider} which 
 * can return more {@link JsfComponentImplementation}s.
 * 
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 * 
 * @since 1.27
 */
public interface JsfComponentProvider {

    public static final String COMPONENTS_PATH = "j2ee/jsf/components";    //NOI18N
    
    /**
     * Get all registered {@link JsfComponentImplementation}s  contained in the module.
     * 
     * @return a set of registered {@link JsfComponentImplementation}s; never null.
     */
    @NonNull
    public Collection<JsfComponentImplementation> getJsfComponents();
    
}
