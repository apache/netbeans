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

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;

public class SystemDefaultBrowserBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor (SystemDefaultBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "CTL_SystemDefaultBrowserName"));
        descr.setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_SystemDefaultBrowserName"));

        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.ExtWebBrowser");  // NOI18N //TODO
	return descr;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        
        try {
            properties = new PropertyDescriptor [] {
                                new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_EXECUTABLE, SystemDefaultBrowser.class, "getBrowserExecutable", null), // NOI18N
                                new PropertyDescriptor(ExtWebBrowser.PROP_DDE_ACTIVATE_TIMEOUT, SystemDefaultBrowser.class),
                                new PropertyDescriptor(ExtWebBrowser.PROP_DDE_OPENURL_TIMEOUT, SystemDefaultBrowser.class)
                             };

            properties[0].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_browserExecutable"));
            properties[0].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_browserExecutable"));
            properties[0].setPreferred(true);

            properties[1].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_DDE_ACTIVATE_TIMEOUT"));
            properties[1].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_DDE_ACTIVATE_TIMEOUT"));
            properties[1].setExpert(true);
            properties[1].setHidden(true);

            properties[2].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_DDE_OPENURL_TIMEOUT"));
            properties[2].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_DDE_OPENURL_TIMEOUT"));
            properties[2].setExpert(true);
            properties[2].setHidden(true);

        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
        
        return properties;
    }

    /**
    * Returns the icon. 
    */
    @Override
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.png"); // NOI18N
    }
    
}
