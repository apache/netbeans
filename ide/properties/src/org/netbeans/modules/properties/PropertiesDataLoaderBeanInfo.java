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


package org.netbeans.modules.properties;


import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;

import org.openide.loaders.MultiFileLoader;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** BeanInfo for properties loader.
 *
 * @author Ian Formanek
 */
public final class PropertiesDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    /**
     * @return Returns an array of PropertyDescriptors
     * describing the editable properties supported by this bean. */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {

            PropertyDescriptor p2 = new PropertyDescriptor(
                "extensions", // NOI18N
                PropertiesDataLoader.class,
                "getExtensions", // NOI18N
                "setExtensions"); // NOI18N

            p2.setDisplayName(NbBundle.getBundle(PropertiesDataLoaderBeanInfo.class).getString("PROP_Ext"));
            p2.setShortDescription(NbBundle.getBundle(PropertiesDataLoaderBeanInfo.class).getString("HINT_Ext"));

            return new PropertyDescriptor[] {p2};
        } catch(IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            
            return null;
        }
    }

    /** @param type Desired type of the icon
     * @return returns the properties loader's icon */
    public Image getIcon(final int type) {
        if((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            return ImageUtilities.loadImage("org/netbeans/modules/properties/propertiesObject.png"); // NOI18N
        } else {
            return ImageUtilities.loadImage("org/netbeans/modules/properties/propertiesObject32.gif"); // NOI18N
        }
    }
}
