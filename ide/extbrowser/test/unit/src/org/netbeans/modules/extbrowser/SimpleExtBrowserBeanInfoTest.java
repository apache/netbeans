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
/*
 * SimpleExtBrowserBeanInfoTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */

package org.netbeans.modules.extbrowser;

import org.netbeans.junit.*;
import java.beans.*;

/**
 *
 * @author rk109395
 */
public class SimpleExtBrowserBeanInfoTest extends NbTestCase {

    public SimpleExtBrowserBeanInfoTest (java.lang.String testName) {
        super(testName);
    }

    /** Test of getBeanDescriptor method, of class org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo. */
    public void testGetBeanDescriptor () {
        if (testObject.getBeanDescriptor () == null)
            fail ("SimpleExtBrowserBeanInfo.getBeanDescriptor () returned <null>.");
    }
    
    /** Test of getPropertyDescriptors method, of class org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo. */
    public void testGetPropertyDescriptors () {
        if (testObject.getPropertyDescriptors () == null)
            fail ("SimpleExtBrowserBeanInfo.getPropertyDescriptors () returned <null>.");
    }
    
    /** Test of getIcon method, of class org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo. */
    public void testGetIcon () {
        if (testObject.getIcon (BeanInfo.ICON_COLOR_32x32) == null)
            fail ("SimpleExtBrowserBeanInfo.getIcon (BeanInfo.ICON_COLOR_32x32) returned <null>.");
    }
    
    protected BeanInfo testObject;
    
    protected void setUp () {
        testObject = new SimpleExtBrowserBeanInfo ();
    }

}
