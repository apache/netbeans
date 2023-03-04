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

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 * Wizard for generating OptionsPanel
 *
 * @author Radek Matous
 * @author Max Sauer
 */
@TemplateRegistration(
    folder=UIUtil.TEMPLATE_FOLDER,
    position=400,
    displayName="#template_options",
    iconBase="org/netbeans/modules/apisupport/project/ui/wizard/options/newOptions.png",
    description="newOptions.html",
    category=UIUtil.TEMPLATE_CATEGORY
)
@Messages("template_options=Options Panel")
public final class NewOptionsIterator extends BasicWizardIterator {
    
    private NewOptionsIterator.DataModel data;
    
    private NewOptionsIterator() {  /* Use factory method. */ }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewOptionsIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new OptionsPanel0(wiz, data),
            new OptionsPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        // use code > 0 && < 1024 for error messages, >= 1024 for info messages
        // and < 0 for warnings. See is...Message() methods
        private static final int SUCCESS = 0;
        private static final int ERR_INVALID_CLASSNAME_PREFIX = 1;
        private static final int MSG_BLANK_SECONDARY_PANEL_TITLE = 1024;
        private static final int MSG_BLANK_PRIMARY_PANEL = 1026;
        private static final int MSG_BLANK_CATEGORY_NAME = 1028;
        private static final int MSG_BLANK_ICONPATH = 1029;
        private static final int MSG_BLANK_PACKAGE_NAME = 1030;
        private static final int MSG_BLANK_CLASSNAME_PREFIX = 1031;
        private static final int MSG_BLANK_KEYWORDS = 1032;
        
        
        private static final int WARNING_INCORRECT_ICON_SIZE = -1;
        
        private static final String[] CATEGORY_BUNDLE_KEYS = {
            "OptionsCategory_Name", // NOI18N
            "OptionsCategory_Keywords"
        };
        
        private static final String[] ADVANCED_BUNDLE_KEYS = {
            "AdvancedOption_DisplayName", // NOI18N
            "AdvancedOption_Keywords" // NOI18N
        };
        
        private static final String[] TOKENS = {
            "PACKAGE_NAME", // NOI18N
            "AdvancedOption_CLASS_NAME", // NOI18N
            "OptionsCategory_CLASS_NAME", // NOI18N
            "Panel_CLASS_NAME", // NOI18N
            "OptionsPanelController_CLASS_NAME", // NOI18N
            "OptionsPanelController_ANNOTATION", // NOI18N
            ADVANCED_BUNDLE_KEYS[0],
            ADVANCED_BUNDLE_KEYS[1],
            CATEGORY_BUNDLE_KEYS[0],
            CATEGORY_BUNDLE_KEYS[1]
        };
                
        private static final String ADVANCED_OPTION = "AdvancedOption"; //NOI18N
        private static final String OPTIONS_CATEGORY = "OptionsCategory"; //NOI18N
        private static final String PANEL = "Panel"; //NOI18N
        private static final String OPTIONS_PANEL_CONTROLLER = "OptionsPanelController"; //NOI18N
        
        private static final String JAVA_TEMPLATE_PREFIX = "template_myplugin"; // NOI18N
        private static final String FORM_TEMPLATE_PREFIX = "template_myplugin_form"; // NOI18N
        
        private CreatedModifiedFiles files;
        private String codeNameBase;
        private boolean advanced;
        
        //Advanced panel
        private String primaryPanel;
        private String secondaryPanelTitle;
        private String primaryKeywords;
        
        //OptionsCategory
        private String categoryName;
        private File icon;
        private String secondaryKeywords;
        private boolean allowAdvanced;
        
        private String classNamePrefix;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }

        int setDataForSecondaryPanel(final String primaryPanel, final String secondaryPanelTitle, final String secondaryKeywords) {
            this.advanced = true;
            this.primaryPanel = primaryPanel;
            this.secondaryPanelTitle = secondaryPanelTitle;
            this.secondaryKeywords = secondaryKeywords;
            return checkFirstPanel();
        }
        
        int setDataForPrimaryPanel(final String categoryName, File icon, final boolean allowAdvanced, final String primaryKeywords) {
            this.advanced = false;
            this.categoryName = categoryName;
            this.icon = icon;
            this.allowAdvanced = allowAdvanced;
            this.primaryKeywords = primaryKeywords;
            return checkFirstPanel();
        }
        
        public @Override String getPackageName() {
            String retValue;
            retValue = super.getPackageName();
            if (retValue == null) {
                retValue = getCodeNameBase();
                super.setPackageName(retValue);
            }
            return retValue;
        }
        
        public int setPackageAndPrefix(String packageName, String classNamePrefix) {
            setPackageName(packageName);
            this.classNamePrefix = classNamePrefix;
            int errCode = checkFinalPanel();
            if (isSuccessCode(errCode)) {
                generateCreatedModifiedFiles();
            }
            return errCode;
        }

        private Map<String, String> getTokenMap(boolean useAnnotations) {
            Map<String, String> retval = new HashMap<String, String>();
            for (int i = 0; i < TOKENS.length; i++) {
                retval.put(TOKENS[i], getReplacement(TOKENS[i], useAnnotations));
            }
            return retval;
        }
        
        private String getReplacement(String key, boolean useAnnotations) {
            if ("PACKAGE_NAME".equals(key)) {// NOI18N
                return getPackageName();
            } else if ("AdvancedOption_CLASS_NAME".equals(key)) {// NOI18N
                return getAdvancedOptionClassName();
            } else if ("OptionsCategory_CLASS_NAME".equals(key)) {// NOI18N
                return getOptionsCategoryClassName();
            } else if ("Panel_CLASS_NAME".equals(key)) {// NOI18N
                return getPanelClassName();
            } else if ("OptionsPanelController_CLASS_NAME".equals(key)) {// NOI18N
                return getOptionsPanelControllerClassName();
            } else if ("OptionsPanelController_ANNOTATION".equals(key)) { // NOI18N
                if (!useAnnotations) {
                    return "";
                } else if (isAdvanced()) {
                    return "@OptionsPanelController.SubRegistration(\n" +
                            // XXX could omit if primaryPanel=Advanced
                            (getPrimaryPanel().equals("Advanced")? "" : "location=\"" + getPrimaryPanel() + "\",\n") +
                            "    displayName=\"#AdvancedOption_DisplayName_" + getClassNamePrefix() + "\",\n" +
                            "    keywords=\"#AdvancedOption_Keywords_" + getClassNamePrefix() + "\",\n" +
                            "    keywordsCategory=\"" + getPrimaryPanel() + "/" + getClassNamePrefix() + "\"\n" +
                            ")\n" +
                            "@org.openide.util.NbBundle.Messages({\"AdvancedOption_DisplayName_" + getClassNamePrefix() + "=" + getSecondaryPanelTitle() + "\", " +
                            "\"AdvancedOption_Keywords_" + getClassNamePrefix() + "=" + getSecondaryKeywords() + "\"})\n";
                } else if (isAdvancedCategory()) {
                    return "<should never be used>";
                } else {
                    return "@OptionsPanelController.TopLevelRegistration(\n" +
                            "    categoryName=\"#OptionsCategory_Name_" + getClassNamePrefix() + "\",\n" +
                            "    iconBase=\"" + getIconPath() + "\",\n" +
                            "    keywords=\"#OptionsCategory_Keywords_" + getClassNamePrefix() + "\",\n" +
                            "    keywordsCategory=\"" + getClassNamePrefix() + "\"\n" +
                            ")\n" +
                            "@org.openide.util.NbBundle.Messages({\"OptionsCategory_Name_" + getClassNamePrefix() + "=" + getCategoryName() + "\", " +
                            "\"OptionsCategory_Keywords_" + getClassNamePrefix() + "=" + getPrimaryKeywords() + "\"})";
                }
            } else {
                return key + "_" + getClassNamePrefix();
            }
            
        }
        
        
        private String getBundleValue(String key) {
            if (key.startsWith("OptionsCategory_Name")) {// NOI18N
                return getCategoryName();
            } else if (key.startsWith("AdvancedOption_DisplayName")) {// NOI18N
                return getSecondaryPanelTitle();
            } else if (key.startsWith("OptionsCategory_Keywords")) {// NOI18N
                return getPrimaryKeywords();
            } else if (key.startsWith("AdvancedOption_Keywords")) {// NOI18N
                return getSecondaryKeywords();
            } else {
                throw new AssertionError(key);
            }
        }
        
        String getMessage(int code) {
            String field = null;
            switch(code) {
                case SUCCESS:
                    return "";
                case MSG_BLANK_SECONDARY_PANEL_TITLE:
                    field = "FIELD_SecondaryPanelTitle";//NOI18N
                    break;
                case MSG_BLANK_PRIMARY_PANEL:
                    field = "FIELD_PrimaryPanel"; //NOI18N
                    break;
                case MSG_BLANK_KEYWORDS:
                    field = "FIELD_Keywords"; // NOI18N
                    break;
                case MSG_BLANK_CATEGORY_NAME:
                    field = "FIELD_CategoryName";//NOI18N
                    break;
                case MSG_BLANK_ICONPATH:
                    field = "FIELD_IconPath";//NOI18N
                    break;
                case MSG_BLANK_PACKAGE_NAME:
                    field = "FIELD_PackageName";//NOI18N
                    break;
                case MSG_BLANK_CLASSNAME_PREFIX:
                    field = "FIELD_ClassNamePrefix";//NOI18N
                    break;
                case ERR_INVALID_CLASSNAME_PREFIX:
                    field = "FIELD_ClassNamePrefix";//NOI18N
                    break;
                case WARNING_INCORRECT_ICON_SIZE:
                    assert icon.exists();
                    return WizardUtils.getIconDimensionWarning(icon, 32, 32);
                default:
                    assert false : "Unknown code: " + code;
            }
            field = NbBundle.getMessage(NewOptionsIterator.class, field);
            if (isErrorCode(code)) {
                return NbBundle.getMessage(NewOptionsIterator.class, "ERR_FieldInvalid", field);    // NOI18N
            }
            if (isInfoCode(code)) {
                return NbBundle.getMessage(NewOptionsIterator.class, "MSG_FieldEmpty", field);    // NOI18N
            }
            return "";//NOI18N
        }
        
        static boolean isSuccessCode(int code) {
            return code == 0;
        }
        
        static boolean isErrorCode(int code) {
            return 0 < code && code < 1024;
        }
        
        static boolean isWarningCode(int code) {
            return code < 0;
        }
        
        
        static boolean isInfoCode(int code) {
            return code >= 1024;
        }

        private int checkFirstPanel() {
            if (advanced) {
                if (getPrimaryPanel().length() == 0) {
                    return MSG_BLANK_PRIMARY_PANEL;
                } else if (getSecondaryPanelTitle().length() == 0) {
                    return MSG_BLANK_SECONDARY_PANEL_TITLE;
                } else if (getSecondaryKeywords().length() == 0) {
                    return MSG_BLANK_KEYWORDS;
                }
            } else {
                if (getCategoryName().length() == 0) {
                    return MSG_BLANK_CATEGORY_NAME;
                } else if (getIconPath().length() == 0) {
                    return MSG_BLANK_ICONPATH;
                } else if (getPrimaryKeywords().length() == 0)  {
                    return MSG_BLANK_KEYWORDS;
                } else {
                    if (!icon.exists()) {
                        return MSG_BLANK_ICONPATH;
                    }
                }
                //warnings should go at latest
                assert icon.exists();
                if (!WizardUtils.isValidIcon(icon, 32, 32)) {
                    return WARNING_INCORRECT_ICON_SIZE;
                }
            }
            return 0;
        }
        
        private int checkFinalPanel() {
            if (getPackageName().length() == 0) {
                return MSG_BLANK_PACKAGE_NAME;
            } else if (getClassNamePrefix().length() == 0) {
                return MSG_BLANK_CLASSNAME_PREFIX;
            } else if (!Utilities.isJavaIdentifier(getClassNamePrefix())) {
                return ERR_INVALID_CLASSNAME_PREFIX;
        }
            
            return 0;
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            if (files == null) {
                files = generateCreatedModifiedFiles();
            }
            return files;
        }
        
        private CreatedModifiedFiles generateCreatedModifiedFiles() {
            assert isSuccessCode(checkFirstPanel()) || isWarningCode(checkFirstPanel());
            assert isSuccessCode(checkFinalPanel());
            files = new CreatedModifiedFiles(getProject());
            boolean useAnnotations = true;
            try {
                SpecificationVersion apiVersion = getModuleInfo().getDependencyVersion("org.netbeans.modules.options.api");
                useAnnotations = apiVersion == null || apiVersion.compareTo(new SpecificationVersion("1.14")) >= 0;
            } catch (IOException x) {
                Logger.getLogger(NewOptionsIterator.class.getName()).log(Level.INFO, null, x);
            }
            generateDependencies();
            if (useAnnotations && isAdvancedCategory()) {
                generatePackageInfo();
            } else {
                generateFiles(useAnnotations);
            }
            if (!useAnnotations) {
                generateBundleKeys();
                generateLayerEntry();
            }
            if (!isAdvanced()) {
                FileObject iconFO = FileUtil.toFileObject(icon);
                if (iconFO != null) {
                    addCreateIconOperation(files, iconFO);
                }
            }
            return files;
        }
    
        private void generateFiles(boolean useAnnotations) {
            if(isAdvanced()) {
                files.add(createJavaFileCopyOperation(OPTIONS_PANEL_CONTROLLER, useAnnotations));
                files.add(createJavaFileCopyOperation(PANEL, useAnnotations));
                files.add(createFormFileCopyOperation(PANEL));
            } else {
                if(!isAdvancedCategory()) {
                    files.add(createJavaFileCopyOperation(OPTIONS_PANEL_CONTROLLER, useAnnotations));
                    files.add(createJavaFileCopyOperation(PANEL, useAnnotations));
                    files.add(createFormFileCopyOperation(PANEL));
                }
            }
        }
        
        private void generateBundleKeys() {
            String[] bundleKeys = (isAdvanced()) ? ADVANCED_BUNDLE_KEYS : CATEGORY_BUNDLE_KEYS;
            for (int i = 0; i < bundleKeys.length; i++) {
                String key = getReplacement(bundleKeys[i], false);
                String value = getBundleValue(key);
                files.add(files.bundleKey(getDefaultPackagePath("Bundle.properties", true),key,value));// NOI18N                        
            }
        }
        
        private void generateDependencies() {
            files.add(files.addModuleDependency("org.openide.util")); // NOI18N
            files.add(files.addModuleDependency("org.openide.util.ui")); //NOI18N
            files.add(files.addModuleDependency("org.openide.util.lookup")); // NOI18N
            files.add(files.addModuleDependency("org.netbeans.modules.options.api","1",null,true));// NOI18N
            files.add(files.addModuleDependency("org.openide.awt")); // NOI18N
        }

        private void generateLayerEntry() {
            if(isAdvanced()) {
                String resourcePathPrefix = "OptionsDialog/"+getPrimaryPanel()+"/";  //NOI18N
                String instanceName = getAdvancedOptionClassName();
                String instanceFullPath = resourcePathPrefix + getPackageName().replace('.','-') + "-" + instanceName + ".instance";//NOI18N

                files.add(files.createLayerEntry(instanceFullPath, null, null, null, null));
                files.add(files.createLayerAttribute(instanceFullPath, "instanceCreate",
                        "methodvalue:org.netbeans.spi.options.AdvancedOption.createSubCategory")); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "controller",
                        "newvalue:" + getPackageName() + "." + getOptionsPanelControllerClassName())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "displayName",
                        "bundlevalue:" + getPackageName() + ".Bundle#AdvancedOption_DisplayName_" + getClassNamePrefix())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "keywords",
                        "bundlevalue:" + getPackageName() + ".Bundle#AdvancedOption_Keywords_" + getClassNamePrefix())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "keywordsCategory",
                        getPrimaryPanel() + "/" + getClassNamePrefix()));
            } else {
                String resourcePathPrefix = "OptionsDialog/"; //NOI18N
                String instanceName = isAdvancedCategory() ? getClassNamePrefix() : getOptionsCategoryClassName();
                String instanceFullPath = resourcePathPrefix + instanceName + ".instance"; //NOI18N
                Map<String, Object> attrsMap = new HashMap<String, Object>(7);
                attrsMap.put("iconBase", getIconPath()); // NOI18N
                attrsMap.put("keywordsCategory", getClassNamePrefix()); //NOI18N

                files.add(files.createLayerEntry(instanceFullPath, null, null, null, attrsMap));
                files.add(files.createLayerAttribute(instanceFullPath, "instanceCreate",
                        "methodvalue:org.netbeans.spi.options.OptionsCategory.createCategory")); //NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "categoryName",
                        "bundlevalue:" + getPackageName() + ".Bundle#OptionsCategory_Name_" + getClassNamePrefix())); //NOI18N
                if (isAdvancedCategory()) {
                    files.add(files.createLayerAttribute(instanceFullPath, "advancedOptionsFolder", resourcePathPrefix + instanceName)); //NOI18N
                    // fails to create the folder for you (but @ContainerRegistration does this)
                } else {
                    files.add(files.createLayerAttribute(instanceFullPath, "controller",
                            "newvalue:" + getPackageName() + "." + getOptionsPanelControllerClassName())); //NOI18N
                }
                files.add(files.createLayerAttribute(instanceFullPath, "keywords",
                        "bundlevalue:" + getPackageName() + ".Bundle#OptionsCategory_Keywords_" + getClassNamePrefix())); //NOI18N
            }
            // XXX positions would be useful as well (for all but advanced with primaryPanel=Advanced)
        }

        private void generatePackageInfo() {
            Map<String,Object> attrs = new LinkedHashMap<String,Object>();
            attrs.put("id", getClassNamePrefix());
            attrs.put("categoryName", "#OptionsCategory_Name_" + getClassNamePrefix());
            attrs.put("iconBase", getIconPath());
            attrs.put("keywords", "#OptionsCategory_Keywords_" + getClassNamePrefix());
            attrs.put("keywordsCategory", getClassNamePrefix());
            Map<String,Map<String,?>> annotations = new LinkedHashMap<String,Map<String,?>>();
            annotations.put("org.netbeans.spi.options.OptionsPanelController.ContainerRegistration", attrs);
            annotations.put("org.openide.util.NbBundle.Messages", Collections.singletonMap("value", new String[] {
                    "OptionsCategory_Name_" + getClassNamePrefix() + "=" + getCategoryName(),
                    "OptionsCategory_Keywords_" + getClassNamePrefix() + "=" + getPrimaryKeywords()}));
            files.add(files.packageInfo(getPackageName(), annotations));
        }

        private CreatedModifiedFiles.Operation createJavaFileCopyOperation(final String templateSuffix, boolean useAnnotations) {
            FileObject template = CreatedModifiedFiles.getTemplate(JAVA_TEMPLATE_PREFIX + templateSuffix + ".java");
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            return files.createFileWithSubstitutions(getFilePath(templateSuffix), template, getTokenMap(useAnnotations));
        }
        
        private String getFilePath(final String templateSuffix) {
            String fileName = getClassNamePrefix()+templateSuffix+ ".java"; // NOI18N
            return getDefaultPackagePath(fileName, false);//NOI18N
        }
        
        private CreatedModifiedFiles.Operation createFormFileCopyOperation(final String templateSuffix) {
            FileObject template = CreatedModifiedFiles.getTemplate(FORM_TEMPLATE_PREFIX + templateSuffix + ".form");
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            String fileName = getClassNamePrefix()+templateSuffix+ ".form";// NOI18N
            String filePath = getDefaultPackagePath(fileName, false);
            return files.createFile(filePath, template);
        }
        
        private String getCodeNameBase() {
            if (codeNameBase == null) {
                NbModuleProvider mod = getProject().getLookup().lookup(NbModuleProvider.class);
                codeNameBase = mod.getCodeNameBase();
            }
            return codeNameBase;
        }
        
        private String getPrimaryPanel() {
            return primaryPanel;
        }
        
        private String getSecondaryPanelTitle() {
            assert !isAdvanced() || secondaryPanelTitle != null;
            return secondaryPanelTitle;
        }
        
        private String getPrimaryKeywords() {
            assert isAdvanced() || primaryKeywords != null;
            return primaryKeywords;
        }

        private String getSecondaryKeywords() {
            assert !isAdvanced() || secondaryKeywords != null;
            return secondaryKeywords;
        }
        
        private String getCategoryName() {
            assert isAdvanced() || categoryName != null;
            return categoryName;
        }
        
        private String getIconPath() {
            assert isAdvanced() || icon != null;
            FileObject iconFO = FileUtil.toFileObject(icon);
            if (iconFO != null) {
                // XXX would be cleaner to use ret value from BasicDataModel.addCreateIconOperation for this:
                String iconRel = FileUtil.getRelativePath(Util.getResourceDirectory(getProject()), iconFO);
                if (iconRel != null) {
                    // Icon already in source tree.
                    return iconRel;
                }
            }
            // To be copied into destination package.
            return getPackageName().replace('.', '/') + '/' + icon.getName();
        }
        
        String getClassNamePrefix() {
            if (classNamePrefix == null) {
                classNamePrefix = isAdvanced() ? getSecondaryPanelTitle() : getCategoryName();
                classNamePrefix = classNamePrefix.trim().replace(" ", "");
                if (!Utilities.isJavaIdentifier(classNamePrefix)) {
                    classNamePrefix = "";
                }
            }
            return classNamePrefix;
        }
        
        private boolean isAdvanced() {
            return advanced;
        }
        
        private boolean isAdvancedCategory() {
            return allowAdvanced;
        }
        
        private String getAdvancedOptionClassName() {
            return getClassName(ADVANCED_OPTION);
        }
        
        private String getOptionsCategoryClassName() {
            return getClassName(OPTIONS_CATEGORY);
        }
        
        private String getPanelClassName() {
            return getClassName(PANEL);
        }
        
        private String getOptionsPanelControllerClassName() {
            return getClassName(OPTIONS_PANEL_CONTROLLER);
        }
        
        private String getClassName(String suffix) {
            return getClassNamePrefix() + suffix;
        }

    }
}
