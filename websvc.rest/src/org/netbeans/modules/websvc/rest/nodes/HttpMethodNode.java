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
package org.netbeans.modules.websvc.rest.nodes;

import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.support.Utils;
import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class HttpMethodNode extends AbstractNode{
    private String methodName;
    private String produceMime;
    private String consumeMime;
    private String returnType;
    
    
    public HttpMethodNode(Project project, RestServiceDescription desc, HttpMethod method) {
        this(project, desc, method, new InstanceContent());
    }
    
    private HttpMethodNode(Project project, RestServiceDescription desc, HttpMethod method,
            InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.methodName = method.getName();
        this.produceMime = method.getProduceMime();
        this.consumeMime = method.getConsumeMime();
        this.returnType = method.getReturnType();
        content.add(this);
        // enable Test method Uri action only for GET methods
        if ("GET".equals(method.getType())) { //NOI18N
            content.add(new MethodUriProvider(desc.getUriTemplate(), method.getPath()));
        }
        content.add(project);
        content.add(OpenCookieFactory.create(project, desc.getClassName(), methodName));
    }
    
    public String getDisplayName() {
        return methodName + "() : " + Utils.stripPackageName(returnType);
    }
    
    public String getShortDescription() {       
        String desc = "";       //NOI18N
        
        if (consumeMime.length() > 0) {
            desc += "@ConsumeMime(\"" + consumeMime + "\") ";        //NOI18N
        }
        
        if (produceMime.length() > 0) {
            desc += "@ProduceMime(\"" + produceMime + "\") ";        //NOI18N
        }
        
        return desc;
    }
    
    public String getKey() {
        return methodName + ":" + returnType + ":" +consumeMime +  ":" + produceMime;  //NOI18N
    }
    
    private static final java.awt.Image METHOD_BADGE =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/rest/nodes/resources/method.png" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        return METHOD_BADGE;
    }
    
    void changeIcon() {
        fireIconChange();
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(TestResourceUriAction.class),
            null,
            //SystemAction.get(DeleteAction.class),
            //null,
            SystemAction.get(PropertiesAction.class),
        };
    }

    private static class MethodUriProvider implements ResourceUriProvider {

        private String resourcePath, methodPath;

        private MethodUriProvider(String resourcePath, String methodPath) {
            this.resourcePath = resourcePath;
            this.methodPath = methodPath;
        }

        public String getResourceUri() {
            if (methodPath == null || methodPath.length() == 0) {
                return resourcePath;
            } else if (methodPath.startsWith("/")) { //NOI18N
                return resourcePath+methodPath;
            } else {
                return resourcePath+"/"+methodPath; //NOI18N
            }
        }

    }
}
