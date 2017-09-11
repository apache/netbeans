/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project;

import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;

/**
 * This is a less vague variant of the {@link SubprojectProvider} for code
 * that wants to access project's dependencies that are also projects.
 * Unlike some java level API this doesn't distinguish between compile, runtime, test level dependencies.
 * The implementation by project types is nonmandatory and if it's missing in the project's lookup, users should fallback to {@link SubprojectProvider}
 * @see Project#getLookup
 * @author mkleint
 * @since 1.56
 */
public interface DependencyProjectProvider {

    @NonNull Result getDependencyProjects();
    
 /**
     * Add a listener to changes in the set of dependency projects.
     * @param listener a listener to add
     */
    void addChangeListener(@NonNull ChangeListener listener);
    
    /**
     * Remove a listener to changes in the set of dependency projects.
     * @param listener a listener to remove
     */
    void removeChangeListener(@NonNull ChangeListener listener);
    
    /**
     * non mutable result object
     */
    public final class Result {
        private final boolean recursive;
        private final Set<? extends Project> projects;
        
        public Result(@NonNull Set<? extends Project> projects, boolean recursive) {
            this.projects = Collections.unmodifiableSet(projects);
            this.recursive = recursive;
        }
        
        public boolean isRecursive() {
            return recursive;
        }

        public @NonNull Set<? extends Project> getProjects() {
            return projects;
        }        
    }
}
