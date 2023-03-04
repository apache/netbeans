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
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.awt.*;

public class J2SEPlatformImplBeanInfo extends SimpleBeanInfo {

    public J2SEPlatformImplBeanInfo () {
    }


    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] descs =  new PropertyDescriptor[] {
                new PropertyDescriptor (J2SEPlatformImpl.PROP_DISPLAY_NAME, J2SEPlatformImpl.class),
                new PropertyDescriptor (J2SEPlatformImpl.PROP_ANT_NAME, J2SEPlatformImpl.class),
                new PropertyDescriptor (J2SEPlatformImpl.PROP_SOURCE_FOLDER, J2SEPlatformImpl.class),
                new PropertyDescriptor (J2SEPlatformImpl.PROP_JAVADOC_FOLDER, J2SEPlatformImpl.class),
            };
            descs[0].setDisplayName(NbBundle.getMessage(J2SEPlatformImplBeanInfo.class,"TXT_Name"));
            descs[0].setBound(true);
            descs[1].setDisplayName(NbBundle.getMessage(J2SEPlatformImplBeanInfo.class,"TXT_AntName"));
            descs[1].setWriteMethod(null);
            descs[2].setDisplayName(NbBundle.getMessage(J2SEPlatformImplBeanInfo.class,"TXT_SourcesFolder"));
            descs[2].setPropertyEditorClass(FileObjectPropertyEditor.class);
            descs[2].setBound(true);
            descs[3].setDisplayName(NbBundle.getMessage(J2SEPlatformImplBeanInfo.class,"TXT_JavaDocFolder"));
            descs[3].setPropertyEditorClass(FileObjectPropertyEditor.class);
            descs[3].setBound(true);
            return descs;
        } catch (IntrospectionException ie) {
            return new PropertyDescriptor[0];
        }
    }


    public Image getIcon(int iconKind) {
        if ((iconKind == BeanInfo.ICON_COLOR_16x16) || (iconKind == BeanInfo.ICON_MONO_16x16)) {
            return ImageUtilities.loadImage("org/netbeans/modules/java/j2seplatform/resources/platform.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
