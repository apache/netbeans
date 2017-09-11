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

package org.netbeans.modules.navigator;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithToolbar;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;

/**
 * Delegating panel for use from {@link NavigatorPanel.Registration}.
 */
public class LazyPanel implements NavigatorPanelWithUndo, NavigatorPanelWithToolbar {

    /**
     * Referenced from generated layer.
     */
    public static NavigatorPanel create(Map<String,?> attrs) {
        return new LazyPanel(attrs);
    }

    private final Map<String,?> attrs;
    private NavigatorPanel delegate;

    private LazyPanel(Map<String,?> attrs) {
        this.attrs = attrs;
    }

    private synchronized NavigatorPanel initialize() {
        if (delegate == null) {
            delegate = (NavigatorPanel) attrs.get("delegate");
        }
        return delegate;
    }

    @Override public String getDisplayName() {
        if (delegate != null) {
            return delegate.getDisplayName();
        } else {
            return (String) attrs.get("displayName");
        }
    }

    @Override public String getDisplayHint() {
        if (delegate != null) {
            return delegate.getDisplayHint();
        } else { // unused currently, so no separate attr
            return (String) attrs.get("displayName");
        }
    }

    @Override public JComponent getComponent() {
        return initialize().getComponent();
    }

    @Override public void panelActivated(Lookup context) {
        initialize().panelActivated(context);
    }

    @Override public void panelDeactivated() {
        initialize().panelDeactivated();
    }

    @Override public Lookup getLookup() {
        return initialize().getLookup();
    }

    @Override public UndoRedo getUndoRedo() {
        NavigatorPanel p = initialize();
        return p instanceof NavigatorPanelWithUndo ? ((NavigatorPanelWithUndo) p).getUndoRedo() : UndoRedo.NONE;
    }

    @Override
    public JComponent getToolbarComponent() {
        NavigatorPanel p = initialize();
        return p instanceof NavigatorPanelWithToolbar ? ((NavigatorPanelWithToolbar) p).getToolbarComponent() : null;
    }

    public boolean panelMatch(NavigatorPanel panel) {
        if (panel == null) {
            return false;
        }
        if (this.getClass().equals(panel.getClass())) {
            return super.equals(panel);
        }
        if (delegate != null) {
            return delegate.equals(panel);
        } else if (panel.getDisplayName().equals(attrs.get("displayName"))) {
            return initialize().equals(panel);
        } else {
            return false;
        }
    }
}
