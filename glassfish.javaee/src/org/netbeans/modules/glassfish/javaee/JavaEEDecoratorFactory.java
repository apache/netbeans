/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.awt.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.glassfish.spi.Decorator;
import org.netbeans.modules.glassfish.spi.DecoratorFactory;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ResourceDecorator;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Peter Williams   
 */
public class JavaEEDecoratorFactory implements DecoratorFactory {

    private static DecoratorFactory singleton = new JavaEEDecoratorFactory();
    
    private JavaEEDecoratorFactory() {
    }
    
    public static DecoratorFactory getDefault() {
        return singleton;
    }
    
    // ------------------------------------------------------------------------
    //  DecoratorFactor implementation
    // ------------------------------------------------------------------------
    @Override
    public boolean isTypeSupported(String type) {
        return decoratorMap.containsKey(type);
    }

    @Override
    public Decorator getDecorator(String type) {
        return decoratorMap.get(type);
    }

    @Override
    public Map<String, Decorator> getAllDecorators() {
        return Collections.unmodifiableMap(decoratorMap);
    }

    // ------------------------------------------------------------------------
    //  Internals...
    // ------------------------------------------------------------------------
    
    private static final String JDBC_RESOURCE_ICON = 
            "org/netbeans/modules/glassfish/javaee/resources/jdbc.gif"; // NOI18N
    private static final String CONNECTOR_ICON =
            "org/netbeans/modules/glassfish/javaee/resources/connector.gif"; // NOI18N
    private static final String APPCLIENT_ICON =
            "org/netbeans/modules/glassfish/javaee/resources/appclient.gif"; // NOI18N
    private static final String JAVAMAIL_ICON =
            "org/netbeans/modules/glassfish/javaee/resources/javamail.gif"; // NOI18N
    
    public static final Decorator J2EE_APPLICATION_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public boolean canDeployTo() { return true; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_FOLDER); }
        @Override public Image getOpenedIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER); }
    };
    
    public static final Decorator J2EE_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_ARCHIVE); }
    };
    
    public static final Decorator WEB_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return true; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.WAR_ARCHIVE); }
    };
    
    public static final Decorator EJB_JAR = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EJB_ARCHIVE); }
    };

    public static final Decorator APPCLIENT = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(APPCLIENT_ICON); }
    };

    public static final Decorator CONNECTOR = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
    };
    
    public static final Decorator DISABLED_J2EE_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_ARCHIVE); }
        @Override public Image getIconBadge() {return Decorator.DISABLED_BADGE; }
    };

    public static final Decorator DISABLED_WEB_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.WAR_ARCHIVE); }
        @Override public Image getIconBadge() {return Decorator.DISABLED_BADGE; }
    };

    public static final Decorator DISABLED_EJB_JAR = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EJB_ARCHIVE); }
        @Override public Image getIconBadge() {return Decorator.DISABLED_BADGE; }
    };

    public static final Decorator DISABLED_APPCLIENT = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(APPCLIENT_ICON); }
        @Override public Image getIconBadge() {return Decorator.DISABLED_BADGE; }
    };

    public static final Decorator DISABLED_CONNECTOR = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canEnable() { return true; }
        @Override public boolean canDisable() { return true; }
        @Override public boolean canShowBrowser() { return false; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
        @Override public Image getIconBadge() {return Decorator.DISABLED_BADGE; }
    };

    public static final Decorator JDBC_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(JDBC_RESOURCE_ICON); }
        @Override public Image getOpenedIcon(int type) { return ImageUtilities.loadImage(JDBC_RESOURCE_ICON); }
    };
    
    public static final Decorator JDBC_MANAGED_DATASOURCES = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(JDBC_RESOURCE_ICON); }
        @Override public String getCmdPropertyName() { return "jdbc_resource_name"; }
    };
    
    public static final Decorator JDBC_NATIVE_DATASOURCES = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(JDBC_RESOURCE_ICON); }
        @Override public String getCmdPropertyName() { return "jdbc_resource_name"; }
    };

    public static final Decorator CONNECTORS_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
        @Override public Image getOpenedIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
    };
    
    public static final Decorator CONNECTION_POOLS = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(JDBC_RESOURCE_ICON); }
        @Override public String getCmdPropertyName() { return "jdbc_connection_pool_id"; }
        @Override public boolean isCascadeDelete() { return true; }
    };

    public static final Decorator CONN_RESOURCE = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
        @Override public String getCmdPropertyName() { return "connector_resource_name"; }
    };
    
    public static final Decorator CONN_CONNECTION_POOL = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
        @Override public String getCmdPropertyName() { return "poolname"; }
        @Override public boolean isCascadeDelete() { return true; }
    };

    public static final Decorator ADMINOBJECT_RESOURCE = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(CONNECTOR_ICON); }
        @Override public String getCmdPropertyName() { return "jndi_name"; }
    };

    public static final Decorator JAVAMAIL_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(JAVAMAIL_ICON); }
        @Override public Image getOpenedIcon(int type) { return ImageUtilities.loadImage(JAVAMAIL_ICON); }
    };
    
    public static final Decorator JAVAMAIL_RESOURCE = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(JAVAMAIL_ICON); }
        @Override public String getCmdPropertyName() { return "jndi_name"; }
    };
    
    private static final Map<String, Decorator> decoratorMap = new HashMap<String, Decorator>();
    
    static {
        // !PW XXX need to put in correct strings, then define as static 
        //   (export in Decorator API, for lack of better place)
        decoratorMap.put(GlassfishModule.WEB_CONTAINER, WEB_APPLICATION);
        decoratorMap.put(GlassfishModule.EJB_CONTAINER, EJB_JAR);
        decoratorMap.put(GlassfishModule.EAR_CONTAINER, J2EE_APPLICATION);
        decoratorMap.put(GlassfishModule.APPCLIENT_CONTAINER, APPCLIENT);
        decoratorMap.put(GlassfishModule.CONNECTOR_CONTAINER, CONNECTOR);
        decoratorMap.put(Decorator.DISABLED+GlassfishModule.WEB_CONTAINER, DISABLED_WEB_APPLICATION);
        decoratorMap.put(Decorator.DISABLED+GlassfishModule.EJB_CONTAINER, DISABLED_EJB_JAR);
        decoratorMap.put(Decorator.DISABLED+GlassfishModule.EAR_CONTAINER, DISABLED_J2EE_APPLICATION);
        decoratorMap.put(Decorator.DISABLED+GlassfishModule.APPCLIENT_CONTAINER, DISABLED_APPCLIENT);
        decoratorMap.put(Decorator.DISABLED+GlassfishModule.CONNECTOR_CONTAINER, DISABLED_CONNECTOR);
        decoratorMap.put(GlassfishModule.JDBC_RESOURCE, JDBC_MANAGED_DATASOURCES);
        decoratorMap.put(GlassfishModule.JDBC_CONNECTION_POOL, CONNECTION_POOLS);
        decoratorMap.put(GlassfishModule.JDBC, JDBC_FOLDER);
        decoratorMap.put(GlassfishModule.CONNECTORS, CONNECTORS_FOLDER);
        decoratorMap.put(GlassfishModule.CONN_RESOURCE, CONN_RESOURCE);
        decoratorMap.put(GlassfishModule.CONN_CONNECTION_POOL, CONN_CONNECTION_POOL);
        decoratorMap.put(GlassfishModule.ADMINOBJECT_RESOURCE, ADMINOBJECT_RESOURCE);
        decoratorMap.put(GlassfishModule.JAVAMAIL, JAVAMAIL_FOLDER);
        decoratorMap.put(GlassfishModule.JAVAMAIL_RESOURCE, JAVAMAIL_RESOURCE);
    };
    
}
