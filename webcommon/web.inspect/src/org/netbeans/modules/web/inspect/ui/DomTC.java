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
package org.netbeans.modules.web.inspect.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * Top component which displays DOM Tree.
 * 
 * @author Jan Stola
 */
@TopComponent.Description(
        preferredID = DomTC.ID,
        persistenceType = TopComponent.PERSISTENCE_ALWAYS,
        iconBase = "org/netbeans/modules/web/inspect/resources/domElement.png") // NOI18N
@TopComponent.Registration(
        mode = "navigator", // NOI18N
        position = 600,
        openAtStartup = false)
@ActionID(
        category = "Window", // NOI18N
        id = "org.netbeans.modules.web.inspect.ui.DomTC") // NOI18N
@ActionReference(
        path = "Menu/Window/Web", // NOI18N
        position = 300)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DomAction", // NOI18N
        preferredID = DomTC.ID)
@NbBundle.Messages({
    "CTL_DomAction=Browser DOM", // NOI18N
    "CTL_DomTC=Browser DOM", // NOI18N
    "HINT_DomTC=This window shows the DOM tree from the browser." // NOI18N
})
public final class DomTC extends TopComponent {
    /** TopComponent ID. */
    public static final String ID = "DomTC"; // NOI18N

    /**
     * Creates a new {@code DomTC}.
     */
    public DomTC() {
        setName(Bundle.CTL_DomTC());
        setToolTipText(Bundle.HINT_DomTC());
        setLayout(new BorderLayout());
        associateLookup(new DomTCLookup());
        PageInspectorImpl.getDefault().addPropertyChangeListener(createInspectorListener());
        update();
    }

    /**
     * Updates the content of this {@code TopComponent}.
     */
    private void update() {
        if (EventQueue.isDispatchThread()) {
            PageModel pageModel = PageInspectorImpl.getDefault().getPage();
            removeAll();
            DomPanel panel = new DomPanel(pageModel);
            add(panel);
            ((DomTCLookup)getLookup()).setPanel(panel);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            });
        }
    }

    /**
     * Creates a page inspector listener.
     * 
     * @return page inspector listener.
     */
    private PropertyChangeListener createInspectorListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
                    update();
                }
            }
        };
    }

    @Override
    public boolean requestFocusInWindow() {
        return getComponent(0).requestFocusInWindow();
    }

    /**
     * Lookup of {@code DomTC}.
     */
    private class DomTCLookup extends ProxyLookup {

        /**
         * Updates the content of this lookup according to the given panel.
         * 
         * @param panel new panel to display in {@code DomTC}.
         */
        void setPanel(DomPanel panel) {
            Lookup lookup = ExplorerUtils.createLookup(panel.getExplorerManager(), getActionMap());
            setLookups(lookup);
        }

    }

}
