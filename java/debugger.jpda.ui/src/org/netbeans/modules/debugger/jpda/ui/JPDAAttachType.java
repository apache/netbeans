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

package org.netbeans.modules.debugger.jpda.ui;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;


/**
 *
 * @author   Jan Jancura
 */
@AttachType.Registration(displayName="#CTL_Connector_name")
public class JPDAAttachType extends AttachType {

    private Reference<ConnectPanel> customizerRef = new WeakReference<ConnectPanel>(null);

    @Override
    public JComponent getCustomizer () {
        ConnectPanel panel = new ConnectPanel ();
        customizerRef = new WeakReference<ConnectPanel>(panel);
        return panel;
    }

    @Override
    public Controller getController() {
        ConnectPanel panel = customizerRef.get();
        if (panel != null) {
            return panel.getController();
        } else {
            return null;
        }
    }

}
