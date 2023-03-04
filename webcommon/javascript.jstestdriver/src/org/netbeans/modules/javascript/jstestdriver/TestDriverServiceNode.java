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

package org.netbeans.modules.javascript.jstestdriver;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.libs.jstestdriver.api.BrowserInfo;
import org.netbeans.libs.jstestdriver.api.ServerListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 */
public class TestDriverServiceNode extends AbstractNode {

    private static final String TESTDRIVER_ICON = 
            "org/netbeans/modules/javascript/jstestdriver/resources/JsTestDriver.png"; // NOI18N
    private static final String RUNNING_BADGE = 
            "org/netbeans/modules/javascript/jstestdriver/resources/running.png"; // NOI18N
    private static final String WAITING_BADGE = 
            "org/netbeans/modules/javascript/jstestdriver/resources/waiting.png"; // NOI18N

    private static TestDriverServiceNode node;

    private TestDriverServiceNode() {
        super(Children.LEAF);
        setName("testdriver"); // NOI18N
        setDisplayName(NbBundle.getMessage(TestDriverServiceNode.class, "Test_Driver_Node_Name"));
        setShortDescription(NbBundle.getMessage(TestDriverServiceNode.class, "Test_Driver_Node_Short_Description"));
        setIconBaseWithExtension(TESTDRIVER_ICON);
    }

    @ServicesTabNodeRegistration(
        name = "testdriver",
        displayName = "org.netbeans.modules.javascript.jstestdriver.Bundle#Test_Driver_Node_Name",
        shortDescription = "org.netbeans.modules.javascript.jstestdriver.Bundle#Test_Driver_Node_Short_Description",
        iconResource = "org/netbeans/modules/javascript/jstestdriver/resources/JsTestDriver.png",
        position = 987
    )
    public static synchronized TestDriverServiceNode getInstance() {
        if (node == null) {
            node = new TestDriverServiceNode();
        }
        return node;
    }

    void refresh() {
        setShortDescription(getShortDescription());
        fireIconChange();
        fireOpenedIconChange();
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
        if (JSTestDriverSupport.getDefault().isStarting()) {
            badge = ImageUtilities.loadImage(WAITING_BADGE);
        }
        if (JSTestDriverSupport.getDefault().isRunning()) {
            badge = ImageUtilities.loadImage(RUNNING_BADGE);
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(StartJSTestDriver.class),
            SystemAction.get(StopJSTestDriver.class),
            SystemAction.get(RestartJSTestDriver.class),
            SystemAction.get(ConfigureJSTestDriver.class)
        };
    }

    @Override
    public String getShortDescription() {
        return JSTestDriverSupport.getDefault().getUserDescription();
    }

    public static class StartJSTestDriver extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            JSTestDriverSupport.getDefault().start(null);
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return !JSTestDriverSupport.getDefault().isRunning() && JSTestDriverCustomizerPanel.getPort() != -1;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TestDriverServiceNode.class, "ACTION_START");
        }


        @Override
        protected boolean asynchronous() {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }

    public static class RestartJSTestDriver extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            JSTestDriverSupport.getDefault().stop();
            JSTestDriverSupport.getDefault().start(null);
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return JSTestDriverSupport.getDefault().isRunning() && JSTestDriverCustomizerPanel.getPort() != -1 && 
                    !JSTestDriverSupport.getDefault().wasStartedExternally();
        }

        @Override
        @NbBundle.Messages("ACTION_RESTART=Restart")
        public String getName() {
            return Bundle.ACTION_RESTART();
        }


        @Override
        protected boolean asynchronous() {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }
    
    public static class StopJSTestDriver extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            JSTestDriverSupport.getDefault().stop();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return JSTestDriverSupport.getDefault().isRunning() && 
                    !JSTestDriverSupport.getDefault().wasStartedExternally();
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TestDriverServiceNode.class, "ACTION_STOP");
        }


        @Override
        protected boolean asynchronous() {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }
    
    public static class ConfigureJSTestDriver extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            JSTestDriverSupport.getDefault().configure();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TestDriverServiceNode.class, "ACTION_CONFIGURE");
        }


        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }
}
