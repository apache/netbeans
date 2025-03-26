/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        return actions.toArray(new Action[0]);
    }    
    
}
