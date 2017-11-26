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
