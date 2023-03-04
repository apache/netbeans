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
package org.netbeans.modules.php.editor.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.codegen.ui.ConstructorPanel;
import org.netbeans.modules.php.editor.codegen.ui.MethodPanel;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public final class CGSGenerator implements CodeGenerator {

    static final String START_OF_GETTER = "get"; //NOI18N
    static final String START_OF_SETTER = "set"; //NOI18N
    static final String NEW_LINE = System.getProperty("line.separator"); //NOI18N
    static final String UNDERSCORED_METHOD_NAME = "${UNDERSCORED_METHOD_NAME}"; //NOI18N
    static final String ACCESS_MODIFIER = "${MODIFIER}"; //NOI18N
    static final String ACCESSOR = "${ACCESSOR}"; //NOI18N
    static final String PROPERTY = "${PROPERTY}"; //NOI18N
    static final String PARAM_NAME = "${PARAM_NAME}"; //NOI18N
    static final String UP_FIRST_LETTER_PROPERTY = "${UpFirstLetterProperty}"; //NOI18N
    static final String UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE = "${UpFirstLetterPropertyWithoutUnderscore}"; //NOI18N
    static final String ASSIGNMENT_TEMPLATE = NEW_LINE + ACCESSOR + PROPERTY + " = $" + PARAM_NAME + ";"; //NOI18N
    private static final String CURSOR = "${cursor}"; //NOI18N
    private static final String PROPERTY_WITHOUT_UNDERSCORE = "${PropertyWithoutUnderscore}"; //NOI18N
    private static final String PARAMS = "${PARAMS}"; //NOI18N
    private static final String ASSIGNMENTS = "${ASSIGNMENT}"; //NOI18N
    private static final String CONSTRUCTOR_TEMPLATE = ACCESS_MODIFIER + "function __construct(" + PARAMS + ") {" + ASSIGNMENTS  + CURSOR + NEW_LINE + "}" + NEW_LINE;    //NOI18N

    public enum GenType {
        CONSTRUCTOR(Panel.CONSTRUCTOR, Visibility.INVISIBLE, Visibility.VISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_CONSTRUCTOR"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    if (!way.equals(CGSGenerator.GenWay.WITH_UNDERSCORE)) {
                        result.addElement(new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getConstructorExample(propertyName), way)); //NOI18N
                    }
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_CONSTRUCTOR"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_CONSTRUCTOR"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo) {
                final StringBuilder params = new StringBuilder();
                final StringBuilder assignments = new StringBuilder();
                for (Property property : cgsInfo.getProperties()) {
                    final String name = property.getName();
                    final String paramName = cgsInfo.getHowToGenerate() == GenWay.WITHOUT_UNDERSCORE
                                ? CodegenUtils.withoutUnderscore(name) : name;
                    if (property.isSelected()) {
                        params.append(", "); //NOI18N
                        String type = property.getType();
                        if (type != null && !type.isEmpty()) {
                            params.append(property.getTypeForTemplate());
                        }
                        params.append("$").append(paramName); //NOI18N
                        assignments.append(ASSIGNMENT_TEMPLATE.replace(PROPERTY, name).replace(PARAM_NAME, paramName).replace(ACCESSOR, property.getAccessor()));
                    }
                }
                if (params.length() == 0) {
                    params.append(", "); //NOI18N
                }
                String accessModifier = cgsInfo.isPublicModifier() ? "public " : ""; //NOI18N
                return CONSTRUCTOR_TEMPLATE.replace(ACCESS_MODIFIER, accessModifier)
                        .replace(PARAMS, params.toString().substring(2))
                        .replace(ASSIGNMENTS, assignments);
            }

        },
        GETTER(Panel.CONSTRUCTOR, Visibility.INVISIBLE, Visibility.VISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_GETTERS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    result.addElement(new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getGetterExample(propertyName), way)); //NOI18N
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_GETTER"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_GETTERS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo) {
                return new SelectedPropertyMethodsCreator().create(cgsInfo.getPossibleGetters(), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo));
            }

        },
        SETTER(Panel.CONSTRUCTOR, Visibility.VISIBLE, Visibility.VISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_SETTERS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    result.addElement(new ComboBoxModelElement(way.getSimpleDescription() + ": " + way.getSetterExample(propertyName), way));
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_SETTER"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_SETTERS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo) {
                return new SelectedPropertyMethodsCreator().create(cgsInfo.getPossibleSetters(), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo));
            }

        },
        GETTER_AND_SETTER(Panel.CONSTRUCTOR, Visibility.VISIBLE, Visibility.VISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_GETTERS_AND_SETTERS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                final DefaultComboBoxModel result = new DefaultComboBoxModel();
                for (CGSGenerator.GenWay way : CGSGenerator.GenWay.values()) {
                    result.addElement(new ComboBoxModelElement(
                            way.getSimpleDescription()
                            + ": " + way.getGetterExample(propertyName)
                            + ", " + way.getSetterExample(propertyName), way)); //NOI18N
                }
                return result;
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_GETTER_AND_SETTER");  //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_GETTERS_AND_SETTERS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo) {
                final StringBuilder gettersAndSetters = new StringBuilder();
                gettersAndSetters.append(new SelectedPropertyMethodsCreator().create(cgsInfo.getPossibleGetters(), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
                gettersAndSetters.append(new SelectedPropertyMethodsCreator().create(cgsInfo.getPossibleSetters(), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
                return gettersAndSetters.toString();
            }

        },
        METHODS(Panel.METHOD, Visibility.INVISIBLE, Visibility.INVISIBLE) {

            @Override
            public String getPanelTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_PANEL_METHODS"); //NOI18N
            }

            @Override
            public ComboBoxModel getModel(final String propertyName) {
                return new DefaultComboBoxModel();
            }

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_METHOD"); //NOI18N
            }

            @Override
            public String getDialogTitle() {
                return NbBundle.getMessage(CGSGenerator.class, "LBL_TITLE_METHODS"); //NOI18N
            }

            @Override
            public String getTemplateText(final CGSInfo cgsInfo) {
                return new SelectedPropertyMethodsCreator().create(cgsInfo.getPossibleMethods(), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo));
            }

        };

        private final Panel panel;
        private final Visibility fluentSetter;
        private final Visibility publicModifier;

        public abstract String getPanelTitle();
        public abstract ComboBoxModel getModel(final String propertyName);
        public abstract String getDisplayName();
        public abstract String getDialogTitle();
        public abstract String getTemplateText(final CGSInfo cgsInfo);

        private GenType(final Panel panel, Visibility fluentSetter, Visibility publicModifier) {
            this.panel = panel;
            this.fluentSetter = fluentSetter;
            this.publicModifier = publicModifier;
        }

        public JPanel createPanel(final CGSInfo cgsInfo) {
            return panel.createPanel(this, cgsInfo);
        }

        public boolean isFluentSetterVisible() {
            return fluentSetter.isVisible();
        }

        public boolean isPublicModifierVisible() {
            return publicModifier.isVisible();
        }

        private enum Panel {
            CONSTRUCTOR {
                @Override
                JPanel createPanel(final GenType genType, final CGSInfo cgsInfo) {
                    return new ConstructorPanel(genType, cgsInfo);
                }
            },
            METHOD {
                @Override
                JPanel createPanel(final GenType genType, final CGSInfo cgsInfo) {
                    return new MethodPanel(cgsInfo);
                }
            };

            abstract JPanel createPanel(final GenType genType, final CGSInfo cgsInfo);
        }

        private enum Visibility {
            VISIBLE {
                @Override
                boolean isVisible() {
                    return true;
                }
            },
            INVISIBLE {
                @Override
                boolean isVisible() {
                    return false;
                }
            };

            abstract boolean isVisible();
        }

    }

    public enum GenWay {
        AS_JAVA(
                NbBundle.getMessage(CGSGenerator.class, "JAVA_STYLE"),
                "__construct($" + PROPERTY + ")",
                START_OF_GETTER + UP_FIRST_LETTER_PROPERTY,
                START_OF_SETTER + UP_FIRST_LETTER_PROPERTY), //NOI18N
        WITH_UNDERSCORE(
                NbBundle.getMessage(CGSGenerator.class, "ADD_UNDERSCORE"),
                "__construct($" + PROPERTY + ")",
                START_OF_GETTER + "_" + UNDERSCORED_METHOD_NAME,
                START_OF_SETTER + "_" + UNDERSCORED_METHOD_NAME), //NOI18N
        WITHOUT_UNDERSCORE(
                NbBundle.getMessage(CGSGenerator.class, "REMOVE_UNDERSCORE"),
                "__construct($" + PROPERTY_WITHOUT_UNDERSCORE + ")",
                START_OF_GETTER + UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE,
                START_OF_SETTER + UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE); //NOI18N

        private final String constructorTemplate;
        private final String getterTemplate;
        private final String setterTemplate;
        /**
         * The description will appear in the ui combobox.
         */
        private final String simpleDescription;

        private GenWay(String simpleDescription, String constructorTemplate, String getterTemplate, String setterTemplate) {
            this.constructorTemplate = constructorTemplate;
            this.getterTemplate = getterTemplate;
            this.setterTemplate = setterTemplate;
            this.simpleDescription = simpleDescription;
        }

        public String getConstructorTemplate() {
            return constructorTemplate;
        }

        public String getGetterTemplate() {
            return getterTemplate;
        }

        public String getSetterTemplate() {
            return setterTemplate;
        }

        public String getSimpleDescription() {
            return simpleDescription;
        }

        public String getConstructorExample(String property) {
            return createExample(getConstructorTemplate(), property);
        }

        public String getGetterExample(String property) {
             return createExample(getGetterTemplate(), property);
        }

        public String getSetterExample(String property) {
             return createExample(getSetterTemplate(), property);
        }

        private String createExample(String template, String property) {
            String example = template;
            if (template.contains(PROPERTY)) {
                example = example.replace(PROPERTY, property);
            }
            if (template.contains(UNDERSCORED_METHOD_NAME)) {
                example = example.replace(UNDERSCORED_METHOD_NAME, property);
            }
            if (template.contains(UP_FIRST_LETTER_PROPERTY)) {
                example = example.replace(UP_FIRST_LETTER_PROPERTY, CodegenUtils.upFirstLetter(property));
            }
            if (template.contains(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE)) {
                example = example.replace(UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, CodegenUtils.upFirstLetterWithoutUnderscore(property));
            }
            if (template.contains(PROPERTY_WITHOUT_UNDERSCORE)) {
                example = example.replace(PROPERTY_WITHOUT_UNDERSCORE, CodegenUtils.withoutUnderscore(property));
            }
            return example;
        }
    }

    //constructor
    private final GenType genType;
    private final CGSInfo cgsInfo;

    private static final String GETTER_SETTER_PROJECT_PROPERTY = "getter.setter.method.name.generation"; //NOI18N
    private static final String FLUENT_SETTER_PROJECT_PROPERTY = "fluent.setter.project.property"; //NOI18N
    private static final String PUBLIC_MODIFIER_PROJECT_PROPERTY = "public.modifier.project.property"; //NOI18N

    private CGSGenerator(CGSInfo cgsInfo, GenType type) {
        this.genType = type;
        this.cgsInfo = cgsInfo;
    }

    @Override
    public void invoke() {
        // obtain the generation from project properties
        JTextComponent component = cgsInfo.getComponent();
        FileObject fo = NbEditorUtilities.getFileObject(component.getDocument());
        Preferences preferences = null;
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            // Share settings because style of generated code is part of project coding standard.
            preferences = ProjectUtils.getPreferences(project, CGSGenerator.class, true);
            try {
                cgsInfo.setHowToGenerate(GenWay.valueOf(preferences.get(GETTER_SETTER_PROJECT_PROPERTY, GenWay.AS_JAVA.name())));
            } catch (IllegalArgumentException ex) {
                cgsInfo.setHowToGenerate(GenWay.AS_JAVA);
            }
            cgsInfo.setFluentSetter(preferences.getBoolean(FLUENT_SETTER_PROJECT_PROPERTY, false));
            cgsInfo.setPublicModifier(preferences.getBoolean(PUBLIC_MODIFIER_PROJECT_PROPERTY, true));
        }
        DialogDescriptor desc = new DialogDescriptor(genType.createPanel(cgsInfo), genType.getDialogTitle());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.setVisible(true);
        dialog.dispose();
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            CodeTemplateManager manager = CodeTemplateManager.get(component.getDocument());
            CodeTemplate template = manager.createTemporary(genType.getTemplateText(cgsInfo));
            template.insert(component);
            if (preferences != null) {
                //save the gen type value to the project properties
                preferences.put(GETTER_SETTER_PROJECT_PROPERTY, cgsInfo.getHowToGenerate().name());
                preferences.putBoolean(FLUENT_SETTER_PROJECT_PROPERTY, cgsInfo.isFluentSetter());
                preferences.putBoolean(PUBLIC_MODIFIER_PROJECT_PROPERTY, cgsInfo.isPublicModifier());
            }
        }
    }

    @Override
    public String getDisplayName() {
        return genType.getDisplayName();
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent textComp = context.lookup(JTextComponent.class);
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            CGSInfo info = CGSInfo.getCGSInfo(textComp);

            if (info.getClassName() != null) { // is the cursor in a class?
                if (!info.hasConstructor()) {
                    ret.add(new CGSGenerator(info, GenType.CONSTRUCTOR));
                }
                if (info.getPossibleGetters().size() > 0) {
                    ret.add(new CGSGenerator(info, GenType.GETTER));
                }
                if (info.getPossibleSetters().size() > 0) {
                    ret.add(new CGSGenerator(info, GenType.SETTER));
                }
                if (info.getPossibleGettersSetters().size() > 0) {
                    ret.add(new CGSGenerator(info, GenType.GETTER_AND_SETTER));
                }
                if (info.getPossibleMethods().size() > 0) {
                    ret.add(new CGSGenerator(info, GenType.METHODS));
                }
            }
            return ret;
        }
    }

}
