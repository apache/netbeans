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

package org.netbeans.api.project.ant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.project.ant.ui.FileChooserAccessory;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;


/**
 * Custom file chooser allowing user to choose how a file will be referenced 
 * from a project - via absolute path, or relative path. Make sure you call
 * {@link #getSelectedPaths} instead of {@link #getSelectedFiles} as it returns relative
 * files and performs file copying if necessary.
 * 
 * @author David Konecny
 * @since org.netbeans.modules.project.ant/1 1.19
 */
public final class FileChooser extends JFileChooser {

    private FileChooserAccessory accessory;

    /**
     * Create chooser for given AntProjectHelper. Standard file chooser is shown
     * if project is not sharable.
     * 
     * @param helper ant project helper; cannot be null
     * @param copyAllowed is file copying allowed
     */
    public FileChooser(AntProjectHelper helper, boolean copyAllowed) {
        super();
        FileObject projectFolder = helper.getProjectDirectory();
        Project p = projectFolder != null ? FileOwnerQuery.getOwner(projectFolder): null;
        LibraryManager lm = p != null ? ReferenceHelper.getProjectLibraryManager(p) : null;
        if (lm != null) {
            URL u = lm.getLocation();
            if (u != null) {
                File libBase = Utilities.toFile(URI.create(u.toExternalForm())).getParentFile();
                accessory = new FileChooserAccessory(this, FileUtil.toFile(helper.getProjectDirectory()), 
                    libBase, copyAllowed);
                setAccessory(accessory);
            }
        }
    }

    /**
     * Create chooser for given base folder and shared libraries folder. 
     * 
     * @param baseFolder base folder to which all selected files will be relativized;
     *  can be null in which case regular file chooser is shown
     * @param sharedLibrariesFolder providing shared libraries folder enables option
     *  of copying selected files there; can be null in which case copying option
     *  is disabled
     */
    public FileChooser(File baseFolder, File sharedLibrariesFolder) {
        super();
        if (baseFolder != null) {
            accessory = new FileChooserAccessory(this, baseFolder, sharedLibrariesFolder, sharedLibrariesFolder != null);
            setAccessory(accessory);
        }
    }
    
    /**
     * Enable or disable variable based selection, that is show or hide 
     * "Use Variable Path" option. For backward compatibility variable based 
     * selection is disabled by default.
     * 
     * @since org.netbeans.modules.project.ant/1 1.22
     */
    public void enableVariableBasedSelection(boolean enable) {
        if (accessory != null) {
            accessory.enableVariableBasedSelection(enable);
        }
    }
    
    /**
     * Returns array of paths selected. The difference from 
     * {@link #getSelectedFiles} is that returned paths might be relative to 
     * base folder this file chooser was created for. If user selected in UI
     * "Use Relative Path" or "Copy To Libraries Folder" then returned paths
     * will be relative. For options "Use Absolute Path" and "Use Variable Path"
     * this method return absolute paths. In case of option "Use Variable Path"
     * see also {@link #getSelectedPathVariables}.
     * 
     * @return array of files which are absolute or relative to base folder this chooser
     *  was created for; e.g. project folder in case of AntProjectHelper 
     *  constructor; never null; can be empty array
     * @throws java.io.IOException any IO problem; for example during 
     *  file copying
     */
    public String[] getSelectedPaths() throws IOException {
        if (accessory != null) {
            accessory.copyFilesIfNecessary();
            if (accessory.isRelative()) {
                return accessory.getFiles();
            }
        }
        if (isMultiSelectionEnabled()) {
            File[] sels = getSelectedFiles();
            String[] toRet = new String[sels.length];
            int index = 0;
            for (File fil : sels) {
                toRet[index] = fil.getAbsolutePath();
                index++;
            }
            return toRet;
        } else {
            if (getSelectedFile() != null) {
                return new String[]{ getSelectedFile().getAbsolutePath() };
            } else {
                return new String[0];
            }
        }
    }
    
    /**
     * For "Use Variable Path" option this method returns list of paths which 
     * start with a variable from {@link org.netbeans.spi.project.support.ant.PropertyUtils#getGlobalProperties()}. eg.
     * "${var.MAVEN}/path/file.jar". For all other options selected this method 
     * returns null.
     * 
     * @return null or list of variable based paths; if not null items in returned 
     * array corresponds to items in array returned from {@link #getSelectedPaths}
     * @since org.netbeans.modules.project.ant/1 1.22
     */
    public String[] getSelectedPathVariables() {
        if (accessory != null) {
            if (accessory.isVariableBased()) {
                return accessory.getVariableBasedFiles();
            }
        }
        return null;
    }

    @Override
    public void approveSelection() {
        if (accessory != null && !accessory.canApprove()) {
            return;
        }
        super.approveSelection();
    }



}
