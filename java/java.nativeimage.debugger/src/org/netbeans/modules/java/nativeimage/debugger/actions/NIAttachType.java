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
package org.netbeans.modules.java.nativeimage.debugger.actions;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;

import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
@NbBundle.Messages("CTL_NIConnector_name=Native Image")
@AttachType.Registration(displayName="#CTL_NIConnector_name")
public class NIAttachType extends AttachType {

    private Reference<NIAttachCustomizer> customizerRef = new WeakReference<>(null);

    @Override
    public JComponent getCustomizer() {
        NIAttachCustomizer ac = new NIAttachCustomizer();
        customizerRef = new WeakReference<>(ac);
        return ac;
    }

    @Override
    public Controller getController() {
        NIAttachCustomizer panel = customizerRef.get();
        if (panel != null) {
            return panel.getController();
        } else {
            return null;
        }
    }

}
