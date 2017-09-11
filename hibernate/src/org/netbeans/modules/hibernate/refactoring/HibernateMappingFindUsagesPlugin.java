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
package org.netbeans.modules.hibernate.refactoring;

import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;

/**
 * Plugin to find usage of a Hibernate mapping file
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingFindUsagesPlugin implements RefactoringPlugin {

    private final WhereUsedQuery query;
    private FileObject fo;

    public HibernateMappingFindUsagesPlugin(WhereUsedQuery query) {
        this.query = query;
        fo = query.getRefactoringSource().lookup(FileObject.class);
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {

        if (query.getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {

            if (fo == null || !fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                // Nothing needs to be done
                return null;
            }

            // Get the old and new resource name
            String origResourceName = getOrigResourceName();
            if (origResourceName == null ) {
                //TODO: return a Problem
                return null;
            }

            // Get the configuration files
            Project proj = FileOwnerQuery.getOwner(fo);
            HibernateEnvironment env = proj.getLookup().lookup(HibernateEnvironment.class);
            if (env == null) {
                // The project does not support Hibernate framework
                return null;
            }
            List<FileObject> configFiles = env.getAllHibernateConfigFileObjects();
            if (configFiles.isEmpty()) {
                // No configuration file. Really? Should not happen
                return null;
            }
            
            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getMappingResourceOccurrences(configFiles, origResourceName, false);

            for (FileObject mappingFile : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(mappingFile);
                for (OccurrenceItem foundPlace : foundPlaces) {
                    HibernateRefactoringElement elem = new HibernateRefactoringElement(mappingFile,
                            origResourceName,
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(query, elem);
                }
            }

            return null;
        }

        return null;
    }
    
    private String getOrigResourceName() {
        Project project = FileOwnerQuery.getOwner(fo);
        SourceGroup[] grp = SourceGroups.getJavaSourceGroups(project);
        if (grp.length == 0) {
            return null;
        }

        String srcRoot = grp[0].getRootFolder().getPath();
        String oldPath = fo.getPath();
        String oldResource = oldPath.substring(srcRoot.length() + 1);
        return oldResource;
    }
}
