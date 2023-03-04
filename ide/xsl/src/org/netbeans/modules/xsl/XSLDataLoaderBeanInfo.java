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
package org.netbeans.modules.xsl;

import java.beans.*;
import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Loader BeanInfo adding metadata missing in org.openide.loaders.MultiFileLoaderBeanInfo.
 *
 * @author Libor Kramolis
 */
public class XSLDataLoaderBeanInfo extends SimpleBeanInfo {
    /* Icon base dir. */
    private static final String ICON_DIR_BASE = "org/netbeans/modules/xsl/resources/"; // NOI18N

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( XSLDataLoader.class , null );
        beanDescriptor.setDisplayName ( NbBundle.getMessage(XSLDataLoaderBeanInfo.class, "NAME_XSLDataLoader") );                              
        
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
        int PROPERTY_extensions = 0;
        PropertyDescriptor[] properties = new PropertyDescriptor[1];
    
        try {
            properties[PROPERTY_extensions] = new PropertyDescriptor (
                    "extensions", XSLDataLoader.class, "getExtensions", "setExtensions" );
            properties[PROPERTY_extensions].setDisplayName ( NbBundle.
                    getMessage(XSLDataLoaderBeanInfo.class, "PROP_XSL_Extensions") );
            properties[PROPERTY_extensions].setShortDescription ( NbBundle.
                    getMessage(XSLDataLoaderBeanInfo.class, "HINT_XSL_Extensions") );
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
    
    /** @param type Desired type of the icon
     * @return returns the Entity loader's icon
     */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return ImageUtilities.loadImage (ICON_DIR_BASE + "xslObject.gif"); // NOI18N
        } else {
            return ImageUtilities.loadImage (ICON_DIR_BASE + "xslObject32.gif"); // NOI18N
        }
    }

    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {
                java.beans.Introspector.getBeanInfo (MultiFileLoader.class)
            };
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return super.getAdditionalBeanInfo();
    }

}
