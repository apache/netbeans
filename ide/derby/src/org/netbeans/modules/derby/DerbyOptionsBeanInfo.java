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

package org.netbeans.modules.derby;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public class DerbyOptionsBeanInfo extends SimpleBeanInfo {

    public DerbyOptionsBeanInfo() {
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] descriptors = new PropertyDescriptor[2];
            descriptors[0] = new PropertyDescriptor(DerbyOptions.PROP_DERBY_LOCATION, DerbyOptions.class);
            descriptors[0].setDisplayName(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "LBL_DerbyLocation"));
            descriptors[0].setShortDescription(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "HINT_DerbyLocation"));
            descriptors[1] = new PropertyDescriptor(DerbyOptions.PROP_DERBY_SYSTEM_HOME, DerbyOptions.class);
            descriptors[1].setDisplayName(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "LBL_DatabaseLocation"));
            descriptors[1].setShortDescription(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "HINT_DatabaseLocation"));
            return descriptors;
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            return new PropertyDescriptor[0];
        }
    }
    
    public Image getIcon(int type)
    {
        Image image = null;
        
        if (type == BeanInfo.ICON_COLOR_16x16) {
            image = ImageUtilities.loadImage("org/netbeans/modules/derby/resources/optionsIcon16.png"); // NOI18N
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            image = ImageUtilities.loadImage("org/netbeans/modules/derby/resources/optionsIcon32.png"); // NOI18N
        }
        
        return image != null ? image : super.getIcon(type);
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(DerbyOptions.class);
        descriptor.setName(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "LBL_DerbyOptions"));
        return descriptor;
    }
}
