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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.DebugAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.CustomizerAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.ProfileAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RefreshAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RemoveAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RenameAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RestartAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.StartAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.StopAction;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * This filter node is used to add additional features to the InstanceNode and 
 * InstanceTargetNode. This filter node defines the node name, displaName, 
 * enhances the original node set of actions with the genaral server instance 
 * actions. Registers a server state changes listener, which will display a server 
 * status badge over the original node icon. Everything else is delegated to the 
 * original node.
 *
 * @author sherold
 */
public class InstanceNodeDecorator extends FilterNode 
        implements ServerInstance.StateListener, PropertyChangeListener {
    
    private static final String WAITING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/waiting.png"; // NOI18N
    private static final String RUNNING_ICON 
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/running.png"; // NOI18N
    private static final String DEBUGGING_ICON 
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/debugging.png"; // NOI18N
    private static final String SUSPENDED_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/suspended.png"; // NOI18N
    private static final String PROFILING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/profiling.png"; // NOI18N
    private static final String PROFILER_BLOCKING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/profilerblocking.png"; // NOI18N
    
    private ServerInstance si;
    
    /** Creates a new instance of InstanceNodeDecorator */
    private InstanceNodeDecorator(Node original, ServerInstance si) {
        super(original);
        this.si = si;
    }

    public static InstanceNodeDecorator getInstance(Node original, ServerInstance si) {
        InstanceNodeDecorator ret = new InstanceNodeDecorator(original, si);
        si.addStateListener(WeakListeners.create(ServerInstance.StateListener.class, ret, si));
        InstanceProperties ip = si.getInstanceProperties();
        ip.addPropertyChangeListener(WeakListeners.propertyChange(ret, ip));

        return ret;
    }
    
    public String getDisplayName() {
        return si.getDisplayName();
    }
    
    public String getName() {
        return si.getUrl(); // unique identifier
    }
    
    public Action[] getActions(boolean context) {
        List actions = new ArrayList();
        actions.addAll(Arrays.asList(new Action[] {
                                        SystemAction.get(StartAction.class),
                                        SystemAction.get(DebugAction.class)
        }));
        if (si.isProfileSupported()) {
            actions.add(
                                        SystemAction.get(ProfileAction.class)
            );
        }
        actions.addAll(Arrays.asList(new Action[] {
                                        SystemAction.get(RestartAction.class),
                                        SystemAction.get(StopAction.class),
                                        SystemAction.get(RefreshAction.class),
                                        null,
                                        SystemAction.get(RenameAction.class),
                                        SystemAction.get(RemoveAction.class)
        }));
        actions.addAll(Arrays.asList(getOriginal().getActions(context)));
        actions.add(null);
        actions.add(SystemAction.get(CustomizerAction.class));
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    public Image getIcon(int type) {
        return badgeIcon(getOriginal().getIcon(type));
    }
    
    public Image getOpenedIcon(int type) {
        return badgeIcon(getOriginal().getOpenedIcon(type));
    }
    
    // private helper methods -------------------------------------------------
        
    private Image badgeIcon(Image origImg) {
        Image badge = null;        
        switch (si.getServerState()) {
            case ServerInstance.STATE_WAITING : 
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            case ServerInstance.STATE_RUNNING : 
                badge = ImageUtilities.loadImage(RUNNING_ICON);
                break;
            case ServerInstance.STATE_DEBUGGING : 
                badge = ImageUtilities.loadImage(DEBUGGING_ICON);
                break;
            case ServerInstance.STATE_SUSPENDED : 
                badge = ImageUtilities.loadImage(SUSPENDED_ICON);
                break;
            case ServerInstance.STATE_PROFILING : 
                badge = ImageUtilities.loadImage(PROFILING_ICON);
                break;
            case ServerInstance.STATE_PROFILER_BLOCKING : 
                badge = ImageUtilities.loadImage(PROFILER_BLOCKING_ICON);
                break;
            case ServerInstance.STATE_PROFILER_STARTING : 
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            default:
                break;
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        // invoke icon change - this causes the server status icon badge to be updated
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                fireIconChange();
            }
        });
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (InstanceProperties.DISPLAY_NAME_ATTR.equals(evt.getPropertyName())) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    fireDisplayNameChange((String) evt.getOldValue(), (String) evt.getNewValue());
                }
            });
        }
    }
}
