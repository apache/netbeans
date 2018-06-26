/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
