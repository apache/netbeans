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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference; 

import javax.swing.JComponent;

import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;

abstract public class NativeBreakpointType extends BreakpointType {

    private final static String CATEGORY_NAME = "native"; // NOI18N

    /**
     * Factory method.
     */

    public abstract NativeBreakpoint newInstance(int flags);

    private Reference<BreakpointPanel> customizerRef = 
	new WeakReference<BreakpointPanel>(null);

    // interface BreakpointType
    @Override
    public JComponent getCustomizer() {
	BreakpointPanel customizer = getCustomizer(null);
	customizerRef = new WeakReference<BreakpointPanel>(customizer);
	return customizer;
    }
    
    // interface BreakpointType
    @Override
    public Controller getController() {
	// OLD BreakpointPanel customizer = getCustomizer(null);
	BreakpointPanel customizer = customizerRef.get();
	if (customizer != null)
	    return customizer.getController();
	else
	    return null;
    }

    /**
     * The following abstract getCustomizer() is not the same as 
     * BreakpointType.getCustomizer()!
     *
     * If 'bpt' is non-null the returned component is used for editing (i.e.
     * CustomizeBreakpoint actions)
     *
     * If 'bpt' is null the returned component is used for creation of
     * new breakpoints.
     */

    abstract public BreakpointPanel getCustomizer(NativeBreakpoint editableBreakpoint);

    // interface BreakpointType
    @Override
    public final String getCategoryDisplayName() {
	    return CATEGORY_NAME;
    } 

    static boolean isOurs(String category) {
	return CATEGORY_NAME.equals(category);
    }

    /**
     * Only one subclass of ours, FunctionBreakpointType, overrides this
     * to return true;.
     */

    // interface BreakpointType
    @Override
    public boolean isDefault() {
	return false;
    } 
    
    // used in xml
    public abstract String id();
}
