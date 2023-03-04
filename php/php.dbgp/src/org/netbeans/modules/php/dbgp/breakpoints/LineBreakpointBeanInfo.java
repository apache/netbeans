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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Don't change this class name. If it is changed, {@link LineBreakpoint} class name
 * must be changed.
 */
public class LineBreakpointBeanInfo extends SimpleBeanInfo {

    private static final Logger LOGGER = Logger.getLogger(LineBreakpointBeanInfo.class.getName());

    @Override
    public BeanDescriptor getBeanDescriptor() {
        Class customizer = null;
        try {
            customizer = Class.forName("org.netbeans.modules.php.dbgp.ui.DbgpLineBreakpointCustomizer", // NOI18N
                    true, Lookup.getDefault().lookup(ClassLoader.class));
        } catch (ClassNotFoundException cnfex) {
            LOGGER.log(Level.WARNING, "No BP customizer", cnfex); // NOI18N
        }
        return new BeanDescriptor(LineBreakpoint.class, customizer);
    }
}
