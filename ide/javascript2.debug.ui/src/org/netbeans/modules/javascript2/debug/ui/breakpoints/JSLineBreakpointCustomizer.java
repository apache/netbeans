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

package org.netbeans.modules.javascript2.debug.ui.breakpoints;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.Customizer;
import javax.swing.JPanel;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
public class JSLineBreakpointCustomizer extends JPanel implements Customizer, Controller {

    private JSLineBreakpoint b;
    private JSLineBreakpointCustomizerPanel c;
    
    public JSLineBreakpointCustomizer() {
    }

    @Override
    public void setObject(Object bean) {
        if (!(bean instanceof JSLineBreakpoint)) {
            throw new IllegalArgumentException(bean.toString());
        }
        this.b = (JSLineBreakpoint) bean;
        init(b);
    }
    
    private void init(JSLineBreakpoint b) {
        c = getCustomizerComponent(b);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(c, gbc);
    }

    @NbBundle.Messages("ACSD_Breakpoint_Customizer_Dialog=Customize this breakpoint's properties")
    public static JSLineBreakpointCustomizerPanel getCustomizerComponent(JSLineBreakpoint lb) {
        JSLineBreakpointCustomizerPanel c;
        c = new JSLineBreakpointCustomizerPanel(lb);
        c.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_Breakpoint_Customizer_Dialog());
        return c;
    }
    
    @Override
    public boolean ok() {
        Controller cc;
        cc = c.getController();
        return cc.ok();
    }

    @Override
    public boolean cancel() {
        Controller cc;
        cc = c.getController();
        return cc.cancel();
    }

}
