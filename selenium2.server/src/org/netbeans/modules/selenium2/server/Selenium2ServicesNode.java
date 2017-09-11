/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
