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

package org.netbeans.modules.xml.catalog.user;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.NbBundle;

/**
 * description of {@link UserXMLCatalog}.
 * @author Milan Kuchtiak
 */
public class UserXMLCatalogBeanInfo extends SimpleBeanInfo {

    public UserXMLCatalogBeanInfo() {}

    private static final int PROPERTY_displayName = 0;
    private static final int PROPERTY_shortDescription = 1;

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties = new PropertyDescriptor[2];
    
        try {
            properties[PROPERTY_displayName] = new PropertyDescriptor ("displayName", UserXMLCatalog.class, "getDisplayName", null);
            properties[PROPERTY_displayName].setDisplayName (NbBundle.getMessage(UserXMLCatalog.class,"PROP_catalog_name"));
            properties[PROPERTY_displayName].setShortDescription (NbBundle.getMessage(UserXMLCatalog.class,"HINT_catalog_name"));
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", UserXMLCatalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName (NbBundle.getMessage(UserXMLCatalog.class,"PROP_catalog_desc"));
            properties[PROPERTY_shortDescription].setShortDescription (NbBundle.getMessage(UserXMLCatalog.class,"HINT_catalog_desc"));
        } catch( java.beans.IntrospectionException e) {
        }
        return properties;
    }
    
}
