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

        int position() default Integer.MAX_VALUE;
;
    }
    
}
