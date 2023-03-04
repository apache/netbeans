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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.awt.*;

public class DefaultPlatformImplBeanInfo extends SimpleBeanInfo {

    public DefaultPlatformImplBeanInfo () {
    }

    public Image getIcon(int iconKind) {
        if ((iconKind == BeanInfo.ICON_COLOR_16x16) || (iconKind == BeanInfo.ICON_MONO_16x16)) {
            return ImageUtilities.loadImage("org/netbeans/modules/java/j2seplatform/resources/platform.gif"); // NOI18N
        } else {
            return null;
        }
    }


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                new PropertyDescriptor (DefaultPlatformImpl.PROP_DISPLAY_NAME, DefaultPlatformImpl.class),
                new PropertyDescriptor (DefaultPlatformImpl.PROP_SOURCE_FOLDER, DefaultPlatformImpl.class),                
                new PropertyDescriptor (DefaultPlatformImpl.PROP_JAVADOC_FOLDER, DefaultPlatformImpl.class),
            };
            pds[0].setDisplayName(NbBundle.getMessage(DefaultPlatformImplBeanInfo.class,"TXT_Name"));
            pds[0].setBound(true);
            pds[1].setDisplayName(NbBundle.getMessage(DefaultPlatformImplBeanInfo.class,"TXT_SourcesFolder"));
            pds[1].setPropertyEditorClass(FileObjectPropertyEditor.class);
            pds[1].setBound(true);
            pds[2].setDisplayName(NbBundle.getMessage(DefaultPlatformImplBeanInfo.class,"TXT_JavaDocFolder"));
            pds[2].setPropertyEditorClass(FileObjectPropertyEditor.class);
            pds[2].setBound(true);
            return pds;
        } catch (IntrospectionException ie) {
            return new PropertyDescriptor[0];
        }
    }

}
