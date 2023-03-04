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

package org.netbeans.modules.ant.freeform.spi;

import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * Description of base freeform project extension. Instances should be
 * registered into default lookup. Freeform project will always call all
 * registered implementations of this interface and it is up to the 
 * implementation to decide (based on the project's metadata) whether they
 * want or should enhance the project or not.
 *
 * @author David Konecny, Jesse Glick
 */
public interface ProjectNature {

    /**
     * Check project and provide additional build targets to be shown in 
     * target mapping customizer panel if it is project of your type. Order
     * of targets is important.
     * @return a list of {@link TargetDescriptor}s (can be empty but not null)
     */
    List<TargetDescriptor> getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux);
    
    /**
     * Get a set of view styles supported by the nature for displaying source folders in the logical view.
     * @return a set of <code>String</code> style names (may be empty but not null)
     */
    Set<String> getSourceFolderViewStyles();
    
    /**
     * Produce a logical view of a source folder in a style supported by the nature.
     * @param project a project displaying the view
     * @param folder a file folder (typically part of the project but not necessarily) to produce a view of
     * @param includes an Ant-style includes list, or null
     * @param excludes an Ant-style excludes list, or null
     * @param style a view style; will be one of {@link #getSourceFolderViewStyles}
     * @param name a suggested code name for the new node
     * @param displayName a suggested display name for the new node (may be null, in which case provider is free to pick an appropriate display name)
     * @return a logical view of that folder
     * @throws IllegalArgumentException if the supplied style is not one of {@link #getSourceFolderViewStyles}
     * @see org.netbeans.spi.project.support.ant.PathMatcher
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    Node createSourceFolderView(Project project, FileObject folder, String includes, String excludes, String style, String name, String displayName) throws IllegalArgumentException;
    
    /**
     * Try to find a node selection in a source folder logical view.
     * @param project a project displaying the view
     * @param root a source folder view node which may have been returned by {@link #createSourceFolderView} (or not)
     * @param target a lookup entry indicating the node to find (e.g. a {@link FileObject})
     * @return a subnode of the root node representing the target, or null if either the target could not be found, or the root node was not recognized
     * @see org.netbeans.spi.project.ui.LogicalViewProvider#findPath
     */
    Node findSourceFolderViewPath(Project project, Node root, Object target);
    
}
