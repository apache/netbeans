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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.customizer.Customizer;
import org.netbeans.modules.javaee.wildfly.customizer.CustomizerDataSupport;
import org.netbeans.modules.javaee.wildfly.ide.WildflyJ2eePlatformFactory;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.nodes.actions.KillServerAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.OpenServerLogAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.ShowAdminToolAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.WildflyEditConfigAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ivan Sidorkin
 */
public class WildflyManagerNode extends AbstractNode implements Node.Cookie {

    private final Lookup lookup;
    private final boolean isWidlfy;
    private static final String ADMIN_URL_WILDFLY = "/console"; //NOI18N
    private static final String HTTP_HEADER = "http://";

    public WildflyManagerNode(Children children, Lookup lookup) {
        super(children);
        this.lookup = lookup;
        this.isWidlfy = getDeploymentManager().getProperties().isWildfly();
        getCookieSet().add(this);
        getCookieSet().add(new EditCookieImpl(getDeploymentManager().getProperties().getServerProfile()));
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("j2eeplugins_property_sheet_server_node_jboss"); //NOI18N
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public Component getCustomizer() {
        CustomizerDataSupport dataSup = new CustomizerDataSupport(getDeploymentManager().getProperties());
        return new Customizer(getDeploymentManager(), dataSup,
                new WildflyJ2eePlatformFactory().getJ2eePlatformImpl(getDeploymentManager()));
    }

    public String getAdminURL() {
        return HTTP_HEADER + getDeploymentManager().getHost() + ":" + getDeploymentManager().getPort() + ADMIN_URL_WILDFLY;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] newActions = new Action[5];
        newActions[0] = null;
        newActions[1] = (SystemAction.get(ShowAdminToolAction.class));
        newActions[2] = (SystemAction.get(WildflyEditConfigAction.class));
        newActions[3] = (SystemAction.get(OpenServerLogAction.class));
        newActions[4] = (SystemAction.get(KillServerAction.class));
        return newActions;
    }

    @Override
    public Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set properties = sheet.get(Sheet.PROPERTIES);
        if (properties == null) {
            properties = Sheet.createPropertiesSet();
            sheet.put(properties);
        }
        final InstanceProperties ip = getDeploymentManager().getInstanceProperties();

        Node.Property property = new PropertySupport.ReadWrite(
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_DISPLAY_NAME"), //NOI18N
                String.class,
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_DISPLAY_NAME"), NbBundle.getMessage(WildflyManagerNode.class, "HINT_DISPLAY_NAME")) {
            @Override
                    public Object getValue() {
                        return ip.getProperty(WildflyPluginProperties.PROPERTY_DISPLAY_NAME);
                    }

            @Override
                    public void setValue(Object val) {
                        ip.setProperty(WildflyPluginProperties.PROPERTY_DISPLAY_NAME, (String) val);
                    }
                };

        properties.put(property);

        // servewr name
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_SERVER_NAME"), //NOI18N
                String.class,
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_SERVER_NAME"), NbBundle.getMessage(WildflyManagerNode.class, "HINT_SERVER_NAME")) {
            @Override
                    public Object getValue() {
                        return ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER);
                    }
                };
        properties.put(property);

        //server location
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_SERVER_PATH"), //NOI18N
                String.class,
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_SERVER_PATH"), NbBundle.getMessage(WildflyManagerNode.class, "HINT_SERVER_PATH")) {
            @Override
                    public Object getValue() {
                        return ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR);
                    }
                };
        properties.put(property);

        //host
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_HOST"), //NOI18N
                String.class,
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_HOST"), NbBundle.getMessage(WildflyManagerNode.class, "HINT_HOST")) {
            @Override
                    public Object getValue() {
                        return ip.getProperty(WildflyPluginProperties.PROPERTY_HOST);
                    }
                };
        properties.put(property);

        //port
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_PORT"), //NOI18N
                Integer.TYPE,
                NbBundle.getMessage(WildflyManagerNode.class, "LBL_PORT"), NbBundle.getMessage(WildflyManagerNode.class, "HINT_PORT")) {
            @Override
                    public Object getValue() {
                        return new Integer(ip.getProperty(WildflyPluginProperties.PROPERTY_PORT));
                    }
                };
        properties.put(property);

        return sheet;
    }

    @Override
    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16) {
            if(isWidlfy) {
                return ImageUtilities.loadImage("org/netbeans/modules/javaee/wildfly/resources/wildfly.png"); // NOI18N
            }
            return ImageUtilities.loadImage("org/netbeans/modules/javaee/wildfly/resources/eap.gif"); // NOI18N
        }
        return super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getShortDescription() {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(getDeploymentManager().getUrl());
        String host = ip.getProperty(WildflyPluginProperties.PROPERTY_HOST);
        String port = ip.getProperty(WildflyPluginProperties.PROPERTY_PORT);
        return HTTP_HEADER + host + ":" + port + "/"; // NOI18N
    }

    public final WildflyDeploymentManager getDeploymentManager() {
        return ((WildflyDeploymentManager) lookup.lookup(WildflyDeploymentManager.class));
    }

    public static class EditCookieImpl implements EditCookie {

        private final String configFile;

        private EditCookieImpl(String configFile) {
            this.configFile = configFile;
        }

        @Override
        public void edit() {
            FileObject fo = FileUtil.toFileObject(new File(configFile));
            try {
                DataObject.find(fo).getLookup().lookup(OpenCookie.class).open();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }
}
