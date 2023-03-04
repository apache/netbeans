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

package org.netbeans.modules.web.struts.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.modules.web.struts.StrutsFrameworkProvider;
import org.openide.util.HelpCtx;

/**
 * Panel asking for web frameworks to use.
 * @author Radko Najman
 */
public final class StrutsConfigurationPanel extends WebModuleExtender {

    private final StrutsFrameworkProvider framework;
    private final ExtenderController controller;
    private StrutsConfigurationPanelVisual component;

    private boolean customizer;

    /** Create the wizard panel descriptor. */
    public StrutsConfigurationPanel(StrutsFrameworkProvider framework, ExtenderController controller, boolean customizer) {
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;
        getComponent();
    }

    @Override
    public StrutsConfigurationPanelVisual getComponent() {
        if (component == null) {
            component = new StrutsConfigurationPanelVisual(this, customizer);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.struts.ui.StrutsConfigurationPanel");
    }

    @Override
    public void update() {
        // nothing to update
    }

    @Override
    public boolean isValid() {
        getComponent();
        return component.valid();
    }
    
    @Override
    public Set extend(WebModule webModule) {
        return framework.extendImpl(webModule);
    }

    public ExtenderController getController() {
        return controller;
    }

    private final Set listeners = new /*<ChangeListener>*/ HashSet(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    public String getURLPattern() {
        return component.getURLPattern();
    }

    public void setURLPattern(String pattern) {
        component.setURLPattern(pattern);
    }

    public String getServletName() {
        return component.getServletName();
    }

    public void setServletName(String name) {
        component.setServletName(name);
    }

    public String getAppResource() {
        return component.getAppResource();
    }

    public void setAppResource(String resource) {
        component.setAppResource(resource);
    }

    public boolean addTLDs() {
        return component.addTLDs();
    }

    public boolean packageWars() {
        return component.packageWars();
    }
}
