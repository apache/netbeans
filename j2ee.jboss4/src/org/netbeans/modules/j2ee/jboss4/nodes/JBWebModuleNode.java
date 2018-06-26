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

import java.awt.Image;
import javax.enterprise.deploy.shared.ModuleType;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenURLAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.UndeployModuleCookieImpl;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Node which describes Web Module.
 *
 * @author Michal Mocnak
 */
public class JBWebModuleNode extends AbstractNode {

    private final JBAbilitiesSupport abilitiesSupport;

    private final String url;

    public JBWebModuleNode(String fileName, Lookup lookup, String url) {
        super(new JBServletsChildren(fileName, lookup));
        setDisplayName(fileName.substring(0, fileName.lastIndexOf('.')));
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
        this.url = url;
        if (abilitiesSupport.isRemoteManagementSupported()
                && (abilitiesSupport.isJB4x() || abilitiesSupport.isJB6x())) {
            // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
            getCookieSet().add(new UndeployModuleCookieImpl(fileName, ModuleType.WAR, lookup));
        }
        
        if (url != null) {
            getCookieSet().add(new OpenURLActionCookieImpl(url));
        }
    }
    
    public Action[] getActions(boolean context) {
        if (getParentNode() instanceof JBEarApplicationNode) {
            return new SystemAction[] {
                SystemAction.get(OpenURLAction.class)
            };
        } else {
            if (abilitiesSupport.isRemoteManagementSupported()
                    && (abilitiesSupport.isJB4x() || abilitiesSupport.isJB6x())) {
                if (url != null) {
                    return new SystemAction[] {
                        SystemAction.get(OpenURLAction.class),
                        SystemAction.get(UndeployModuleAction.class)
                    };
                } else {
                    return new SystemAction[] {
                        SystemAction.get(UndeployModuleAction.class)
                    };
                }
            } else if (url != null) {
                // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
                return new SystemAction[] {
                    SystemAction.get(OpenURLAction.class),
                };
            }
        }
        return new SystemAction[]{};
    }
    
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    private static class OpenURLActionCookieImpl implements OpenURLActionCookie {
        
        private String url;
        
        public OpenURLActionCookieImpl(String url) {
            this.url = url;
        }
        
        public String getWebURL() {
            return url;
        }
    }
}
