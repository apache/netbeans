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

package org.netbeans.modules.remote.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class NotConnectedNode extends AbstractNode implements ConnectionListener {

    private final ExecutionEnvironment env;

    public NotConnectedNode(ExecutionEnvironment env) {
        super(Children.LEAF, Lookups.singleton(env));
        this.env = env;
        ConnectionManager.getInstance().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getInstance()));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "LBL_NotConnected");
    }

    @Override
    public Image getOpenedIcon(int type) {
       return getIcon(type);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/disconnected.png"); // NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SharedClassObject.findObject(ConnectAction.class, true)
        };
    }

    @Override
    public Action getPreferredAction() {
        return SharedClassObject.findObject(ConnectAction.class, true);
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        Node parent = getParentNode();
        if (parent instanceof FileSystemRootNode) {
            FileSystemRootNode fsn = (FileSystemRootNode) parent;
            fsn.refresh();
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }
}
