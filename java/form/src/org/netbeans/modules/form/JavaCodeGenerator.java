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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.text.BadLocationException;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.IndentEngine;

import org.netbeans.api.java.classpath.ClassPath;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

import org.netbeans.modules.form.editors.ModifierEditor;
import org.netbeans.modules.form.editors.CustomCodeEditor;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutCodeGenerator;
import org.netbeans.modules.form.project.ClassPathUtils;

import java.beans.*;
import java.io.*;
import java.lang.reflect.*; 
import java.util.*;
import javax.swing.text.Document;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.form.project.ClassSource.Entry;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * JavaCodeGenerator is the default code generator which produces a Java source
 * for the form.
 *
 * @author Ian Formanek, Jan Stola
 */

class JavaCodeGenerator extends CodeGenerator {

    static final String PROP_VARIABLE_MODIFIER = "modifiers"; // NOI18N
    static final String PROP_TYPE_PARAMETERS = "typeParameters"; // NOI18N
    static final String PROP_VARIABLE_LOCAL = "useLocalVariable"; // NOI18N
    static final String PROP_SERIALIZE_TO = "serializeTo"; // NOI18N
    static final String PROP_CODE_GENERATION = "codeGeneration"; // NOI18N
    static final String PROP_CREATE_CODE_PRE = "creationCodePre"; // NOI18N
    static final String PROP_CREATE_CODE_POST = "creationCodePost"; // NOI18N
    static final String PROP_CREATE_CODE_CUSTOM = "creationCodeCustom"; // NOI18N
    static final String PROP_INIT_CODE_PRE = "initCodePre"; // NOI18N
    static final String PROP_INIT_CODE_POST = "initCodePost"; // NOI18N
    static final String PROP_LISTENERS_POST = "listenersCodePost"; // NOI18N
    static final String PROP_ADDING_PRE = "addingCodePre"; // NOI18N
    static final String PROP_ADDING_POST = "addingCodePost"; // NOI18N
    static final String PROP_LAYOUT_PRE = "layoutCodePre"; // NOI18N
    static final String PROP_LAYOUT_POST = "layoutCodePost"; // NOI18N
    static final String PROP_ALL_SET_POST = "allCodePost"; // NOI18N
    static final String PROP_DECLARATION_PRE = "declarationPre"; // NOI18N
    static final String PROP_DECLARATION_POST = "declarationPost"; // NOI18N
    static final String PROP_GENERATE_MNEMONICS = "generateMnemonicsCode"; // Mnemonics support // NOI18N
    static final String PROP_GENERATE_LAMBDA_LISTENERS = "generateLambdaListeners"; // Mnemonics support // NOI18N
    static final String PROP_LISTENER_GENERATION_STYLE = "listenerGenerationStyle"; // NOI18N

    static final String AUX_VARIABLE_MODIFIER =
        "JavaCodeGenerator_VariableModifier"; // NOI18N
    static final String AUX_TYPE_PARAMETERS =
        "JavaCodeGenerator_TypeParameters"; // NOI18N
    static final String AUX_VARIABLE_LOCAL =
        "JavaCodeGenerator_VariableLocal"; // NOI18N
    static final String AUX_SERIALIZE_TO =
        "JavaCodeGenerator_SerializeTo"; // NOI18N
    static final String AUX_CODE_GENERATION =
        "JavaCodeGenerator_CodeGeneration"; // NOI18N
    static final String AUX_CREATE_CODE_PRE =
        "JavaCodeGenerator_CreateCodePre"; // NOI18N
    static final String AUX_CREATE_CODE_POST =
        "JavaCodeGenerator_CreateCodePost"; // NOI18N
    static final String AUX_CREATE_CODE_CUSTOM =
        "JavaCodeGenerator_CreateCodeCustom"; // NOI18N
    static final String AUX_INIT_CODE_PRE =
        "JavaCodeGenerator_InitCodePre"; // NOI18N
    static final String AUX_INIT_CODE_POST =
        "JavaCodeGenerator_InitCodePost"; // NOI18N
    static final String AUX_LISTENERS_POST =
        "JavaCodeGenerator_ListenersCodePost"; // NOI18N
    static final String AUX_ADDING_PRE =
        "JavaCodeGenerator_AddingCodePre"; // NOI18N
    static final String AUX_ADDING_POST =
        "JavaCodeGenerator_AddingCodePost"; // NOI18N
    static final String AUX_LAYOUT_PRE =
        "JavaCodeGenerator_LayoutCodePre"; // NOI18N
    static final String AUX_LAYOUT_POST =
        "JavaCodeGenerator_LayoutCodePost"; // NOI18N
    static final String AUX_ALL_SET_POST =
        "JavaCodeGenerator_allCodePost"; // NOI18N
    static final String AUX_DECLARATION_PRE =
        "JavaCodeGenerator_DeclarationPre"; // NOI18N
    static final String AUX_DECLARATION_POST =
        "JavaCodeGenerator_DeclarationPost"; // NOI18N

    static final Integer VALUE_GENERATE_CODE = Integer.valueOf(0);
    static final Integer VALUE_SERIALIZE = Integer.valueOf(1);

    // types of code generation of event listeners
    static final int ANONYMOUS_INNERCLASSES = 0;
    static final int CEDL_INNERCLASS = 1;
    static final int CEDL_MAINCLASS = 2;
    static final int LAMBDAS = 3;

    // types of code generation of layout code
    static final int LAYOUT_CODE_AUTO = 0;
    static final int LAYOUT_CODE_JDK6 = 1;
    static final int LAYOUT_CODE_LIBRARY = 2;

    private static final String EVT_SECTION_PREFIX = "event_"; // NOI18N
    private static final String EVT_VARIABLE_NAME = "evt"; // NOI18N
    private static final String DEFAULT_LISTENER_CLASS_NAME = "FormListener"; // NOI18N

    static final String CUSTOM_CODE_MARK = "\u001F"; // NOI18N
    private static final String CODE_MARK = "*/\n\\"; // NOI18N
    private static final String CODE_MARK_END = "*/\n\\0"; // NOI18N
    private static final String CODE_MARK_LINE_COMMENT = "*/\n\\1"; // NOI18N
    private static final String CODE_MARK_VARIABLE_SUBST = "*/\n\\2"; // NOI18N

    private Map<String,String> repeatedCodeVariables;

    private String bindingGroupVariable;
    private Map<String,String> bindingVariables;
    private static String variablesHeader;
    private static String variablesFooter;
    private static String eventDispatchCodeComment;

    private FormModel formModel;
    private EditorSupport formEditorSupport;
    private FormEditor formEditor;

    private boolean initialized = false;
    private boolean canGenerate = true;
    private boolean codeUpToDate = true;

    private boolean mnemonicsClassPathUpdated;

    private String listenerClassName;
    private String listenerVariableName;

    // data needed when listener generation style is CEDL_MAINCLASS
    private Class[] listenersInMainClass;
    private Class[] listenersInMainClass_lastSet;

    private int emptyLineCounter;
    private int emptyLineRequest;

    private Map<RADComponent, List<FormProperty>> constructorProperties;
    private Map<RADComponent, List<FormProperty>> parentDependentProperties;
    private Map<RADComponent, List<FormProperty>> childrenDependentProperties;

    private SwingLayoutCodeGenerator swingGenerator;

    private static int indentSize = 4;
    private static boolean braceOnNewLine = false;

    private static class PropertiesFilter implements FormProperty.Filter {
		
	private final List<FormProperty> properties;
	
	public PropertiesFilter(List<FormProperty> properties) {
	    this.properties = properties;
	}
	
        @Override
        public boolean accept(FormProperty property) {	    		     
	    return (property.isChanged()
                       && !ResourceSupport.isInjectedProperty(property)
                       && (properties == null
                           || !properties.contains(property)))
                    || property.getPreCode() != null
                    || property.getPostCode() != null;
        }
    };

    /** Creates new JavaCodeGenerator */

//    public JavaCodeGenerator() {
//    }

    @Override
    public void initialize(FormModel formModel) {
        if (!initialized) {
            this.formModel = formModel;
            FormDataObject formDO = FormEditor.getFormDataObject(formModel);
            formEditorSupport = formDO.getFormEditorSupport();
            formEditor = FormEditor.getFormEditor(formModel);

            if (formDO.getPrimaryFile().canWrite()) {
                canGenerate = true;
                formModel.addFormModelListener(new FormListener());
            }
            else canGenerate = false;

            if (formEditor.getGuardedSectionManager() == null) {
                System.err.println("ERROR: Cannot initialize guarded sections... code generation is disabled."); // NOI18N
                canGenerate = false;
                return;
            }
            SimpleSection initComponentsSection = formEditor.getInitComponentSection();
            SimpleSection variablesSection = formEditor.getVariablesSection();

            if (initComponentsSection == null || variablesSection == null) {
                System.err.println("ERROR: Cannot find guarded sections... code generation is disabled."); // NOI18N

                formModel.setReadOnly(true);
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        FormUtils.getBundleString("MSG_ERR_GuardesBlocks"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                d.setTitle(FormUtils.getBundleString("MSG_ERR_GuardesBlocksTitle")); // NOI18N
                DialogDisplayer.getDefault().notifyLater(d);

                canGenerate = false;
            }

            initialized = true;
        }
    }

    /**
     * Allows the code generator to provide synthetic properties for specified
     * component which are specific to the code generation method.  E.g. a
     * JavaCodeGenerator will return variableName property, as it generates
     * global Java variable for every component
     * @param component The RADComponent for which the properties are to be obtained
     */
    @Override
    public Node.Property[] getSyntheticProperties(final RADComponent component) {
        ResourceBundle bundle = FormUtils.getBundle();
        List<Node.Property> propList = new ArrayList<Node.Property>();
        if (component == null) {
            propList.add(new VariablesModifierProperty());
            propList.add(new LocalVariablesProperty());
            propList.add(new GenerateFQNProperty());
            propList.add(new GenerateMnemonicsCodeProperty());
            propList.add(new ListenerGenerationStyleProperty());
            FormServices services = Lookup.getDefault().lookup(FormServices.class);
            if (services.isLayoutExtensionsLibrarySupported()) {
                propList.add(new LayoutCodeTargetProperty());
            }
        } else if (component != formModel.getTopRADComponent()) {
            
            propList.add(createBeanClassNameProperty(component));
            
            propList.add(new PropertySupport.ReadWrite(
                RADComponent.PROP_NAME,
                String.class,
                bundle.getString("MSG_JC_VariableName"), // NOI18N
                bundle.getString("MSG_JC_VariableDesc")) // NOI18N
            {
                @Override
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    component.rename((String)value);
                    component.getNodeReference().firePropertyChangeHelper(
                        RADComponent.PROP_NAME, null, null); // NOI18N
                }

                @Override
                public Object getValue() {
                    return component.getName();
                }

                @Override
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });

            final FormProperty modifProp = new FormProperty(
                PROP_VARIABLE_MODIFIER,
                Integer.class,
                bundle.getString("MSG_JC_VariableModifiers"), // NOI18N
                null)
            {
                @Override
                public void setTargetValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    Object oldValue = getTargetValue();

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= ~CodeVariable.ALL_MODIF_MASK;
                    varType |= ((Integer)value).intValue() & CodeVariable.ALL_MODIF_MASK;

                    if ((varType & CodeVariable.ALL_MODIF_MASK)
                            != (formModel.getSettings().getVariablesModifier()
                                & CodeVariable.ALL_MODIF_MASK))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_MODIFIER,
                                Integer.valueOf(varType & CodeVariable.ALL_MODIF_MASK)); // value
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        if (component.getAuxValue(AUX_VARIABLE_MODIFIER) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_MODIFIER);
                        }
                    }

                    String typeParameters = exp.getVariable().getDeclaredTypeParameters();
                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, typeParameters, varName);
                }

                @Override
                public Object getTargetValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_MODIFIER);
                    if (val != null)
                        return val;

                    return Integer.valueOf(formModel.getSettings().getVariablesModifier());
                }

                @Override
                public boolean supportsDefaultValue() {
                    return component.getAuxValue(AUX_VARIABLE_LOCAL) == null;
                }

                @Override
                public Object getDefaultValue() {
                    return component.getAuxValue(AUX_VARIABLE_LOCAL) == null ?
                           Integer.valueOf(formModel.getSettings().getVariablesModifier()) : null;
                }

                @Override
                protected void propertyValueChanged(Object old, Object current) {
                    super.propertyValueChanged(old, current);
                    if (isChangeFiring()) {
                        formModel.fireSyntheticPropertyChanged(
                            component, getName(), old, current);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                getName(), null, null);
                        }
                    }
                }

                @Override
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                @Override
                public PropertyEditor getExpliciteEditor() { // getPropertyEditor
                    return new ModifierEditor() {
                        private void updateMask() {
                            Boolean local = (Boolean) component.getAuxValue(AUX_VARIABLE_LOCAL);
                            if (local == null) {
                                local = formModel.getSettings().getVariablesLocal();
                            }
                            int mask = Boolean.TRUE.equals(local) ? Modifier.FINAL
                                    : Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
                                      Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE;
                            setMask(mask);
                        }

                        @Override
                        public void setAsText(String string) throws IllegalArgumentException {
                            updateMask();
                            super.setAsText(string);
                        }

                        @Override
                        public Component getCustomEditor() {
                            updateMask();
                            return super.getCustomEditor();
                        }
                    };
                }
            };
            modifProp.setShortDescription(bundle.getString("MSG_JC_VariableModifiersDesc")); // NOI18N
            propList.add(modifProp);
            
            final FormProperty paramTypesProp = new FormProperty(
                PROP_TYPE_PARAMETERS,
                String.class,
                bundle.getString("MSG_JC_TypeParameters"), // NOI18N
                null)
            {
                @Override
                public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    if (value instanceof String) {
                        // [this would be better to do in the PropertyEditor, but we don't have our own here yet]
                        String typeParams = ((String)value).trim();
                        if (typeParams.length() > 0 && !typeParams.startsWith("<")) { // NOI18N
                            typeParams = "<" + typeParams + ">"; // NOI18N
                        }
                        value = typeParams;
                    }
                    super.setValue(value);
                }
                
                @Override
                public void setTargetValue(Object value) {
                    if ((value != null) && !(value instanceof String))
                        throw new IllegalArgumentException();

                    // PENDING check for syntax of the value
                    
                    component.setAuxValue(AUX_TYPE_PARAMETERS, value);

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, (String)value, varName);
                }

                @Override
                public Object getTargetValue() {
                    Object value = component.getAuxValue(AUX_TYPE_PARAMETERS);
                    return (value == null) ? "" : value; // NOI18N
                }

                @Override
                public boolean supportsDefaultValue() {
                    return true;
                }

                @Override
                public Object getDefaultValue() {
                    return ""; // NOI18N
                }

                @Override
                protected void propertyValueChanged(Object old, Object current) {
                    super.propertyValueChanged(old, current);
                    if (isChangeFiring()) {
                        formModel.fireSyntheticPropertyChanged(
                            component, getName(), old, current);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                getName(), null, null);
                        }
                    }
                }

                @Override
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                @Override
                public PropertyEditor getExpliciteEditor() {
                    // PENDING replace by property editor that is able to determine
                    // formal type parameters of this class and can offer you
                    // a nice visual customizer
                    return super.getExpliciteEditor();
                }
            };
            paramTypesProp.setShortDescription(bundle.getString("MSG_JC_TypeParametersDesc")); // NOI18N
            propList.add(paramTypesProp);

            FormProperty localProp = new FormProperty(
                PROP_VARIABLE_LOCAL,
                Boolean.TYPE,
                bundle.getString("MSG_JC_UseLocalVar"), // NOI18N
                null)
            {
                @Override
                public void setTargetValue(Object value) {
                    if (!(value instanceof Boolean))
                        throw new IllegalArgumentException();

                    Boolean oldValue = (Boolean) getTargetValue();
//                    if (value.equals(oldValue)) return;

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= CodeVariable.FINAL
                               | ~(CodeVariable.ALL_MODIF_MASK | CodeVariable.SCOPE_MASK);
                    if (Boolean.TRUE.equals(value))
                        varType |= CodeVariable.LOCAL;
                    else
                        varType |= CodeVariable.FIELD
                                   | formModel.getSettings().getVariablesModifier();

                    if (((varType & CodeVariable.LOCAL) != 0)
                            != (formModel.getSettings().getVariablesLocal()))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_LOCAL, value);
                        try {
                            modifProp.setValue(Integer.valueOf(varType & CodeVariable.ALL_MODIF_MASK));
                        }
                        catch (Exception ex) { // should not happen
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        if (component.getAuxValue(AUX_VARIABLE_LOCAL) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_LOCAL);
                        }
                        try {
                            modifProp.restoreDefaultValue();
                        }
                        catch (Exception ex) { // should not happen
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }

                    String typeParameters = exp.getVariable().getDeclaredTypeParameters();
                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, typeParameters, varName);
                }

                @Override
                public Object getTargetValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_LOCAL);
                    if (val != null)
                        return val;

                    return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
                }

                @Override
                public boolean supportsDefaultValue() {
                    return true;
                }

                @Override
                public Object getDefaultValue() {
                    return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
                }

                @Override
                protected void propertyValueChanged(Object old, Object current) {
                    super.propertyValueChanged(old, current);
                    if (isChangeFiring()) {
                        formModel.fireSyntheticPropertyChanged(
                            component, getName(), old, current);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                getName(), null, null);
                        }
                    }
                }

                @Override
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            };
            localProp.setShortDescription(bundle.getString("MSG_JC_UseLocalVarDesc")); // NOI18N
            propList.add(localProp);

            // Mnemonics support - start -
            if (javax.swing.JLabel.class.isAssignableFrom(component.getBeanClass())
                    || javax.swing.AbstractButton.class.isAssignableFrom(component.getBeanClass()))
                propList.add(new PropertySupport.ReadWrite(
                    PROP_GENERATE_MNEMONICS, 
                    Boolean.TYPE,
                    bundle.getString("PROP_GENERATE_MNEMONICS"), // NOI18N
                    bundle.getString("HINT_GENERATE_MNEMONICS2")) // NOI18N
                {
                    private boolean writable = JavaCodeGenerator.this.canGenerate
                                && !component.isReadOnly()
                        // don't allow turing mnemonics on if not supported (and not already on)
                                && (isUsingMnemonics(component)
                                    || formEditorSupport.canGenerateNBMnemonicsCode());
                    @Override
                    public void setValue(Object value) {
                        Object oldValue = getValue();
                        component.setAuxValue(PROP_GENERATE_MNEMONICS, value);
                        formModel.fireSyntheticPropertyChanged(
                            component, PROP_GENERATE_MNEMONICS, oldValue, value);
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_GENERATE_MNEMONICS, null, null); // NOI18N
                    }

                    @Override
                    public Object getValue() {
                        return isUsingMnemonics(component) ?
                               Boolean.TRUE : Boolean.FALSE;
                    }

                    @Override
                    public boolean canWrite() {
                        return writable;
                    }

                    @Override
                    public boolean supportsDefaultValue() {
                        return true;
                    }

                    @Override
                    public boolean isDefaultValue() {
                        Object mnem = component.getAuxValue(PROP_GENERATE_MNEMONICS);
                        return mnem == null || mnem.equals(formModel.getSettings().getGenerateMnemonicsCode());
                    }

                    @Override
                    public void restoreDefaultValue() {
                        setValue(null);
                    }
                });
            // Mnemonics support - end -

            propList.add(new CodeProperty(
                    component,
                    PROP_CREATE_CODE_CUSTOM, AUX_CREATE_CODE_CUSTOM,
                    bundle.getString("MSG_JC_CustomCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_CustomCreationCodeDesc"), // NOI18N
                    FormModel.FormVersion.BASIC));

            propList.add(new CodeProperty(
                    component,
                    PROP_CREATE_CODE_PRE, AUX_CREATE_CODE_PRE,
                    bundle.getString("MSG_JC_PreCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_PreCreationCodeDesc"), // NOI18N
                    FormModel.FormVersion.BASIC));
            propList.add(new CodeProperty(
                    component,
                    PROP_CREATE_CODE_POST, AUX_CREATE_CODE_POST,
                    bundle.getString("MSG_JC_PostCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_PostCreationCodeDesc"), // NOI18N
                    FormModel.FormVersion.BASIC));

            propList.add(new CodeProperty(
                    component,
                    PROP_INIT_CODE_PRE, AUX_INIT_CODE_PRE,
                    bundle.getString("MSG_JC_PreInitCode"), // NOI18N
                    bundle.getString("MSG_JC_PreInitCodeDesc"), // NOI18N
                    FormModel.FormVersion.BASIC));
            propList.add(new CodeProperty(
                    component,
                    PROP_INIT_CODE_POST, AUX_INIT_CODE_POST,
                    bundle.getString("MSG_JC_PostInitCode"), // NOI18N
                    bundle.getString("MSG_JC_PostInitCodeDesc"), // NOI18N
                    FormModel.FormVersion.BASIC));

            propList.add(new CodeProperty(
                    component,
                    PROP_LISTENERS_POST, AUX_LISTENERS_POST,
                    bundle.getString("MSG_JC_PostListenersCode"), // NOI18N
                    bundle.getString("MSG_JC_PostListenersCodeDesc"), // NOI18N
                    FormModel.FormVersion.NB60));

            if (component.getParentComponent() != null) {
                propList.add(new CodeProperty(
                        component,
                        PROP_ADDING_PRE, AUX_ADDING_PRE,
                        bundle.getString("MSG_JC_PreAddCode"), // NOI18N
                        bundle.getString("MSG_JC_PreAddCodeDesc"), // NOI18N
                        FormModel.FormVersion.NB60));
                propList.add(new CodeProperty(
                        component,
                        PROP_ADDING_POST, AUX_ADDING_POST,
                        bundle.getString("MSG_JC_PostAddCode"), // NOI18N
                        bundle.getString("MSG_JC_PostAddCodeDesc"), // NOI18N
                        FormModel.FormVersion.NB60));
            }

            if (component instanceof ComponentContainer) {
                propList.add(new CodeProperty(
                        component,
                        PROP_LAYOUT_PRE, AUX_LAYOUT_PRE,
                        bundle.getString("MSG_JC_PrePopulationCode"), // NOI18N
                        bundle.getString("MSG_JC_PrePopulationCodeDesc"), // NOI18N
                        FormModel.FormVersion.NB60));
                propList.add(new CodeProperty(
                        component,
                        PROP_LAYOUT_POST, AUX_LAYOUT_POST,
                        bundle.getString("MSG_JC_PostPopulationCode"), // NOI18N
                        bundle.getString("MSG_JC_PostPopulationCodeDesc"), // NOI18N
                        FormModel.FormVersion.NB60));
            }

            propList.add(new CodeProperty(
                    component,
                    PROP_ALL_SET_POST, AUX_ALL_SET_POST,
                    bundle.getString("MSG_JC_AfterAllSetCode"), // NOI18N
                    bundle.getString("MSG_JC_AfterAllSetCodeDesc"), // NOI18N
                    FormModel.FormVersion.NB60));

            propList.add(new CodeProperty(
                    component,
                    PROP_DECLARATION_PRE, AUX_DECLARATION_PRE,
                    bundle.getString("MSG_JC_PreDeclaration"), // NOI18N
                    bundle.getString("MSG_JC_PreDeclarationDesc"), // NOI18N
                    FormModel.FormVersion.NB60_PRE));
            propList.add(new CodeProperty(
                    component,
                    PROP_DECLARATION_POST, AUX_DECLARATION_POST,
                    bundle.getString("MSG_JC_PostDeclaration"), // NOI18N
                    bundle.getString("MSG_JC_PostDeclarationDesc"), // NOI18N
                    FormModel.FormVersion.NB60_PRE));

            propList.add(new PropertySupport.ReadWrite(
                PROP_CODE_GENERATION,
                Integer.TYPE,
                bundle.getString("MSG_JC_CodeGeneration"), // NOI18N
                bundle.getString("MSG_JC_CodeGenerationDesc")) // NOI18N
            {
                @Override
                public void setValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!getDefaultValue().equals(value))
                        component.setAuxValue(AUX_CODE_GENERATION, value);
                    else if (component.getAuxValue(AUX_CODE_GENERATION) != null) {
                        component.getAuxValues().remove(AUX_CODE_GENERATION);
                    }

                    if (value.equals(VALUE_SERIALIZE)
                            && component.getAuxValue(AUX_SERIALIZE_TO) == null)
                        component.setAuxValue(AUX_SERIALIZE_TO,
                                              getDefaultSerializedName(component));

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_CODE_GENERATION, oldValue, value);
                    component.getNodeReference().firePropertyChangeHelper(
                        PROP_CODE_GENERATION, null, null); // NOI18N
                }

                @Override
                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CODE_GENERATION);
                    if (value == null)
                        value = getDefaultValue();
                    return value;
                }

                @Override
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                @Override
                public PropertyEditor getPropertyEditor() {
                    return new CodeGenerateEditor(component);
                }

                private Object getDefaultValue() {
                    return component.hasHiddenState() ?
                                VALUE_SERIALIZE : VALUE_GENERATE_CODE;
                }
            });

            propList.add(new PropertySupport.ReadWrite(
                PROP_SERIALIZE_TO,
                String.class,
                bundle.getString("MSG_JC_SerializeTo"), // NOI18N
                bundle.getString("MSG_JC_SerializeToDesc")) // NOI18N
            {
                @Override
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value)) // NOI18N
                        component.setAuxValue(AUX_SERIALIZE_TO, value);
                    else if (component.getAuxValue(AUX_SERIALIZE_TO) != null) {
                        component.getAuxValues().remove(AUX_SERIALIZE_TO);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_SERIALIZE_TO, oldValue, value);
                    component.getNodeReference().firePropertyChangeHelper(
                        PROP_SERIALIZE_TO, null, null); // NOI18N
                }

                @Override
                public Object getValue() {
                    Object value = component.getAuxValue(AUX_SERIALIZE_TO);
                    if (value == null)
                        value = getDefaultSerializedName(component);
                    return value;
                }

                @Override
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });
        } else if (component instanceof RADVisualComponent) { // i.e. the top component is not a container
            propList.add(new PropertySupport.ReadWrite<Dimension>(
                FormDesigner.PROP_DESIGNER_SIZE,
                Dimension.class,
                bundle.getString("MSG_DesignerSize"), // NOI18N
                bundle.getString("HINT_DesignerSize")) // NOI18N
            {
                @Override
                public void setValue(Dimension value) {
                    if (!getDefaultValue().equals(value))
                        component.setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, value);
                    else if (component.getAuxValue(FormDesigner.PROP_DESIGNER_SIZE) != null) {
                        component.getAuxValues().remove(FormDesigner.PROP_DESIGNER_SIZE);
                    }
                    formModel.fireSyntheticPropertyChanged(component,
                        FormDesigner.PROP_DESIGNER_SIZE, null, null);
                }

                @Override
                public Dimension getValue() {
                    Dimension value = (Dimension)component.getAuxValue(FormDesigner.PROP_DESIGNER_SIZE);
                    if (value == null)
                        value = getDefaultValue();
                    return value;
                }

                private Dimension getDefaultValue() {
                    return new Dimension(400, 300);
                }
            });
        }

        Node.Property[] props = new Node.Property[propList.size()];
        propList.toArray(props);
        return props;
    }

    public static PropertySupport createBeanClassNameProperty(final RADComponent component) {
        final ResourceBundle bundle = FormUtils.getBundle();
        
        return new PropertySupport.ReadOnly(
                "beanClass", // NOI18N
                String.class,
                bundle.getString("MSG_JC_BeanClass"), // NOI18N
                bundle.getString("MSG_JC_BeanClassDesc")) // NOI18N
            {
		String invalid = null;
            @Override
                public Object getValue() {
                    if(!component.isValid()) {
			if(invalid==null) {
			    invalid = bundle.getString("CTL_LB_InvalidComponent");  // NOI18N
			}
                        return component.getMissingClassName() + ": [" + invalid + "]"; // NOI18N
                    }
                    Class beanClass = component.getBeanClass();
                    if(beanClass!=null) {
                        return beanClass.toString();
                    }
                    return ""; // NOI18N
                }

                @Override
                public boolean canWrite() {
                    return false;                    
                }     
                
                @Override
                public PropertyEditor getPropertyEditor() {
                    return new PropertyEditorSupport(){};
                }
            };
    }

    static void setupComponentFromAuxValues(RADComponent comp) {
        Object val = comp.getAuxValue(JavaCodeGenerator.AUX_VARIABLE_MODIFIER);
        int newType = val instanceof Integer ? ((Integer)val).intValue() : -1;

        val = comp.getAuxValue(JavaCodeGenerator.AUX_VARIABLE_LOCAL);
        if (val instanceof Boolean) {
            if (newType == -1) {
                newType = 0;
            }
            newType |= Boolean.TRUE.equals(val)
                       ? CodeVariable.LOCAL | CodeVariable.EXPLICIT_DECLARATION
                       : CodeVariable.FIELD;
        }

        val = comp.getAuxValue(JavaCodeGenerator.AUX_TYPE_PARAMETERS);
        String typeParameters = null;
        if (val instanceof String) {
            typeParameters = (String)val;
        }

        if ((newType > -1) || (typeParameters != null)) { // set variable type
            CodeExpression exp = comp.getCodeExpression();
            int varType = exp.getVariable().getType();

            if (newType > -1) {
                varType &= ~CodeVariable.ALL_MODIF_MASK;
                varType |= newType & CodeVariable.ALL_MODIF_MASK;

                if ((newType & CodeVariable.SCOPE_MASK) != 0) {
                    varType &= ~CodeVariable.SCOPE_MASK;
                    varType |= newType & CodeVariable.SCOPE_MASK;
                }

                if ((newType & CodeVariable.DECLARATION_MASK) != 0) {
                    varType &= ~CodeVariable.DECLARATION_MASK;
                    varType |= newType & CodeVariable.DECLARATION_MASK;
                }
            }

            CodeStructure codeStructure = comp.getFormModel().getCodeStructure();
            String varName = comp.getName(); // get the original name
            codeStructure.removeExpressionFromVariable(exp);
            codeStructure.createVariableForExpression(exp, varType, typeParameters, varName);
        }
    }

    //
    // Private Methods
    //

    private String getDefaultSerializedName(RADComponent component) {
        return component.getFormModel().getName()
            + "_" + component.getName(); // NOI18N
    }

    void regenerateInitComponents() {
        if (!initialized || !canGenerate)
            return;

        // find indent engine to use or imitate
        IndentEngine indentEngine = FormLoaderSettings.getInstance().getUseIndentEngine()
                ? IndentEngine.find(formEditor.getSourcesDocument()) : null;

        final SimpleSection initComponentsSection = formEditor.getInitComponentSection();
        int initComponentsOffset = initComponentsSection.getCaretPosition().getOffset();

        // create Writer for writing the generated code in
        StringWriter initCodeBuffer = new StringWriter(1024);
        CodeWriter initCodeWriter;
        if (indentEngine != null) { // use original indent engine
            initCodeWriter = new CodeWriter(
                    indentEngine.createWriter(formEditor.getSourcesDocument(),
                                              initComponentsOffset,
                                              initCodeBuffer),
                    true);
        } else {
            initCodeWriter = new CodeWriter(initCodeBuffer, true);
        }
        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        cleanup();

        try {
            boolean foldGeneratedCode = FormLoaderSettings.getInstance().getFoldGeneratedCode();
            Boolean foldState = null;
            if (foldGeneratedCode) {
                writer.write("// <editor-fold defaultstate=\"collapsed\" desc=\""); // NOI18N
                writer.write(FormUtils.getBundleString("MSG_GeneratedCode")); // NOI18N
                writer.write("\">\n"); // NOI18N
                foldState = formEditorSupport.getFoldState(initComponentsSection.getStartPosition().getOffset());
            }

            writer.write("private void initComponents() {\n"); // NOI18N

            addLocalVariables(writer);

            if (bindingGroupVariable != null) {
                initCodeWriter.write(bindingGroupVariable + " = new " + getBindingGroupClass().getName() + "();\n\n"); // NOI18N
            }

            emptyLineRequest++;
            Collection<RADComponent> otherComps = formModel.getOtherComponents();
            for (RADComponent metacomp : otherComps) {
                addCreateCode(metacomp, initCodeWriter);
            }
            RADComponent top = formModel.getTopRADComponent();
            addCreateCode(top, initCodeWriter);

            if (formModel.getSettings().getListenerGenerationStyle() == CEDL_INNERCLASS
                && anyEvents())
            {
                emptyLineRequest++;
                addDispatchListenerDeclaration(writer);
            }

            for (RADComponent metacomp : otherComps) {
                addInitCode(metacomp, initCodeWriter, null);
            }
            addInitCode(top, initCodeWriter, null);

            if (bindingGroupVariable != null) {
                initCodeWriter.write("\n" + bindingGroupVariable + ".bind();\n"); // NOI18N
            }

            generateFormSizeCode(writer);

            writer.write("}"); // no new line because of fold footer // NOI18N

            int listenerCodeStyle = formModel.getSettings().getListenerGenerationStyle();
            if ((listenerCodeStyle == CEDL_INNERCLASS
                  || listenerCodeStyle == CEDL_MAINCLASS)
                && anyEvents())
            {
                writer.write("\n\n"); // NOI18N
                writer.write(getEventDispatchCodeComment());
                writer.write("\n"); // NOI18N

                generateDispatchListenerCode(writer);
            }
            else listenersInMainClass = null;

            if (foldGeneratedCode) {
                writer.write("// </editor-fold>\n"); // NOI18N
            }
            else {
                writer.write("\n"); // NOI18N
            }
            writer.close();

             // set the text into the guarded block
            String newText = initCodeBuffer.toString();
            if (indentEngine == null) {
                newText = indentCode(newText, 1);
            }
            initComponentsSection.setText(newText);

            if (foldState != null) {
                formEditorSupport.restoreFoldState(foldState,
                        initComponentsSection.getStartPosition().getOffset(),
                        initComponentsSection.getEndPosition().getOffset());
            }

            clearUndo();
        }
        catch (IOException e) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        cleanup();
    }

    private Class getBindingGroupClass() {
        return formEditor.getBindingSupport().getBindingGroupClass();
    }

    private void cleanup() {
        emptyLineCounter = 0;
        emptyLineRequest = 0;
        if (constructorProperties != null)
            constructorProperties.clear();
        if (parentDependentProperties != null)
            parentDependentProperties.clear();
        if (childrenDependentProperties != null)
            childrenDependentProperties.clear();
        formModel.getCodeStructure().clearExternalVariableNames();
        repeatedCodeVariables = null;
        // preventive cleanup
        bindingVariables = null;
        if (bindingGroupVariable != null) { // we need to keep this variable registered
            bindingGroupVariable = formModel.getCodeStructure().getExternalVariableName(
                    getBindingGroupClass(), bindingGroupVariable, true);
        }
    }

    /**
     * Returns the set of generated variables.
     * 
     * @return the set of generated variables.
     */
    private void regenerateVariables() {
        if (!initialized || !canGenerate) {
            return;
        }

        IndentEngine indentEngine = FormLoaderSettings.getInstance().getUseIndentEngine()
                ? IndentEngine.find(formEditor.getSourcesDocument()) : null;

        StringWriter variablesBuffer = new StringWriter(1024);
        CodeWriter variablesWriter;
        final SimpleSection variablesSection = formEditor.getVariablesSection();

        if (indentEngine != null) {
            variablesWriter = new CodeWriter(
                    indentEngine.createWriter(formEditor.getSourcesDocument(),
                                              variablesSection.getCaretPosition().getOffset(),
                                              variablesBuffer),
                    false);
        } else {
            variablesWriter = new CodeWriter(variablesBuffer, false);
        }

        try {
	    variablesWriter.write(getVariablesHeaderComment());
            variablesWriter.write("\n"); // NOI18N

            addFieldVariables(variablesWriter);
            
            variablesWriter.write(getVariablesFooterComment());
            variablesWriter.write("\n"); // NOI18N
            variablesWriter.getWriter().close();

            String newText = variablesBuffer.toString();
            if (indentEngine == null) {
                newText = indentCode(newText, 1);
            }

            variablesSection.setText(newText);        
            clearUndo();
        }
        catch (IOException e) { // should not happen
            e.printStackTrace();
        }
    }   
    
    private void addCreateCode(RADComponent comp, CodeWriter initCodeWriter)
        throws IOException
    {
        if (comp == null)
            return;

        if (comp != formModel.getTopRADComponent()) {
            generateComponentCreate(comp, initCodeWriter, true, null);
        }
        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                addCreateCode(children[i], initCodeWriter);
            }
        }
    }

    private void addInitCode(RADComponent comp,
                             CodeWriter initCodeWriter,
                             CustomCodeData codeData)
        throws IOException
    {
        if (comp == null)
            return;

        Writer writer = initCodeWriter.getWriter();

        int counter0 = emptyLineCounter;
        int request0 = emptyLineRequest;
        emptyLineRequest++;

        generateComponentProperties(comp, initCodeWriter, codeData);
        generateComponentEvents(comp, initCodeWriter, codeData);

        if (comp instanceof ComponentContainer) {
            boolean freeDesign = RADVisualContainer.isFreeDesignContainer(comp);
            ComponentContainer cont = (ComponentContainer) comp;
            if (!freeDesign) // layout and pre-population code before sub-components
                generateOldLayout(cont, initCodeWriter, codeData);

            if (codeData == null) { // normal code generation
                // generate code of sub-components
                RADComponent[] subBeans = cont.getSubBeans();
                for (RADComponent subcomp : subBeans) {
                    addInitCode(subcomp, initCodeWriter, null);
                }
                if (freeDesign) { // generate complete layout code
                    // GroupLayout setup code also adds all sub-components
                    RADVisualContainer visualCont = (RADVisualContainer) cont;
                    emptyLineRequest++;
                    generatePrePopulationCode(visualCont, writer, null);
                    emptyLineRequest++;
                    for (RADVisualComponent subcomp : visualCont.getSubComponents()) {
                        generateComponentAddPre(subcomp, writer, null);
                        generateLayeredPaneCode(subcomp, writer);
                    }
                    emptyLineRequest++;
                    generateFreeDesignLayoutCode(visualCont, initCodeWriter); // this always generates something
                    emptyLineRequest++;
                    // some code of sub-components is generated after adding
                    // them to the container (a11y, after-all-set)
                    for (RADVisualComponent subcomp : visualCont.getSubComponents()) { // excluding menu
                        generateComponentAddPost(subcomp, initCodeWriter, null);
                        generateAccessibilityCode(subcomp, initCodeWriter, null);
                        generateInjectionCode(subcomp, initCodeWriter, null);
                        generateAfterAllSetCode(subcomp, writer, null);
                    }
                    emptyLineRequest++;
                }
                else if (subBeans.length > 0)
                    emptyLineRequest++; // empty line after sub-components
            }
            else { // build code data for editing
                if (RADVisualContainer.isFreeDesignContainer(comp)) {
                    String substCode = "// " + FormUtils.getBundleString("CustomCode-SubstSub"); // NOI18N
                    codeData.addGuardedBlock(substCode);
                    generatePrePopulationCode(comp, writer, codeData);
                    substCode = "// " + FormUtils.getBundleString("CustomCode-SubstLayout"); // NOI18N
                    codeData.addGuardedBlock(substCode);
                }
                else { // with LM, the pre-layout code is elsewhere (before properties)
                    String substCode = "// " + FormUtils.getBundleString("CustomCode-SubstSubAndLayout"); // NOI18N
                    codeData.addGuardedBlock(substCode);
                }
            }

            int counter1 = emptyLineCounter;
            emptyLineRequest++;
            generatePostPopulationCode(comp, initCodeWriter, codeData);
            if (emptyLineCounter == counter1)
                emptyLineRequest--; // no post-population code, don't force empty line
            else
                emptyLineRequest++; // force empty line after post-population
        }

        if (emptyLineCounter == counter0)
            emptyLineRequest = request0; // no code was generated, don't force empty line

        if (!RADVisualContainer.isInFreeDesign(comp)) { // in container with LM, or menu component
            // add to parent container (if not root itself)
            generateComponentAddCode(comp, initCodeWriter, codeData);
            boolean endingCode = false;
            if (generateAccessibilityCode(comp, initCodeWriter, codeData))
                endingCode = true;
            if (generateInjectionCode(comp, initCodeWriter, codeData))
                endingCode = true;
            if (generateAfterAllSetCode(comp, writer, codeData))
                endingCode = true;
            if (endingCode)
                emptyLineRequest++; // force empty line after
        }
        else if (codeData != null) { // build code data for editing
            // In free design this is generated with parent container (see above).
            // But building code data is invoked only for the component itself,
            // not for its parent, so we must do it here.
            generateComponentAddPre(comp, writer, codeData);
            String substCode = "// " + FormUtils.getBundleString("CustomCode-SubstAdding"); // NOI18N
            codeData.addGuardedBlock(substCode);
            generateComponentAddPost(comp, initCodeWriter, codeData);
            generateAccessibilityCode(comp, initCodeWriter, codeData);
            generateInjectionCode(comp, initCodeWriter, codeData);
            generateAfterAllSetCode(comp, writer, codeData);
        }
    }

    private void generateOldLayout(ComponentContainer cont,
                                   CodeWriter initCodeWriter,
                                   CustomCodeData codeData)
        throws IOException
    {
        RADVisualContainer visualCont = cont instanceof RADVisualContainer ?
                                        (RADVisualContainer) cont : null;
        LayoutSupportManager layoutSupport = visualCont != null ?
                                             visualCont.getLayoutSupport() : null;

        if (layoutSupport != null) { // setLayout code for old layout support
            if (layoutSupport.isLayoutChanged()) {
                Iterator it = layoutSupport.getLayoutCode().getStatementsIterator();
                if (codeData == null && it.hasNext())
                    generateEmptyLineIfNeeded(initCodeWriter.getWriter());
                while (it.hasNext()) {
                    CodeStatement statement = (CodeStatement) it.next();
                    initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
                }

                if (codeData != null) { // build code data for editing
                    String code = indentCode(initCodeWriter.extractString());
                    codeData.addGuardedBlock(code);
                }
            }
        }

        generatePrePopulationCode((RADComponent)cont, initCodeWriter.getWriter(), codeData);
    }

    private boolean generateAfterAllSetCode(RADComponent comp,
                                            Writer writer,
                                            CustomCodeData codeData)
        throws IOException
    {
        boolean generated = false;

        String postCode = (String) comp.getAuxValue(AUX_ALL_SET_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_ALL_SET_POST),
                                      0,
                                      FormUtils.getBundleString("CustomCode-AfterAllSet"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostPopulationCodeDesc")); // NOI18N
        }
        // normal code generation
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
            generated = true;
        }

        return generated;
    }

    private void generateComponentCreate(RADComponent comp,
                                         CodeWriter initCodeWriter,
                                         boolean insideMethod, // if this for initComponents
                                         CustomCodeData codeData)
        throws IOException
    {
        if (comp instanceof RADMenuItemComponent
            && ((RADMenuItemComponent)comp).getMenuItemType()
                   == RADMenuItemComponent.T_SEPARATOR)
        { // do not generate anything for AWT separator as it is not a real component
            return;
        }

        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        CodeVariable var = comp.getCodeExpression().getVariable();
        int varType = var.getType();
        boolean localVariable = (varType & CodeVariable.SCOPE_MASK) == CodeVariable.LOCAL;

        if (insideMethod) {
            if (isFinalFieldVariable(varType))
                return; // is generated in field variables (here we are in initComponents)

            String preCode = (String) comp.getAuxValue(AUX_CREATE_CODE_PRE);
            if (codeData != null) { // build code data for editing
                codeData.addEditableBlock(preCode,
                                          (FormProperty) comp.getSyntheticProperty(PROP_CREATE_CODE_PRE),
                                          2, // preference index
                                          FormUtils.getBundleString("CustomCode-PreCreation"), // NOI18N
                                          FormUtils.getBundleString("MSG_JC_PreCreationCodeDesc")); // NOI18N
            }
            else if (preCode != null && !preCode.equals("")) { // NOI18N
                // normal generation of custom pre-creation code
                generateEmptyLineIfNeeded(writer);
                writer.write(preCode);
                if (!preCode.endsWith("\n")) // NOI18N
                    writer.write("\n"); // NOI18N
            }
        }

        Integer generationType = (Integer)comp.getAuxValue(AUX_CODE_GENERATION);
        if (comp.hasHiddenState() || VALUE_SERIALIZE.equals(generationType)) {
            // generate code for restoring serialized component [only works for field variables]
            if (!insideMethod)
                return;

            String serializeTo = (String)comp.getAuxValue(AUX_SERIALIZE_TO);
            if (serializeTo == null) {
                serializeTo = getDefaultSerializedName(comp);
                comp.setAuxValue(AUX_SERIALIZE_TO, serializeTo);
            }
            if (codeData == null)
                generateEmptyLineIfNeeded(writer);
            writer.write("try {\n"); // NOI18N
            writer.write(comp.getName());
            writer.write(" =("); // NOI18N
            writer.write(getSourceClassName(comp.getBeanClass()));
            writer.write(")java.beans.Beans.instantiate(getClass().getClassLoader(), \""); // NOI18N

            // write package name
            FileObject fo = formEditor.getFormDataObject().getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            String packageName = cp.getResourceName(fo.getParent());
            if (!"".equals(packageName)) { // NOI18N
                writer.write(packageName + "."); // NOI18N
            }
            writer.write(serializeTo);
            writer.write("\");\n"); // NOI18N
            writer.write("} catch (ClassNotFoundException e) {\n"); // NOI18N
            writer.write("e.printStackTrace();\n"); // NOI18N
            writer.write("} catch (java.io.IOException e) {\n"); // NOI18N
            writer.write("e.printStackTrace();\n"); // NOI18N
            writer.write("}\n"); // NOI18N
            if (codeData != null) {
                codeData.addGuardedBlock(indentCode(initCodeWriter.extractString()));
            }
        }
        else { // generate standard component creation code
            if (codeData == null)
                generateEmptyLineIfNeeded(writer);

            StringBuilder buf = new StringBuilder(); // we need the entire creation statement written at once

            if (localVariable || isFinalFieldVariable(varType)) { // also generate declaration
                generateDeclarationPre(comp, writer, codeData);
                buf.append(Modifier.toString(varType & CodeVariable.ALL_MODIF_MASK));
                buf.append(" "); // NOI18N
                buf.append(getSourceClassName(comp.getBeanClass()));
                String typeParameters = var.getDeclaredTypeParameters();
                if ((typeParameters != null) && !"".equals(typeParameters)) { // NOI18N
                    buf.append(typeParameters);
                }
                buf.append(" "); // NOI18N
            }

            buf.append(var.getName()); // now buf contains variable code, more code added later

            // only for code data editing
            String customCode = null;
            boolean codeCustomized = false;
            String variableCode = buf.toString();

            String customCreationCode = (String) comp.getAuxValue(AUX_CREATE_CODE_CUSTOM);
            if (customCreationCode != null && !customCreationCode.equals("")) { // NOI18N
                // there is a custom creation code provided
                if (customCreationCode.endsWith(";")) { // NOI18N
                    customCreationCode = customCreationCode.substring(0, customCreationCode.length()-1);
                }
                if (codeData == null) { // normal code generation
                    buf.append(" = ").append(customCreationCode).append(";\n"); // NOI18N
                    initCodeWriter.write(buf.toString());
                    // and we are done
                }
                else { // build code data for editing
                    customCode = composeCustomCreationCode(variableCode, customCreationCode);
                    codeCustomized = true;
                }
            }
            if (customCreationCode == null || customCreationCode.equals("") || codeData != null) { // NOI18N
                // compose default creation code (if building code data for editing 
                // we need both custom and default creation code)
                CreationDescriptor desc = CreationFactory.getDescriptor(
                                                              comp.getBeanClass());
                if (desc == null)
                    desc = new CreationDescriptor(comp.getBeanClass());

                CreationDescriptor.Creator creator =
                    desc.findBestCreator(comp.getKnownBeanProperties(),
                                         CreationDescriptor.CHANGED_ONLY);
                if (creator == null) // known properties are not enough...
                    creator = desc.findBestCreator(comp.getAllBeanProperties(),
                                               CreationDescriptor.CHANGED_ONLY);

                Class[] exceptions = creator.getExceptionTypes();
                if (insideMethod && needTryCode(exceptions)) {
                    if (localVariable) { // separate the declaration statement
                        buf.append(";\n"); // NOI18N
                        writer.write(buf.toString());
                        buf.delete(0, buf.length());
                        buf.append(var.getName());
                    }
                    writer.write("try {\n"); // NOI18N
                }
                else {
                    exceptions = null;
                }

                String[] propNames = creator.getPropertyNames();		
                FormProperty[] props;
                if (propNames.length > 0) {
                    if (constructorProperties == null)
                        constructorProperties = new HashMap<RADComponent, List<FormProperty>>();

		    List<FormProperty> usedProperties = new ArrayList<FormProperty>(propNames.length);
                    props = new FormProperty[propNames.length];

                    for (int i=0; i < propNames.length; i++) {
                        FormProperty prop = comp.getBeanProperty(propNames[i]);
                        props[i] = prop;
			usedProperties.add(prop);                        
                    }
		    constructorProperties.put(comp, usedProperties);
                }
                else {
                    props = RADComponent.NO_PROPERTIES;
                }
                String typeParams = (String)comp.getAuxValue(AUX_TYPE_PARAMETERS);
                if (typeParams != null && typeParams.startsWith("<") // NOI18N
                        && ClassPathUtils.isJava7ProjectPlatform(formEditor.getFormDataObject().getPrimaryFile())) {
                    typeParams = "<>"; // NOI18N
                }

                String defaultCreationCode = creator.getJavaCreationCode(props, null, null, null, typeParams);
                buf.append(" = ").append(defaultCreationCode).append(";\n"); // NOI18N
                initCodeWriter.write(buf.toString());

                if (codeData != null && !codeCustomized) { // get default code for custom editing (without try/catch)
                    customCode = composeCustomCreationCode(variableCode, defaultCreationCode);
                    // TODO: the default creation code might contain code marks from property editor - should be filtered out
                }

                if (exceptions != null) {
                    generateCatchCode(exceptions, writer);
                }
            }

            if (codeData != null) { // code data for creation code
                String defaultCode = indentCode(initCodeWriter.extractString());
                codeData.addGuardedBlock(defaultCode, customCode, CUSTOM_CODE_MARK, codeCustomized,
                                         (FormProperty) comp.getSyntheticProperty(PROP_CREATE_CODE_CUSTOM),
                                         FormUtils.getBundleString("CustomCode-Creation"), // NOI18N
                                         FormUtils.getBundleString("CustomCode-Creation_Hint")); // NOI18N
            }

            if (localVariable || isFinalFieldVariable(varType)) { // declaration generated
                generateDeclarationPost(comp, writer, codeData);
            }
        }

        if (insideMethod) {
            String postCode = (String) comp.getAuxValue(AUX_CREATE_CODE_POST);
            if (codeData != null) { // build code data for editing
                codeData.addEditableBlock(postCode,
                                          (FormProperty) comp.getSyntheticProperty(PROP_CREATE_CODE_POST),
                                          0, // preference index
                                          FormUtils.getBundleString("CustomCode-PostCreation"), // NOI18N
                                          FormUtils.getBundleString("MSG_JC_PostCreationCodeDesc")); // NOI18N
            }
            else if (postCode != null && !postCode.equals("")) { // NOI18N
                // normal generation of post-creation code
                writer.write(postCode);
                if (!postCode.endsWith("\n")) // NOI18N
                    writer.write("\n"); // NOI18N
            }
        }
    }

    // used only when building "code data" for editing
    private String composeCustomCreationCode(String variableCode, String creationCode) {
        return indentCode(variableCode + " = " + CUSTOM_CODE_MARK + creationCode + CUSTOM_CODE_MARK + ";\n"); // NOI18N
    }

    private void generateComponentProperties(RADComponent comp,
                                             CodeWriter initCodeWriter,
                                             CustomCodeData codeData)
        throws IOException
    {
        Writer writer = initCodeWriter.getWriter();

        String preCode = (String) comp.getAuxValue(AUX_INIT_CODE_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_INIT_CODE_PRE),
                                      10, // preference index
                                      FormUtils.getBundleString("CustomCode-PreInit"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PreInitCodeDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }

        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized, generate properties
	    List<FormProperty> usedProperties = constructorProperties != null ? constructorProperties.get(comp) : null;
            Iterator<? extends FormProperty> it = comp.getBeanPropertiesIterator(new PropertiesFilter(usedProperties), false);
            while (it.hasNext()) {
                FormProperty prop = it.next();

                List<FormProperty> depPropList = null;
                if (FormUtils.isMarkedParentDependentProperty(prop)) {
                    // needs to be generated after the component is added to the parent container
                    if (parentDependentProperties != null)
                        depPropList = parentDependentProperties.get(comp);
                    else {
                        parentDependentProperties = new HashMap<RADComponent,List<FormProperty>>();
                        depPropList = null;
                    }
                    if (depPropList == null) {
                        depPropList = new LinkedList<FormProperty>();
                        parentDependentProperties.put(comp, depPropList);
                    }
                    depPropList.add(prop);
                }
                if (FormUtils.isMarkedChildrenDependentProperty(prop)) {
                    // needs to be added after all sub-components are added to this container
                    if (childrenDependentProperties != null)
                        depPropList = childrenDependentProperties.get(comp);
                    else {
                        childrenDependentProperties = new HashMap<RADComponent,List<FormProperty>>();
                        depPropList = null;
                    }
                    if (depPropList == null) {
                        depPropList = new LinkedList<FormProperty>();
                        childrenDependentProperties.put(comp, depPropList);
                    }
                    depPropList.add(prop);
                }
                
                if (depPropList == null) { // independent property, generate here directly
                    generateProperty(prop, comp, null, initCodeWriter, codeData);
                }
            }
        }

        generateComponentBindings(comp, initCodeWriter);

        String postCode = (String) comp.getAuxValue(AUX_INIT_CODE_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_INIT_CODE_POST),
                                      7, // preference index
                                      FormUtils.getBundleString("CustomCode-PostInit"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostInitCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }
    
    private void generateComponentBindings(RADComponent comp,
                                           CodeWriter initCodeWriter)
        throws IOException
    {
        boolean anyBinding = false;
        for (BindingProperty prop : comp.getKnownBindingProperties()) {
            MetaBinding bindingDef = prop.getValue();
            if (bindingDef != null) {
                if (!anyBinding) {
                    initCodeWriter.write("\n"); // NOI18N
                    anyBinding = true;
                    if (bindingGroupVariable == null) { // Should happen only for Code Customizer
                        bindingGroupVariable = formModel.getCodeStructure().getExternalVariableName(
                            getBindingGroupClass(), "bindingGroup", true); // NOI18N
                    }
                }
                StringBuilder buf = new StringBuilder();
                String variable = formEditor.getBindingSupport().generateBinding(prop, buf, getBindingContext());
                initCodeWriter.write(buf.toString());

                if (bindingDef.isNullValueSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getNullValueProperty(), variable + ".setSourceNullValue"); // NOI18N
                }
                if (bindingDef.isIncompletePathValueSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getIncompleteValueProperty(), variable + ".setSourceUnreadableValue"); // NOI18N
                }
                if (bindingDef.isConverterSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getConverterProperty(), variable + ".setConverter"); // NOI18N
                }
                if (bindingDef.isValidatorSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getValidatorProperty(), variable + ".setValidator"); // NOI18N
                }
                initCodeWriter.write(bindingGroupVariable + ".addBinding(" + variable + ");\n"); // NOI18N
                if (bindingDef.isBindImmediately()) {
                    initCodeWriter.write(variable + ".bind();"); // NOI18N
                }
            }
        }
        if (anyBinding) {
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    BindingDesignSupport.CodeGeneratorContext bindingContext;
    private BindingDesignSupport.CodeGeneratorContext getBindingContext() {
        if (bindingContext == null) {
            bindingContext = new BindingDesignSupport.CodeGeneratorContext() {
                @Override
                public String getBindingDescriptionVariable(Class descriptionType, StringBuilder buf, boolean create) {
                    return JavaCodeGenerator.this.getBindingDescriptionVariable(descriptionType, buf, create);
                }

                @Override
                public String getExpressionJavaString(CodeExpression exp, String thisStr) {
                    return JavaCodeGenerator.getExpressionJavaString(exp, thisStr);
                }
            };
        }
        return bindingContext;
    }

    private void generateComponentBinding0(CodeWriter initCodeWriter, FormProperty property, String method) throws IOException {
        initCodeWriter.write(method + "(" + property.getJavaInitializationString() + ");\n"); // NOI18N
    }

    String getBindingDescriptionVariable(Class descriptionType, StringBuilder buf, boolean create) {
        String variable = null;
        if (bindingVariables == null) {
            bindingVariables = new HashMap<String,String>();
        } else {
            variable = bindingVariables.get(descriptionType.getName());
        }

        if (create && (variable == null)) {
            String name = descriptionType.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            variable = formModel.getCodeStructure().getExternalVariableName(
                    descriptionType, name, true);
            bindingVariables.put(descriptionType.getName(), variable);

            String typeName = descriptionType.getName();
            typeName = typeName.replace("$", "."); // NOI18N innerclasses
            buf.append(typeName);
        }
        return variable;
    }

    private boolean generateAccessibilityCode(RADComponent comp,
                                              CodeWriter initCodeWriter,
                                              CustomCodeData codeData)
        throws IOException
    {
        boolean generated = false;
        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized
            FormProperty[] props = comp.getKnownAccessibilityProperties();

            for (int i=0; i < props.length; i++) {
                boolean gen = generateProperty(props[i], comp, null, initCodeWriter, codeData);
                if (gen)
                    generated = true;
            }
        }
        return generated;
    }

    private boolean generateInjectionCode(RADComponent metacomp, CodeWriter initCodeWriter, CustomCodeData codeData)
        throws IOException
    {
        String injectionCode = ResourceSupport.getInjectionCode(
                metacomp, getComponentParameterString(metacomp, true));
        if (injectionCode != null) {
            if (!injectionCode.endsWith("\n")) { // NOI18N
                injectionCode = injectionCode + "\n"; // NOI18N
            }
            initCodeWriter.write(injectionCode);
            if (codeData != null) { // build code data for editing
                String code = indentCode(initCodeWriter.extractString());
                codeData.addGuardedBlock(code);
            }
            return true;
        }
        else return false;
    }

    private void generateComponentAddCode(RADComponent comp,
                                          CodeWriter initCodeWriter,
                                          CustomCodeData codeData)
        throws IOException
    {
        RADComponent parent = comp.getParentComponent();
        if (parent == null)
            return;

        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        generateComponentAddPre(comp, initCodeWriter.getWriter(), codeData);

        if (comp instanceof RADVisualComponent) {
            if (comp == ((RADVisualContainer)parent).getContainerMenu()) { // 
                assert comp.getBeanInstance() instanceof javax.swing.JMenuBar
                       && parent.getBeanInstance() instanceof javax.swing.RootPaneContainer;
                if (codeData == null) {
                    generateEmptyLineIfNeeded(writer);
                }
                writer.write(getComponentInvokeString(parent, true));
                writer.write("setJMenuBar("); // NOI18N
                writer.write(getComponentParameterString(comp, true));
                writer.write(");\n"); // NOI18N
            } else { // adding visual component to container with old layout support
                RADVisualComponent visualComp = (RADVisualComponent) comp;
                LayoutSupportManager laysup = visualComp.getParentLayoutSupport();
                CodeGroup componentCode = laysup != null ?
                    laysup.getComponentCode(visualComp) : null;
                if (componentCode != null) {
                    generateLayeredPaneCode(visualComp, writer);
                    Iterator it = componentCode.getStatementsIterator();
                    if (codeData == null && it.hasNext())
                        generateEmptyLineIfNeeded(writer);
                    while (it.hasNext()) {
                        CodeStatement statement = (CodeStatement) it.next();
                        initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                        initCodeWriter.write("\n"); // NOI18N
                    }
                }
            } // this method is not called for visual components in freee design
        }
        else if (comp instanceof RADMenuItemComponent) { // AWT menu
            if (parent instanceof RADVisualContainer) { // menu bar to visual container
                assert comp.getBeanInstance() instanceof java.awt.MenuBar //getMenuItemType() == RADMenuItemComponent.T_MENUBAR
                       && parent.getBeanInstance() instanceof java.awt.Frame;
                if (codeData == null) {
                    generateEmptyLineIfNeeded(writer);
                }
                writer.write(getComponentInvokeString(parent, true));
                writer.write("setMenuBar("); // NOI18N
                writer.write(getComponentParameterString(comp, true));
                writer.write(");\n"); // NOI18N
            }
            else { // menu component to another component
                assert parent instanceof RADMenuComponent;
                RADMenuItemComponent menuComp = (RADMenuItemComponent) comp;
                if (codeData == null) {
                    generateEmptyLineIfNeeded(writer);
                }
                if (menuComp.getMenuItemType() == RADMenuItemComponent.T_SEPARATOR) {
                    // treat AWT Separator specially - it is not a regular component
                    writer.write(getComponentInvokeString(parent, true));
                    writer.write("addSeparator();"); // NOI18N
                }
                else {
                    writer.write(getComponentInvokeString(parent, true));
                    writer.write("add("); // NOI18N
                    writer.write(getComponentParameterString(comp, true));
                    writer.write(");\n"); // NOI18N
                }
            }
        }
        // no other type of adding supported [assert false ?]

        if (codeData != null) { // build code data for editing
            String code = initCodeWriter.extractString();
            if (code != null && !code.equals("")) // NOI18N
                codeData.addGuardedBlock(indentCode(code));
        }

        generateComponentAddPost(comp, initCodeWriter, codeData);
    }

    private void generateComponentAddPre(RADComponent comp,
                                         Writer writer,
                                         CustomCodeData codeData)
        throws IOException
    {
        String preCode = (String) comp.getAuxValue(AUX_ADDING_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_ADDING_PRE),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PreAdding"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PreAddCodeDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    private void generateComponentAddPost(RADComponent comp,
                                          CodeWriter initCodeWriter,
                                          CustomCodeData codeData)
        throws IOException
    {
        // some known (i.e. hardcoded) properties need to be set after
        // the component is added to the parent container
        List<FormProperty> postProps;
        if (parentDependentProperties != null
            && (postProps = parentDependentProperties.get(comp)) != null)
        {
            for (FormProperty prop : postProps) {
                generateProperty(prop, comp, null, initCodeWriter, codeData);
            }
        }

        String postCode = (String) comp.getAuxValue(AUX_ADDING_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_ADDING_POST),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PostAdding"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostAddCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(initCodeWriter.getWriter());
            initCodeWriter.getWriter().write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }
    }

    private void generateFreeDesignLayoutCode(RADVisualContainer cont, CodeWriter initCodeWriter)
        throws IOException
    {
        LayoutComponent layoutCont = formModel.getLayoutModel().getLayoutComponent(cont.getId());
        if (layoutCont == null)
            return;

        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        RADVisualComponent[] comps = cont.getSubComponents();
        FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);

        // layout code and adding sub-components
        generateEmptyLineIfNeeded(writer);
        SwingLayoutCodeGenerator.ComponentInfo[] infos = new SwingLayoutCodeGenerator.ComponentInfo[comps.length];
        for (int i=0; i<comps.length; i++) {
            RADVisualComponent subComp = comps[i];
            SwingLayoutCodeGenerator.ComponentInfo info = new SwingLayoutCodeGenerator.ComponentInfo();
            info.id = subComp.getId();
            info.variableName = getExpressionJavaString(subComp.getCodeExpression(), ""); // NOI18N
            Object comp = formDesigner != null ? formDesigner.getComponent(subComp) : null;
            if (!(comp instanceof Component)) {
                comp = subComp.getBeanInstance();
            }
            info.component = (Component) comp;
            Node.Property minProp = subComp.getPropertyByName("minimumSize"); // NOI18N
            Node.Property prefProp = subComp.getPropertyByName("preferredSize"); // NOI18N
            Node.Property maxProp = subComp.getPropertyByName("maximumSize"); // NOI18N
            info.sizingChanged = !(((minProp == null) || minProp.isDefaultValue())
                && ((prefProp == null) || prefProp.isDefaultValue())
                && ((maxProp == null) || maxProp.isDefaultValue()));
            infos[i] = info;
        }
        CodeExpression contExpr = LayoutSupportManager.containerDelegateCodeExpression(
                                    cont, formModel.getCodeStructure());
        String contExprStr = getExpressionJavaString(contExpr, ""); // NOI18N
        CodeVariable contVar = cont.getCodeExpression().getVariable();
        String contVarName = (contVar == null) ? null : contVar.getName();
        getSwingGenerator().generateContainerLayout(
            writer,
            layoutCont,
            contExprStr,
            contVarName,
            infos,
            formModel.getSettings().getLayoutCodeTarget() == LAYOUT_CODE_LIBRARY);
    }

    private boolean generateLayeredPaneCode(RADVisualComponent metacomp, Writer writer) throws IOException {
        String layerJavaString = metacomp.getComponentLayerJavaInitCode();
        if (layerJavaString != null) {
            generateEmptyLineIfNeeded(writer);
            writer.write(getComponentInvokeString(metacomp.getParentContainer(), true));
            writer.write("setLayer("); // NOI18N
            writer.write(metacomp.getName());
            writer.write(", "); // NOI18N
            writer.write(layerJavaString);
            writer.write(");\n"); // NOI18N
            return true;
        }
        return false;
    }

    private SwingLayoutCodeGenerator getSwingGenerator() {
        if (swingGenerator == null) {
            swingGenerator = new SwingLayoutCodeGenerator(formModel.getLayoutModel());
        }
        return swingGenerator;
    }

    private void generatePrePopulationCode(RADComponent cont,
                                           Writer writer,
                                           CustomCodeData codeData)
        throws IOException
    {
        String preCode = (String) cont.getAuxValue(AUX_LAYOUT_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) cont.getSyntheticProperty(PROP_LAYOUT_PRE),
                                      2, // preference index
                                      FormUtils.getBundleString("CustomCode-PrePopulation"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PrePopulationCodeDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    private void generatePostPopulationCode(RADComponent cont,
                                            CodeWriter initCodeWriter,
                                            CustomCodeData codeData)
        throws IOException
    {
        // some known (i.e. hardcoded) container properties need to be set after
        // all sub-components are added
        List<FormProperty> postProps;
        if (childrenDependentProperties != null
            && (postProps = childrenDependentProperties.get(cont)) != null)
        {
            for (FormProperty prop : postProps) {
                generateProperty(prop, cont, null, initCodeWriter, codeData);
            }
        }

        // custom post-layout (post-population) code
        String postCode = (String) cont.getAuxValue(AUX_LAYOUT_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) cont.getSyntheticProperty(PROP_LAYOUT_POST),
                                      4, // preference index
                                      FormUtils.getBundleString("CustomCode-PostPopulation"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostPopulationCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(initCodeWriter.getWriter());
            initCodeWriter.getWriter().write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }
    }

    private void generateFormSizeCode(Writer writer) throws IOException {
        if (formModel.getTopRADComponent() instanceof RADVisualFormContainer) {
            RADVisualFormContainer visualForm =
                (RADVisualFormContainer) formModel.getTopRADComponent();

            // generate size code according to form size policy
            int formPolicy = visualForm.getFormSizePolicy();
            boolean genSize = visualForm.getGenerateSize();
            boolean genCenter = visualForm.getGenerateCenter();
            boolean genPosition = !genCenter && visualForm.getGeneratePosition();
            Dimension formSize = visualForm.getFormSize();
            Point formPosition = visualForm.getFormPosition();

            StringBuilder buf = new StringBuilder();
            if (formPolicy == RADVisualFormContainer.GEN_PACK) {
                buf.append("pack();\n"); // NOI18N
            } else if (formPolicy == RADVisualFormContainer.GEN_BOUNDS) {
                if (genPosition && genSize) { // both position and size
                    buf.append("setBounds("); // NOI18N
                    buf.append(formPosition.x);
                    buf.append(", "); // NOI18N
                    buf.append(formPosition.y);
                    buf.append(", "); // NOI18N
                    buf.append(formSize.width);
                    buf.append(", "); // NOI18N
                    buf.append(formSize.height);
                    buf.append(");\n"); // NOI18N
                } else if (genPosition) { // only position
                    buf.append("setLocation(new java.awt.Point("); // NOI18N
                    buf.append(formPosition.x);
                    buf.append(", "); // NOI18N
                    buf.append(formPosition.y);
                    buf.append("));\n"); // NOI18N
                } else if (genSize) { // only size
                    buf.append("setSize(new java.awt.Dimension("); // NOI18N
                    buf.append(formSize.width);
                    buf.append(", "); // NOI18N
                    buf.append(formSize.height);
                    buf.append("));\n"); // NOI18N
                }
            }
            if (genCenter && formPolicy != RADVisualFormContainer.GEN_NOTHING) {
                if (formPolicy == RADVisualFormContainer.GEN_BOUNDS && !genSize) {
                    // Before fixing bug 192435 centering could not be specified when "Generate pack()"
                    // was chosen. The combination of pack and center could be achieved by selecting
                    // "Generate Resize Code" and unchecking "Generate Size". It does not have much
                    // sense now, but we need to keep this behavior for existing forms.
                    buf.append("pack();\n"); // NOI18N
                }
                buf.append("setLocationRelativeTo(null);\n"); // NOI18N
            }
            String sizeText = buf.toString();
            if (!sizeText.equals("")) { // NOI18N
                emptyLineRequest++;
                generateEmptyLineIfNeeded(writer);
                writer.write(sizeText);
            }
        }
    }

    private boolean generateProperty(FormProperty prop,
                                     RADComponent comp,
                                     String setterVariable,
                                     CodeWriter initCodeWriter,
                                     CustomCodeData codeData)
        throws IOException
    {
        String preCode = prop.getPreCode();
        String postCode = prop.getPostCode();
        boolean valueSet = prop.isChanged();

        if ((preCode == null || preCode.equals("")) // NOI18N
            && (postCode == null || postCode.equals("")) // NOI18N
            && !valueSet)
            return false; // nothing set

        if (codeData == null)
            generateEmptyLineIfNeeded(initCodeWriter.getWriter());

        // 1. pre-initialization code
        if (codeData != null) { // build code data for editing
            String name;
            if (prop.getWriteMethod() != null)
                name = prop.getWriteMethod().getName();
            else {
                name = prop.getName();
                if (name.indexOf('.') >= 0)
                    name = name.substring(name.lastIndexOf('.')+1);
            }
            codeData.addEditableBlock(
                    preCode, prop, 0, // preference index
                    FormUtils.getFormattedBundleString("CustomCode-PreProperty_Format", // NOI18N
                                                       new Object[] { name }),
                    FormUtils.getBundleString("CustomCode-PreProperty_Hint"), // NOI18N
                    true, false);
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            initCodeWriter.getWriter().write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }

        // 2. property setter code
        if (valueSet && !ResourceSupport.isInjectedProperty(prop)) {
	    if (setterVariable == null)
		setterVariable = getComponentInvokeString(comp, true);

            generatePropertySetter(prop, comp, setterVariable, initCodeWriter, codeData);

            if (codeData != null) { // build code data for editing
                String customCode = indentCode(initCodeWriter.extractString());
                String defaultCode;
                boolean codeCustomized = isPropertyWithCustomCode(prop);
                if (codeCustomized)
                    defaultCode = "// " + FormUtils.getBundleString("CustomCode-SubstNoValue"); // NOI18N
                else {
                    generatePropertySetter(prop, comp, setterVariable, initCodeWriter, null);
                    defaultCode = indentCode(initCodeWriter.extractString());
                }
                codeData.addGuardedBlock(defaultCode, customCode, CUSTOM_CODE_MARK, codeCustomized,
                                         prop,
                                         FormUtils.getBundleString("CustomCode-Property"), // NOI18N
                                         FormUtils.getBundleString("CustomCode-Property_Hint")); // NOI18N
            }
        }

        // 3. post-initialization code
        if (codeData != null) { // build code data for editing
            String name;
            if (prop.getWriteMethod() != null)
                name = prop.getWriteMethod().getName();
            else {
                name = prop.getName();
                if (name.indexOf('.') >= 0)
                    name = name.substring(name.lastIndexOf('.')+1);
            }
            codeData.addEditableBlock(
                    postCode, prop, 0, // preference index
                    FormUtils.getFormattedBundleString("CustomCode-PostProperty_Format", // NOI18N
                                                       new Object[] { name }),
                    FormUtils.getBundleString("CustomCode-PostProperty_Hint"), // NOI18N
                    false, true);
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            initCodeWriter.getWriter().write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }

        return true;
    }

    static boolean isPropertyWithCustomCode(Node.Property prop) {
        try {
            Object value = prop.getValue();
            return value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue
                   && ((RADConnectionPropertyEditor.RADConnectionDesignValue)value).getType()
                        == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE;
        }
        catch (Exception ex) {} // should not happen
        return false;
    }

    private void generatePropertySetter(FormProperty prop,
                                        RADComponent comp,
                                        String setterVariable,
                                        CodeWriter initCodeWriter,
                                        CustomCodeData codeData)
        throws IOException
    {
        Object value = null;
        try {
            value = prop.getValue();
        }
        catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return;
        }

        PropertyEditor currentEditor = prop.getCurrentEditor();
        if (currentEditor instanceof BeanPropertyEditor && value != null) {
            String propertyInitializationString = generatePropertyBeanInitialization(prop, value, initCodeWriter, codeData);
            if (propertyInitializationString != null) {
                generateSimpleSetterCode(prop, prop.getPartialSetterCode(propertyInitializationString),
                                         setterVariable, initCodeWriter);
            }
        } else if (currentEditor instanceof FormCodeAwareEditor) {
            if (currentEditor.getValue() != value) {
                currentEditor.setValue(value);
            }
            String code = ((FormCodeAwareEditor)currentEditor).getSourceCode();
            if (code != null) {
                initCodeWriter.write(code);
            }
        } else {
            String propValueCode = prop.getJavaInitializationString();
            if (codeData != null) // building code data for editing
                propValueCode = CUSTOM_CODE_MARK + propValueCode + CUSTOM_CODE_MARK;
            String javaStr = null;

            if ((javaStr = prop.getWholeSetterCode(propValueCode)) != null) { // button group property
                initCodeWriter.write(javaStr);
                if (!javaStr.endsWith("\n")) // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
            }
            // Mnemonics support - start -
            else if (comp != null
                     && "text".equals(prop.getName()) // NOI18N
                     && canUseMnemonics(comp) && isUsingMnemonics(comp))
            {
                if (propValueCode != null) {
                    initCodeWriter.write("org.openide.awt.Mnemonics.setLocalizedText(" // NOI18N
                        + comp.getName() + ", " + propValueCode + ");\n"); // NOI18N
                    updateProjectClassPathForMnemonics();
                }
            }
            // Mnemonics support - end -
            else if ((javaStr = prop.getPartialSetterCode(propValueCode)) != null) {
                // this is a normal property
                generateSimpleSetterCode(prop, javaStr, setterVariable, initCodeWriter);
            }
        }
    }

    private String generatePropertyBeanInitialization(FormProperty prop,
                                                      Object value,
                                                      CodeWriter initCodeWriter,
                                                      CustomCodeData codeData) throws IOException {
	Class propertyType = prop.getRealValue(value).getClass();
	prop.getCurrentEditor().setValue(value);
	BeanPropertyEditor beanPropertyEditor = (BeanPropertyEditor) prop.getCurrentEditor();
	FormProperty[] properties = (FormProperty[]) beanPropertyEditor.getProperties();
        if (properties == null) {
            return null;
        }

	CreationDescriptor.Creator creator = getPropertyCreator(propertyType, properties);
        if (creator == null) { // Issue 136252
            String message = "No Creator found for " + propertyType; // NOI18N
            if (prop instanceof RADProperty) {
                String component = ((RADProperty)prop).getRADComponent().getName();
                message += "\nCheck " + prop.getName() + " property of " + component + " component."; // NOI18N
            }
            Logger.getLogger(getClass().getName()).log(Level.WARNING, message);
            return null;
        }

	List<FormProperty> creatorProperties = getCreatorProperties(creator, properties);
	List<FormProperty> remainingProperties = null;
        List<String> subBeanPropNames = null;
        List<String> subBeanPropCodes = null;
        for (FormProperty subProp : properties) {
            if (!subProp.isChanged()) {
                continue;
            }
            if (creatorProperties.contains(subProp)) {
                PropertyEditor prEd = subProp.getCurrentEditor();
                if (prEd instanceof BeanPropertyEditor) {
                    Object val = null;
                    try {
                        val = subProp.getValue();
                    } catch (Exception ex) { // should not happen
                        Exceptions.printStackTrace(ex);
                    }
                    if (val != null) {
                        // if the sub-property is a "bean property", it may also need to generate
                        // additional property setters, then represented by a variable (propCode)
                        String propCode = generatePropertyBeanInitialization(subProp, val, initCodeWriter, null);
                        if (propCode != null) {
                            if (subBeanPropNames == null) {
                                subBeanPropNames = new ArrayList<String>();
                                subBeanPropCodes = new ArrayList<String>();
                            }
                            subBeanPropNames.add(subProp.getName());
                            subBeanPropCodes.add(propCode);
                        }
                    }
                }
            } else {
                if (remainingProperties == null) {
                    remainingProperties = new ArrayList<FormProperty>();
                }
                remainingProperties.add(subProp);
            }
        }

	String propertyInitializationString = creator.getJavaCreationCode(creatorProperties.toArray(new FormProperty[0]),
                subBeanPropNames != null ? subBeanPropNames.toArray(new String[0]) : null,
                subBeanPropCodes != null ? subBeanPropCodes.toArray(new String[0]) : null,
                prop.getValueType(),
                null);
        if (codeData != null) {
            propertyInitializationString = CUSTOM_CODE_MARK + propertyInitializationString + CUSTOM_CODE_MARK;
        }

        if (remainingProperties != null && !remainingProperties.isEmpty()) {
	    return generatePropertiesInitialization(propertyType,  propertyInitializationString, remainingProperties, initCodeWriter);
	} else {
            return propertyInitializationString;
        }
    }

    private List<FormProperty> getCreatorProperties(CreationDescriptor.Creator creator, FormProperty[] properties) {
	String[] propNames = creator.getPropertyNames();	
	List<FormProperty> creatorProperties; 
	if (propNames.length > 0) {
	    creatorProperties = new ArrayList<FormProperty>(propNames.length);		    
	    for (int i=0; i < propNames.length; i++) {
		for (int j = 0; j < properties.length; j++) {
		    if(properties[j].getName().equals(propNames[i])) {
			creatorProperties.add(properties[j]);							
			break;
		    }			    			    
		}                        
	    }
	} else {
	    creatorProperties = new ArrayList<FormProperty>(0);
	}
	return creatorProperties;
    }
    
    private CreationDescriptor.Creator getPropertyCreator(Class clazz, FormProperty[] properties) {	
	CreationDescriptor creationDesc = CreationFactory.getDescriptor(clazz);
	return (creationDesc == null) ? null : creationDesc.findBestCreator(properties,
					    // XXX CHANGED_ONLY ???
					    CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);	
    }
    
    private String generatePropertiesInitialization(Class propertyType,
                                                    String propertyInitializationString,
                                                    List<FormProperty> remainingProperties,
                                                    CodeWriter initCodeWriter) throws IOException {
	String variableName = formModel.getCodeStructure().getExternalVariableName(propertyType, null, true);
	String javaStr = propertyType.getName() + " " + variableName + " = " + propertyInitializationString; // NOI18N
	initCodeWriter.write(javaStr);
	initCodeWriter.write(";\n"); // NOI18N
	for (FormProperty p : remainingProperties) {
	    generateProperty(p, null, variableName + ".", initCodeWriter, null); // NOI18N
	}
        return variableName;
    }

    private void generateSimpleSetterCode(FormProperty prop,
				          String partialSetterCode,
	                                  String setterVariable,
				          CodeWriter initCodeWriter)
	throws IOException
    {

	// if the setter throws checked exceptions,
	// we must generate try/catch block around it.
	Class[] exceptions = null;
	Method writeMethod = prop.getWriteMethod(); 
	if (writeMethod != null) {
	    exceptions = writeMethod.getExceptionTypes();
	    if (needTryCode(exceptions))
		initCodeWriter.write("try {\n"); // NOI18N
	    else
		exceptions = null;
	}

	initCodeWriter.write(setterVariable + partialSetterCode + ";\n"); // NOI18N

	// add the catch code if needed
	if (exceptions != null)
	    generateCatchCode(exceptions, initCodeWriter.getWriter());
    }    

    // generates code for handling events of one component
    // (all component.addXXXListener() calls)
    private void generateComponentEvents(RADComponent component,
                                         CodeWriter initCodeWriter,
                                         CustomCodeData codeData)
        throws IOException
    {
        Writer writer = initCodeWriter.getWriter();

        EventSetDescriptor lastEventSetDesc = null;
        List<Event> listenerEvents = null;

        // we must deal somehow with the fact that for some (pathological)
        // events only anonymous innerclass listener can be generated
        // (CEDL cannot be used)
        int defaultMode = formModel.getSettings().getListenerGenerationStyle();
        int mode = defaultMode;
        boolean mixedMode = false;

        Event[] events = component.getKnownEvents();
        for (int i=0; i < events.length; i++) {
            Event event = events[i];
            if (!event.hasEventHandlers())
                continue;

            EventSetDescriptor eventSetDesc = event.getEventSetDescriptor();
            if (eventSetDesc != lastEventSetDesc) {
                if (lastEventSetDesc != null) {
                    // new listener encountered, generate the previous one
                    if (codeData == null)
                        generateEmptyLineIfNeeded(writer);
                    generateListenerAddCode(component, lastEventSetDesc, listenerEvents, mode, writer);
                    if (mixedMode)
                        generateListenerAddCode(component, lastEventSetDesc, listenerEvents, defaultMode, writer);
                    if (listenerEvents != null)
                        listenerEvents.clear();
                }

                lastEventSetDesc = eventSetDesc;
            }

            if (defaultMode != ANONYMOUS_INNERCLASSES && defaultMode != LAMBDAS) {
                if (mode == defaultMode) {
                    if (!event.isInCEDL())
                        mode = ANONYMOUS_INNERCLASSES;
                } else if (event.isInCEDL()) {
                    mixedMode = true;
                }
            }

            if (defaultMode == ANONYMOUS_INNERCLASSES || defaultMode == LAMBDAS || !event.isInCEDL()) {
                if (listenerEvents == null)
                    listenerEvents = new ArrayList<Event>();
                listenerEvents.add(event);
            }
        }

        if (lastEventSetDesc != null) {
            // generate the last listener
            if (codeData == null)
                generateEmptyLineIfNeeded(writer);
            generateListenerAddCode(component, lastEventSetDesc, listenerEvents, mode, writer);
            if (mixedMode)
                generateListenerAddCode(component, lastEventSetDesc, listenerEvents, defaultMode, writer);
        }

        String postCode = (String) component.getAuxValue(AUX_LISTENERS_POST);
        if (codeData != null) { // build code data for editing
            String code = initCodeWriter.extractString();
            if (code != null && !code.equals("")) // NOI18N
                codeData.addGuardedBlock(indentCode(code));
            codeData.addEditableBlock(postCode,
                                      (FormProperty) component.getSyntheticProperty(PROP_LISTENERS_POST),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PostListeners"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostListenersCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    // generates complete code for handling one listener
    // (one component.addXXXListener() call)
    private void generateListenerAddCode(RADComponent comp,
                                         EventSetDescriptor eventSetDesc,
                                         List<Event> eventList,
                                         int mode,
                                         Writer codeWriter)
        throws IOException
    {
        Method addListenerMethod = eventSetDesc.getAddListenerMethod();
        Class[] exceptions = addListenerMethod.getExceptionTypes();
        if (needTryCode(exceptions))
            codeWriter.write("try {\n"); // NOI18N
        else
            exceptions = null;

        codeWriter.write(getComponentInvokeString(comp, true));
        codeWriter.write(addListenerMethod.getName());
        codeWriter.write("("); // NOI18N

        switch (mode) {
            case ANONYMOUS_INNERCLASSES:
            case LAMBDAS:
                generateInnerClasses(codeWriter, eventSetDesc, eventList, mode == LAMBDAS);
                break;

            case CEDL_INNERCLASS:
                codeWriter.write(getListenerVariableName());
                break;

            case CEDL_MAINCLASS:
                codeWriter.write("this"); // NOI18N
                break;
        }

        codeWriter.write(");\n"); // NOI18N

        if (exceptions != null)
            generateCatchCode(exceptions, codeWriter);
    }

    private void generateInnerClasses(Writer codeWriter, EventSetDescriptor eventSetDesc, List<Event> eventList, boolean useLambdas) throws IOException {

        if (useLambdas && eventSetDesc.getListenerMethods().length == 1) {
            generateWithReferenceOrLambda(codeWriter, eventList);
        } else {
            codeWriter.write("new "); // NOI18N
            // try to find adpater to use instead of full listener impl
            Class listenerType = eventSetDesc.getListenerType();
            Class adapterClass = BeanSupport.getAdapterForListener(
                    listenerType);
            if (adapterClass != null) { // use listener adapter class
                codeWriter.write(getSourceClassName(adapterClass) + "() {\n"); // NOI18N

                for (int i = 0; i < eventList.size(); i++) {
                    Event event = eventList.get(i);
                    String[] paramNames = generateListenerMethodHeader(
                            null, event.getListenerMethod(), codeWriter);
                    generateEventHandlerCalls(event, paramNames, codeWriter, true);
                    codeWriter.write("}\n"); // NOI18N
                }
            } else { // generate full listener implementation (all methods)
                codeWriter.write(getSourceClassName(listenerType) + "() {\n"); // NOI18N

                Method[] methods = eventSetDesc.getListenerMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method m = methods[i];
                    Event event = null;
                    for (int j = 0; j < eventList.size(); j++) {
                        Event e = eventList.get(j);
                        if (m.equals(e.getListenerMethod())) {
                            event = e;
                            break;
                        }
                    }
                    String[] paramNames =
                            generateListenerMethodHeader(null, m, codeWriter);
                    if (event != null)
                        generateEventHandlerCalls(event, paramNames, codeWriter, true);
                    codeWriter.write("}\n"); // NOI18N
                }
            }
            codeWriter.write("}"); // NOI18N
        }
    }

    private void generateWithReferenceOrLambda(Writer codeWriter, List<Event> eventList) throws IOException {
        if (eventList.get(0).getEventHandlers().length == 1) {
            codeWriter.append("this::" + eventList.get(0).getEventHandlers()[0]);
        } else if (eventList.get(0).getEventHandlers().length > 1) {
            Class[] paramTypes = eventList.get(0).getListenerMethod().getParameterTypes();
            String[] paramNames = generateParamNames(paramTypes);
            String paramsString = generateParamsString(paramNames);

            codeWriter.append("(");
            codeWriter.append(paramsString);
            codeWriter.append(") -> { \n");

            for (String event : eventList.get(0).getEventHandlers()) {
                codeWriter.append(event);
                codeWriter.append("(");
                codeWriter.append(paramsString);
                codeWriter.append(");\n");
            }
            codeWriter.append("}");
        }
    }


    private String generateParamsString(String[] paramNames) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < paramNames.length; i++) {
            params.append(paramNames[i]);
            if (i + 1 < paramNames.length)
                params.append(", "); // NOI18N
        }
        return params.toString();
    }

    private RADComponent codeVariableToRADComponent(CodeVariable var) {
        RADComponent metacomp = null;
        Iterator iter = var.getAttachedExpressions().iterator();
        if (iter.hasNext()) {
            Object metaobject = ((CodeExpression)iter.next()).getOrigin().getMetaObject();
            if (metaobject instanceof RADComponent) {
                metacomp = (RADComponent)metaobject;
            }
        }
        return metacomp;
    }

    private Set<String> addFieldVariables(CodeWriter variablesWriter)
        throws IOException
    {
        Set<String> variableNames = new HashSet<String>();
        Iterator<CodeVariable> it = getSortedVariables(CodeVariable.FIELD, CodeVariable.SCOPE_MASK);

        while (it.hasNext()) {
            CodeVariable var = it.next();
            RADComponent metacomp = codeVariableToRADComponent(var);
            if (metacomp != null) {
                generateComponentFieldVariable(metacomp, variablesWriter, null);
                variableNames.add(var.getName());
            }
            // there should not be other than component variables as fields
        }

        // is there any binding?
        boolean anyBinding = false;
        for (RADComponent metacomp : formModel.getAllComponents()) {
            if (metacomp.hasBindings()) {
                anyBinding = true;
                if (bindingGroupVariable == null) {
                    bindingGroupVariable = formModel.getCodeStructure().getExternalVariableName(
                            getBindingGroupClass(), "bindingGroup", true); // NOI18N
                }
                variablesWriter.write("private " + getBindingGroupClass().getName() + " " + bindingGroupVariable + ";\n"); // NOI18N
                variableNames.add(bindingGroupVariable);
                break;
            }
        }
        if (!anyBinding) {
            bindingGroupVariable = null;
        }
        return variableNames;
    }

    private void addLocalVariables(Writer writer)
        throws IOException
    {
        Iterator<CodeVariable> it = getSortedVariables(
            CodeVariable.LOCAL | CodeVariable.EXPLICIT_DECLARATION,
            CodeVariable.SCOPE_MASK | CodeVariable.DECLARATION_MASK);

        if (it.hasNext())
            generateEmptyLineIfNeeded(writer);

        while (it.hasNext()) {
            CodeVariable var = it.next();
            if (codeVariableToRADComponent(var) == null) {
                // other than component variable (e.g. GridBagConstraints)
                writer.write(var.getDeclaration().getJavaCodeString(null, null));
                writer.write("\n"); // NOI18N
            }
        }
    }

    private void generateComponentFieldVariable(RADComponent metacomp,
                                           CodeWriter codeWriter,
                                           CustomCodeData codeData)
        throws IOException
    {
        // optimization - only properties need to go through CodeWriter
        Writer writer = codeWriter.getWriter();

        CodeVariable var = metacomp.getCodeExpression().getVariable();
        if (isFinalFieldVariable(var.getType())) { // add also creation assignment
            generateComponentCreate(metacomp, codeWriter, false, codeData);
        }
        else { // simple declaration
            generateDeclarationPre(metacomp, writer, codeData);
            writer.write(var.getDeclaration().getJavaCodeString(null, null));
            writer.write("\n"); // NOI18N

            if (codeData != null) { // build code data for editing
                String code = indentCode(codeWriter.extractString());
                codeData.addGuardedBlock(code);
            }
            generateDeclarationPost(metacomp, writer, codeData);
        }
    }

    private static boolean isFinalFieldVariable(int varType) {
        return (varType & (CodeVariable.FINAL | CodeVariable.SCOPE_MASK))
                == (CodeVariable.FINAL | CodeVariable.FIELD);
    }

    private static void generateDeclarationPre(RADComponent metacomp,
                                               Writer writer,
                                               CustomCodeData codeData)
        throws IOException
    {
        String preCode = (String) metacomp.getAuxValue(AUX_DECLARATION_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) metacomp.getSyntheticProperty(PROP_DECLARATION_PRE),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PreDeclaration"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PreDeclarationDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    private static void generateDeclarationPost(RADComponent metacomp,
                                                Writer writer,
                                                CustomCodeData codeData)
        throws IOException
    {
        String postCode = (String) metacomp.getAuxValue(AUX_DECLARATION_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) metacomp.getSyntheticProperty(PROP_DECLARATION_POST),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PostDeclaration"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostDeclarationDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    /** Adds new empty line if currentAreaNumber has raised from last time.
     * Should never be called when building "code data" for editing.
     */
    private void generateEmptyLineIfNeeded(Writer writer) throws IOException {
        if (emptyLineCounter != emptyLineRequest) {
            writer.write("\n"); // NOI18N
        }
        emptyLineCounter = emptyLineRequest;
    }

    private Iterator<CodeVariable> getSortedVariables(int type, int typeMask) {
        Collection allVariables = formModel.getCodeStructure().getAllVariables();
        List<CodeVariable> variables = new ArrayList<CodeVariable>(allVariables.size());
        Iterator it = allVariables.iterator();
        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            if (var.getDeclaredType() == org.netbeans.modules.form.Separator.class)
                continue; // treat AWT Separator specially - it is not a component
            if ((var.getType() &  typeMask) == (type & typeMask))
                variables.add(var);
        }
        variables.sort(new Comparator<CodeVariable>() {
            @Override
            public int compare(CodeVariable o1, CodeVariable o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return variables.iterator();
    }

    // Mnemonics support - start -
    static boolean canUseMnemonics(RADComponent comp) {
        return javax.swing.JLabel.class.isAssignableFrom(comp.getBeanClass())
               || javax.swing.AbstractButton.class.isAssignableFrom(comp.getBeanClass());
    }

    static boolean isUsingMnemonics(RADComponent comp) {
        Object mnem = comp.getAuxValue(PROP_GENERATE_MNEMONICS);
        if (mnem != null)
            return Boolean.TRUE.equals(mnem);

        return comp.getFormModel().getSettings().getGenerateMnemonicsCode();
    }

    private void updateProjectClassPathForMnemonics() {
        if (!mnemonicsClassPathUpdated) {
            FileObject srcFile = formEditor.getFormDataObject().getPrimaryFile();
            if (!ClassPathUtils.checkUserClass("org.openide.awt.Mnemonics", srcFile)) { // NOI18N
                Entry cpe = ClassSource.unpickle("dependency", "org.openide.awt"); // NOI18N
                if (cpe != null) {
                    try {
                        cpe.addToProjectClassPath(srcFile,  ClassPath.COMPILE);
                    } catch (Exception ex) {
                        Logger.getLogger(JavaCodeGenerator.class.getName()).log(Level.INFO, null, ex);
                    }
                }
            }
            mnemonicsClassPathUpdated = true;
        }
    }
    // Mnemonics support - end -

    private String getComponentParameterString(RADComponent component,
                                               boolean inMainClass)
    {
        if (component == formModel.getTopRADComponent())
            return inMainClass ?
                     "this" : // NOI18N
                     formEditor.getFormDataObject().getName() + ".this"; // NOI18N
        else
            return component.getName();
    }

    private String getComponentInvokeString(RADComponent component,
                                            boolean inMainClass)
    {
        if (component == formModel.getTopRADComponent())
            return inMainClass ?
                     "" : // NOI18N
                     formEditor.getFormDataObject().getName() + ".this."; // NOI18N
        else
            return component.getName() + "."; // NOI18N
    }

    static String getSourceClassName(Class cls) {
        return cls.getName().replace('$', '.').replace('+', '.').replace('/', '.'); // NOI18N
    }

    private static String getVariablesHeaderComment() {
        if (variablesHeader == null)
            variablesHeader = FormUtils.getBundleString("MSG_VariablesBegin"); // NOI18N
        return variablesHeader;
    }

    private static String getVariablesFooterComment() {
        if (variablesFooter == null)
            variablesFooter = FormUtils.getBundleString("MSG_VariablesEnd"); // NOI18N
        return variablesFooter;
    }

    private static String getEventDispatchCodeComment() {
        if (eventDispatchCodeComment == null)
            eventDispatchCodeComment = FormUtils.getBundleString("MSG_EventDispatchCodeComment"); // NOI18N
        return eventDispatchCodeComment;
    }

    private boolean needTryCode(Class[] exceptions) {
        if (exceptions != null)
            for (int i=0; i < exceptions.length; i++)
                if (Exception.class.isAssignableFrom(exceptions[i])
                    && !RuntimeException.class.isAssignableFrom(exceptions[i]))
                {
                    return true;
                }

        return false;
    }

    private void generateCatchCode(Class[] exceptions, Writer initCodeWriter)
        throws IOException
    {
        initCodeWriter.write("}"); // NOI18N
        for (int i=0, exCount=0; i < exceptions.length; i++) {
            Class exception = exceptions[i];
            if (!Exception.class.isAssignableFrom(exception)
                    || RuntimeException.class.isAssignableFrom(exception))
                continue; // need not be caught

            if (i > 0) {
                int j;
                for (j=0; j < i; j++)
                    if (exceptions[j].isAssignableFrom(exception))
                        break;
                if (j < i)
                    continue; // a subclass of this exception already caught
            }

            initCodeWriter.write(" catch ("); // NOI18N
            initCodeWriter.write(getSourceClassName(exception));
            initCodeWriter.write(" "); // NOI18N

            String varName = "e" + ++exCount; // NOI18N

            initCodeWriter.write(varName);
            initCodeWriter.write(") {\n"); // NOI18N
            initCodeWriter.write(varName);
            initCodeWriter.write(".printStackTrace();\n"); // NOI18N
            // [shouldn't return be generated here?]
            initCodeWriter.write("}"); // NOI18N
        }
        initCodeWriter.write("\n"); // NOI18N
    }

    private void addDispatchListenerDeclaration(Writer codeWriter)
        throws IOException
    {
        generateEmptyLineIfNeeded(codeWriter);

        listenerVariableName = null;
        codeWriter.write(getListenerClassName());
        codeWriter.write(" "); // NOI18N
        codeWriter.write(getListenerVariableName());
        codeWriter.write(" = new "); // NOI18N
        codeWriter.write(getListenerClassName());
        codeWriter.write("();\n"); // NOI18N
    }

    private void generateDispatchListenerCode(Writer codeWriter)
        throws IOException
    {   // always ends up with } as last character (no new line - because of fold footer)
        FormEvents formEvents = formModel.getFormEvents();
        boolean innerclass = formModel.getSettings().getListenerGenerationStyle() == CEDL_INNERCLASS;
        boolean mainclass = formModel.getSettings().getListenerGenerationStyle() == CEDL_MAINCLASS;

        Class[] listenersToImplement = formEvents.getCEDLTypes();
        Arrays.sort(listenersToImplement, new Comparator<Class>() {
            @Override
            public int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        listenersInMainClass = mainclass ? listenersToImplement : null;

        if (innerclass) {
            String lClassName = getListenerClassName();
            codeWriter.write("private class "); // NOI18N
            codeWriter.write(lClassName);
            codeWriter.write(" implements "); // NOI18N
            for (int i=0; i < listenersToImplement.length; i++) {
                codeWriter.write(getSourceClassName(listenersToImplement[i]));
                if (i + 1 < listenersToImplement.length)
                    codeWriter.write(", "); // NOI18N
            }
            codeWriter.write(" {\n"); // NOI18N
            codeWriter.write(lClassName + "() {}\n"); // NOI18N Issue 72346 resp. 15242
        }

        for (int i=0; i < listenersToImplement.length; i++) {
            boolean implementedInSuperclass =
                mainclass && listenersToImplement[i].isAssignableFrom(
                                          formModel.getFormBaseClass());

            Method[] methods = listenersToImplement[i].getMethods();
            Arrays.sort(methods, new Comparator<Method>() {
                @Override
                public int compare(Method o1, Method o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (int j=0; j < methods.length; j++) {
                Method method = methods[j];
                Event[] events = formEvents.getEventsForCEDLMethod(method);
                if (implementedInSuperclass && events.length == 0)
                    continue;

                String[] paramNames =
                    generateListenerMethodHeader(null, method, codeWriter);

                for (int k=0; k < events.length; k++) {
                    Event event = events[k];
                    if (k + 1 < events.length
                        || method.getReturnType() == Void.TYPE)
                    {                                               
			String componentParameterString = getComponentParameterString(
							    event.getComponent(), false);
			
			CodeVariable variable = event.getComponent().getCodeExpression().getVariable();                                                
			if( variable!= null && ( (variable.getType() & CodeVariable.LOCAL) == CodeVariable.LOCAL) ) {
			    codeWriter.write(
				FormUtils.getFormattedBundleString(
				    "MSG_WrongLocalVariableSettingComment", // NOI18N
				    new Object[] { componentParameterString }));
			}
			
                        codeWriter.write(k == 0 ? "if (" : "else if ("); // NOI18N
                        codeWriter.write(paramNames[0]);
                        codeWriter.write(".getSource() == "); // NOI18N
                        codeWriter.write(componentParameterString);
                        codeWriter.write(") {\n"); // NOI18N						

                        generateEventHandlerCalls(event, paramNames, codeWriter, false);
                        codeWriter.write("}\n"); // NOI18N
                        
                    }
                    else { // the listener method returns something
                        if (k > 0)
                            codeWriter.write("else {\n"); // NOI18N
                        generateEventHandlerCalls(event, paramNames, codeWriter, false);
                        if (k > 0)
                            codeWriter.write("}\n"); // NOI18N
                    }
                }
                if (implementedInSuperclass)
                    generateSuperListenerCall(method, paramNames, codeWriter);

                if (j+1 < methods.length || i+1 < listenersToImplement.length)
                    codeWriter.write("}\n\n"); // NOI18N
                else if (innerclass)
                    codeWriter.write("}\n"); // NOI18N
                else
                    codeWriter.write("}"); // last char // NOI18N
            }
        }

        if (innerclass)
            codeWriter.write("}"); // last char // NOI18N
    }

    // modifies the form class declaration to implement required listeners
    // (when event dispatching code is generated as CEDL_MAINCLASS)
    private void ensureMainClassImplementsListeners() {
        if (listenersInMainClass == listenersInMainClass_lastSet)
            return; // no change from last time

        Collection<String> toAdd;
        if (listenersInMainClass != null) {
            if (listenersInMainClass_lastSet != null
                    && listenersInMainClass.length == listenersInMainClass_lastSet.length) {
                boolean different = false;
                for (int i=0; i < listenersInMainClass.length; i++) {
                    if (listenersInMainClass[i] != listenersInMainClass_lastSet[i]) {
                        different = true;
                        break;
                    }
                }
                if (!different) {
                    return; // no change from last time
                }
            }
            toAdd = new ArrayList(listenersInMainClass.length);
            for (Class cls : listenersInMainClass) {
                toAdd.add(cls.getCanonicalName());
            }
        } else {
            toAdd = Collections.emptyList();
        }

        Collection<String> toRemove;
        if (listenersInMainClass_lastSet != null) {
            toRemove = new ArrayList(listenersInMainClass_lastSet.length);
            for (int i=0; i < listenersInMainClass_lastSet.length; i++) {
                String cls = listenersInMainClass_lastSet[i].getCanonicalName();
                if (!toAdd.contains(cls)) {
                    toRemove.add(cls);
                }
            }
        } else {
            toRemove = Collections.emptyList();
        }

        if (formEditor.getFormJavaSource().modifyInterfaces(toAdd, toRemove)) {
            listenersInMainClass_lastSet = listenersInMainClass;
        }
    }

    // ---------
    // generating general code structure (metadata from codestructure package)

    // java code for a statement
    private static String getStatementJavaString(CodeStatement statement,
                                                 String thisStr)
    {
        CodeExpression parent = statement.getParentExpression();
        String parentStr;
        if (parent != null) {
            parentStr = getExpressionJavaString(parent, thisStr);
            if ("this".equals(parentStr)) // NOI18N
                parentStr = thisStr;
        }
        else parentStr = null;

        CodeExpression[] params = statement.getStatementParameters();
        String[] paramsStr = new String[params.length];
        for (int i=0; i < params.length; i++)
            paramsStr[i] = getExpressionJavaString(params[i], thisStr);

        return statement.getJavaCodeString(parentStr, paramsStr);
    }

    // java code for an expression
    static String getExpressionJavaString(CodeExpression exp,
                                                  String thisStr)
    {
        CodeVariable var = exp.getVariable();
        if (var != null)
            return var.getName();

        CodeExpressionOrigin origin = exp.getOrigin();
        if (origin == null)
            return null;

        CodeExpression parent = origin.getParentExpression();
        String parentStr;
        if (parent != null) {
            parentStr = getExpressionJavaString(parent, thisStr);
            if ("this".equals(parentStr)) // NOI18N
                parentStr = thisStr;
        }
        else parentStr = null;

        CodeExpression[] params = origin.getCreationParameters();
        String[] paramsStr = new String[params.length];
        for (int i=0; i < params.length; i++)
            paramsStr[i] = getExpressionJavaString(params[i], thisStr);

        return origin.getJavaCodeString(parentStr, paramsStr);
    }

    // ---------
    // Events

    private boolean anyEvents() {
        return formModel.getFormEvents().hasEventsInCEDL();
    }

    private String getListenerClassName() {
        if (listenerClassName == null) {
            String initText = formEditor.getInitComponentSection().getText();
            int index = initText.lastIndexOf("private class "); // NOI18N
            if (index >= 0) {
                StringBuilder nameBuffer = new StringBuilder(16);
                index += "private class ".length(); // NOI18N

                int length = initText.length();
                while (index < length && initText.charAt(index) == ' ')
                    index++;

                int i = index;
                while (i < length && initText.charAt(i) != ' ')
                    nameBuffer.append(initText.charAt(i++));

                if (i < length)
                    listenerClassName = nameBuffer.toString();
            }

            if (listenerClassName == null) {
                javax.swing.text.Document document = formEditor.getSourcesDocument();
                try {
                    String wholeText = document.getText(0, document.getLength());
                    listenerClassName = DEFAULT_LISTENER_CLASS_NAME;
                    while (wholeText.indexOf(listenerClassName) >= 0)
                        listenerClassName = "_" + listenerClassName; // NOI18N
                }
                catch (javax.swing.text.BadLocationException ex) {} // ignore
            }

            if (listenerClassName == null)
                listenerClassName = DEFAULT_LISTENER_CLASS_NAME;
        }

        return listenerClassName;
    }

    private String getListenerVariableName() {
        if (listenerVariableName == null) {
            listenerVariableName = formModel.getCodeStructure().findFreeVariableName("formListener"); // NOI18N
        }
        return listenerVariableName;
    }

    // -----------------------------------------------------------------------------
    // Event handlers

    /** Generates the specified event handler.
     */
    private void generateEventHandler(String handlerName,
                                      Method originalMethod,
                                      String bodyText,
                                      String annotationText)
    {
        if (!initialized || !canGenerate)
            return;

        InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null && bodyText == null)
            return; // already exists, no need to generate

        IndentEngine indentEngine = FormLoaderSettings.getInstance().getUseIndentEngine()
                ? IndentEngine.find(formEditor.getSourcesDocument()) : null;
        StringWriter buffer = new StringWriter();

        try {
            if (sec == null) {
                sec = insertEvendHandlerSection(handlerName);
            }
            // optimization - only properties need to go through CodeWriter
            Writer codeWriter;
            if (indentEngine != null) { // use original indent engine
                codeWriter = indentEngine.createWriter(formEditor.getSourcesDocument(),
                                                       sec.getStartPosition().getOffset(),
                                                       buffer);
            } else {
                codeWriter = buffer;
            }

            int i0, i1, i2;

            if (annotationText != null) {
                codeWriter.write(annotationText);
                codeWriter.flush();
            }
            i0 = buffer.getBuffer().length();
            generateListenerMethodHeader(handlerName, originalMethod, codeWriter);
            codeWriter.flush();
            i1 = buffer.getBuffer().length();
            if (bodyText == null) {
                bodyText = getDefaultEventBody();
            } else if (!bodyText.endsWith("\n")) { // NOI18N
                bodyText += '\n'; // Issue 202459
            }
            codeWriter.write(bodyText);
            codeWriter.flush();
            i2 = buffer.getBuffer().length();
            codeWriter.write("}\n"); // footer with new line // NOI18N
            codeWriter.flush();

            if (i0 != 0) {
                formEditor.getSourcesDocument().insertString(sec.getStartPosition().getOffset(), annotationText, null);
            }
            String s = buffer.getBuffer().substring(i0, i1);
            if (indentEngine == null) {
                s = indentCode(s, 1);
            }
            sec.setHeader(s);
            s = buffer.getBuffer().substring(i1, i2);
            if (indentEngine == null) {
                s = indentCode(s, 2);
            }
            sec.setBody(s);
            s = buffer.getBuffer().substring(i2);
            if (indentEngine == null) {
                s = indentCode(s, 1);
            }
            sec.setFooter(s);

            codeWriter.close();
        } 
        catch (javax.swing.text.BadLocationException e) {
            return;
        }
        catch (java.io.IOException ioe) {
            return;
        }

        if (!formModel.getSettings().getGenerateFQN()) {
            importFQNs(false, false, handlerName);
        }

        clearUndo();
    }

    /** Removes the specified event handler - removes the whole method together with the user code!
     * @param handlerName The name of the event handler
     */
    private String deleteEventHandler(String handlerName, int startPos) {
        if (!initialized || !canGenerate) {
            return null;
        }
        InteriorSection section = getEventHandlerSection(handlerName);
        if (section == null) {
            return null;
        }

        Document doc = formEditor.getSourcesDocument();
        int sectionStartPos = section.getStartPosition().getOffset();
        // Fine tune the start position of method code (e.g. annotation or comment
        // added by the user) incl. spaces at the beginning of the line.
        int preCodeLength = (startPos >= 0 && startPos < sectionStartPos)
                            ? sectionStartPos - startPos : -1;
        if (preCodeLength > 0) {
            try {
                String txt = doc.getText(0, startPos);
                int i = startPos - 1;
                while (i >= 0 && (txt.charAt(i) == ' ' || txt.charAt(i) == '\t')) {
                    i--;
                }
                startPos = i + 1;
                preCodeLength = sectionStartPos - startPos;
            } catch (BadLocationException ex) {
                preCodeLength = -1; // but no reason this should happen...
            }
        }
        if (preCodeLength < 0) {
            startPos = sectionStartPos;
        }

        // if there is another guarded section right before or after without
        // a gap, the neighbor sections could merge strangely - prevent this by
        // inserting an empty line (#94165)
        int endPos = section.getEndPosition().getOffset();
        for (GuardedSection sec : formEditor.getGuardedSectionManager().getGuardedSections()) {
            if (sec.getEndPosition().getOffset()+1 == startPos) { // close section before
                try {
                    doc.insertString(startPos, "\n", null); // NOI18N
                    startPos++;
                } catch (javax.swing.text.BadLocationException ex) {} // should not happen, ignore
            } else if (sec.getStartPosition().getOffset() == endPos+1) { // close section after
                try {
                    doc.insertString(endPos+1, "\n", null); // NOI18N
                } catch (javax.swing.text.BadLocationException ex) {} // should not happen, ignore
            }
        }
        section.deleteSection();
        String preCode = null;
        if (preCodeLength > 0) {
            try {
                preCode = doc.getText(startPos, preCodeLength);
                formEditor.getSourcesDocument().remove(startPos, preCodeLength);
            } catch (BadLocationException ex) {} // should not happen, ignore
        }
        clearUndo();
        return preCode;
    }

    private String getDefaultEventBody() {
        return " " + FormUtils.getBundleString("MSG_EventHandlerBody"); // NOI18N
    }

    /** Renames the specified event handler to the given new name.
     * @param oldHandlerName The old name of the event handler
     * @param newHandlerName The new name of the event handler
     */
    private void renameEventHandler(String oldHandlerName,
                                    String newHandlerName)
    {
        if (!initialized || !canGenerate) {
            return;
        }
        InteriorSection sec = getEventHandlerSection(oldHandlerName);
        if (sec == null) {
            return;
        }

        // find the old handler name in the handler method header and replace
        // it with the new name
        String header = sec.getHeader();
        int index = header.indexOf('(');
        if (index < 0) {
            return; // should not happen unless the handler code is corrupted
        }
        index = header.substring(0, index).lastIndexOf(oldHandlerName);
        if (index < 0) {
            return; // old name not found; should not happen
        }
        try {
            sec.setHeader(header.substring(0, index) + newHandlerName + header.substring(index + oldHandlerName.length()));
            sec.setName(getEventSectionName(newHandlerName));
        } 
        catch (java.beans.PropertyVetoException e) {
            return;
        }

        clearUndo();
    }

    /** Focuses the specified event handler in the editor. */
    private void gotoEventHandler(String handlerName) {
        if (initialized) {
            InteriorSection sec = getEventHandlerSection(handlerName);
            if (sec != null) {
                formEditorSupport.openAt(sec.getCaretPosition());
            }
        }
    }

    /** Gets the body (text) of event handler of given name. */
    String getEventHandlerText(String handlerName) {
        InteriorSection section = getEventHandlerSection(handlerName);
        return (section == null) ? null : section.getBody();
    }

    // ------------------------------------------------------------------------------------------
    // Private methods

    /** Clears undo buffer after code generation */
    private void clearUndo() {
        formEditorSupport.discardEditorUndoableEdits();
    }

    // sections acquirement

    private InteriorSection getEventHandlerSection(String handlerName) {
        return formEditor.getGuardedSectionManager().findInteriorSection(getEventSectionName(handlerName));
    }

    private InteriorSection insertEvendHandlerSection(String handlerName) throws javax.swing.text.BadLocationException {
        int endPos = formEditor.getInitComponentSection().getEndPosition().getOffset();
        // find last event handler
        for (GuardedSection sec : formEditor.getGuardedSectionManager().getGuardedSections()) {
            if (sec instanceof InteriorSection) {
                int pos = sec.getEndPosition().getOffset();
                if (pos > endPos) {
                    endPos = pos;
                }
            }
        }
        // if there is another guarded section following with no gap, insert empty line (#109242)
        for (GuardedSection sec : formEditor.getGuardedSectionManager().getGuardedSections()) {
            if (sec.getStartPosition().getOffset() == endPos + 1) {
                formEditor.getSourcesDocument().insertString(endPos+1, "\n", null); // NOI18N
                break;
            }
        }
        // create the new guarded section
        return formEditor.getGuardedSectionManager().createInteriorSection(
                formEditor.getSourcesDocument().createPosition(endPos + 1),
                getEventSectionName(handlerName));
    }

    // other

    private String getEventSectionName(String handlerName) {
        return EVT_SECTION_PREFIX + handlerName;
    }

    private String[] generateListenerMethodHeader(String methodName,
                                                  Method originalMethod,
                                                  Writer writer)
        throws IOException
    {
        Class[] paramTypes = originalMethod.getParameterTypes();
        String[] paramNames = generateParamNames(paramTypes);

        // generate the method
        writer.write(methodName != null ? "private " : "public "); // NOI18N
        writer.write(getSourceClassName(originalMethod.getReturnType()));
        writer.write(" "); // NOI18N
        writer.write(methodName != null ? methodName : originalMethod.getName());
        writer.write("("); // NOI18N

        for (int i=0; i < paramTypes.length; i++) {
            writer.write(getSourceClassName(paramTypes[i]));
            writer.write(" "); // NOI18N
            writer.write(paramNames[i]);
            if (i + 1 < paramTypes.length)
                writer.write(", "); // NOI18N
        }
        writer.write(")"); // NOI18N

        Class[] exceptions = originalMethod.getExceptionTypes();
        if (exceptions.length != 0) {
            writer.write("throws "); // NOI18N
            for (int i=0; i < exceptions.length; i++) {
                writer.write(getSourceClassName(exceptions[i]));
                if (i + 1 < exceptions.length)
                    writer.write(", "); // NOI18N
            }
        }

        writer.write(" {\n"); // NOI18N

        return paramNames;
    }

    private String[] generateParamNames(Class[] paramTypes){
        String[] paramNames;
        if (paramTypes.length == 1
                && EventObject.class.isAssignableFrom(paramTypes[0]))
        {
            paramNames = new String[] { EVT_VARIABLE_NAME };
        }
        else {
            paramNames = new String[paramTypes.length];
            for (int i=0; i < paramTypes.length; i++)
                paramNames[i] = "param" + i; // NOI18N
        }
        return paramNames;
    }

    private void generateSuperListenerCall(Method method,
                                           String[] paramNames,
                                           Writer codeWriter)
        throws IOException
    {
        if (method.getReturnType() != Void.TYPE)
            codeWriter.write("return "); // NOI18N

        codeWriter.write("super."); // NOI18N
        codeWriter.write(method.getName());
        codeWriter.write("("); // NOI18N

        for (int i=0; i < paramNames.length; i++) {
            codeWriter.write(paramNames[i]);
            if (i + 1 < paramNames.length)
                codeWriter.write(", "); // NOI18N
        }

        codeWriter.write(");\n"); // NOI18N
    }

    private void generateEventHandlerCalls(Event event,
                                           String[] paramNames,
                                           Writer codeWriter,
                                           boolean useShortNameIfPossible)
        throws IOException
    {
        String mainClassRef = null;

        String[] handlers = event.getEventHandlers();
        for (int i=0; i < handlers.length; i++) {
            if (i + 1 == handlers.length
                    && event.getListenerMethod().getReturnType() != Void.TYPE)
                codeWriter.write("return "); // NOI18N

            // with anonymous innerclasses, try to avoid generating full names
            // (for the reason some old forms might be used as innerclasses)
            if (!useShortNameIfPossible
                || event.getListenerMethod().getName().equals(handlers[i]))
            {
                if (mainClassRef == null)
                    mainClassRef = formEditor.getFormDataObject().getName()
                                   + ".this."; // NOI18N
                codeWriter.write(mainClassRef);
            }
            codeWriter.write(handlers[i]);
            codeWriter.write("("); // NOI18N

            for (int j=0; j < paramNames.length; j++) {
                codeWriter.write(paramNames[j]);
                if (j + 1 < paramNames.length)
                    codeWriter.write(", "); // NOI18N
            }

            codeWriter.write(");\n"); // NOI18N
        }
    }

    @Override
    public void regenerateCode() {
        if (!codeUpToDate) {	    
            codeUpToDate = true;
            refreshFormattingSettings();
            regenerateVariables();
            regenerateInitComponents();
            if (!formModel.getSettings().getGenerateFQN()) {
                importFQNs(true, true);
                clearUndo();
            }
            ensureMainClassImplementsListeners();            
            FormModel.t("code regenerated"); // NOI18N	    
        }
    }

    private void refreshFormattingSettings() {
        indentSize = formEditorSupport.getCodeIndentSize();
        braceOnNewLine = formEditorSupport.getCodeBraceOnNewLine();
    }

    private void importFQNs(boolean handleInitComponents, boolean handleVariables, String... eventHandlers) {
        List<int[]> list = new ArrayList<>();
        if (handleInitComponents) {
            SimpleSection initComponentsSection = formEditor.getInitComponentSection();
            int[] span = formEditor.getFormJavaSource().getMethodSpan("initComponents"); // NOI18N
            if (span == null) {
                return;
            }
            list.add(new int[] { span[0], initComponentsSection.getEndPosition().getOffset() });
            // also includes the listener class if generated
        }
        if (handleVariables) {
            SimpleSection variablesSection = formEditor.getVariablesSection();
            list.add(new int[] { variablesSection.getStartPosition().getOffset(),
                                 variablesSection.getEndPosition().getOffset() });
        }
        if (eventHandlers != null) {
            for (String handlerName : eventHandlers) {
                int[] range = getEventHandlerFQNImportRange(handlerName);
                if (range != null) {
                    list.add(range);
                }
            }
        }
        int [][] ranges = list.toArray(new int[0][]);
        formEditor.getFormJavaSource().importFQNs(ranges);
    }

    private int[] getEventHandlerFQNImportRange(String handlerName) {
        InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null) {
            int start = sec.getStartPosition().getOffset();
            String header = sec.getHeader();
            int i1 = header.indexOf('(');
            int i2 = header.lastIndexOf(')');
            if (i1 >= 0 && i2 > i1+1) {
                return new int[] { start+i1+1, start+i2 };
            }
        }
        return null;
    }

    static CustomCodeData getCodeData(RADComponent metacomp) {
        CodeGenerator gen = FormEditor.getCodeGenerator(metacomp.getFormModel());
        return gen instanceof JavaCodeGenerator ?
            ((JavaCodeGenerator)gen).getCodeData0(metacomp) : null;
    }

    private CustomCodeData getCodeData0(RADComponent metacomp) {
        CustomCodeData codeData = new CustomCodeData();
        codeData.setDefaultCategory(CustomCodeData.CodeCategory.CREATE_AND_INIT);
        CodeWriter codeWriter = new CodeWriter(new StringWriter(1024), true);
        cleanup();

        CodeVariable var = metacomp.getCodeExpression().getVariable();

        try { // creation & init code
            if (var != null && !isFinalFieldVariable(var.getType()))
                generateComponentCreate(metacomp, codeWriter, true, codeData);
            // with final field variable the creation statement is part of declaration

            addInitCode(metacomp, codeWriter, codeData);

            if (var != null) { // add declaration
                boolean fieldVariable = (var.getType() & CodeVariable.SCOPE_MASK) == CodeVariable.FIELD;
                if (fieldVariable) {
                    codeData.setDefaultCategory(CustomCodeData.CodeCategory.DECLARATION);
                    generateComponentFieldVariable(metacomp, codeWriter, codeData);
                }
                codeData.setDeclarationData(!fieldVariable, var.getType() & CodeVariable.ALL_MODIF_MASK);
            }
        }
        catch (IOException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        cleanup();

        return codeData;
    }

    private String indentCode(String code) {
        return indentCode(code, 0);
    }

    private String indentCode(String code, int minIndentLevel) {
        StringBuilder tab = new StringBuilder(indentSize);
        for (int i=0; i < indentSize; i++) {
            tab.append(" "); // NOI18N
        }
        return doIndentation(code, minIndentLevel, tab.toString(), braceOnNewLine);
    }
    
    // simple indentation method
    private static String doIndentation(String code,
            int minIndentLevel,
            String tab,
            boolean braceOnNewLine) {

        int indentLevel = minIndentLevel;
        boolean lastLineEmpty = false;
        int codeLength = code.length();
        StringBuilder buffer = new StringBuilder(codeLength);
        
        int i = 0;
        while (i < codeLength) {
            int lineStart = i;
            int lineEnd;
            boolean startingSpace = true;
            boolean firstClosingBr = false;
            boolean closingBr = false;
            int lastOpeningBr = -1;
            int endingSpace = -1;
            boolean insideString = false;
            int brackets = 0;
            char c;
            
            do { // go through one line
                c = code.charAt(i);
                if (!insideString) {
                    if (c == '}') {
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        } else if (!closingBr)
                            brackets--;
                    } else if (c == ')') {
                        
                        int bracketCount = 0;
                        int begin = i;
                        while (begin < code.length() && (code.charAt(begin) == ')')) {
                            bracketCount += 1;
                            begin += 1;
                        }
                        
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        } else if (!closingBr)
                            brackets -= bracketCount;
                        if (bracketCount > 1) {
                            i += bracketCount - 1;
                        }
                    } else if (c == '{' || c == '(') {
                        closingBr = false;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        } else if (c == '{') // possible last brace on the line
                            lastOpeningBr = i;
                        brackets++;
                    } else if (c == '\"') { // start of String, its content is ignored
                        insideString = true;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        
                    } else if (c == ' ' || c == '\t') {
                        if (endingSpace < 0)
                            endingSpace = i;
                    } else {
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        if (c != '\n') { // this char is not a whitespace
                            endingSpace = -1;
                            if (lastOpeningBr > -1)
                                lastOpeningBr = -1;
                        }
                    }
                } else if (c == '\"' && code.charAt(i-1) != '\\') // end of String
                    insideString = false;
                
                i++;
            }
            while (c != '\n' && i < codeLength);
            
            if ((i-1 == lineStart && code.charAt(lineStart) == '\n')
            || (i-2 == lineStart && code.charAt(lineStart) == '\r')) {
                // the line is empty
                if (!lastLineEmpty) {
                    buffer.append("\n"); // NOI18N
                    lastLineEmpty = true;
                }
                continue; // skip second and more empty lines
            } else lastLineEmpty = false;
            
            // adjust indentation level for the line
            if (firstClosingBr) { // the line starts with } or )
                if (indentLevel > minIndentLevel)
                    indentLevel--;
                if (brackets < 0)
                    brackets = 0; // don't change indentation for the next line
            }
            
            // write indentation space
            for (int j=0; j < indentLevel; j++)
                buffer.append(tab);
            
            if (lastOpeningBr > -1 && braceOnNewLine) {
                // write the line without last opening brace
                // (indentation option "Add New Line Before Brace")
                endingSpace = lastOpeningBr;
                c = code.charAt(endingSpace-1);
                while (c == ' ' || c == '\t') {
                    endingSpace--;
                    c = code.charAt(endingSpace-1);
                }
                i = lastOpeningBr;
                brackets = 0;
            }
            
            // calculate line end
            if (endingSpace < 0) {
                if (c == '\n')
                    if (code.charAt(i-2) == '\r')
                        lineEnd = i-2; // \r\n at the end of the line
                    else
                        lineEnd = i-1; // \n at the end of the line
                else
                    lineEnd = i; // end of whole code string
            } else // skip spaces at the end of the line
                lineEnd = endingSpace;
            
            // write the line
            buffer.append(code.substring(lineStart, lineEnd));
            buffer.append("\n"); // NOI18N
            
            // calculate indentation level for the next line
            if (brackets < 0) {
                if (indentLevel > minIndentLevel)
                    indentLevel += brackets;
                
            } else if (brackets > 0)
                indentLevel++;
        }
        return buffer.toString();
    }

    /**
     * Class for filtering generated code - processing special marks in the code
     * (provided by properties/property editors).
     * This way (e.g.) code for ResourceBundle.getBundle can be optimized
     * (caching the bundle in a variable) or line comments for property setters
     * placed correctly.
     * [In future pre-init and post-init code could be done this way as well
     *  (and it would work also for nested properties or layout constraints).]
     * To work correctly, the write method requires to be given complete
     * statements as they appear in initComponents(), so it can add a preceding
     * or following statement).
     */
    private class CodeWriter {
        private Writer writer;
        private boolean inMethod;

        CodeWriter(Writer writer, boolean method) {
            this.writer = writer;
            this.inMethod = method;
        }

        void write(String str) throws IOException {
            int idx = str.indexOf(CODE_MARK);
            if (idx >= 0) {
                StringBuilder buf = new StringBuilder(str.length());
                if (idx > 0) {
                    buf.append(str.substring(0, idx));
                }
                String lineComment = null;
                String codeToSubst = null;
                String varType = null;

                do {
                    String part;
                    if (str.startsWith(CODE_MARK_LINE_COMMENT, idx)) {
                        // line comment to be added at the end of the line
                        int sub = idx + CODE_MARK_LINE_COMMENT.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        String lc = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                        if (lineComment == null) {
                            lineComment = lc;
                        } else if (!lineComment.equals(lc)) {
                            lineComment = lineComment + " " + lc; // NOI18N
                        }
                        continue;
                    } else if (str.startsWith(CODE_MARK_VARIABLE_SUBST, idx)) {
                        // there is a code that can be cached in a local varaible to
                        // avoid calling it multiple times (e.g. ResourceBundle.getBundle)
                        int sub = idx + CODE_MARK_VARIABLE_SUBST.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        String s = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                        part = null;
                        if (codeToSubst == null) {
                            codeToSubst = s;
                        } else if (varType == null) {
                            varType = s;
                        } else {
                            if (inMethod) {
                                part = replaceCodeWithVariable(codeToSubst, varType, s, lineComment, writer);
                            } else { // can't replace when in member field variable init
                                part = codeToSubst;
                            }
                            codeToSubst = varType = null;
                        }
                        if (part == null) { // no real code in this fragment
                            continue;
                        }
                    } else if (str.startsWith(CODE_MARK_END, idx)) { // plain code follows
                        int sub = idx + CODE_MARK_END.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        part = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                    } else {
                        int sub = idx;
                        idx = str.indexOf(CODE_MARK, sub);
                        part = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                    }
                    if (lineComment != null) {
                        int eol = part.indexOf('\n');
                        if (eol >= 0) {
                            buf.append(part.substring(0, eol));
                            buf.append(" // "); // NOI18N
                            buf.append(lineComment);
                            buf.append("\n"); // NOI18N
                            part = part.substring(eol+1);
                            lineComment = null;
                        }
                    }
                    buf.append(part);
                }
                while (idx >= 0);

                if (lineComment != null) {
                    buf.append(" // "); // NOI18N
                    buf.append(lineComment);
                }

                str = buf.toString();
            }
            writer.write(str);
        }

        Writer getWriter() {
            return writer;
        }

        void clearBuffer() {
            if (writer instanceof StringWriter) {
                StringBuffer buf = ((StringWriter)writer).getBuffer();
                buf.delete(0, buf.length());
            }
        }

        public String extractString() {
            String str = writer.toString();
            clearBuffer();
            return str;
        }
    }

    private String replaceCodeWithVariable(String codeToSubst, String varType, String varName,
                                           String lineComment,
                                           Writer writer)
        throws IOException 
    {
        String variable;
        if (repeatedCodeVariables != null) {
            variable = repeatedCodeVariables.get(codeToSubst);
        } else {
            repeatedCodeVariables = new HashMap<String,String>();
            variable = null;
        }
        if (variable == null) {
            Class type;
            try {
                type = FormUtils.loadClass(varType, formModel);
            } catch (Exception ex) { // should not happen - load things like ResourceBundle or ResourceMap
                Logger.getLogger(getClass().getName()).log(Level.INFO, "", ex); // NOI18N
                return codeToSubst;
            } catch (LinkageError ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "", ex); // NOI18N
                return codeToSubst;
            }
            variable = formModel.getCodeStructure().getExternalVariableName(type, varName, true);
            repeatedCodeVariables.put(codeToSubst, variable);
            // add varaible declaration
            writer.write(varType);
            writer.write(" "); // NOI18N
            writer.write(variable);
            writer.write(" = "); // NOI18N
            writer.write(codeToSubst);
            writer.write(";"); // NOI18N
            if (lineComment != null) {
                writer.write(" // "); // NOI18N
                writer.write(lineComment);
            }
            writer.write("\n"); // NOI18N
        }
        return variable;
    }

    //
    // {{{ FormListener
    //

    private class FormListener implements FormModelListener {

        @Override
        public void formChanged(FormModelEvent[] events) {
            if (events == null)
                return;

            boolean modifying = false;
            boolean toBeSaved = false;
            boolean toBeClosed = false;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];

                // form loaded
                if (ev.getChangeType() == FormModelEvent.FORM_LOADED) {
                    if (formModel.getSettings().getListenerGenerationStyle() == CEDL_MAINCLASS) {
                        if (FormEditor.getFormEditor(formModel).needPostCreationUpdate()) {
                            listenersInMainClass_lastSet = new Class[0];
                        } else {
                            listenersInMainClass_lastSet = formModel.getFormEvents().getCEDLTypes();
                        }
                    }
                    continue;
                }

                if (ev.isModifying())
                    modifying = true;

                if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_ADDED) {
                    if (canGenerate) {
                        String handlerName = ev.getEventHandler();
                        String bodyText = ev.getEventHandlerContent();
                        String annotationText = ev.getEventHandlerAnnotation();
                        if ((ev.getCreatedDeleted() || bodyText != null) && ev.getComponent().isInModel()) {
                            if (!ev.getCreatedDeleted()) {
                                ev.setEventHandlerContent(getEventHandlerText(handlerName));
                            }
                            refreshFormattingSettings();
                            generateEventHandler(handlerName,
                                                (ev.getComponentEvent() == null) ?
                                                    formModel.getFormEvents().getOriginalListenerMethod(handlerName) :
                                                    ev.getComponentEvent().getListenerMethod(),
                                                 bodyText, annotationText);
                        }
                        if (events.length == 1 && bodyText == null)
                            gotoEventHandler(handlerName);
                    }
                }
                else if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_REMOVED) {
                    if (canGenerate && ev.getCreatedDeleted()) {
                        String handlerName = ev.getEventHandler();
                        ev.setEventHandlerContent(getEventHandlerText(handlerName));
                        int[] span = formEditor.getFormJavaSource().getEventHandlerMethodSpan(
                                handlerName, ev.getComponentEvent().getEventParameterType());
                        String preMethodCode = deleteEventHandler(handlerName, span != null ? span[0] : -1);
                        ev.setEventHandlerAnnotation(preMethodCode);
                    }
                }
                else if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_RENAMED) {
                    renameEventHandler(ev.getOldEventHandler(),
                                       ev.getNewEventHandler());
                }
                else if (ev.getChangeType() == FormModelEvent.FORM_TO_BE_SAVED)
                    toBeSaved = true;
                else if (ev.getChangeType() == FormModelEvent.FORM_TO_BE_CLOSED)
                    toBeClosed = true;
            }

            if (modifying)
                codeUpToDate = false;

            if ((!codeUpToDate && toBeSaved) || (formEditorSupport.isJavaEditorDisplayed())) {
		regenerateCode();
            }

            if (toBeSaved) {
                RADComponent[] components =
                    formModel.getModelContainer().getSubBeans();
                for (int i=0; i < components.length; i++)
                    serializeComponentsRecursively(components[i]);
            }
        }
        
        private void serializeComponentsRecursively(RADComponent comp) {
            Object value = comp.getAuxValue(AUX_CODE_GENERATION);
            if (comp.hasHiddenState()
                    || (value != null && VALUE_SERIALIZE.equals(value))) {
                String serializeTo =(String)comp.getAuxValue(AUX_SERIALIZE_TO);
                if (serializeTo != null) {
                    try {
                        FileObject fo = formEditor.getFormDataObject().getPrimaryFile();
                        FileObject serFile = fo.getParent().getFileObject(serializeTo, "ser"); // NOI18N
                        if (serFile == null) {
                            serFile = fo.getParent().createData(serializeTo, "ser"); // NOI18N
                        }
                        if (serFile != null) {
                            FileLock lock = null;
                            ObjectOutputStream oos = null;
                            try {
                                lock = serFile.lock();
                                oos = new OOS(serFile.getOutputStream(lock));
                                if (comp instanceof RADVisualContainer) {
                                    // [PENDING - remove temporarily the subcomponents]
                                }
                                oos.writeObject(comp.getBeanInstance());
                            } finally {
                                if (oos != null) oos.close();
                                if (lock != null) lock.releaseLock();
                            }
                        } else {
                            // [PENDING - handle problem]
                        }
                    } catch (java.io.NotSerializableException e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    } catch (Exception e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    }
                } else {
                    // [PENDING - notify error]
                }
            }
            if (comp instanceof ComponentContainer) {
                RADComponent[] children =((ComponentContainer)comp).getSubBeans();
                for (int i = 0; i < children.length; i++) {
                    serializeComponentsRecursively(children[i]);
                }
            }
        }
    }

    // hacked ObjectOutputStream - to replace special values used by property
    // editors (like SuperColor from ColorEditor or NbImageIcon from IconEditor)
    private static class OOS extends ObjectOutputStream {
        OOS(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
            if (obj.getClass().getName().startsWith("org.netbeans.") // NOI18N
                || obj.getClass().getName().startsWith("org.openide.")) // NOI18N
            {
                if (obj instanceof java.awt.Color)
                    return new java.awt.Color(((java.awt.Color)obj).getRGB());
                if (obj instanceof javax.swing.ImageIcon)
                    return new javax.swing.ImageIcon(
                        ((javax.swing.ImageIcon)obj).getImage());
            }
            return obj;
        }
    }

    //
    // {{{ CodeGenerateEditor
    //

    public static final class CodeGenerateEditor extends PropertyEditorSupport
    {
        private RADComponent component;

        /** Display Names for alignment. */
        private static final String generateName =
            FormUtils.getBundleString("VALUE_codeGen_generate"); // NOI18N
        private static final String serializeName =
            FormUtils.getBundleString("VALUE_codeGen_serialize"); // NOI18N

        public CodeGenerateEditor(RADComponent component) {
            this.component = component;
        }

        /** @return names of the possible directions */
        @Override
        public String[] getTags() {
            if (component.hasHiddenState()) {
                return new String[] { serializeName } ;
            } else {
                return new String[] { generateName, serializeName } ;
            }
        }

        /** @return text for the current value */
        @Override
        public String getAsText() {
            Integer value =(Integer)getValue();
            if (value.equals(VALUE_SERIALIZE)) return serializeName;
            else return generateName;
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        @Override
        public void setAsText(String str) {
            if (component.hasHiddenState()) {
                setValue(VALUE_SERIALIZE);
            } else {
                if (serializeName.equals(str)) {
                    setValue(VALUE_SERIALIZE);
                } else if (generateName.equals(str)) {
                    setValue(VALUE_GENERATE_CODE);
                }
            }
        }
    }

    // }}}

    //
    // {{{ CodeProperty
    //

    class CodeProperty extends FormProperty {
        // using FormProperty to be able to disable change firing for temporary
        // changes in CodeCustomizer
        private String auxKey;
        private RADComponent component;
        private FormModel.FormVersion versionLevel;

        CodeProperty(RADComponent metacomp,
                     String propertyName, String auxKey ,
                     String displayName, String shortDescription,
                     FormModel.FormVersion versionLevel)
        {
            super(propertyName, String.class, displayName, null);
            setShortDescription(shortDescription); // FormProperty adds the type to the tooltip
            this.auxKey = auxKey;
            component = metacomp;
            this.versionLevel = versionLevel;
            try {
                reinstateProperty();
            }
            catch (Exception ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        @Override
        public void setTargetValue(Object value) {
            if (value != null && !(value instanceof String))
                throw new IllegalArgumentException();

            if (value != null && !value.equals("")) { // NOI18N
                component.setAuxValue(auxKey, value);
                component.getFormModel().raiseVersionLevel(versionLevel, versionLevel);
            } else if (component.getAuxValue(auxKey) != null) {
                component.getAuxValues().remove(auxKey);
            }
        }

        @Override
        public Object getTargetValue() {
            Object value = component.getAuxValue(auxKey);
            if (value == null)
                value = ""; // NOI18N
            return value;
        }

        @Override
        public boolean supportsDefaultValue () {
            return true;
        }

        @Override
        public Object getDefaultValue() {
            return ""; // NOI18N
        }

        @Override
        protected void propertyValueChanged(Object old, Object current) {
            super.propertyValueChanged(old, current);
            if (isChangeFiring()) {
                formModel.fireSyntheticPropertyChanged(
                    component, getName(), old, current);
                if (component.getNodeReference() != null) {
                    component.getNodeReference().firePropertyChangeHelper(
                        getName(), null, null);
                }
            }
        }

        @Override
        public PropertyEditor getExpliciteEditor() {
            return new CodeEditor();
        }

        @Override
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !formModel.isReadOnly();
        }
    }

    private class CodeEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv env;

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }

        @Override
        public Component getCustomEditor() {
            return new CustomCodeEditor(this, env, FormEditor.createCodeEditorPane(formModel));
        }

        @Override
        public boolean supportsCustomEditor() {
            return !formModel.isReadOnly();
        }
    }

    // }}}
    
    // Properties
    
    private class VariablesModifierProperty extends PropertySupport.ReadWrite {
        
        private VariablesModifierProperty() {
            super(PROP_VARIABLE_MODIFIER,
                Integer.class,
                FormUtils.getBundleString("PROP_VARIABLES_MODIFIER"), // NOI18N
                FormUtils.getBundleString("HINT_VARIABLES_MODIFIER")); // NOI18N
            setValue("changeImmediate", Boolean.FALSE); // NOI18N
        }
            
        @Override
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            int varType;
            int variablesModifier = newValue.intValue();
            if (formModel.getSettings().getVariablesLocal()) {
                varType = CodeVariable.LOCAL | (variablesModifier & CodeVariable.FINAL); // | CodeVariable.EXPLICIT_DECLARATION;
            } else varType = CodeVariable.FIELD | variablesModifier;

            formModel.getCodeStructure().setDefaultVariableType(varType);
            formModel.getSettings().setVariablesModifier(variablesModifier);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_MODIFIER, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.getFormRootNode().firePropertyChangeHelper(
                PROP_VARIABLE_MODIFIER, oldValue, newValue);
        }
        
        @Override
        public Object getValue() {
            return Integer.valueOf(formModel.getSettings().getVariablesModifier());
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }
        
        @Override
        public void restoreDefaultValue() {
            setValue(Integer.valueOf(FormLoaderSettings.getInstance().getVariablesModifier()));
        }
        
        @Override
        public boolean isDefaultValue() {
            return (formModel.getSettings().getVariablesModifier() ==
                FormLoaderSettings.getInstance().getVariablesModifier());
        }
        
        @Override
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            boolean local = formModel.getSettings().getVariablesLocal();
            return local ? new ModifierEditor(Modifier.FINAL) :
                new ModifierEditor(Modifier.PUBLIC
                    | Modifier.PROTECTED
                    | Modifier.PRIVATE
                    | Modifier.STATIC
                    | Modifier.FINAL
                    | Modifier.TRANSIENT
                    | Modifier.VOLATILE);
        }
        
    }
    
    private class LocalVariablesProperty extends PropertySupport.ReadWrite {

        private LocalVariablesProperty() {
            super(PROP_VARIABLE_LOCAL,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_VARIABLES_LOCAL"), // NOI18N
                FormUtils.getBundleString("HINT_VARIABLES_LOCAL")); // NOI18N
        }
        
        @Override
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();            
            if (value.equals(getValue())) return;
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            FormSettings formSettings = formModel.getSettings();
            boolean variablesLocal = newValue.booleanValue();
            int variablesModifier = variablesLocal ? (formSettings.getVariablesModifier() & CodeVariable.FINAL)
                : formSettings.getVariablesModifier();
            Integer oldModif = Integer.valueOf(formModel.getSettings().getVariablesModifier());
            Integer newModif = Integer.valueOf(variablesModifier);
            int varType = variablesLocal ?
                CodeVariable.LOCAL | variablesModifier // | CodeVariable.EXPLICIT_DECLARATION
                : CodeVariable.FIELD | variablesModifier;

            formModel.getCodeStructure().setDefaultVariableType(varType);
            formSettings.setVariablesLocal(variablesLocal);
            formSettings.setVariablesModifier(variablesModifier);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_LOCAL, oldValue, newValue);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_MODIFIER, oldModif, newModif);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            FormNode formRootNode = formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_LOCAL, oldValue, newValue);
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_MODIFIER, oldModif, newModif);
        }
        
        @Override
        public Object getValue() {
            return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
        }
        
        @Override
        public boolean supportsDefaultValue() {
            return true;
        }
        
        @Override
        public void restoreDefaultValue() {
            setValue(Boolean.valueOf(FormLoaderSettings.getInstance().getVariablesLocal()));
        }
        
        @Override
        public boolean isDefaultValue() {
            return (formModel.getSettings().getVariablesLocal() == 
                FormLoaderSettings.getInstance().getVariablesLocal());
        }
        
        @Override
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
    }
    
    private class GenerateMnemonicsCodeProperty extends PropertySupport.ReadWrite {
        private boolean writable = JavaCodeGenerator.this.canGenerate
                                   && !JavaCodeGenerator.this.formModel.isReadOnly()
                // don't allow turing mnemonics on if not supported (and not already on)
                                   && (formModel.getSettings().getGenerateMnemonicsCode()
                                       || formEditorSupport.canGenerateNBMnemonicsCode());

        private GenerateMnemonicsCodeProperty() {
            super(PROP_GENERATE_MNEMONICS,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_GENERATE_MNEMONICS"), // NOI18N
                FormUtils.getBundleString("HINT_GENERATE_MNEMONICS2")); // NOI18N
        }
            
        @Override
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            formModel.getSettings().setGenerateMnemonicsCode(newValue.booleanValue());
            formModel.fireSyntheticPropertyChanged(null, PROP_GENERATE_MNEMONICS, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.getFormRootNode().firePropertyChangeHelper(
                PROP_GENERATE_MNEMONICS, oldValue, newValue);
        }
        
        @Override
        public Object getValue() {
            return Boolean.valueOf(formModel.getSettings().getGenerateMnemonicsCode());
        }
        
        @Override
        public boolean canWrite() {
            return writable;
        }

        @Override
        public boolean supportsDefaultValue() {
            return false;
        }
    }

    private class ListenerGenerationStyleProperty extends PropertySupport.ReadWrite {

        private ListenerGenerationStyleProperty() {
            super(PROP_LISTENER_GENERATION_STYLE,
                Integer.class,
                FormUtils.getBundleString("PROP_LISTENER_GENERATION_STYLE"), // NOI18N
                FormUtils.getBundleString("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
        }
            
        @Override
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            formModel.getSettings().setListenerGenerationStyle(newValue.intValue());
            formModel.fireSyntheticPropertyChanged(null, PROP_LISTENER_GENERATION_STYLE, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.getFormRootNode().firePropertyChangeHelper(
                PROP_LISTENER_GENERATION_STYLE, oldValue, newValue);
        }
        
        @Override
        public Object getValue() {
            return Integer.valueOf(formModel.getSettings().getListenerGenerationStyle());
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }
        
        @Override
        public void restoreDefaultValue() {
            setValue(Integer.valueOf(FormLoaderSettings.getInstance().getListenerGenerationStyle()));
        }
        
        @Override
        public boolean isDefaultValue() {
            return (formModel.getSettings().getListenerGenerationStyle() ==
                FormLoaderSettings.getInstance().getListenerGenerationStyle());
        }
        
        @Override
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new ListenerGenerationStyleEditor();
        }
        
    }

    // analogical to ListenerGenerationStyleProperty ...
    private class LayoutCodeTargetProperty extends PropertySupport.ReadWrite {
        
        private LayoutCodeTargetProperty() {
            super(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET,
                Integer.class,
                FormUtils.getBundleString("PROP_LAYOUT_CODE_TARGET"), // NOI18N
                FormUtils.getBundleString("HINT_LAYOUT_CODE_TARGET")); // NOI18N
        }
            
        @Override
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            formModel.getSettings().setLayoutCodeTarget(newValue.intValue());
            FormEditor.updateProjectForNaturalLayout(formModel);
            formModel.fireSyntheticPropertyChanged(null, FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, oldValue, newValue);
            FormEditor.getFormEditor(formModel).getFormRootNode().firePropertyChangeHelper(
                FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, oldValue, newValue);
        }

        @Override
        public Object getValue() {
            return Integer.valueOf(formModel.getSettings().getLayoutCodeTarget());
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        @Override
        public void restoreDefaultValue() {
            setValue(Integer.valueOf(FormLoaderSettings.getInstance().getLayoutCodeTarget()));
        }

        @Override
        public boolean isDefaultValue() {
            return (formModel.getSettings().getLayoutCodeTarget() ==
                    FormLoaderSettings.getInstance().getLayoutCodeTarget());
        }

        @Override
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new LayoutCodeTargetEditor(true);
        }

    }

    private class GenerateFQNProperty extends PropertySupport.ReadWrite<Boolean> {
        
        private GenerateFQNProperty() {
            super(FormLoaderSettings.PROP_GENERATE_FQN,
                Boolean.class,
                FormUtils.getBundleString("PROP_GENERATE_FQN"), // NOI18N
                FormUtils.getBundleString("HINT_GENERATE_FQN")); // NOI18N
        }

        @Override
        public void setValue(Boolean value) {
            Boolean oldValue = getValue();
            formModel.getSettings().setGenerateFQN(value);
            if (!value) {
                importFQNs(false, false, formModel.getFormEvents().getAllEventHandlers());
            }
            formModel.fireSyntheticPropertyChanged(null, FormLoaderSettings.PROP_GENERATE_FQN, oldValue, value);
            FormEditor.getFormEditor(formModel).getFormRootNode().firePropertyChangeHelper(
                FormLoaderSettings.PROP_GENERATE_FQN, oldValue, value);
        }

        @Override
        public Boolean getValue() {
            return formModel.getSettings().getGenerateFQN();
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        @Override
        public void restoreDefaultValue() {
            setValue(FormLoaderSettings.getInstance().getGenerateFQN());
        }

        @Override
        public boolean isDefaultValue() {
            return (formModel.getSettings().getGenerateFQN() ==
                    FormLoaderSettings.getInstance().getGenerateFQN());
        }

        @Override
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
    }
    
    public static final class LayoutCodeTargetEditor
                      extends org.netbeans.modules.form.editors.EnumEditor
    {
        public LayoutCodeTargetEditor() {
            this(false);
        }
        public LayoutCodeTargetEditor(boolean specific) {
            super(specific ?
                new Object[] {
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_JDK6"), // NOI18N
                    Integer.valueOf(JavaCodeGenerator.LAYOUT_CODE_JDK6),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_LIBRARY"), // NOI18N
                    Integer.valueOf(JavaCodeGenerator.LAYOUT_CODE_LIBRARY),
                    "" // NOI18N
                }
                :
                new Object[] {
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_AUTO"), // NOI18N
                    Integer.valueOf(JavaCodeGenerator.LAYOUT_CODE_AUTO),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_JDK6"), // NOI18N
                    Integer.valueOf(JavaCodeGenerator.LAYOUT_CODE_JDK6),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_LIBRARY"), // NOI18N
                    Integer.valueOf(JavaCodeGenerator.LAYOUT_CODE_LIBRARY),
                    "" // NOI18N
                });
        }
    }
    
    public static final class ListenerGenerationStyleEditor
                      extends org.netbeans.modules.form.editors.EnumEditor
    {
        public ListenerGenerationStyleEditor() {
            super(new Object[] {
                FormUtils.getBundleString("CTL_LISTENER_LAMBDAS"), // NOI18N
                Integer.valueOf(JavaCodeGenerator.LAMBDAS),
                "" ,// NOI18N
                FormUtils.getBundleString("CTL_LISTENER_ANONYMOUS_CLASSES"), // NOI18N
                Integer.valueOf(JavaCodeGenerator.ANONYMOUS_INNERCLASSES),
                "", // NOI18N
                FormUtils.getBundleString("CTL_LISTENER_CEDL_INNERCLASS"), // NOI18N
                Integer.valueOf(JavaCodeGenerator.CEDL_INNERCLASS),
                "", // NOI18N
                FormUtils.getBundleString("CTL_LISTENER_CEDL_MAINCLASS"), // NOI18N
                Integer.valueOf(JavaCodeGenerator.CEDL_MAINCLASS),
                "" // NOI18N
            });
        }
    }
}
