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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonInstance;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.SynchronizeAction")
@ActionRegistration(displayName="#LBL_SynchronizeAction", iconInMenu=false, lazy=false)
@ActionReference(path=HudsonInstance.ACTION_PATH, position=500)
@Messages("LBL_SynchronizeAction=&Synchronize")
public class SynchronizeAction extends AbstractAction implements ContextAwareAction {

    private final Collection<? extends HudsonInstance> instances;

    public SynchronizeAction() {
        this(Collections.<HudsonInstance>emptySet());
    }

    @Override public Action createContextAwareInstance(Lookup actionContext) {
        return new SynchronizeAction(actionContext.lookupAll(HudsonInstance.class));
    }

    @Messages("LBL_SynchronizeAction_disconnected=Connect")
    private SynchronizeAction(Collection<? extends HudsonInstance> instances) {
        this.instances = instances;
        boolean allForbidden = true;
        boolean allDisconnected = true;
        for (HudsonInstance instance : instances) {
            if (!instance.isForbidden()) {
                allForbidden = false;
            }
            if (instance.isConnected()) {
                allDisconnected = false;
            }
        }
        if (allForbidden) {
            // LogInAction would do the same thing, so confusing to show this as well.
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        } else if (allDisconnected) {
            putValue(NAME, LBL_SynchronizeAction_disconnected());
        } else {
            putValue(NAME, LBL_SynchronizeAction());
        }
    }
    
    public @Override void actionPerformed(ActionEvent e) {
        for (HudsonInstance instance : instances) {
            instance.synchronize(true);
        }
    }

}
