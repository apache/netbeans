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
        return (Action[])actions.toArray(new Action[0]);
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
