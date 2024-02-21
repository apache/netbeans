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

package org.netbeans.modules.payara.common.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.payara.common.nodes.actions.DisableModulesAction;
import org.netbeans.modules.payara.common.nodes.actions.EditDetailsAction;
import org.netbeans.modules.payara.common.nodes.actions.EnableCDIProbeModeAction;
import org.netbeans.modules.payara.common.nodes.actions.DisableCDIProbeModeAction;
import org.netbeans.modules.payara.common.nodes.actions.EnableModulesAction;
import org.netbeans.modules.payara.common.nodes.actions.OpenTestURLAction;
import org.netbeans.modules.payara.common.nodes.actions.OpenURLAction;
import org.netbeans.modules.payara.common.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.payara.common.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.payara.common.nodes.actions.UnregisterResourceAction;
import org.netbeans.modules.payara.spi.Decorator;
import org.netbeans.modules.payara.spi.ResourceDecorator;
import org.openide.actions.CopyAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Extensible node.
 * <p/>
 * @author Ludovic Champenois, Peter Williams, Tomas Kraus
 * @author Gaurav Gupta
 */
public class Hk2ItemNode extends AbstractNode {


    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Resources icon. */
    private static final String RESOURCES_ICON = 
            "org/netbeans/modules/payara/common/resources/resources.gif";

    /** Web services icon. */
    private static final String WS_ICON =
            "org/netbeans/modules/payara/common/resources/webservice.png";

    /** Java EE applications folder. */
    public static final Decorator J2EE_APPLICATION_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
        @Override public boolean canDeployTo() {
            return true;
        }
    };

    /** Resources folder. */
    public static final Decorator RESOURCES_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(RESOURCES_ICON);
        }
        @Override public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    };

    /** Web services folder. */
    public static final Decorator WS_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
    };

    /** Web service. */
    public static final Decorator WS_ENDPOINT = new Decorator() {
        @Override public boolean canTest() {
            return true;
        }
        @Override public boolean canCopy() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(WS_ICON);
        }
        @Override public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    };

    /** Java EE application. */
    public static final Decorator J2EE_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() {
            return true;
        }
        @Override public boolean canEnable() {
            return true;
        }
        @Override public boolean canDisable() {
            return true;
        }
        @Override public boolean canShowBrowser() {
            return true;
        }
    };

    /** Refreshable folder. */
    public static final Decorator REFRESHABLE_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
        @Override public boolean canDeployTo() {
            return true;
        }
    };

    /** JDBC managed data sources. */
    public static final Decorator JDBC_MANAGED_DATASOURCES
            = new ResourceDecorator() {
        @Override public boolean canUnregister() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(RESOURCES_ICON);
        }
        @Override public String getCmdPropertyName() {
            return "jdbc_resource_name";
        }
    };

    /** Connection pools. */
    public static final Decorator CONNECTION_POOLS = new ResourceDecorator() {
        @Override public boolean canUnregister() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(RESOURCES_ICON);
        }
        @Override public String getCmdPropertyName() {
            return "jdbc_connection_pool_id";
        }
        @Override public boolean isCascadeDelete() {
            return true;
        }
    };


    /** Node decorator. */
    protected final Decorator decorator;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of extensible node without setting node display name.
     * <p/>
     * @param children  Child nodes.
     * @param lookup    Lookup containing {@see CommonServerSupport}.
     * @param name      Node name.
     * @param decorator Node decorator.
     */
    protected Hk2ItemNode(Children children, final Lookup lookup,
            final String name, final Decorator decorator) {
        super(children);
        this.decorator = decorator;
        
        if(decorator.isRefreshable()) {
            getCookieSet().add(new Hk2Cookie.Refresh(children));
        }
        if(decorator.canDeployTo()) {
            getCookieSet().add(new Hk2Cookie.Deploy(lookup)); 
        }
        if(decorator.canUndeploy()) {
            getCookieSet().add(new Hk2Cookie.Undeploy(lookup, name));
        }
        if(decorator.canEnable()) {
            getCookieSet().add(new Hk2Cookie.Enable(lookup, name));
        }
        if(decorator.canDisable()) {
            getCookieSet().add(new Hk2Cookie.Disable(lookup, name));
        }
        if(decorator.canCDIProbeEnable()) {
            getCookieSet().add(new Hk2Cookie.EnableCDIProbeMode(lookup, name));
        }
        if(decorator.canCDIProbeDisable()) {
            getCookieSet().add(new Hk2Cookie.DisableCDIProbeMode(lookup, name));
        }
    }
        
    /**
     * Creates an instance of extensible node and sets node display name.
     * <p/>
     * @param children  Child nodes.
     * @param lookup    Lookup containing {@see CommonServerSupport}.
     * @param name      Node name.
     * @param decorator Node decorator.
     */
    public Hk2ItemNode(final Lookup lookup,
            Children children, final String name, final Decorator decorator) {
        this(children, lookup, name, decorator);
        setDisplayName(name);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Find an icon for this node (in the closed state).
     * <p/>
     * @param type Constant from {@link java.beans.BeanInfo}.
     * @return Icon to use to represent this node.
     */
    @Override
    public Image getIcon(int type) {
        Image image;
        Image badge = decorator.getIconBadge();
        if(badge != null) {
            if (null == decorator.getIcon(type)) {
                image = badgeFolder(badge, false);
            } else {
                image = badgeIcon(badge,decorator.getIcon(type));
            }
        } else {
            image = decorator.getIcon(type);
        }
        return image != null ? image : getIconDelegate().getIcon(type);
    }
    
    /**
     * Find an icon for this node (in the open state).
     * <p/>
     * This icon is used when the node may have children and is expanded.
     * <p/>
     * @param type Constant from {@link java.beans.BeanInfo}.
     * @return Icon to use to represent this node when open.
     */
    @Override
    public Image getOpenedIcon(int type) {
        Image image;
        Image badge = decorator.getIconBadge();
        if(badge != null) {
            image = badgeFolder(badge, true);
        } else {
            image = decorator.getOpenedIcon(type);
        }
        return image != null ? image : getIconDelegate().getOpenedIcon(type);
    }
    
    /** Get the set of actions that are associated with this node.
     * <p/>
     * This set is used to construct the context menu for the node.
     * By default this method delegates to the deprecated getActions
     * or getContextActions method depending on the value of supplied argument.
     * It is supposed to be overridden by subclasses accordingly.
     * <p/>
     * @param context Whether to find actions for context meaning or for
     *                the node itself.
     * @return {@see List} of actions (you may include nulls for separators).
     */
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        if(decorator.isRefreshable()) {
            actions.add(SystemAction.get(RefreshModulesAction.class));
        }
        if(decorator.canDeployTo()) {
//            actions.add(SystemAction.get(DeployDirectoryAction.class));
        }
        if(decorator.canUndeploy()) {
            actions.add(SystemAction.get(UndeployModuleAction.class));
        }
        if(decorator.canEnable()) {
            actions.add(SystemAction.get(EnableModulesAction.class));
        }
        if(decorator.canDisable()) {
            actions.add(SystemAction.get(DisableModulesAction.class));
        }
        if(decorator.canCDIProbeEnable()) {
            actions.add(SystemAction.get(EnableCDIProbeModeAction.class));
        }
        if(decorator.canCDIProbeDisable()) {
            actions.add(SystemAction.get(DisableCDIProbeModeAction.class));
        }
        if(decorator.canUnregister()) {
            actions.add(SystemAction.get(UnregisterResourceAction.class));
        }
        if(decorator.canShowBrowser()) {
            actions.add(SystemAction.get(OpenURLAction.class));
        }
        if(decorator.canTest()) {
            actions.add(SystemAction.get(OpenTestURLAction.class));
        }
        if(decorator.canCopy()) {
            actions.add(SystemAction.get(CopyAction.class));
        }
        if (decorator.canEditDetails()) {
            actions.add(SystemAction.get(EditDetailsAction.class));
        }
        return actions.toArray(new Action[0]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates and returns the instance of the node representing the status
     * 'WAIT' of the node.
     * <p/>
     * It is used when it spent more time to create elements hierarchy.
     * <p/>
     * @return The wait node.
     */
    public static Node createWaitNode() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(NbBundle.getMessage(Hk2ItemNode.class,
                "LBL_WaitNode_DisplayName"));
        node.setIconBaseWithExtension("org/openide/src/resources/wait.gif");
        return node;
    }

    /**
     * Applies a badge to an open or closed folder icon.
     * <p/>
     * @param badge  Badge image for folder.
     * @param opened Use open or closed folder.
     * @return An image of the badged folder.
     */
    public static Image badgeFolder(Image badge, boolean opened) {
        Node folderNode = getIconDelegate();
        Image folder = opened
                ? folderNode.getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
                : folderNode.getIcon(BeanInfo.ICON_COLOR_16x16);
        return ImageUtilities.mergeImages(folder, badge, 7, 7);
    }
    
    /**
     * Applies a badge to an icon.
     * <p/>
     * @param badge Badge image for folder.
     * @param icon  Tthe image to be badged.
     * @return An image of the badged folder.
     */
    public static Image badgeIcon(Image badge, Image icon) {
        return ImageUtilities.mergeImages(icon, badge, 7, 7);
    }

    /**
     * Retrieves the IDE's standard folder node, so we can access the default
     * open/closed folder icons.
     * <p/>
     * @return Standard folder node.
     */
    private static Node getIconDelegate() {
        return DataFolder.findFolder(
                FileUtil.getConfigRoot()).getNodeDelegate();
    }

}
