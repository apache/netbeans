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

package org.netbeans.modules.javascript2.debug.breakpoints;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 *
 * @author Martin
 */
public class JSLineBreakpointBeanInfo extends SimpleBeanInfo {

    private static final Logger LOG = Logger.getLogger(JSLineBreakpointBeanInfo.class.getName());
    
    public JSLineBreakpointBeanInfo() {}

    @Override
    public BeanDescriptor getBeanDescriptor() {
        Class customizer = null;
        try {
            customizer = Class.forName("org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointCustomizer",
                                       true, Lookup.getDefault().lookup(ClassLoader.class));
        } catch (ClassNotFoundException cnfex) {
            LOG.log(Level.WARNING, "No BP customizer", cnfex);
        }
        return new BeanDescriptor(
                JSLineBreakpoint.class,
                customizer);
    }
    
}
