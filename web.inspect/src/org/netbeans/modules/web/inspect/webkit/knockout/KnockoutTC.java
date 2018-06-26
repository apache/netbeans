/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
