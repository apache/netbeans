/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.editor.hints.projects.support;

import java.io.File;
import java.net.URI;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.editor.hints.projects.ProjectSettings;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**Utility class to be used by projects without special needs to provide support for
 * per-project hints.
 * 
 * <p>Use {@link #createSettings } to augment the
 * project's {@link Lookup} with the per-project hints settings. Use {@link #createCustomizerProvider }
 * to augment the project's customizer with the standard version of per-project hints customizer.
 * Note that both of these need to be registered to get the proper per-project hints behavior.
 * 
 * @author lahvac
 */
public class StandardProjectSettings {
    
    public static final String ATTR_ENABLE_KEY = "enableKey";
    public static final String ATTR_HINT_FILE_KEY = "hintFileKey";
    public static final String ATTR_DEFAULT_HINT_FILE = "defaultHintFileKey";
    public static final String ATTR_CUSTOMIZERS_FOLDER = "customizersFolder";
    
    private static final String KEY_USE_PROJECT = "perProjectHintSettingsEnabled";
    private static final boolean DEF_USE_PROJECT = false;
    private static final String KEY_PROJECT_SETTINGS = "perProjectHintSettingsFile";
    private static final String DEF_PROJECT_SETTINGS = "nbproject/cfg_hints.xml";
        
    private static String getAttributeWithDefault(FileObject file, String key, String def) {
        Object value = file.getAttribute(key);
        
        if (value instanceof String) return (String) value;
        
        return def;
    }
    
    /**Augment the given project's {@link Lookup} with the per-project hints settings.
     * Similar to invoking {@link #createSettings(java.lang.String, java.lang.String, java.lang.String) },
     * but the parameters are read from the file attributes. Intended to be put directly
     * into the layer.xml.
     * 
     * @param file whose attributes should be used to alter the default settings
     * @return a {@link LookupProvider} that augments the given project's {@link Lookup} with per-project settings
     */
    public static @NonNull LookupProvider createSettings(FileObject file) {
        final String keyUseProject = getAttributeWithDefault(file, ATTR_ENABLE_KEY, KEY_USE_PROJECT);
        final String keyHintSettingsFile = getAttributeWithDefault(file, ATTR_HINT_FILE_KEY, KEY_PROJECT_SETTINGS);
        final String defaultHintLocation = getAttributeWithDefault(file, ATTR_DEFAULT_HINT_FILE, DEF_PROJECT_SETTINGS);
        
        return createSettings(keyUseProject, keyHintSettingsFile, defaultHintLocation);
    }
    
    /**Augment the given project's {@link Lookup} with the per-project hints settings.
     * 
     * @param keyUseProject the {@link ProjectUtils#getPreferences(org.netbeans.api.project.Project, java.lang.Class, boolean) } key
     *                      which will be set to true when per-project hints are enabled. If null, defaults to <code>perProjectHintSettingsEnabled</code>.
     * @param keyHintSettingsFile the {@link ProjectUtils#getPreferences(org.netbeans.api.project.Project, java.lang.Class, boolean) } key
     *                            which will be set to the location of the hint settings file. If null, defaults to <code>perProjectHintSettingsFile</code>.
     * @param defaultHintLocation the default location of the hint settings file. If null, defaults to <code>nbproject/cfg_hints.xml</code>.
     *                            It is recommended to use files that start with <code>cfg_</code>.
     * @return a {@link LookupProvider} that augments the given project's {@link Lookup} with per-project settings
     */
    public static @NonNull LookupProvider createSettings(@NullAllowed final String keyUseProject, @NullAllowed final String keyHintSettingsFile, @NullAllowed final String defaultHintLocation) {
        return new LookupProvider() {
            @Override public Lookup createAdditionalLookup(Lookup baseContext) {
                Project project = baseContext.lookup(Project.class);
                assert project != null;
                return Lookups.fixed(new Standard(project, keyUseProject != null ? keyUseProject : KEY_USE_PROJECT, keyHintSettingsFile != null ? keyHintSettingsFile : KEY_PROJECT_SETTINGS,  defaultHintLocation != null ? defaultHintLocation : DEF_PROJECT_SETTINGS));
            }
        };
    }
    
    /**Augment the given project's {@link Lookup} with the per-project hints settings.
     * 
     * @return a {@link LookupProvider} that augments the given project's {@link Lookup} with per-project settings
     * @since 1.4
     */
    public static @NonNull LookupProvider createPreferencesBasedSettings() {
        return new LookupProvider() {
            @Override public Lookup createAdditionalLookup(Lookup baseContext) {
                Project project = baseContext.lookup(Project.class);
                assert project != null;
                return Lookups.fixed(new Standard(project, KEY_USE_PROJECT, null, null));
            }
        };
    }
    
    /**Augments the project's customizer with the standard version of per-project hints customizer.
     * Similar to {@link #createCustomizerProvider(java.lang.String) }, but the <code>customizersFolder</code>
     * is read get from <code>customizersFolder</code> attribute of the given file. Intended to be put directly
     * into the layer.xml.
     * 
     * <p>Use <b>customizersFolder</b> attributed of the given file to specify the 
     *
     * @param file whose attributes should be used.
     * @return a {@link CompositeCategoryProvider} that augments the project's customizer
     */
    public static @NonNull CompositeCategoryProvider createCustomizerProvider(FileObject file) {
        final String customizersFolder = getAttributeWithDefault(file, ATTR_CUSTOMIZERS_FOLDER, null);
        
        if (customizersFolder == null) {
            throw new IllegalStateException("Must specify " + ATTR_CUSTOMIZERS_FOLDER);
        }
        
        return createCustomizerProvider(customizersFolder);
    }
    
    /**Augments the project's customizer with the standard version of per-project hints customizer.
     * 
     * @param customizersFolder location on the system filesystem from which the
     *                          customizers should be read.
     * @return a {@link CompositeCategoryProvider} that augments the project's customizer
     */
    public static @NonNull CompositeCategoryProvider createCustomizerProvider(@NonNull String customizersFolder) {
        Parameters.notNull("customizersFolder", customizersFolder);
        
        return new ProjectCustomizer(customizersFolder);
    }

    static final class Standard implements ProjectSettings {

        private final Project project;
        
        private final @NonNull String keyUseProject;
        private final @NullAllowed String keyHintSettingsFile;
        private final @NullAllowed String defaultHintLocation;

        public Standard(Project project, @NonNull String keyUseProject, @NullAllowed String keyHintSettingsFile, @NullAllowed String defaultHintLocation) {
            this.keyUseProject = keyUseProject;
            this.keyHintSettingsFile = keyHintSettingsFile;
            this.defaultHintLocation = defaultHintLocation;
            this.project = project;
        }

        @Override
        public boolean getUseProjectSettings() {
            return ProjectUtils.getPreferences(project, ProjectSettings.class, true).getBoolean(keyUseProject, DEF_USE_PROJECT);
        }

        public void setUseProjectSettings(boolean value) {
            if (value) {
                ProjectUtils.getPreferences(project, ProjectSettings.class, true).putBoolean(keyUseProject, true);
            } else {
                ProjectUtils.getPreferences(project, ProjectSettings.class, true).remove(keyUseProject);
            }
        }

        public String getSettingsFileLocation() {
            assert hasLocation();
            String result = ProjectUtils.getPreferences(project, ProjectSettings.class, true).get(keyHintSettingsFile, null);
            
            return result != null ? result : defaultHintLocation;
        }

        public void setSettingsFileLocation(String settings) {
            assert hasLocation();
            ProjectUtils.getPreferences(project, ProjectSettings.class, true).put(keyHintSettingsFile, settings);
        }

        private String encodeSettingsFileLocation(String fileLocation) {
            return fileLocation.replace(" ", "%20"); //NOI18N
        }
        
        @Override
        public Preferences getProjectSettings(String mimeType) {
            if (hasLocation()) {
                URI settingsLocation = project.getProjectDirectory().toURI().resolve(encodeSettingsFileLocation(getSettingsFileLocation()));
                return ToolPreferences.from(settingsLocation).getPreferences(HINTS_TOOL_ID, mimeType);
            } else {
                return ProjectUtils.getPreferences(project, ProjectSettings.class, true).node(mimeType);
            }
        }
        
        public ToolPreferences preferencesFrom(String source) {
            assert hasLocation();
            URI settingsLocation = project.getProjectDirectory().toURI().resolve(encodeSettingsFileLocation(getSettingsFileLocation()));
            return ToolPreferences.from(settingsLocation);
        }

        public String getDefaultHintLocation() {
            return defaultHintLocation;
        }
        
        public File getProjectLocation() {
            return FileUtil.toFile(project.getProjectDirectory());
        }
        
        public boolean hasLocation() {
            return keyHintSettingsFile != null;
        }
        
    }
}
