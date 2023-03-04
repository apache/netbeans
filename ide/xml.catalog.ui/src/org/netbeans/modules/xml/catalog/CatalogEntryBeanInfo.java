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
package org.netbeans.modules.xml.catalog;

import java.beans.*;
import java.awt.Image;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CatalogEntryBeanInfo extends SimpleBeanInfo {

    private static final String ICON_DIR_BASE = "org/netbeans/modules/xml/catalog/resources/"; // NOI18N

    private static final String PUBLICID_N = NbBundle.getMessage(CatalogEntryBeanInfo.class, "PROP_public_id");
    private static final String PUBLICID_D = NbBundle.getMessage(CatalogEntryBeanInfo.class, "PROP_public_id_desc");
    private static final String SYSTEMID_D = NbBundle.getMessage(CatalogEntryBeanInfo.class, "PROP_system_id_desc");
    private static final String SYSTEMID_N = NbBundle.getMessage(CatalogEntryBeanInfo.class, "PROP_system_id");
    private static final String URI_D = NbBundle.getMessage(CatalogEntryBeanInfo.class, "PROP_uri_desc");
    private static final String URI_N = NbBundle.getMessage(CatalogEntryBeanInfo.class, "PROP_uri");
    
    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        int PROPERTY_publicID = 0;
        int PROPERTY_systemID = 1;
        int PROPERTY_URI = 2;
        PropertyDescriptor[] properties = new PropertyDescriptor[3];

        try {
            properties[PROPERTY_publicID] = new PropertyDescriptor ( "publicID", CatalogEntry.class, "getPublicIDValue", null ); // NOI18N
            properties[PROPERTY_publicID].setDisplayName ( PUBLICID_N );
            properties[PROPERTY_publicID].setShortDescription ( PUBLICID_D );
            properties[PROPERTY_systemID] = new PropertyDescriptor ( "systemID", CatalogEntry.class, "getSystemIDValue", null ); // NOI18N
            properties[PROPERTY_systemID].setDisplayName ( SYSTEMID_N );
            properties[PROPERTY_systemID].setShortDescription ( SYSTEMID_D );
            properties[PROPERTY_URI] = new PropertyDescriptor ( "uri", CatalogEntry.class, "getUriValue", null ); // NOI18N
            properties[PROPERTY_URI].setDisplayName ( URI_N );
            properties[PROPERTY_URI].setShortDescription ( URI_D );
        }
        catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return properties;
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return new EventSetDescriptor[0];
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return  An array of MethodDescriptors describing the methods 
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }

    /**
     * This method returns an image object that can be used to
     * represent the bean in toolboxes, toolbars, etc.   Icon images
     * will typically be GIFs, but may in future include other formats.
     * <p>
     * Beans aren't required to provide icons and may return null from
     * this method.
     * <p>
     * There are four possible flavors of icons (16x16 color,
     * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
     * support a single icon we recommend supporting 16x16 color.
     * <p>
     * We recommend that icons have a "transparent" background
     * so they can be rendered onto an existing background.
     *
     * @param  iconKind  The kind of icon requested.  This should be
     *    one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32, 
     *    ICON_MONO_16x16, or ICON_MONO_32x32.
     * @return  An image object representing the requested icon.  May
     *    return null if no suitable icon is available.
     */
    public Image getIcon (int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return ImageUtilities.loadImage (ICON_DIR_BASE + "catalog-entry.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
