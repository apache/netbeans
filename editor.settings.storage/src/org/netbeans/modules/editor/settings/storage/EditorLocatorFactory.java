/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.settings.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.netbeans.modules.editor.settings.storage.SettingsType.DefaultLocator.MODULE_FILES_FOLDER;
import org.netbeans.modules.editor.settings.storage.SettingsType.LocatorFactory;
import org.netbeans.modules.editor.settings.storage.fontscolors.ColoringStorage;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = LocatorFactory.class)
public class EditorLocatorFactory implements LocatorFactory {
    private static final Logger LOG = Logger.getLogger(EditorLocatorFactory.class.getName()); 
    
    @Override
    public SettingsType.Locator createLocator(StorageDescription sd) {
        if (ColoringStorage.ID.equals(sd.getId())) {
            return new FontsColorsLocator(sd.getId(), sd.isUsingProfiles(), sd.getMimeType(), sd.getLegacyFileName());
        } else if (KeyMapsStorage.ID.equals(sd.getId())) {
            return new LegacyTextBaseLocator(sd.getId(), sd.isUsingProfiles(), sd.getMimeType(), sd.getLegacyFileName());
        }
        return null;
    }
    
    private static final class FontsColorsLocator extends SettingsType.DefaultLocator {
        
        private static final String [] M_LEGACY_FILE_NAMES = new String [] {
            MODULE_FILES_FOLDER + "/defaultColoring.xml", // NOI18N
            MODULE_FILES_FOLDER + "/coloring.xml", // NOI18N
            MODULE_FILES_FOLDER + "/editorColoring.xml", // NOI18N
        };
        
        private static final String [] U_LEGACY_FILE_NAMES = new String [] {
            "defaultColoring.xml", // NOI18N
            "coloring.xml", // NOI18N
            "editorColoring.xml", // NOI18N
        };
        
        public FontsColorsLocator(String settingTypeId, boolean hasProfiles, String mimeType, String legacyFileName) {
            super(settingTypeId, hasProfiles, mimeType, legacyFileName);
        }
        
        @Override
        protected void addModulesLegacyFiles(
            FileObject mimeFolder,
            String profileId,
            boolean fullScan,
            Map<String, List<Object []>> files
        ) {
            addFiles(mimeFolder, profileId, fullScan, M_LEGACY_FILE_NAMES, files, true);
        }

        @Override
        protected void addUsersLegacyFiles(
            FileObject mimeFolder,
            String profileId,
            boolean fullScan,
            Map<String, List<Object []>> files
        ) {
            addFiles(mimeFolder, profileId, fullScan, U_LEGACY_FILE_NAMES, files, false);
        }

        private void addFiles(
            FileObject mimeFolder,
            String profileId,
            boolean fullScan,
            String [] filePaths,
            Map<String, List<Object []>> files,
            boolean moduleFiles
        ) {
            if (profileId == null) {
                FileObject [] profileHomes = mimeFolder.getChildren();
                for(FileObject f : profileHomes) {
                    if (!f.isFolder()) {
                        continue;
                    }
                    
                    String id = f.getNameExt();
                    addFiles(f, filePaths, fullScan, files, id, f, moduleFiles); //NOI18N
                }
            } else {
                FileObject profileHome = mimeFolder.getFileObject(profileId);
                if (profileHome != null && profileHome.isFolder()) {
                    addFiles(profileHome, filePaths, fullScan, files, profileId, profileHome, moduleFiles);
                }
            }
        }
        
        private void addFiles(FileObject folder, String [] filePaths, boolean fullScan, Map<String, List<Object []>> files, String profileId, FileObject profileHome, boolean moduleFiles) {
            for(String filePath : filePaths) {
                FileObject f = folder.getFileObject(filePath);
                if (f != null) {
                    List<Object []> pair = files.get(profileId);
                    if (pair == null) {
                        pair = new ArrayList<Object[]>();
                        files.put(profileId, pair);
                    }
                    pair.add(new Object [] { profileHome, f, moduleFiles, null, true });

                    if (LOG.isLoggable(Level.INFO)) {
                        Utils.logOnce(LOG, Level.INFO, settingTypeId + " settings " + //NOI18N
                            "should reside in '" + settingTypeId + "' subfolder, " + //NOI18N
                            "see #90403 for details. Offending file '" + f.getPath() + "'", null); //NOI18N
                    }
                    
                    if (!fullScan) {
                        break;
                    }
                }
            }
        }
    } // End of FontsColorsLocator class

    private static final class LegacyTextBaseLocator extends SettingsType.DefaultLocator {
        
        public LegacyTextBaseLocator(String settingTypeId, boolean hasProfiles, String mimeType, String legacyFileName) {
            super(settingTypeId, hasProfiles, mimeType, legacyFileName);
        }
        
        @Override
        protected FileObject getLegacyMimeFolder(FileObject baseFolder, String mimeType) {
            if (mimeType == null || mimeType.length() == 0) {
                return baseFolder.getFileObject(Utils.TEXT_BASE_MIME_TYPE);
            } else {
                return super.getMimeFolder(baseFolder, mimeType);
            }
        }
    } // End of KeybindingsLocator class
}
