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
package org.netbeans.modules.apisupport.project.ui.wizard.spi;

import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.JComponent;
import org.netbeans.modules.apisupport.project.ui.wizard.TypeChooserPanelImpl;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Class for communicating with UI panel for embedding into the First step 
 * of NetBeans module project wizard.
 * <p>
 * Panel is created using createComponent(WizardDescriptor) method. It contains UI to select
 * <ul>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 * All communication with created panel is performed through WizardDescriptor 
 * using static methods of ths class.
 * 
 * @author akorostelev
 * @since org.netbeans.modules.apisupport.project 1.23
 */
public class ModuleTypePanel{

    /** 
     * Key for module type property.
     * key for Boolean value. use 
     */
    private static final String IS_STANDALONE_OR_SUITE_COMPONENT 
                        = TypeChooserPanelImpl.IS_STANDALONE_OR_SUITE_COMPONENT;

    /** 
     * Suite root directory.
     * key for String value. 
     */
    private static final String SUITE_ROOT = TypeChooserPanelImpl.SUITE_ROOT;

    /** 
     * Active platform ID value to be used as value 
     * for nbplatform.active property in platform.properties file.
     * key for String value. 
     */
    private static final String ACTIVE_PLATFORM_ID 
                        = TypeChooserPanelImpl.ACTIVE_PLATFORM_ID;

    /** 
     * Active platform Object. 
     * key for org.netbeans.modules.apisupport.project.universe.NbPlatform value. 
     */
    private static final String ACTIVE_NB_PLATFORM  
                        = TypeChooserPanelImpl.ACTIVE_NB_PLATFORM;
    
    /** 
     * true if project is created in NetBeans sources.
     * key for Boolean value. 
     */
    private static final String IS_NETBEANS_ORG 
                        = TypeChooserPanelImpl.IS_NETBEANS_ORG;

    /** 
     * Is used to provide project's folder to TypeChooserPanel.
     * Is used by TypeChooserPanel implementation to decide whether specified forder 
     * is in NetBeans.org repository.
     * key for File value. 
     */
    private static final String PROJECT_FOLDER 
                        = TypeChooserPanelImpl.PROJECT_FOLDER;

    /**
     * private constructor to prevent direct instantiation.
     */
    private ModuleTypePanel() {
    }
    
    /**
     * creates Panel's UI Component.
     * @param wizard Wizard descriptor to be used for getting property changge updates from Component.
     * @return JComponent Panel object
     */
    public static JComponent createComponent(WizardDescriptor wizard) {
        return (JComponent) new TypeChooserPanelImpl(wizard);
    }

    /**
     * validates panel. Error messages should be added to provided settings 
     * by setting WizardDescriptor.PROP_ERROR_MESSAGE property value:
     * <pre>
     * settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); 
     * </pre>
     * @param settings 
     * @return true if panel is valid. false otherwise.
     */
    public static boolean validate(WizardDescriptor settings) {
        if (isSuiteComponent(settings) && getSuiteRoot(settings) == null) {
            setErrorMessage(settings, NbBundle.getMessage(
                    ModuleTypePanel.class, "MSG_ChooseRegularSuite"));// NOI18N
            return false;
        } else if (isStandalone(settings) &&
                (getActiveNbPlatform(settings) == null || !(getActiveNbPlatform(settings)).isValid())) {
            setErrorMessage(settings, NbBundle.getMessage(
                    ModuleTypePanel.class, "MSG_ChosenPlatformIsInvalid"));// NOI18N
            return false;
        }
        return true;
    }

    /**
     * Should be used by WizardDecriptor PropertyChangeListener
     * to test if receined PropertyChangeEvent is caused by changes in panel.
     * <p>
     * New values can be got using 
     * isSuiteComponent(WizardDescriptor), isStandalone(WizardDescriptor),
     * getSuiteRoot(WizardDescriptor), getActivePlatformId(WizardDescriptor)
     * methods.
     * @param evt PropertyChangeEvent
     * @return true is module type, module suite or active platform selection is changed.
     */
    public static boolean isPanelUpdated(PropertyChangeEvent evt){
        String name = evt.getPropertyName();
        if (IS_STANDALONE_OR_SUITE_COMPONENT.equals(name)
                || SUITE_ROOT.equals(name)
                || ACTIVE_PLATFORM_ID.equals(name))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Notifies panel that project folder is changed 
     * and panel UI should be updated.
     * @param settings Wizard descriptor 
     * @param value new Project folder value
     */
    public static void setProjectFolder(WizardDescriptor settings, File value) {
        settings.putProperty(PROJECT_FOLDER, value);
    }
        
    /**
     * Checks in Wizard Descriptor properties if "Add To Module Suite" was selected 
     * on Component created using createComponent(WizardDescriptor).
     * @param wizard Wizard descriptor 
     * @return true if "Add To Module Suite" radiobutton was selected. false if not. 
     */
    public static boolean isSuiteComponent(WizardDescriptor wizard){
        Object value = wizard.getProperty(IS_STANDALONE_OR_SUITE_COMPONENT);
        return !extractBoolean(value, true);
    }
    
    /**
     * Checks in Wizard Descriptor properties if "Standalone Module" was selected 
     * on Component created using createComponent(WizardDescriptor).
     * @param wizard Wizard descriptor 
     * @return true if "Standalone Module" radiobutton was selected. false if not. 
     */
    public static boolean isStandalone(WizardDescriptor wizard){
        Object value = wizard.getProperty(IS_STANDALONE_OR_SUITE_COMPONENT);
        return extractBoolean(value, false);
    }
    
    /**
     * Returns true if Module with project folder set by setProjectFolder(WizardDescriptor, File)
     * is NetBeans sourcees root
     * @param wizard Wizard descriptor 
     * @return true if specified Project folder is NetBeans sourcees root
     */
    public static boolean isNetBeansOrg(WizardDescriptor wizard){
        Object value = wizard.getProperty(IS_NETBEANS_ORG);
        return extractBoolean(value, false);
    }
    
    /**
     * Returns suite root directory stored in wizard descriptor by 
     * Component created using createComponent(WizardDescriptor).
     * @param wizard Wizard descriptor 
     * @return suite root directory
     */
    public static String getSuiteRoot(WizardDescriptor wizard){
        Object value = wizard.getProperty(SUITE_ROOT);
        return extractString(value, null);
    }
    
    /**
     * Returns platform id stored in wizard descriptor by 
     * Component created using createComponent(WizardDescriptor).
     * @param wizard Wizard descriptor 
     * @return platform id
     */
    public static String getActivePlatformId(WizardDescriptor wizard){
        Object value = wizard.getProperty(ACTIVE_PLATFORM_ID);
        return extractString(value, null);
    }
    
    private static NbPlatform getActiveNbPlatform(WizardDescriptor wizard){
        Object value = wizard.getProperty(ACTIVE_NB_PLATFORM);
        if (value instanceof NbPlatform){
            return (NbPlatform)value;
        }
        return null;
    }
    
    private static boolean extractBoolean(Object value, boolean defaultValue){
        if (value instanceof Boolean){
            return (Boolean)value;
        }
        return defaultValue;
    }
    
    private static String extractString(Object value, String defaultValue){
        if (value instanceof String){
            return (String)value;
        }
        return defaultValue;
    }
    
    private static void setErrorMessage(WizardDescriptor settings, String message) {
        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); 
    }

}
