/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
