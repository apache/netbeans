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
package org.netbeans.modules.j2ee.jboss4.nodes;

import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.customizer.CustomizerDataSupport;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenServerLogAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.ShowAdminToolAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.ShowJMXConsoleAction;
import org.openide.nodes.*;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.awt.Image;
import java.beans.BeanInfo;
import org.netbeans.modules.j2ee.jboss4.customizer.Customizer;
import org.netbeans.modules.j2ee.jboss4.ide.JBJ2eePlatformFactory;
import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBManagerNode extends AbstractNode implements Node.Cookie {
    
    private Lookup lookup;
    private static final String ADMIN_URL = "/web-console/"; //NOI18N
    private static final String ADMIN_URL_60 = "/admin-console/"; //NOI18N
    private static final String ADMIN_URL_70 = "/console"; //NOI18N
    private static final String JMX_CONSOLE_URL = "/jmx-console/"; //NOI18N
    private static final String HTTP_HEADER = "http://";
    private volatile Boolean isJB7x;
    
    public JBManagerNode(Children children, Lookup lookup) {
        super(children);
        this.lookup = lookup;
        getCookieSet().add(this);
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("j2eeplugins_property_sheet_server_node_jboss"); //NOI18N
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public Component getCustomizer() {
        CustomizerDataSupport dataSup = new CustomizerDataSupport(getDeploymentManager().getProperties());
        return new Customizer(dataSup, new JBJ2eePlatformFactory().getJ2eePlatformImpl(getDeploymentManager()));
    }
    
    public String  getAdminURL() {
        Version version = getDeploymentManager().getProperties().getServerVersion();
        if (version != null && JBPluginUtils.JBOSS_7_0_0.compareTo(version) <= 0) {
            return HTTP_HEADER+getDeploymentManager().getHost()+":"+getDeploymentManager().getPort()+ ADMIN_URL_70;
        } else if (version != null && JBPluginUtils.JBOSS_6_0_0.compareTo(version) <= 0) {
            return HTTP_HEADER+getDeploymentManager().getHost()+":"+getDeploymentManager().getPort()+ ADMIN_URL_60;
        } 
        return HTTP_HEADER+getDeploymentManager().getHost()+":"+getDeploymentManager().getPort()+ ADMIN_URL;
    }
    
    public String  getJMXConsoleURL() {
        return HTTP_HEADER+getDeploymentManager().getHost()+":"+getDeploymentManager().getPort()+ JMX_CONSOLE_URL;
    }
    
    public Action[] getActions(boolean context) {
        Action[]  newActions = new Action[isJB7x() ? 3 : 4] ;
        newActions[0]= null;
        newActions[1]= (SystemAction.get(ShowAdminToolAction.class));
        if (isJB7x()) {
            newActions[2]= (SystemAction.get(OpenServerLogAction.class));            
        } else {
            newActions[2]= (SystemAction.get(ShowJMXConsoleAction.class));
            newActions[3]= (SystemAction.get(OpenServerLogAction.class));
        }
        return newActions;
    }
    
    public Sheet createSheet(){
        Sheet sheet = super.createSheet();
        Sheet.Set properties = sheet.get(Sheet.PROPERTIES);
        if (properties == null) {
            properties = Sheet.createPropertiesSet();
            sheet.put(properties);
        }
        final InstanceProperties ip = getDeploymentManager().getInstanceProperties();
        
        Node.Property property=null;
        
        // DISPLAY NAME
        property = new PropertySupport.ReadWrite(
                NbBundle.getMessage(JBManagerNode.class, "LBL_DISPLAY_NAME"), //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_DISPLAY_NAME"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_DISPLAY_NAME")   // NOI18N
                ) {
            public Object getValue() {
                return ip.getProperty(JBPluginProperties.PROPERTY_DISPLAY_NAME);
            }
            
            public void setValue(Object val) {
                ip.setProperty(JBPluginProperties.PROPERTY_DISPLAY_NAME, (String)val);
            }
        };
        
        properties.put(property);
        
        // servewr name
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_NAME"),    //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_NAME"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_SERVER_NAME")   // NOI18N
                ) {
            public Object getValue() {
                return ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
            }
        };
        properties.put(property);
        
        //server location
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_PATH"),   //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_PATH"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_SERVER_PATH")   // NOI18N
                ) {
            public Object getValue() {
                return ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
            }
        };
        properties.put(property);
        
        //host
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_HOST"),    //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_HOST"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_HOST")   // NOI18N
                ) {
            public Object getValue() {
                return ip.getProperty(JBPluginProperties.PROPERTY_HOST);
            }
        };
        properties.put(property);
        
        //port
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_PORT"),    //NOI18N
                Integer.TYPE,
                NbBundle.getMessage(JBManagerNode.class, "LBL_PORT"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_PORT")   // NOI18N
                ) {
            public Object getValue() {
                return new Integer(ip.getProperty(JBPluginProperties.PROPERTY_PORT));
            }
        };
        properties.put(property);
        
        return sheet;
    }
    
    private boolean isJB7x() {
        if (isJB7x == null) {
            Version version = getDeploymentManager().getProperties().getServerVersion();
            isJB7x = version != null && JBPluginUtils.JBOSS_7_0_0.compareTo(version) <= 0;
        }
        return isJB7x;
    }
        
    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16) {
            if (isJB7x()) {
                return ImageUtilities.loadImage("org/netbeans/modules/j2ee/jboss4/resources/as7_16x16.png"); // NOI18N
            }
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/jboss4/resources/16x16.gif"); // NOI18N
        }
        return super.getIcon(type);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    public String getShortDescription() {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(getDeploymentManager().getUrl());
        String host = ip.getProperty(JBPluginProperties.PROPERTY_HOST);
        String port = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
        return  HTTP_HEADER + host + ":" + port + "/"; // NOI18N
    }
    
    public JBDeploymentManager getDeploymentManager() {
        return ((JBDeploymentManager) lookup.lookup(JBDeploymentManager.class));
    }
}
