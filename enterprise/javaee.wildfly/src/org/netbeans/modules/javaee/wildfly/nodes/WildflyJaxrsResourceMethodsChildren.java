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

import static org.openide.nodes.Children.LEAF;

import java.awt.Image;
import java.util.Collection;
import javax.swing.Action;
import org.netbeans.modules.javaee.wildfly.nodes.actions.OpenURLAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.OpenURLActionCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author <a href="mailto:ehugonne@redhat.com">Emmanuel Hugonnet</a> (c) 2015 Red Hat, inc.
 */
public class WildflyJaxrsResourceMethodsChildren extends Children.Keys {
    private final String serverUrl;
    public WildflyJaxrsResourceMethodsChildren(String serverUrl, Collection methods) {
        super(false);
        this.serverUrl = serverUrl;
        setKeys(methods);
    }

    @Override
    protected Node[] createNodes(final Object key) {
        return new Node[]{new JaxrsResourceMethodNode(key.toString(), serverUrl)};
    }

    private static class JaxrsResourceMethodNode extends AbstractNode {
        private final String url;
        public JaxrsResourceMethodNode(String jaxrsMethod, String serverUrl) {
            super(LEAF);
            setDisplayName(jaxrsMethod);
            setName(jaxrsMethod);
            setShortDescription(jaxrsMethod);
            int baseIndex = jaxrsMethod.indexOf(' ');
            String removedMethod = jaxrsMethod.substring(baseIndex + 1);
            url = serverUrl + removedMethod.substring(0, removedMethod.indexOf(' '));
            if (url != null) {
                getCookieSet().add(new JaxrsResourceMethodNode.OpenURLActionCookieImpl(url));
            }
        }

        @Override
        public Action[] getActions(boolean context) {
            if (url != null) {
                return new SystemAction[]{
                    SystemAction.get(OpenURLAction.class)
                };
            } else {
                return new SystemAction[0];
            }
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(Util.JAXRS_METHOD_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return ImageUtilities.loadImage(Util.JAXRS_METHOD_ICON);
        }
        private static class OpenURLActionCookieImpl implements OpenURLActionCookie {

            private final String url;

            public OpenURLActionCookieImpl(String url) {
                this.url = url;
            }

            @Override
            public String getWebURL() {
                return url;
            }
        }
    }
}
