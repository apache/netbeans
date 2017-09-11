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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Plugin for refactor/move Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingMovePlugin implements RefactoringPlugin {

    private MoveRefactoring refactoring;
    private Project project;
    private String srcRoot;
    private ArrayList<MappingFileData> toBeMovedMappingFiles;

    public HibernateMappingMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {

        URL targetURL = refactoring.getTarget().lookup(URL.class);
        if (targetURL == null) {
            // TODO: return a Problem
            return null;
        }

        String targetPackageName = HibernateRefactoringUtil.getPackageName(targetURL);
        if (targetPackageName == null) {
            // TODO: return a Problem
            return null;
        }
        
        // The files to be moved. Can be multiple of them
        toBeMovedMappingFiles = getToBeMovedMappingFileData(targetPackageName);

        // The mapping files in the target package
        ArrayList<MappingFileData> mappingFilesInTargetPkg = getMappingFileDataInTargetPkg(targetPackageName);

        // Make sure the to be moved mapping does not exist in the target package
        Problem fastCheckProblem = null;
        for (MappingFileData toBeMovedFo : toBeMovedMappingFiles) {

            for (MappingFileData existingFo : mappingFilesInTargetPkg) {
                if (!toBeMovedFo.getFo().equals(existingFo.getFo()) && toBeMovedFo.getNewResourcename().equals(existingFo.getResourceName())) {

                    fastCheckProblem = HibernateRefactoringUtil.createProblem(fastCheckProblem,
                            true, NbBundle.getMessage(HibernateMappingRenamePlugin.class, "MSG_Name_Exists", toBeMovedFo.getFo().getNameExt()));

                    return fastCheckProblem;
                }
            }
        }
        return fastCheckProblem;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {

        // Get the configuration files
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
        if (env == null) {
            // The project does not support Hibernate framework
            return null;
        }
        List<FileObject> configFiles = env.getAllHibernateConfigFileObjects();
        if (configFiles.isEmpty()) {
            return null;
        }

        // TODO: have all the modifications in one transaction
        for(MappingFileData tobemoved : toBeMovedMappingFiles) {
            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getMappingResourceOccurrences(configFiles, tobemoved.getResourceName(), false);

            for (FileObject configFile : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(configFile);
                for (OccurrenceItem foundPlace : foundPlaces) {
                    HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(configFile,
                            tobemoved.getResourceName(),
                            tobemoved.getNewResourcename(),
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(refactoring, elem);
                }
            }
            refactoringElements.registerTransaction(new HibernateMappingRenameTransaction(
                    occurrences.keySet(), tobemoved.getResourceName(), tobemoved.getNewResourcename()));
        }

        return null;
    }

    private ArrayList<MappingFileData> getToBeMovedMappingFileData(String targetPkgName) {

        ArrayList<MappingFileData> fileData = new ArrayList<MappingFileData>();
        for (FileObject fo : refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
            if(!fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                continue;
            }
            
            if (project == null) {
                project = FileOwnerQuery.getOwner(fo);
            }

            if(srcRoot == null) {
                SourceGroup[] grp = SourceGroups.getJavaSourceGroups(project);
                if (grp.length == 0) {
                    return null;
                }

                srcRoot = grp[0].getRootFolder().getPath();
            }
            String oldPath = fo.getPath();
            String oldResource = oldPath.substring(srcRoot.length() + 1);
            String fileName = oldResource.substring(oldResource.lastIndexOf("/") + 1);
            String newResource = (fileName.endsWith(".hbm.xml") ? targetPkgName.replace('.', '/') : targetPkgName) + "/" + fileName;//NOI18N, safe fix for 242415 in case if this data is used in more places

            fileData.add(new MappingFileData(fo, oldResource, newResource));
        }

        return fileData;
    }

    private ArrayList<MappingFileData> getMappingFileDataInTargetPkg(String targetPkgName) {

        ArrayList<MappingFileData> fileData = new ArrayList<MappingFileData>();
        HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
        if (hibernateEnv == null) {
            // The project does not support Hibernate framework
            return fileData;
        }
        List<FileObject> mappingFiles = hibernateEnv.getAllHibernateMappingFileObjects();
        for (FileObject fo : mappingFiles) {
            String path = fo.getPath();
            String resourceName = path.substring(srcRoot.length() + 1);
            int separator = resourceName.lastIndexOf("/");
            if (separator > 0) {
                String packageName = resourceName.substring(0, separator);
                if (packageName.equals(targetPkgName)) {
                    fileData.add(new MappingFileData(fo, resourceName, null));
                }
            }            
        }

        return fileData;
    }

    private class MappingFileData {

        private FileObject fo;
        private String resourceName;
        private String newResourcename;

        public MappingFileData(FileObject fo, String resourceName, String newResourcename) {
            this.fo = fo;
            this.resourceName = resourceName;
            this.newResourcename = newResourcename;
        }

        public FileObject getFo() {
            return fo;
        }

        public String getNewResourcename() {
            return newResourcename;
        }

        public String getResourceName() {
            return resourceName;
        }
    }
}
