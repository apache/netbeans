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

package org.netbeans.modules.payara.jakartaee;

import java.awt.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.payara.spi.Decorator;
import org.netbeans.modules.payara.spi.DecoratorFactory;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.ResourceDecorator;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Peter Williams
 * @author Gaurav Gupta
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
            "org/netbeans/modules/payara/jakartaee/resources/jdbc.gif"; // NOI18N
    private static final String CONNECTOR_ICON =
            "org/netbeans/modules/payara/jakartaee/resources/connector.gif"; // NOI18N
    private static final String APPCLIENT_ICON =
            "org/netbeans/modules/payara/jakartaee/resources/appclient.gif"; // NOI18N
    private static final String JAVAMAIL_ICON =
            "org/netbeans/modules/payara/jakartaee/resources/javamail.gif"; // NOI18N
    
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
        @Override public boolean canCDIProbeEnable() { return true; }
        @Override public boolean canCDIProbeDisable() { return true; }
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
        decoratorMap.put(PayaraModule.WEB_CONTAINER, WEB_APPLICATION);
        decoratorMap.put(PayaraModule.EJB_CONTAINER, EJB_JAR);
        decoratorMap.put(PayaraModule.EAR_CONTAINER, J2EE_APPLICATION);
        decoratorMap.put(PayaraModule.APPCLIENT_CONTAINER, APPCLIENT);
        decoratorMap.put(PayaraModule.CONNECTOR_CONTAINER, CONNECTOR);
        decoratorMap.put(Decorator.DISABLED+PayaraModule.WEB_CONTAINER, DISABLED_WEB_APPLICATION);
        decoratorMap.put(Decorator.DISABLED+PayaraModule.EJB_CONTAINER, DISABLED_EJB_JAR);
        decoratorMap.put(Decorator.DISABLED+PayaraModule.EAR_CONTAINER, DISABLED_J2EE_APPLICATION);
        decoratorMap.put(Decorator.DISABLED+PayaraModule.APPCLIENT_CONTAINER, DISABLED_APPCLIENT);
        decoratorMap.put(Decorator.DISABLED+PayaraModule.CONNECTOR_CONTAINER, DISABLED_CONNECTOR);
        decoratorMap.put(PayaraModule.JDBC_RESOURCE, JDBC_MANAGED_DATASOURCES);
        decoratorMap.put(PayaraModule.JDBC_CONNECTION_POOL, CONNECTION_POOLS);
        decoratorMap.put(PayaraModule.JDBC, JDBC_FOLDER);
        decoratorMap.put(PayaraModule.CONNECTORS, CONNECTORS_FOLDER);
        decoratorMap.put(PayaraModule.CONN_RESOURCE, CONN_RESOURCE);
        decoratorMap.put(PayaraModule.CONN_CONNECTION_POOL, CONN_CONNECTION_POOL);
        decoratorMap.put(PayaraModule.ADMINOBJECT_RESOURCE, ADMINOBJECT_RESOURCE);
        decoratorMap.put(PayaraModule.JAVAMAIL, JAVAMAIL_FOLDER);
        decoratorMap.put(PayaraModule.JAVAMAIL_RESOURCE, JAVAMAIL_RESOURCE);
    };
    
}
