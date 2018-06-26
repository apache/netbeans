/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
