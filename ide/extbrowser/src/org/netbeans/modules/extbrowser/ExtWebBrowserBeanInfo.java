/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class ExtWebBrowserBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(ExtWebBrowser.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        if (Utilities.isWindows()) {
            try {
                properties = new PropertyDescriptor [] {
                                    new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_EXECUTABLE, ExtWebBrowser.class),
                                 };

                properties[0].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_browserExecutable"));
                properties[0].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_browserExecutable"));
                properties[0].setPreferred(true);

            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        } else {
            try {
                properties = new PropertyDescriptor [] {
                                    new PropertyDescriptor (ExtWebBrowser.PROP_BROWSER_EXECUTABLE, ExtWebBrowser.class),
                                 };

                properties[0].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_browserExecutable"));
                properties[0].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_browserExecutable"));

            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }
        return properties;
    }

    /**
    * Returns the icon. 
    */
    @Override
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
    
}
