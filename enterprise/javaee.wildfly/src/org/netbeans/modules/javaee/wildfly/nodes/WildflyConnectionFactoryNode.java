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
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.javaee.wildfly.config.WildflyConnectionFactory;
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
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyConnectionFactoryNode extends AbstractNode {


    WildflyConnectionFactoryNode(WildflyConnectionFactory connectionFactory, Lookup lookup) {
        super(Children.LEAF);
        setDisplayName(connectionFactory.getName());
        setName(connectionFactory.getName());
        setShortDescription(connectionFactory.getName());
        initProperties(connectionFactory.getConfiguration());
    }

    protected final void initProperties(Map<String, String> configuration) {
        for(Map.Entry<String, String> property : configuration.entrySet()) {
            addProperty(property.getKey(), property.getValue());
        }
    }

    private void addProperty(String name, String value) {
        PropertySupport ps = new SimplePropertySupport(name, value, name, name);
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
        return new SystemAction[]{SystemAction.get(PropertiesAction.class)};
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(Util.CONNECTOR_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(Util.CONNECTOR_ICON);
    }

}
