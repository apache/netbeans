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
package org.netbeans.modules.selenium2.server;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Theofanis Oikonomou
 */
public class Selenium2ServicesNode extends AbstractNode {

    private static final String RUNNING_BADGE = "org/netbeans/modules/selenium2/server/resources/running.png"; // NOI18N
    private static final String WAITING_BADGE = "org/netbeans/modules/selenium2/server/resources/waiting.png"; // NOI18N
    private static final String SELENIUM_ICON = "org/netbeans/modules/selenium2/server/resources/selenium16.png";  //NOI18N
    private static Selenium2ServicesNode INSTANCE;
    private static final String NODE_NAME = "seleniumserver"; // NOI18N

    @ServicesTabNodeRegistration(
        name = NODE_NAME,
        displayName = "#Selenium2_Server_Node_DisplayName",
        shortDescription = "#Selenium2_Server_Node_Short_Description",
        iconResource = SELENIUM_ICON,
        position = 990
    )
    static synchronized Selenium2ServicesNode getInstance() {
        if (INSTANCE == null){
            INSTANCE = new Selenium2ServicesNode();
        }
        return INSTANCE;
    }

    private Selenium2ServicesNode() {
        super(Children.LEAF);
        setName(NODE_NAME);
        setDisplayName(NbBundle.getMessage(Selenium2ServicesNode.class, "Selenium2_Server_Node_DisplayName"));
        setIconBaseWithExtension(SELENIUM_ICON);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(StartServerAction.class),
            SystemAction.get(StopServerAction.class),
            SystemAction.get(RestartServerAction.class),
            null,
            SystemAction.get(ConfigureServerAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }

    private Image badgeIcon(Image origImg) {
        Image badge = null;
        if (Selenium2ServerSupport.getInstance().isStarting()) {
            badge = ImageUtilities.loadImage(WAITING_BADGE);
        } else {
            if (Selenium2ServerSupport.getInstance().isRunning()) {
                badge = ImageUtilities.loadImage(RUNNING_BADGE);
            }
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }
    
    public void refresh() {
        fireIconChange();
        fireOpenedIconChange();
    }

    // --------------- actions ---------------------- //

    @NbBundle.Messages("Selenium2_Server_Restart_Action_Name=&Restart")
    public static class RestartServerAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            Task serverTask = Selenium2ServerSupport.getInstance().restartServer();
        }

        @Override
        public String getName() {
            return Bundle.Selenium2_Server_Restart_Action_Name();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return Selenium2ServerSupport.getInstance().isRunning();
        }
        
        @Override
        protected boolean asynchronous() {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }

    @NbBundle.Messages("Selenium2_Server_Start_Action_Name=&Start")
    public static class StartServerAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            Task serverTask = Selenium2ServerSupport.getInstance().startServer();
        }

        @Override
        public String getName() {
            return Bundle.Selenium2_Server_Start_Action_Name();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return !Selenium2ServerSupport.getInstance().isRunning();
        }
        
        @Override
        protected boolean asynchronous() {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }

    @NbBundle.Messages("Selenium2_Server_Stop_Action_Name=S&top")
    public static class StopServerAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            Task serverTask = Selenium2ServerSupport.getInstance().stopServer();
        }

        @Override
        public String getName() {
            return Bundle.Selenium2_Server_Stop_Action_Name();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return Selenium2ServerSupport.getInstance().isRunning();
        }
        
        @Override
        protected boolean asynchronous() {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }

    @NbBundle.Messages("Selenium2_Server_Configure_Action_Name=&Configure")
    public static class ConfigureServerAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            Selenium2ServerSupport.getInstance().configureServer();
        }

        @Override
        public String getName() {
            return Bundle.Selenium2_Server_Configure_Action_Name();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }
}
