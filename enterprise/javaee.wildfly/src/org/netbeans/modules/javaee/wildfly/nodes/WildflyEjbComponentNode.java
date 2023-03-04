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
import static org.netbeans.modules.javaee.wildfly.nodes.Util.EJB_ENTITY_ICON;
import static org.netbeans.modules.javaee.wildfly.nodes.Util.EJB_MESSAGE_ICON;
import static org.netbeans.modules.javaee.wildfly.nodes.Util.EJB_SESSION_ICON;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyEjbComponentNode extends AbstractNode {

   public enum Type {
        MDB("message-driven-bean", EJB_MESSAGE_ICON),
        SINGLETON("singleton-bean", EJB_SESSION_ICON),
        STATELESS( "stateless-session-bean", EJB_SESSION_ICON),
        ENTITY("entity-bean", EJB_ENTITY_ICON),
        STATEFULL("stateful-session-bean", EJB_SESSION_ICON);

        private final String propertyName;
        private final String icon;
        Type(final String propertyName, final String icon) {
            this.propertyName = propertyName;
            this.icon = icon;
        }

        public String getPropertyName() {
            return this.propertyName;
        }

        public String getIcon() {
            return this.icon;
        }
    };

   private final Type ejbType;
    public WildflyEjbComponentNode(String ejbName, Type ejbType) {
        super(Children.LEAF);
        this.ejbType = ejbType;
        setDisplayName(ejbName);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new SystemAction[]{};

    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(ejbType.getIcon());
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
}
