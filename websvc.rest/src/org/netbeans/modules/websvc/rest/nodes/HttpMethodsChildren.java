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

import org.netbeans.modules.websvc.rest.support.Utils;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;



public class HttpMethodsChildren extends ChildFactory<HttpMethodNode> implements PropertyChangeListener {
    
    public HttpMethodsChildren(Project project,RestServicesModel model, 
            String serviceName) {
        this.project = project;
        this.model = model;
        this.serviceName = serviceName;
        model.addPropertyChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
     */
    @Override
    protected boolean createKeys( final List<HttpMethodNode> keys ) {
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.getRestServiceDescription(
                            serviceName);
                    
                    if (desc != null) {
                        for (RestMethodDescription method : desc.getMethods()) {
                            if (method instanceof HttpMethod) {
                                keys.add(new HttpMethodNode(project, desc,
                                        (HttpMethod) method));
                            }
                        }
                        Collections.sort( keys , COMPARATOR );
                    }
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
    
    @Override
    protected Node createNodeForKey( HttpMethodNode node ){
        return node;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (RestServices.PROP_SERVICES.equals(evt.getPropertyName())) {
            refresh(false);
        }
    }
            
            
    
    static class HttpMethodsComparator implements Comparator<HttpMethodNode> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare( HttpMethodNode node1, HttpMethodNode node2 ) {
            String key1 = node1.getKey();
            String key2 = node2.getKey();
            return key1.compareTo( key2 );
        }
        
    }
  
    private static final Comparator<HttpMethodNode> COMPARATOR = 
        new HttpMethodsComparator();
    
    private Project project;
    private RestServicesModel model;
    private String serviceName;
    
}
