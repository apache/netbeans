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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;


/**
 * @author ads
 *
 */
class SubResourceLocatorsChildrenFactory extends ChildFactory<SubResourceLocatorNode> {
    
    SubResourceLocatorsChildrenFactory(Project project, RestServicesModel model, 
            String serviceName) {
        this.project = project;
        this.model = model;
        this.serviceName = serviceName;
    }

    @Override
    protected boolean createKeys( final List<SubResourceLocatorNode> list ) {
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                @Override
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.
                        getRestServiceDescription(serviceName);
                    
                    if (desc != null) {
                        String className = desc.getClassName();
                        List<RestMethodDescription> methods = desc.getMethods();
                        List<SubResourceLocator> locators = 
                            new ArrayList<SubResourceLocator>( methods.size());
                        for (RestMethodDescription method : methods ) {
                            if (method instanceof SubResourceLocator) {
                                locators.add((SubResourceLocator)method);
                            }
                        }
                        Collections.sort( locators , COMPARATOR );
                        for (SubResourceLocator locator : locators){
                            list.add( new SubResourceLocatorNode(project, 
                                   className , locator));
                        }
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
    protected Node createNodeForKey( SubResourceLocatorNode node ) {
        return node;
    }
    
    private static class SubResourceLocatorComparator implements 
        Comparator<SubResourceLocator> 
    {

        @Override
        public int compare( SubResourceLocator locator1, 
                SubResourceLocator locator2 ) 
        {
            String key1 = SubResourceLocatorNode.getKey(locator1);
            String key2 = SubResourceLocatorNode.getKey(locator2);
            return key1.compareTo(key2);
        }
        
    }
    
    private static final Comparator<SubResourceLocator>  COMPARATOR = 
        new SubResourceLocatorComparator();

    private Project project;
    private RestServicesModel model;
    private String serviceName;
}
