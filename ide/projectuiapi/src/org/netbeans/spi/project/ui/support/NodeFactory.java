/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.project.ui.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.project.Project;

/**
 * Factory interface for distributed creation of project node's children. Implementation
 * instances are assumed to be registered in layers at a location specific for the particular
 * project type. Project types wanting to make use of NodeFactory can use the 
 * {@link org.netbeans.spi.project.ui.support.NodeFactorySupport#createCompositeChildren}
 * method to create the Project nodes's children.
 * 
<pre>
public class FooBarLogicalViewProvider implements LogicalViewProvider {
    public Node createLogicalView() {
        return new FooBarRootNode(NodeFactorySupport.createCompositeChildren(myProject, "Projects/org-foo-bar-project/Nodes"));
    }
  
}
</pre>
 * @author RichUnger
 * @since org.netbeans.modules.projectuiapi/1 1.18
 */
public interface NodeFactory {
    
    /** 
     * Create a list of children nodes for the given project. If the list is to be static,
     * use the {@link org.netbeans.spi.project.ui.support.NodeFactorySupport#fixedNodeList}
     * @return never return null, if the project is not relevant to the NodeFactory,
     * use {@link org.netbeans.spi.project.ui.support.NodeFactorySupport#fixedNodeList} empty value.
     */ 
    NodeList<?> createNodes(Project p);
    
//    Node findPath(Project p, Node root, Object target);
    /**
     * annotation to register NodeFactory instances.
     * @since org.netbeans.modules.projectuiapi 1.33
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {
        /**
         * token(s) denoting one or more project types, eg. org-netbeans-modules-maven or org-netbeans-modules-java-j2seproject
         * {@link NodeFactorySupport#createCompositeChildren} may be passed a path of {@code Projects/TYPE/Nodes}.
         */
        String[] projectType();
        
        String parentPath() default "";

        int position() default Integer.MAX_VALUE;
;
    }
    
}
