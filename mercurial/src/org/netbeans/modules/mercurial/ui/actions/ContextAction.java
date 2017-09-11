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

package org.netbeans.modules.mercurial.ui.actions;

import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.LifecycleManager;
import java.awt.event.ActionEvent;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.ImageUtilities;

/**
 * Base for all context-sensitive HG actions.
 *
 * @author Maros Sandor
 */
public abstract class ContextAction extends NodeAction {

    // it's singleton
    // do not declare any instance data

    protected ContextAction() {
        this(null);
    }

    protected ContextAction (String menuIcon) {
        if (menuIcon == null) {
            setIcon(null);
            putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        } else {
            setIcon(ImageUtilities.loadImageIcon(menuIcon, true));
        }
    }

    /**
     * @return bundle key base name
     * @see #getName
     */
    protected abstract String getBaseName(Node[] activatedNodes);

    protected boolean enable(Node[] nodes) {
        return true;
    }

    /**
     * Synchronizes memory modificatios with disk and calls
     * {@link  #performContextAction}.
     */
    protected void performAction(final Node[] nodes) {
        // TODO try to save files in invocation context only
        // list somehow modified file in the context and save
        // just them.
        // The same (global save) logic is in CVS, no complaint
        LifecycleManager.getDefault().saveAll();
        Utils.logVCSActionEvent("HG");                                  //NOI18N
        performContextAction(nodes);
    }

    protected abstract void performContextAction(Node[] nodes);

    /** Be sure nobody overwrites */
    @Override
    public final boolean isEnabled() {
        return super.isEnabled();
    }

    /** Be sure nobody overwrites */
    @Override
    public final void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /** Be sure nobody overwrites */
    @Override
    public final void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
    }

    /** Be sure nobody overwrites */
    @Override
    public final void performAction() {
        super.performAction();
    }

    public String getName() {
        return getName("", TopComponent.getRegistry().getActivatedNodes()); // NOI18N
    }

    /**
     * Display name, it seeks action class bundle for:
     * <ul>
     *   <li><code>getBaseName()</code> key
     * </ul>
     */
    public String getName(String role, Node[] activatedNodes) {
        String baseName = getBaseName(activatedNodes) + role;
        return NbBundle.getBundle(this.getClass()).getString(baseName);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    protected int getFileEnabledStatus() {
        return ~0;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
