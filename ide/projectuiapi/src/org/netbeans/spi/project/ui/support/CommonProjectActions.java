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

package org.netbeans.spi.project.ui.support;

import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.LogicalViewProvider;

/**
 * Factory for commonly needed generic project actions.
 * @author Jesse Glick, Petr Hrebejk
 */
public class CommonProjectActions {

    /**
     * {@link org.openide.filesystems.FileObject} value honored by {@link #newProjectAction}
     * that defines initial value for existing sources directory choosers.
     *
     * @since org.netbeans.modules.projectuiapi/1 1.3
     */
    public static final String EXISTING_SOURCES_FOLDER = "existingSourcesFolder";
    
    /**
     * {@link java.io.File} value honored by {@link #newProjectAction}
     * that defines initial value for parent folder
     *
     * @since 1.67
     */
    public static final String PROJECT_PARENT_FOLDER = "projdir";
    
    /**
     * {@link String}-valued action property honored by {@link #newProjectAction} 
     * that defines the project category (subfolder code name) to be selected.
     * 
     * @since 1.81
     */
    public static final String PRESELECT_CATEGORY = "PRESELECT_CATEGORY";

    /**
     * {@link String}[]-valued action property honored by {@link #newProjectAction} 
     * for propagating custom properties to the new project wizard's 
     * {@link org.openide.WizardDescriptor}
     * 
     * @since 1.81
     */
    public static final String INITIAL_VALUE_PROPERTIES = "initialValueProperties";    
    
    private CommonProjectActions() {}
        
    /**
     * Create an action "Set As Main Project".
     * It should be invoked with an action context containing
     * one {@link org.netbeans.api.project.Project}.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action setAsMainProjectAction() {
        return Utilities.getActionsFactory().setAsMainProjectAction();
    }
    
    /**
     * Create an action "Customize Project".
     * It should be invoked with an action context containing
     * one {@link org.netbeans.api.project.Project}.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action customizeProjectAction() {
        return Utilities.getActionsFactory().customizeProjectAction();
    }
    
    /**
     * Create an action "Open Subprojects".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     * @see org.netbeans.spi.project.SubprojectProvider
     * @see org.netbeans.spi.project.ProjectContainerProvider
     */
    public static Action openSubprojectsAction() {
        return Utilities.getActionsFactory().openSubprojectsAction();
    }
    
    /**
     * Create an action "Close Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action closeProjectAction() {
        return Utilities.getActionsFactory().closeProjectAction();
    }
    
    /**
     * Create an action project dependent "New File" action.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     * @see org.netbeans.spi.project.ui.PrivilegedTemplates
     * @see org.netbeans.spi.project.ui.RecommendedTemplates
     */
    public static Action newFileAction() {
        return Utilities.getActionsFactory().newFileAction();
    }
    
    /**
     * Create an action "Delete Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.8
     * @return an action
     */
    public static Action deleteProjectAction() {
        return Utilities.getActionsFactory().deleteProjectAction();
    }

    /**
     * Create an action "Copy Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.10
     * @return an action
     */
    public static Action copyProjectAction() {
        return Utilities.getActionsFactory().copyProjectAction();
    }
    
    /**
     * Create an action "Move Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.10
     * @return an action
     */
    public static Action moveProjectAction() {
        return Utilities.getActionsFactory().moveProjectAction();
    }
    
    /**
     * Create an action "Rename Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.10
     * @return an action
     */
    public static Action renameProjectAction() {
        return Utilities.getActionsFactory().renameProjectAction();
    }
    
    /**
     * Creates action that invokes <b>New Project</b> wizard.
     * 
     * <p>{@link #EXISTING_SOURCES_FOLDER} keyed action
     * value can carry {@link org.openide.filesystems.FileObject} that points
     * to existing sources folder. {@link Action#putValue Set this value}
     * if you open the wizard and you know user
     * expectations about initial value for wizard
     * choosers that refers to existing sources location.
     * 
     * <p>{@link #PRESELECT_CATEGORY} keyed action value can carry a {@link String}
     * that presents a category path to be selected in the new project wizard.
     * 
     * <p>{@link #INITIAL_VALUE_PROPERTIES} keyed action value can carry a 
     * {@link String} array of custom property names which are to be propagated 
     * to the new project wizard's {@link org.openide.WizardDescriptor} <br>
     * <pre>
     * Action a = newProjectAction();
     * a.putValue(INITIAL_VALUE_PROPERTIES, new String[] {key1, key2});
     * a.putValue(key1, value1);
     * a.putValue(key2, value2);
     * </pre>
     * @return an action
     *
     * @since org.netbeans.modules.projectuiapi/1 1.3
     */
    public static Action newProjectAction() {
        return Utilities.getActionsFactory().newProjectAction();
    }    

    /**
     * Creates action that invokes the <b>New Project</b> wizard, preselects the 
     * given category path and propagates a set of custom properties the wizard's 
     * {@link org.openide.WizardDescriptor}.
     * 
     * @param categoryPath the category path to be selected
     * @param initialProperties a map of custom properties which are propagated 
     * to the new project wizard's {@link org.openide.WizardDescriptor}
     *
     * @since 1.81
     * @return an action
     */
    public static Action newProjectAction(String categoryPath, Map<String, Object> initialProperties) {
        Action a = newProjectAction();
        a.putValue(PRESELECT_CATEGORY, categoryPath );
        String[] keys = initialProperties.keySet().toArray(new String[initialProperties.size()]);
        a.putValue(INITIAL_VALUE_PROPERTIES, keys);
        for (String key : keys) {
            a.putValue(key, initialProperties.get(key));
        }
        return a;
    }

    /**
     * Creates an action that sets the configuration of the selected project.
     * It should be displayed with an action context containing
     * exactly one {@link org.netbeans.api.project.Project}.
     * The action itself should not be invoked but you may use its popup presenter.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     * @since org.netbeans.modules.projectuiapi/1 1.17
     * @see org.netbeans.spi.project.ProjectConfigurationProvider
     */
    public static Action setProjectConfigurationAction() {
        return Utilities.getActionsFactory().setProjectConfigurationAction();
    }

    /**
     * Loads actions to be displayed in the context menu of {@link LogicalViewProvider#createLogicalView}.
     * The current implementation simply loads actions from {@code Projects/<projectType>/Actions}
     * but in the future it may merge in actions from another location as well.
     * <p>The folder is recommended to contain a link to {@code Projects/Actions} at some position
     * in order to pick up miscellaneous actions applicable to all project types.
     * @param projectType a type token, such as {@code org-netbeans-modules-java-j2seproject}
     * @return a list of actions
     * @since org.netbeans.modules.projectuiapi/1 1.43
     */
    public static Action[] forType(String projectType) {
        List<? extends Action> actions = org.openide.util.Utilities.actionsForPath("Projects/" + projectType + "/Actions");
        return actions.toArray(new Action[0]);
    }

}
