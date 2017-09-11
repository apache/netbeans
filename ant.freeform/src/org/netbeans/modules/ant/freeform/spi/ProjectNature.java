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
