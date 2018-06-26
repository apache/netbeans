/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
