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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.netbeans.modules.web.javascript.debugger.breakpoints.ui.XHRBreakpointCustomizer;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@NbBundle.Messages({"XHRBreakpointTypeName=XMLHttpRequest"})
@BreakpointType.Registration(displayName="#XHRBreakpointTypeName")
public class XHRBreakpointType extends BreakpointType {

    private Reference<XHRBreakpointCustomizer> customizerRef = new WeakReference<XHRBreakpointCustomizer>(null);
    
    @Override
    public String getCategoryDisplayName() {
        return Bundle.JavaScriptBreakpointTypeCategory();
    }

    @Override
    public JComponent getCustomizer() {
        XHRBreakpointCustomizer cust = new XHRBreakpointCustomizer();
        customizerRef = new WeakReference<XHRBreakpointCustomizer>(cust);
        return cust;
    }

    @Override
    public Controller getController() {
        XHRBreakpointCustomizer cust = customizerRef.get();
        if (cust != null) {
            return cust.getController();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeDisplayName() {
        return Bundle.XHRBreakpointTypeName();
    }
    
    @Override
    public boolean isDefault() {
        return false;
    }
    
}
