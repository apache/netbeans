/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Version;
import org.w3c.dom.Element;

/**
 *
 * @author lfischme
 */
public class UninstallUtils {

    private static final String UPDATE_TRACKING_MODULE_VERSION_ELEMENT = "module_version";//NOI18N
    private static final String UPDATE_TRACKING_ORIGIN_ATRIBUTE = "origin";//NOI18N
    private static final String UPDATE_TRACKING_NAME_ATRIBUTE = "name";//NOI18N    
    private static final String UPDATE_TRACKING_ORIGIN_INSTALLER_STRING = "installer";//NOI18N    
    private static final String UPDATE_TRACKING_LOCATION = "update_tracking";//NOI18N
    private static final String UPDATE_BACKUP_LOCATION = "update";//NOI18N

    private static File installationLocation;
    private static List<File> emptyFolders;
    private static Set<File> updatedFiles;    
    private static Set<File> clustersRoots;
    private static Set<File> filesToDelete;

    /**
     * Returns all files that should be deleted, but not listed in registry
     * Except empty folders
     * 
     * @return all files that should be deleted, but not listed in registry
     */
    public static Set<File> getFilesToDeteleAfterUninstallation() {
        if (filesToDelete == null) {
            filesToDelete = new HashSet<File>();

            filesToDelete.addAll(getUpdatedFiles());

            for (File clusterRoot : getClustersRoots()) {
                File updateTrackingFolder = new File(clusterRoot, UPDATE_TRACKING_LOCATION);
                filesToDelete.addAll(getAllDescendantsRecursively(updateTrackingFolder));

                File lastModifiedFile = new File(clusterRoot, ".lastModified");
                if (lastModifiedFile.exists()) {
                    filesToDelete.add(lastModifiedFile);
                }

                File updateBackupFolder = new File(clusterRoot, UPDATE_BACKUP_LOCATION);
                filesToDelete.addAll(getAllDescendantsRecursively(updateBackupFolder));
            }
        }

        return filesToDelete;
    }

    /**
     * Returns list of empty folders in specified location
     *
     * @return list of empty folders in specified location
     */
    public static List<File> getEmptyFolders() {
        if (emptyFolders == null) {
            emptyFolders = new ArrayList<File>();
            for (File clusterRoot : getClustersRoots()) {
                isFolderEmpty(clusterRoot);
            }
        }

        return emptyFolders;
    }

    /**
     * Returns list of all files installed via IDE - Plugins and updates in
     * installation folder
     *
     * @return list of files installed via IDE
     */
    private static Set<File> getUpdatedFiles() {        
        if (updatedFiles == null) {
            Set<File> clustersRootsList = getClustersRoots();

            if (clustersRootsList == null || clustersRootsList.isEmpty()) {
                return Collections.EMPTY_SET;
            }
            
            FileFilter onlyXmlFilter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getPath().endsWith(".xml");
                }
            };

            updatedFiles = new HashSet<File>();

            for (File clusterRoot : clustersRootsList) {
                File clusterUpdateTrackingFolder = new File(clusterRoot, UPDATE_TRACKING_LOCATION);
                File[] updateTrackingFiles = clusterUpdateTrackingFolder.listFiles(onlyXmlFilter);

                if (updateTrackingFiles != null) {
                    for (File trackingFile : updateTrackingFiles) {
                        try {
                            Element root = XMLUtils.getDocumentElement(trackingFile);
                            for (Element element : XMLUtils.getChildren(root, UPDATE_TRACKING_MODULE_VERSION_ELEMENT)) {
                                if (!element.getAttribute(UPDATE_TRACKING_ORIGIN_ATRIBUTE).equals(UPDATE_TRACKING_ORIGIN_INSTALLER_STRING)) {
                                    for (Element fileElement : XMLUtils.getChildren(element)) {
                                        String fileName = fileElement.getAttribute(UPDATE_TRACKING_NAME_ATRIBUTE);
                                        File file = new File(clusterRoot, fileName);
                                        updatedFiles.add(file);
                                    }
                                }
                            }
                        } catch (XMLException ex) {
                            LogManager.log(ex);
                        }
                    }
                }
            }
        }

        return updatedFiles;
    }

    /**
     * Returns list of clusters
     *
     * @return list of clusters
     */
    private static Set<File> getClustersRoots() {
        if (clustersRoots == null) {
            File installationLoc = getInstallationLocation();

            if (installationLoc != null && installationLoc.exists()) {
                FileFilter onlyDirFilter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                };
                clustersRoots = new HashSet<File>(Arrays.asList(installationLoc.listFiles(onlyDirFilter)));
            } else {
                clustersRoots = Collections.EMPTY_SET;
            }
        }

        return clustersRoots;
    }

    /**
     * Returns all descendants for specified folder
     * 
     * @param folder
     * @return all descendants for specified folder
     */
    private static List<File> getAllDescendantsRecursively(File folder) {
        if (folder != null && folder.isDirectory()) {
            List<File> descendants = new ArrayList<File>();
            for (File actualFile : folder.listFiles()) {
                if (actualFile.isDirectory()) {
                    descendants.addAll(getAllDescendantsRecursively(actualFile));
                }
                descendants.add(actualFile);
            }
            return descendants;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Returns installation location
     *
     * @return installation location
     */
    private static File getInstallationLocation() {
        if (installationLocation == null) {
            String target = System.getProperty(Registry.TARGET_COMPONENT_UID_PROPERTY);
            Registry registry = Registry.getInstance();

            for (Product product : registry.getProductsToUninstall()) {
                if (product.getUid().equals(target)) {
                    installationLocation = product.getInstallationLocation();
                }
            }
        }

        return installationLocation;
    }

    /**
     * Checks if folder is empty, if checking file, false is returned. Also
     * fills emptyFolders list by empty descendant folders.
     *
     * @param file file - folder to check
     * @return if folder is empty, if checking file, false is returned
     */
    private static boolean isFolderEmpty(File file) {
        boolean result = true;
        if (file != null && file.isDirectory()) {
            for (File descendant : file.listFiles()) {
                result = isFolderEmpty(descendant) & result;
            }
            if (result) {
                emptyFolders.add(file);
            }
        } else {
            result = false;
        }
        return result;
    }
}
