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
package org.netbeans.modules.beans;

import javax.lang.model.element.Element;
import javax.swing.JComponent;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@NavigatorPanel.Registration(mimeType="text/x-java", position=500, displayName="#LBL_BeanPatterns")
public class BeanPanel implements NavigatorPanel {

    private BeanPanelUI component;

    public BeanPanel() {}

    public void panelActivated(Lookup context) {
        assert context != null;
        // System.out.println("Panel Activated");
        BeanNavigatorJavaSourceFactory.getInstance().setLookup(context, getBeanPanelUI());
        getBeanPanelUI().showWaitNode();
    }

    public void panelDeactivated() {
        getBeanPanelUI().showWaitNode(); // To clear the ui
        BeanNavigatorJavaSourceFactory.getInstance().setLookup(Lookup.EMPTY, null);
    }

    public Lookup getLookup() {
        return this.getBeanPanelUI().getLookup();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(BeanPanel.class,"LBL_BeanPatterns");
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(BeanPanel.class,"HINT_BeanPatterns");
    }

    public JComponent getComponent() {
        return getBeanPanelUI();
    }

    public void selectElement(ElementHandle<Element> eh) {
        getBeanPanelUI().selectElementNode(eh);
    }
    
    private synchronized BeanPanelUI getBeanPanelUI() {
        if (this.component == null) {
            this.component = new BeanPanelUI();
        }
        return this.component;
    }
    
}
