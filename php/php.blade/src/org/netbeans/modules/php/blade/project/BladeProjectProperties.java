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
package org.netbeans.modules.php.blade.project;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.blade.editor.ui.customizer.UIOptionsUtils;
import org.openide.util.NbPreferences;

/**
 * @todo ADD NEW OPTION VALUES
 * @todo use nodes for files
 *
 * @author Haidu Bogdan
 */
public final class BladeProjectProperties {

    private static final Map<Project, BladeProjectProperties> INSTANCES = new HashMap<>();
    private static final String DIRECTIVE_CUSTOMIZER_PATH_LIST = "directive_customizer.path.list"; // NOI18N
    private static final String VIEW_PATH_LIST = "views.path.list"; // NOI18N
    private static final String BLADE_COMPONENT_CLASS_FOLDER_LIST = "blade_component_class.folder.list"; // NOI18N
    private static final String NON_LARAVEL_DECL_FINDER = "non_laravel.decl.finder"; // NOI18N
    private final Project project;

    private DefaultListModel<String> directiveCustomizerPathList = new DefaultListModel();
    private DefaultListModel<String> viewsPathList = new DefaultListModel();
    private DefaultListModel<String> bladeComponentsClassFolderList = new DefaultListModel();
    // enable declaration finder outside of framework plugin
    private final AtomicBoolean nonLaravelDeclFinder = new AtomicBoolean(false);
    // the pipe "|" char needs to be escaped
    public static final String ESCAPED_VIEW_PATH_SEPARATOR = "\\|"; // NOI18N

    private BladeProjectProperties(Project project) {
        this.project = project;
        initModelsFromPreferences();
    }

    public static BladeProjectProperties getInstance(Project project) {
        synchronized (INSTANCES) {
            if (INSTANCES.containsKey(project)) {
                return INSTANCES.get(project);
            }
            BladeProjectProperties instance = new BladeProjectProperties(project);
            INSTANCES.put(project, instance);
            return instance;
        }
    }

    public static void closeProject(Project project) {
        synchronized (INSTANCES) {
            INSTANCES.remove(project);
        }
    }

    private Preferences getPreferences() {
        if (project != null) {
            return ProjectUtils.getPreferences(project, this.getClass(), false);
        }
        return NbPreferences.forModule(this.getClass());
    }

    private void initModelsFromPreferences() {
        directiveCustomizerPathList = createModelForDirectiveCusomizerPathList();
        viewsPathList = createModelForViewsPathList();
        nonLaravelDeclFinder.set(getPreferences().getBoolean(NON_LARAVEL_DECL_FINDER, false));
        this.bladeComponentsClassFolderList = createModelForBladeComponentFolderList();
    }

    public void storeDirectiveCustomizerPaths() {
        String includePath = UIOptionsUtils.encodeToStrings(directiveCustomizerPathList.elements());
        getPreferences().put(DIRECTIVE_CUSTOMIZER_PATH_LIST, includePath);
    }

    public void storeViewsPaths() {
        String includePath = UIOptionsUtils.encodeToStrings(viewsPathList.elements());
        getPreferences().put(VIEW_PATH_LIST, includePath);
    }

    public void storeNonLaravelDeclFinderFlag(boolean status) {
        nonLaravelDeclFinder.set(status);
        getPreferences().putBoolean(NON_LARAVEL_DECL_FINDER, status);
    }

    public void addDirectiveCustomizerPath(String path) {
        directiveCustomizerPathList.addElement(path);
    }

    public void addViewsPath(String path) {
        viewsPathList.addElement(path);
    }

    public void removeCustomizerPath(int index) {
        directiveCustomizerPathList.remove(index);
    }

    public void removeViewsPath(int index) {
        viewsPathList.remove(index);
    }

    public void setViewsPathList(DefaultListModel<String> list) {
        String includePath = UIOptionsUtils.encodeToStrings(list.elements());
        getPreferences().put(VIEW_PATH_LIST, includePath);
    }

    public DefaultListModel<String> createModelForDirectiveCusomizerPathList() {
        return creatModelFromPreferences(DIRECTIVE_CUSTOMIZER_PATH_LIST);
    }

    public DefaultListModel<String> createModelForViewsPathList() {
        return creatModelFromPreferences(VIEW_PATH_LIST);
    }

    public DefaultListModel<String> getModelForDirectiveCusomizerPathList() {
        return directiveCustomizerPathList;
    }

    public DefaultListModel<String> getModelViewsPathList() {
        return viewsPathList;
    }

    public boolean getNonLaravelDeclFinderFlag() {
        return nonLaravelDeclFinder.get();
    }

    //blade components
    public void addCustomBladeComponentClassFolder(String path) {
        bladeComponentsClassFolderList.addElement(path);
    }
    
    public void removeCustomBladeComponentClassFolder(int index) {
        bladeComponentsClassFolderList.remove(index);
    }
    
    public DefaultListModel<String> createModelForBladeComponentFolderList() {
        return creatModelFromPreferences(BLADE_COMPONENT_CLASS_FOLDER_LIST);
    }
    
    public DefaultListModel<String> getModelForBladeComponentsClassFolderList() {
        return bladeComponentsClassFolderList;
    }
    
    public void storeBladeComponentsFolder() {
        String includePath = UIOptionsUtils.encodeToStrings(bladeComponentsClassFolderList.elements());
        getPreferences().put(BLADE_COMPONENT_CLASS_FOLDER_LIST, includePath);
    }
    
    private DefaultListModel<String> creatModelFromPreferences(String pathName) {
        DefaultListModel<String> model = new DefaultListModel<>();
        String encodedCompilerPathList = getPreferences().get(pathName, null);
        String[] paths;

        if (encodedCompilerPathList != null) {
            paths = encodedCompilerPathList.split(ESCAPED_VIEW_PATH_SEPARATOR, -1);
        } else {
            return model;
        }
        if (paths.length == 0) {
            return model;
        }

        for (String path : paths) {
            model.addElement(path);
        }

        return model;
    }

    public String[] getCompilerPathList() {
        String encodedCompilerPathList = getPreferences().get(DIRECTIVE_CUSTOMIZER_PATH_LIST, null);
        String[] paths = new String[]{};
        if (encodedCompilerPathList != null) {
            return encodedCompilerPathList.split(ESCAPED_VIEW_PATH_SEPARATOR, -1);
        }
        return paths;
    }

    public String[] getViewsFolderPathList() {
        String encodedViewsFolderPathList = getPreferences().get(VIEW_PATH_LIST, null);
        String[] paths = new String[]{};
        if (encodedViewsFolderPathList != null) {
            return encodedViewsFolderPathList.split(ESCAPED_VIEW_PATH_SEPARATOR, -1);
        }
        return paths;
    }
    
    public String[] getBladeComponentsClassPathList() {
        String encodedBladeComponentFolderPathList = getPreferences().get(BLADE_COMPONENT_CLASS_FOLDER_LIST, null);
        String[] paths = new String[]{};
        if (encodedBladeComponentFolderPathList != null) {
            return encodedBladeComponentFolderPathList.split(ESCAPED_VIEW_PATH_SEPARATOR, -1);
        }
        return paths;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        getPreferences().addPreferenceChangeListener(preferenceChangeListener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        getPreferences().removePreferenceChangeListener(preferenceChangeListener);
    }

}
