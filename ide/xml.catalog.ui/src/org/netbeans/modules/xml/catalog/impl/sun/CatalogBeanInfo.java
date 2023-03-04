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
package org.netbeans.modules.xml.catalog.impl.sun;

import java.beans.*;
import java.awt.Image;
import org.netbeans.modules.xml.catalog.impl.sun.Catalog;
import static org.netbeans.modules.xml.catalog.impl.sun.res.Bundle.*;

import org.openide.util.ImageUtilities;
import org.netbeans.modules.xml.catalog.spi.*;

public class CatalogBeanInfo extends SimpleBeanInfo {

    private static final String ICON_DIR_BASE = "org/netbeans/modules/xml/catalog/impl/sun/"; // NOI18N

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
	BeanDescriptor beanDescriptor = new BeanDescriptor  ( Catalog.class , CatalogCustomizer.class );
        beanDescriptor.setDisplayName (PROP_Catalog());
        beanDescriptor.setShortDescription (PROP_Catalog_desc());

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;
    }

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
        int PROPERTY_displayName = 0;
        int PROPERTY_shortDescription = 1;
        int PROPERTY_icon = 2;
        PropertyDescriptor[] properties = new PropertyDescriptor[3];

        try {
            properties[PROPERTY_displayName] = new PropertyDescriptor ( "displayName", Catalog.class, "getDisplayName", null );
            properties[PROPERTY_displayName].setDisplayName (PROP_catalog_name());
            properties[PROPERTY_displayName].setShortDescription (PROP_catalog_name_desc());
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", Catalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName (PROP_catalog_info());
            properties[PROPERTY_shortDescription].setShortDescription (PROP_catalog_info_desc());
            properties[PROPERTY_icon] = new IndexedPropertyDescriptor ( "iconResource", Catalog.class, null, null, "getIconResource", null );
            properties[PROPERTY_icon].setHidden ( true );
        }
        catch( IntrospectionException e) {}
        properties[PROPERTY_shortDescription].setName(CatalogDescriptorBase.PROP_CATALOG_DESC);
        properties[PROPERTY_displayName].setName(CatalogDescriptorBase.PROP_CATALOG_NAME);
        properties[PROPERTY_icon].setName(CatalogDescriptorBase.PROP_CATALOG_ICON);

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

            return ImageUtilities.loadImage (ICON_DIR_BASE + "sunCatalog.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
