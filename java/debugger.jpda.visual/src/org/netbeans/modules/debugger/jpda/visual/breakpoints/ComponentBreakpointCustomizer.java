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
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.Customizer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ControllerProvider;
import org.netbeans.spi.debugger.ui.Controller;

/**
 *
 * @author martin
 */
public class ComponentBreakpointCustomizer extends JPanel implements Customizer, Controller {

    private ComponentBreakpoint b;
    private JComponent c;
    
    @Override
    public void setObject(Object bean) {
        if (!(bean instanceof ComponentBreakpoint)) {
            throw new IllegalArgumentException(bean.toString());
        }
        this.b = (ComponentBreakpoint) bean;
        init(b);
    }
    
    private void init(ComponentBreakpoint b) {
        c = new ComponentBreakpointPanel(b);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(c, gbc);
    }

    @Override
    public boolean ok() {
        Controller cc;
        if (c instanceof ControllerProvider) {
            cc = ((ControllerProvider) c).getController();
        } else {
            cc = (Controller) c;
        }
        return cc.ok();
    }

    @Override
    public boolean cancel() {
        Controller cc;
        if (c instanceof ControllerProvider) {
            cc = ((ControllerProvider) c).getController();
        } else {
            cc = (Controller) c;
        }
        return cc.cancel();
    }

}
