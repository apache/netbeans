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

package org.netbeans.spi.project;

import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

/**
 * Optional ability of projects which may have a list of "subprojects".
 * The exact interpretation of this term is at the discretion of the project,
 * but typically subprojects would be "built" as part of this project or somehow
 * used in it as dependencies; or they may just be contained or agglomerated in
 * it somehow.
 * <b>Note:</b>Since 1.56, there are a more specifically defined variants <code>DependencyProjectProvider</code> and <code>ProjectContainerProvider</code> that if defined in project provide a list of
 * projects the current project depends on or contains. In some project types ( currently maven support) that is the preferred and supported way of getting project's dependency projects and maven modules. 
 * @see Project#getLookup
 * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/ReferenceHelper.html#createSubprojectProvider()"><code>ReferenceHelper.createSubprojectProvider</code></a>
 * @author Jesse Glick
 */
public interface SubprojectProvider {
    
    /**
     * Get a set of projects which this project can be considered to depend upon somehow.
     * This information is likely to be used only for UI purposes.
     * Only direct subprojects need be listed, not all recursive subprojects.
     * There may be no direct or indirect cycles in the project dependency graph
     * but it may be a DAG, i.e. two projects may both depend on the same subproject.
     * @return an immutable and unchanging set of {@link Project}s
     * @see org.netbeans.api.project.ProjectUtils#hasSubprojectCycles
     */
    Set<? extends Project> getSubprojects();
    
    /**
     * Add a listener to changes in the set of subprojects.
     * @param listener a listener to add
     */
    void addChangeListener(ChangeListener listener);
    
    /**
     * Remove a listener to changes in the set of subprojects.
     * @param listener a listener to remove
     */
    void removeChangeListener(ChangeListener listener);
    
}
