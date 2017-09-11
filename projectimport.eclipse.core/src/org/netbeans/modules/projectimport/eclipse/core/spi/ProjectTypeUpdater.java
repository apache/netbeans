/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.project.Project;

/**
 */
public interface ProjectTypeUpdater extends ProjectTypeFactory {

    /**
     * Returns identifier uniquely identifying data in given ProjectImportModel 
     * instance. Identifier will be used for comparison of different versions of
     * ProjectImportModel data and equality of identifier will mean that project
     * is up to data and does not require update. Identifier should also contain
     * enough data to calculate difference between ProjectImportModel it represents
     * and any given ProjectImportModel.
     * 
     * <p>Example of identifier could be: 
     * "src=src;con=org.eclipse.jdt.launching.JRE_CONTAINER;var=MAVEN_REPO/commons-lang-2.3.jar;output=bin"
     */
    String calculateKey(ProjectImportModel model);

    /**
     * Update given project.
     * 
     * <p>This method is permited to show blocking UI.
     * 
     * <p>Always called under project write mutex.
     * 
     * 
     * @param project
     * @param model
     * @param oldKey
     * @return resulting new key to be stored; it can be different from value returend by {@link #calculateKey}
     *  because it is result of actual update and may keep some items which could not be removed or added
     *  succesfully.
     */
    String update(Project project, ProjectImportModel model, String oldKey, List<String> importProblems) throws IOException;
    
}
