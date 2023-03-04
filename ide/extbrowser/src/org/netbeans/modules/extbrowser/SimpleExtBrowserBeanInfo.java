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

public class SimpleExtBrowserBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor (SimpleExtBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "CTL_SimpleExtBrowser"));
        descr.setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_SimpleExtBrowser"));
        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.SimpleExtBrowser");  // NOI18N
        return descr;
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
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        try {
            properties = new PropertyDescriptor [] {
                new PropertyDescriptor (SimpleExtBrowser.PROP_BROWSER_EXECUTABLE, SimpleExtBrowser.class)    // NOI18N
            };
            properties[0].setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "PROP_browserExecutable"));
            properties[0].setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_browserExecutable"));
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
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
}

