/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.ui;

import java.awt.BorderLayout;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The Navigator content of component hierarchy.
 * 
 * @author Martin Entlicher
 */
public class ComponentHierarchy extends JPanel implements NavigatorPanel, ExplorerManager.Provider {
    
    private static final Logger logger = Logger.getLogger(ComponentHierarchy.class.getName());
    
    private static ComponentHierarchy CH;
    
    private TreeView treeView;
    private Lookup lookup;
    private ExplorerManager explorerManager;
    
    public ComponentHierarchy() {
        createComponents();
    }
    
    private void createComponents() {
        explorerManager = new ExplorerManager();
        lookup = ExplorerUtils.createLookup(explorerManager, getActionMap());
        setLayout(new java.awt.BorderLayout());
        treeView = new BeanTreeView();
        add(treeView, BorderLayout.CENTER);
    }
    
    public static synchronized ComponentHierarchy getInstance() {
        if (CH == null) {
            CH = new ComponentHierarchy();
        }
        return CH;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ComponentHierarchy.class, "CTL_ComponentHierarchy");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(ComponentHierarchy.class, "HINT_ComponentHierarchy");
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void panelActivated(Lookup context) {
        ComponentInfo ci = context.lookup(ComponentInfo.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("panelActivated("+context+") ci = "+ci+", tc = "+context.lookup(ScreenshotComponent.class));
            if (ci != null) {
                logger.fine("  ci name = "+ci.getDisplayName());
            }
        }
        ExplorerUtils.activateActions(explorerManager, true);
    }

    @Override
    public void panelDeactivated() {
        ExplorerUtils.activateActions(explorerManager, false);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
}
