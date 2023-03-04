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

package org.netbeans.modules.web.freeform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author David Konecny
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.ant.freeform.spi.ProjectNature.class, position=10)
public class WebProjectNature implements ProjectNature {

    public static final String NS_WEB_1 = "http://www.netbeans.org/ns/freeform-project-web/1"; // NOI18N
    public static final String EL_WEB = "web-data";
    public static final String NS_WEB_2 = "http://www.netbeans.org/ns/freeform-project-web/2"; // NOI18N
    
  
    public WebProjectNature() {}
    
    public List<TargetDescriptor> getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        ArrayList<TargetDescriptor> l = new ArrayList<TargetDescriptor>();
        if (!LookupProviderImpl.isMyProject(aux)) {
            return l;
        }
        l.add(getExtraTarget());
        return l;
    }
    
    public Set<String> getSourceFolderViewStyles() {
        return Collections.<String>emptySet();
    }
    
    public Node createSourceFolderView(Project project, FileObject folder, String includes, String excludes, String style, String name, String displayName) throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    public Node findSourceFolderViewPath(Project project, Node root, Object target) {
        return null;
    }
    
    public static TargetDescriptor getExtraTarget() {
        return new TargetDescriptor(WebProjectConstants.COMMAND_REDEPLOY, Arrays.asList(new String[]{"deploy", ".*deploy.*"}),  // NOI18N
            NbBundle.getMessage(WebProjectNature.class, "LBL_TargetMappingPanel_Deploy"), // NOI18N
            NbBundle.getMessage(WebProjectNature.class, "ACSD_TargetMappingPanel_Deploy")); // NOI18N
    }
}
