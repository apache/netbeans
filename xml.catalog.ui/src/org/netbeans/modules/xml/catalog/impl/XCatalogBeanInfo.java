/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.catalog.impl;

import java.beans.*;
import java.awt.Image;
import org.netbeans.modules.xml.catalog.impl.XCatalog;
import static org.netbeans.modules.xml.catalog.impl.res.Bundle.*;

import org.openide.util.ImageUtilities;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase;

public class XCatalogBeanInfo extends SimpleBeanInfo {

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( XCatalog.class , XCatalogCustomizer.class );
        beanDescriptor.setDisplayName (NAME_x_catalog());
        beanDescriptor.setShortDescription (TEXT_x_catalog_desc());
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
        PropertyDescriptor[] properties = new PropertyDescriptor[4];
        int PROPERTY_source = 0;
        int PROPERTY_displayName = 1;
        int PROPERTY_shortDescription = 2;
        int PROPERTY_icon = 3;
        try {
            properties[PROPERTY_source] = new PropertyDescriptor ( "source", XCatalog.class, "getSource", "setSource" );
            properties[PROPERTY_source].setExpert ( true );
            properties[PROPERTY_source].setDisplayName (PROP_xcatalog_location());
            properties[PROPERTY_source].setShortDescription (PROP_xcatalog_location_desc());
            properties[PROPERTY_displayName] = new PropertyDescriptor ( "displayName", XCatalog.class, "getDisplayName", null );
            properties[PROPERTY_displayName].setDisplayName (PROP_xcatalog_name());
            properties[PROPERTY_displayName].setShortDescription (PROP_xcatalog_name_desc());
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", XCatalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName (PROP_xcatalog_info());
            properties[PROPERTY_shortDescription].setShortDescription (PROP_xcatalog_info_desc());
            properties[PROPERTY_icon] = new IndexedPropertyDescriptor ( "iconResource", XCatalog.class, null, null, "getIconResource", null );
            properties[PROPERTY_icon].setHidden ( true );
        }
        catch( IntrospectionException e) {}                          
        
        // Here you can add code for customizing the properties array.
        
        properties[PROPERTY_shortDescription].setName(CatalogDescriptorBase.PROP_CATALOG_DESC);
        properties[PROPERTY_displayName].setName(CatalogDescriptorBase.PROP_CATALOG_NAME);
        properties[PROPERTY_icon].setName(CatalogDescriptorBase.PROP_CATALOG_ICON);
	return properties;
    }

    public Image getIcon (int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return ImageUtilities.loadImage ("org/netbeans/modules/xml/catalog/impl/xmlCatalog.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
