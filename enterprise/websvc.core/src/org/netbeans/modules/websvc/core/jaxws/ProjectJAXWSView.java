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

package org.netbeans.modules.websvc.core.jaxws;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsRootNode;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class ProjectJAXWSView implements JAXWSViewImpl {
    
    /** Creates a new instance of ProjectJAXWSView */
    public ProjectJAXWSView() {
    }

    public Node createJAXWSView(Project project) {
        if (project != null) {
            JaxWsModel model = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            
            if (model != null) {
                Sources sources = (Sources)project.getLookup().lookup(Sources.class);
                if (sources!=null) {
                    SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    if (groups!=null) {
                        List<FileObject> roots = new ArrayList<FileObject>();
                        for (SourceGroup group: groups) {
                            roots.add(group.getRootFolder());
                        }
                        FileObject[] srcRoots = new FileObject[roots.size()];
                        roots.toArray(srcRoots);
                        return new JaxWsRootNode(project, model,srcRoots);
                    }   
                }
            }
        }
        return null;
    }
    
}
