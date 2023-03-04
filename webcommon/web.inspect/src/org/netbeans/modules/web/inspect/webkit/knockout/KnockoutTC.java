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
package org.netbeans.modules.web.inspect.webkit.knockout;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * Top component that displays information related to Knockout.
 * 
 * @author Jan Stola
 */
@TopComponent.Description(
        preferredID = KnockoutTC.ID,
        persistenceType = TopComponent.PERSISTENCE_ALWAYS,
        iconBase = "org/netbeans/modules/web/inspect/resources/knockout.png") // NOI18N
@TopComponent.Registration(
        mode = "properties", // NOI18N
        openAtStartup = false)
@ActionID(
        category = "Window", // NOI18N
        id = "org.netbeans.modules.web.inspect.webkit.knockout.KnockoutTC") // NOI18N
@ActionReference(
        path = "Menu/Window/Web", // NOI18N
        position = 350)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_KnockoutAction", // NOI18N
        preferredID = KnockoutTC.ID)
@NbBundle.Messages({
    "CTL_KnockoutAction=Knockout", // NOI18N
    "CTL_KnockoutTC=Knockout", // NOI18N
    "HINT_KnockoutTC=This window shows the Knockout context of the selected node." // NOI18N
})
public final class KnockoutTC extends TopComponent {
    /** TopComponent ID. */
    public static final String ID = "KnockoutTC"; // NOI18N
    /** Panel shown in this {@code TopComponent}. */
    private KnockoutPanel currentPanel;

    /**
     * Creates a new {@code KnockoutTC}.
     */
    public KnockoutTC() {
        setName(Bundle.CTL_KnockoutTC());
        setToolTipText(Bundle.HINT_KnockoutTC());
        setLayout(new BorderLayout());
        associateLookup(new KnockoutTCLookup());
        PageInspectorImpl.getDefault().addPropertyChangeListener(createInspectorListener());
        update();
    }

    /**
     * Updates the content of this {@code TopComponent}.
     */
    final void update() {
        if (EventQueue.isDispatchThread()) {
            PageModel pageModel = PageInspectorImpl.getDefault().getPage();
            if (currentPanel != null) {
                if (pageModel == currentPanel.getPageModel()) {
                    return;
                } else {
                    currentPanel.dispose();
                }
            }
            removeAll();
            currentPanel = new KnockoutPanel((WebKitPageModel)pageModel);
            if (lastKnockoutPageModel != null) {
                PageModel knockoutPageModel = lastKnockoutPageModel.get();
                if (knockoutPageModel != null && knockoutPageModel == pageModel) {
                    currentPanel.knockoutUsed(lastKnockoutVersion);
                }
            }
            add(currentPanel);
            ((KnockoutTCLookup)getLookup()).setPanel(currentPanel);
            revalidate();
            repaint();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            });
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        update();
    }

    private Reference<PageModel> lastKnockoutPageModel;
    private String lastKnockoutVersion;

    /**
     * Invoked when page knockout is found in the specified page model.
     * 
     * @param pageModel page model where knockout was found.
     * @param koVersion version of Knockout used by the inspected page.
     */
    void knockoutUsed(PageModel pageModel, String koVersion) {
        assert EventQueue.isDispatchThread();
        if (currentPanel != null) {
            if (currentPanel.getPageModel() == pageModel) {
                currentPanel.knockoutUsed(koVersion);
                lastKnockoutVersion = null;
            } else {
                lastKnockoutPageModel = new WeakReference<PageModel>(pageModel);
                lastKnockoutVersion = koVersion;
            }
        }
    }

    /**
     * Determines whether the inspected page uses Knockout.
     * 
     * @return {@code true} when the inspected page uses knockout,
     * returns {@code false} otherwise.
     */
    boolean isKnockoutUsed() {
        if (currentPanel == null) {
            return lastKnockoutVersion != null;
        } else {
            return currentPanel.isKnockoutUsed();
        }
    }

    /**
     * Ensures that Knockout context is shown in Knockout TC.
     */
    void showKnockoutContext() {
        if (currentPanel != null) {
            currentPanel.showKnockoutContext();
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

    /**
     * Lookup of {@code KnockoutTC}.
     */
    private class KnockoutTCLookup extends ProxyLookup {

        /**
         * Updates the content of this lookup according to the given panel.
         * 
         * @param panel new panel to display in {@code KnockoutTC}.
         */
        void setPanel(KnockoutPanel panel) {
            Lookup lookup = Lookups.proxy(panel.createLookupProvider(getActionMap()));
            setLookups(lookup);
        }

    }

}
