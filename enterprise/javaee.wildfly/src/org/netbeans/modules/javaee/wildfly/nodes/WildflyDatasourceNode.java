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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.javaee.wildfly.nodes.actions.ResourceType;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleCookieImpl;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ehugonnet
 */
public class WildflyDatasourceNode extends AbstractNode {

    public WildflyDatasourceNode(String name, Datasource ds, Lookup lookup) {
        super(Children.LEAF);
        getCookieSet().add(new UndeployModuleCookieImpl(name, ResourceType.DATASOURCE, lookup));
        setDisplayName(ds.getJndiName());
        setName(name);
        setShortDescription(ds.getDisplayName());
        initProperties(ds);
    }

    protected void initProperties(Datasource ds) {
        addProperty("Driver", ds.getDriverClassName());
        addProperty("JndiName", ds.getJndiName());
        addProperty("Url", ds.getUrl());
        addProperty("Username", ds.getUsername());
        addProperty("Password", ds.getPassword());
    }

    private void addProperty(String name, String value) {
        String displayName = NbBundle.getMessage(WildflyDatasourceNode.class, "LBL_Resources_Datasources_Datasource_" + name);
        String description = NbBundle.getMessage(WildflyDatasourceNode.class, "DESC_Resources_Datasources_Datasource_" + name);
        PropertySupport ps = new SimplePropertySupport(name, value, displayName, description);
        getSheet().get(Sheet.PROPERTIES).put(ps);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        setSheet(sheet);
        return sheet;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new SystemAction[]{
            SystemAction.get(PropertiesAction.class),
            SystemAction.get(UndeployModuleAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(Util.JDBC_RESOURCE_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(Util.JDBC_RESOURCE_ICON);
    }
}
