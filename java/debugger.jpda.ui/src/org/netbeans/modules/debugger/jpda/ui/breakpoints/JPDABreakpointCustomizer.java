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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.Customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;

import org.netbeans.modules.debugger.jpda.ui.models.BreakpointsActionsProvider;
import org.netbeans.spi.debugger.ui.Controller;

/**
 *
 * @author martin
 */
public class JPDABreakpointCustomizer extends JPanel implements Customizer, Controller {
    
    private JPDABreakpoint b;
    private JComponent c;
    
    public JPDABreakpointCustomizer() {
    }

    public void setObject(Object bean) {
        if (!(bean instanceof JPDABreakpoint)) {
            throw new IllegalArgumentException(bean.toString());
        }
        this.b = (JPDABreakpoint) bean;
        init(b);
    }
    
    private void init(JPDABreakpoint b) {
        c = BreakpointsActionsProvider.getCustomizerComponent(b);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(c, gbc);
    }

    public boolean ok() {
        Controller cc;
        if (c instanceof ControllerProvider) {
            cc = ((ControllerProvider) c).getController();
        } else {
            cc = (Controller) c;
        }
        return cc.ok();
    }

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
