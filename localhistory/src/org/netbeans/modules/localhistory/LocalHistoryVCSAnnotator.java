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
package org.netbeans.modules.localhistory;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.localhistory.ui.actions.RevertDeletedAction;
import org.netbeans.modules.localhistory.ui.view.ShowHistoryAction;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * Provides the Local History Actions to the IDE
 * 
 * @author Tomas Stupka
 */
public class LocalHistoryVCSAnnotator extends VCSAnnotator {
    
    /** Creates a new instance of LocalHistoryVCSAnnotator */
    public LocalHistoryVCSAnnotator() {
    }
 
    public Image annotateIcon(Image icon, VCSContext context) {
        // not supported yet
        return super.annotateIcon(icon, context);
    }    
            
    public String annotateName(String name, VCSContext context) {
        // not supported yet
        return super.annotateName(name, context);
    }
    
    public Action[] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        Lookup context = ctx.getElements();
        List<Action> actions = new ArrayList<Action>();
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(SystemAction.get(ShowHistoryAction.class));
            actions.add(SystemAction.get(RevertDeletedAction.class));
        } else {
            actions.add(SystemActionBridge.createAction(
                                            SystemAction.get(ShowHistoryAction.class), 
                                            NbBundle.getMessage(ShowHistoryAction.class, "CTL_ShowHistory"), 
                                            context));
            actions.add(SystemActionBridge.createAction(
                                            SystemAction.get(RevertDeletedAction.class), 
                                            NbBundle.getMessage(RevertDeletedAction.class, "CTL_ShowRevertDeleted"),  
                                            context));           
            
        }
        return actions.toArray(new Action[actions.size()]);
    }    
    
}
