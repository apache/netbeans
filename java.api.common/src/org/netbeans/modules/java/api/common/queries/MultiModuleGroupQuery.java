/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2017 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.queries;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.SourceGroup;

/**
 * Provides module-related extended information on SourceGroup. In a multi-module project with
 * multiple source roots and module paths defined by masks, several package roots may reside in
 * folder with the same name (e.g. {@code "classes"}). The default {@link SourceGroup} presentation
 * (display name) includes module information, so the user can identify individual package root
 * folders. In some cases, different presentation, or more precise information on
 * relationship between a SourceGroup and module is needed. Such information can be obtained
 * using {@code MultiModuleGroupQuery}.
 * <p/>
 * The object can be obtained from project Lookup. If it is not present, the project does not support
 * java modules.
 * <p/>
 * If the source level of the project is 8 or less, the {@code MultiModuleGroupQuery} may be present,
 * but will return {@code null} on all queries, as modules are not supported before JDK9.
 * @author sdedic
 * @since 1.103
 */
public interface MultiModuleGroupQuery {
    /**
     * Obtains module-related information for the passed {@link SourceGroup}.
     * Returns {@code null}, if {@code SourceGroup} is not known, is not owned by any module, or language
     * level for the project does not support modules.
     * 
     * @param grp not null, the {@code SourceGroup} instance
     * @return module-related information, or {@code null} 
     */
    @CheckForNull 
    public Result       findModuleInfo(@NonNull SourceGroup grp);
    
    /**
     * Determines which {@link SourceGroup}s are owned by a particular module.
     * Returns {@link SourceGroups} contained within the module, empty array
     * if module does not exist or does not contain any of the passed {@code SourceGroups}.
     * @param modName module name
     * @param groups groups, to filter.
     * @return SourceGroups contained by that particular module.
     */
    @NonNull
    public SourceGroup[] filterModuleGroups(@NonNull String modName, @NonNull SourceGroup[] groups);
    
    /**
     * Describes properties of a {@link SourceGroup} related to modular
     * structure of the project.
     */
    public static final class Result {
        private final String  moduleName;
        private final String  modulePath;
        
        Result(String moduleName, String modulePath) {
            this.moduleName = moduleName;
            this.modulePath = modulePath;
        }

        /**
         * @return name of module owning the source group
         */
        public String getModuleName() {
            return moduleName;
        }
        
        /**
         * @return path to source group within the module
         */
        public String getPathFromModule() {
            return modulePath;
        }
    }
}
