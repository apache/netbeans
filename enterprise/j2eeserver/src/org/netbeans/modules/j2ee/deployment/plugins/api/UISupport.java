/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * UI support for plugins provided by the j2eeserver.
 *
 * @author sherold
 * @since  1.7
 */
public final class UISupport {
    
    private static final WeakHashMap ioWeakMap = new WeakHashMap();
    
    /**
     * Server icon constants.
     *
     * @since 1.19
     */
    public enum ServerIcon { 
        EJB_ARCHIVE, WAR_ARCHIVE, EAR_ARCHIVE,     
        EJB_FOLDER, EAR_FOLDER, WAR_FOLDER,
        EJB_OPENED_FOLDER, EAR_OPENED_FOLDER, WAR_OPENED_FOLDER
    };
    
    /** Do not allow to create instances of this class */
    private UISupport() {
    }
    
    /**
     * Returns the specified icon.
     *
     * @return The specified icon.
     *
     * @since 1.19
     */
    public static Image getIcon(ServerIcon serverIcon) {
        switch (serverIcon) {
            case EJB_ARCHIVE :
                return ImageUtilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/ejb.png"); // NOI18N
            case WAR_ARCHIVE :
                return ImageUtilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/war.png"); // NOI18N
            case EAR_ARCHIVE :
                return ImageUtilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/ear.png"); // NOI18N
            default :
                return computeIcon(serverIcon);
        }
    }
    
    private static Image computeIcon(ServerIcon serverIcon) {
        // get the default folder icon
        Node folderNode = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        Image folder;
        if (serverIcon == ServerIcon.EJB_OPENED_FOLDER || serverIcon == ServerIcon.WAR_OPENED_FOLDER 
                || serverIcon == ServerIcon.EAR_OPENED_FOLDER) {
            folder = folderNode.getOpenedIcon(BeanInfo.ICON_COLOR_16x16);
        } else {
            folder = folderNode.getIcon(BeanInfo.ICON_COLOR_16x16);
        }
        Image badge;
        if (serverIcon == ServerIcon.EJB_FOLDER || serverIcon == ServerIcon.EJB_OPENED_FOLDER) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/ejbBadge.png"); // NOI18N
        } else if (serverIcon == ServerIcon.WAR_FOLDER || serverIcon == ServerIcon.WAR_OPENED_FOLDER) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/warBadge.png"); // NOI18N
        } else if (serverIcon == ServerIcon.EAR_FOLDER || serverIcon == ServerIcon.EAR_OPENED_FOLDER) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/earBadge.png" ); // NOI18N
        } else {
            return null;
        }
        return ImageUtilities.mergeImages(folder, badge, 7, 7);
    }
    
    /**
     * Get a named instance of InputOutput, which represents an output tab in
     * the output window. The output tab will expose server state management 
     * actions for the given server: start, debug, restart, stop and refresh. 
     * Streams for reading/writing can be accessed via getters on the returned 
     * instance. If the InputOutput already exists for the given server, the 
     * existing instance will be returned. The display name of the given server
     * will be used as a name for the tab.
     *
     * @param  url server instance id (DeploymentManager url).
     *
     * @return an <code>InputOutput</code> instance for accessing the new tab,
     *         null if there is no registered server instance with the given url.
     *         
     */
    public static InputOutput getServerIO(String url) {

        ServerInstance si = ServerRegistry.getInstance().getServerInstance(url);

        if (si == null) {
            return null;
        }

        // look in the cache
        InputOutput io = (InputOutput) ioWeakMap.get(si);
        if (io != null) {
            return io;
        }

        if (si.getDisplayName() == null) {
            Logger.getLogger(UISupport.class.getName()).log(Level.INFO,
                    "Server without display name requested IO tab: {0}", si);
            return null;
        }

        Action[] actions = new Action[] {
            new StartAction.OutputAction(si),
            new DebugAction.OutputAction(si),
            new RestartAction.OutputAction(si),
            new StopAction.OutputAction(si),
            new RefreshAction.OutputAction(si)
        };
        InputOutput newIO = IOProvider.getDefault().getIO(si.getDisplayName(), actions);
        
        // put the newIO in the cache
        ioWeakMap.put(si, newIO);
        return newIO;
    }
}
