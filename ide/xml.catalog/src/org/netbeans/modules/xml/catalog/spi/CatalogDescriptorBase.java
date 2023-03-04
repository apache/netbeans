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
package org.netbeans.modules.xml.catalog.spi;

import java.beans.*;

/**
 * It provides information about a catalog instance.
 * Information about the class can be provided as BeanInfo.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface CatalogDescriptorBase {

    /**
     * Name of icon property if fired.
     */
    public static final String PROP_CATALOG_ICON = "ca-icon"; // NOI18N

    /**
     * Name of name property
     */
    public static final String PROP_CATALOG_NAME = "ca-name"; // NOI18N

    /**
     * Name of short description property
     */
    public static final String PROP_CATALOG_DESC = "ca-desc"; // NOI18N
    
    /**
     * @return I18N display name
     */
    public String getDisplayName();
    
    /**
     * @return I18N short description
     */
    public String getShortDescription();
    
    /** Registers new listener. */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /** Unregister the listener. */
    public void removePropertyChangeListener(PropertyChangeListener l);
}
