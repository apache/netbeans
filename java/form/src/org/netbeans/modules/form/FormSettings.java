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

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.UIManager;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Settings for one form.
 *
 * @author Jan Stola
 */
public class FormSettings {
    /** Prefix for session settings. */
    private static final String SESSION_PREFIX = "Session_"; // NOI18N
    private FormModel formModel;
    private Map<String,Object> settings = new TreeMap<String,Object>();

    FormSettings(FormModel formModel) {
        this.formModel = formModel;

        // Variables Modifier
        int variablesModifier = FormLoaderSettings.getInstance().getVariablesModifier();
        settings.put(FormLoaderSettings.PROP_VARIABLES_MODIFIER, Integer.valueOf(variablesModifier));
        
        // Local Variables
        boolean localVariables = FormLoaderSettings.getInstance().getVariablesLocal();
        settings.put(FormLoaderSettings.PROP_VARIABLES_LOCAL, Boolean.valueOf(localVariables));
        
        // Generate Mnemonics Code
        boolean generateMnemonicsCode = FormLoaderSettings.getInstance().getGenerateMnemonicsCode();
        settings.put(FormLoaderSettings.PROP_GENERATE_MNEMONICS, Boolean.valueOf(generateMnemonicsCode));
        
        // Listener Generation Style
        int listenerGenerationStyle = FormLoaderSettings.getInstance().getListenerGenerationStyle();
        settings.put(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE, Integer.valueOf(listenerGenerationStyle));

        // Generate FQN
        boolean generateFQN = FormLoaderSettings.getInstance().getGenerateFQN();
        settings.put(FormLoaderSettings.PROP_GENERATE_FQN, generateFQN);
    }

    // -----
    // code generation

    public int getVariablesModifier() {
        Integer variablesModifier = (Integer)settings.get(FormLoaderSettings.PROP_VARIABLES_MODIFIER);
        return variablesModifier.intValue();
    }
    
    public void setVariablesModifier(int value) {
        settings.put(FormLoaderSettings.PROP_VARIABLES_MODIFIER, Integer.valueOf(value));
    }
    
    public boolean getVariablesLocal() {
        Boolean variablesLocal = (Boolean)settings.get(FormLoaderSettings.PROP_VARIABLES_LOCAL);
        return variablesLocal.booleanValue();
    }
    
    public void setVariablesLocal(boolean value) {
        settings.put(FormLoaderSettings.PROP_VARIABLES_LOCAL, Boolean.valueOf(value));
    }
    
    public boolean getAutoSetComponentName() {
        Boolean setting = (Boolean) settings.get(FormLoaderSettings.PROP_AUTO_SET_COMPONENT_NAME);
        boolean autoName;
        if (setting != null) {
            autoName = setting.booleanValue();
        } else { // no setting - detect for newly created form, false otherwise
            autoName = FormEditor.getFormEditor(formModel).needPostCreationUpdate()
                    ? getDefaultAutoSetComponentName() : false;
            setAutoSetComponentName(autoName);
        }
        return autoName;
    }

    public void setAutoSetComponentName(boolean setName) {
        settings.put(FormLoaderSettings.PROP_AUTO_SET_COMPONENT_NAME, Boolean.valueOf(setName));
    }

    boolean getDefaultAutoSetComponentName() {
        int globalNaming = FormLoaderSettings.getInstance().getAutoSetComponentName();
        boolean autoName = globalNaming == FormLoaderSettings.AUTO_NAMING_ON;
        if (globalNaming == FormLoaderSettings.AUTO_NAMING_DEFAULT) {
            ResourceSupport resourceSupport = FormEditor.getResourceSupport(formModel);
            if (resourceSupport.projectUsesResources()) {
                autoName = true;
            }
        }
        return autoName;
    }

    public boolean getGenerateMnemonicsCode() {
        Boolean generateMnemonicsCode = (Boolean)settings.get(FormLoaderSettings.PROP_GENERATE_MNEMONICS);
        return generateMnemonicsCode.booleanValue();
    }
    
    public void setGenerateMnemonicsCode(boolean value) {
        settings.put(FormLoaderSettings.PROP_GENERATE_MNEMONICS, Boolean.valueOf(value));
    }
    
    public int getListenerGenerationStyle() {
        Integer listenerGenerationStyle = (Integer)settings.get(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE);
        return listenerGenerationStyle.intValue();
    }
    
    public void setListenerGenerationStyle(int value) {
        settings.put(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE, Integer.valueOf(value));
    }

    public int getLayoutCodeTarget() {
        return checkLayoutCodeTarget();
    }

    public void setLayoutCodeTarget(int value) {
        settings.put(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, Integer.valueOf(value));
    }

    private int checkLayoutCodeTarget() {
        Integer lctSetting = (Integer)settings.get(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET);
        int layoutCodeTarget;
        if (lctSetting != null) {
            layoutCodeTarget = lctSetting.intValue();
        }
        else { // no setting
            layoutCodeTarget = JavaCodeGenerator.LAYOUT_CODE_AUTO;
        }
        if (layoutCodeTarget == JavaCodeGenerator.LAYOUT_CODE_AUTO) {
            int globalLCT = FormLoaderSettings.getInstance().getLayoutCodeTarget();
            if (globalLCT == JavaCodeGenerator.LAYOUT_CODE_AUTO) {
                if (!Lookup.getDefault().lookup(FormServices.class).isLayoutExtensionsLibrarySupported()) {
                    layoutCodeTarget = JavaCodeGenerator.LAYOUT_CODE_JDK6;
                } else {
                    FileObject fo = FormEditor.getFormDataObject(formModel).getPrimaryFile();
                    if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { // workaround for old Aqua LF (bug #173912) // NOI18N
                        layoutCodeTarget = ClassPathUtils.isJava7ProjectPlatform(fo) ?
                                           JavaCodeGenerator.LAYOUT_CODE_JDK6 : JavaCodeGenerator.LAYOUT_CODE_LIBRARY;
                    } else {
                        layoutCodeTarget = ClassPathUtils.isJava6ProjectPlatform(fo) ?
                                           JavaCodeGenerator.LAYOUT_CODE_JDK6 : JavaCodeGenerator.LAYOUT_CODE_LIBRARY;
                    }
                }
            } else {
                layoutCodeTarget = globalLCT;
            }
            setLayoutCodeTarget(layoutCodeTarget);
        }
        else if (lctSetting == null) {
            setLayoutCodeTarget(layoutCodeTarget);
        }

        return layoutCodeTarget;
    }

    // -----
    // resource management / internationalization

    // for compatibility
    static final String PROP_AUTO_I18N = "i18nAutoMode"; // NOI18N

    void setResourceAutoMode(int value) {
        settings.put(ResourceSupport.PROP_AUTO_RESOURCING, value);
        settings.put(PROP_AUTO_I18N, value == ResourceSupport.AUTO_I18N); // for compatibility
    }

    int getResourceAutoMode() {
        Integer resSetting = (Integer) settings.get(ResourceSupport.PROP_AUTO_RESOURCING);
        int resAutoMode = ResourceSupport.AUTO_OFF;
        if (resSetting != null) {
            resAutoMode = resSetting.intValue();
        }
        else {
            Boolean i18nSetting = (Boolean) settings.get(PROP_AUTO_I18N);
            if (i18nSetting != null) {
                if (Boolean.TRUE.equals(i18nSetting))
                    resAutoMode = ResourceSupport.AUTO_I18N;
            }
            else { // no setting available
                FormEditor formEditor = FormEditor.getFormEditor(formModel);
                if (formEditor.needPostCreationUpdate()) {
                    int globalResAutoMode = FormLoaderSettings.getInstance().getI18nAutoMode();
                    if (globalResAutoMode == FormLoaderSettings.AUTO_RESOURCE_ON) {
                        ResourceSupport resourceSupport = FormEditor.getResourceSupport(formModel);
                        if (resourceSupport.projectUsesResources())
                            resAutoMode = ResourceSupport.AUTO_RESOURCING; // only if app framework already on cp
                        else
                            resAutoMode = ResourceSupport.AUTO_I18N;
                    }
                    else if (globalResAutoMode == FormLoaderSettings.AUTO_RESOURCE_DEFAULT) { // detect
                        ResourceSupport resourceSupport = FormEditor.getResourceSupport(formModel);
                        if (resourceSupport.projectWantsUseResources())
                            resAutoMode = ResourceSupport.AUTO_RESOURCING; // only if app framework already on cp
                        else if (resourceSupport.isDefaultInternationalizableProject()) {
                            resAutoMode = ResourceSupport.AUTO_I18N; // NBM project
                            if (formEditor.getEditorSupport().canGenerateNBMnemonicsCode()) {
                                setGenerateMnemonicsCode(true);
                            }
                        }
                    }
                }
                setResourceAutoMode(resAutoMode);
            }
        }
        return resAutoMode;
    }

    public boolean isI18nAutoMode() {
        return getResourceAutoMode() == ResourceSupport.AUTO_I18N;
    }

    public void setFormBundle(String bundleName) {
        settings.put(ResourceSupport.PROP_FORM_BUNDLE, bundleName);
    }

    public String getFormBundle() {
        return (String) settings.get(ResourceSupport.PROP_FORM_BUNDLE);
    }

    public void setGenerateFQN(boolean generateFQN) {
        settings.put(FormLoaderSettings.PROP_GENERATE_FQN, generateFQN);
    }

    public boolean getGenerateFQN() {
        return (Boolean)settings.get(FormLoaderSettings.PROP_GENERATE_FQN);
    }

    // design locale is not persisted in settings

    // -----


    public void set(String name, Object value) {
        set(name, value, false);
    }

    public void set(String name, Object value, boolean session) {
        if (session) {
            name = SESSION_PREFIX + name;
        }
        settings.put(name, value);
    }
    
    public Object get(String name) {
        Object value;
        if (settings.containsKey(name)) {
            value = settings.get(name);
        } else {
            value = settings.get(SESSION_PREFIX + name);
        }
        return value;
    }

    boolean isSessionSetting(String name) {
        return name.startsWith(SESSION_PREFIX);
    }
    
    Map<String,Object> allSettings() {
        return Collections.unmodifiableMap(settings);
    }

}
