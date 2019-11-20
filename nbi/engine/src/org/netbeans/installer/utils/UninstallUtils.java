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
            return Collections.<File>emptyList();
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
