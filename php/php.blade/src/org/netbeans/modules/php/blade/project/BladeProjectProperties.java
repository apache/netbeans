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
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.blade.editor.ui.customizer.UiOptionsUtils;
//import org.netbeans.modules.php.blade.editor.actions.ToggleBlockCommentAction;
import org.openide.util.NbPreferences;

/**
 * @todo ADD NEW OPTION VALUES
 * @todo use nodes for files
 *
 * @author Haidu Bogdan
 */
public final class BladeProjectProperties {

    private static final Map<Project, BladeProjectProperties> INSTANCES = new HashMap<>();
    private static final String BLADE_VERSION = "blade.version"; // NOI18N
    private static final String DIRECTIVE_CUSTOMIZER_PATH_LIST = "directive_customizer.path.list"; // NOI18N
    private static final String VIEW_PATH_LIST = "views.path.list"; // NOI18N
    private static final String NON_LARAVEL_DECL_FINDER = "non_laravel.decl.finder"; // NOI18N
    public Project project;

    DefaultListModel<String> directiveCustomizerPathList = new DefaultListModel();
    DefaultListModel<String> viewsPathList = new DefaultListModel();
    boolean nonLaravelDeclFinder = false;

    private BladeProjectProperties(Project project) {
        this.project = project;
        initModelsFromPreferences();
    }

    public static BladeProjectProperties getInstance(Project project) {
        if (INSTANCES.containsKey(project)) {
            return INSTANCES.get(project);
        }
        BladeProjectProperties instance = new BladeProjectProperties(project);
        INSTANCES.put(project, instance);
        return instance;
    }
    
    public static void closeProject(Project project){
        if (INSTANCES.containsKey(project)) {
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
        nonLaravelDeclFinder = getPreferences().getBoolean(NON_LARAVEL_DECL_FINDER, false);
    }

    public void storeDirectiveCustomizerPaths() {
        String includePath = UiOptionsUtils.encodeToStrings(directiveCustomizerPathList.elements());
        getPreferences().put(DIRECTIVE_CUSTOMIZER_PATH_LIST, includePath);
    }
    
    
    public void storeViewsPaths() {
        String includePath = UiOptionsUtils.encodeToStrings(viewsPathList.elements());
        getPreferences().put(VIEW_PATH_LIST, includePath);
    }
    
    public void storeNonLaravelDeclFinderFlag(boolean status) {
        nonLaravelDeclFinder = status;
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
        String includePath = UiOptionsUtils.encodeToStrings(list.elements());
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
        return nonLaravelDeclFinder;
    }
    
    private DefaultListModel<String> creatModelFromPreferences(String pathName) {
        DefaultListModel<String> model = new DefaultListModel<>();
        String encodedCompilerPathList = getPreferences().get(pathName, null);
        String[] paths;

        if (encodedCompilerPathList != null) {
            paths = encodedCompilerPathList.split("\\|", -1);
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
            return encodedCompilerPathList.split("\\|", -1);
        }
        return paths;
    }

    public String[] getViewsPathList() {
        String encodedCompilerPathList = getPreferences().get(VIEW_PATH_LIST, null);
        String[] paths = new String[]{};
        if (encodedCompilerPathList != null) {
            return encodedCompilerPathList.split("\\|", -1);
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
